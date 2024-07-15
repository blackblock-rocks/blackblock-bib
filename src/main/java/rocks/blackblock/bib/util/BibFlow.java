package rocks.blackblock.bib.util;

import org.spongepowered.asm.mixin.Final;
import rocks.blackblock.bib.monitor.GlitchGuru;
import rocks.blackblock.bib.runnable.TickRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Library for helping with control flow
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibFlow {

    // The main timer
    private static Timer FLOW_TIMER = null;

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibFlow() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Do something within a certain amount of ticks
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static TickRunnable onTickTimeout(Runnable runnable, int ticks) {
        TickRunnable instance = new TickRunnable(runnable, ticks + BibServer.getTick());

        TickRunnable.queueTickRunnable(instance);

        return instance;
    }

    /**
     * Do something within a certain amount of ms
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static TickRunnable onMsTimeout(Runnable runnable, long delay_in_ms) {

        if (FLOW_TIMER == null) {
            FLOW_TIMER = new Timer(true);
        }

        TickRunnable instance = new TickRunnable(runnable, 0);

        FLOW_TIMER.schedule(new WrapperTimerTask(instance), delay_in_ms);

        return instance;
    }

    /**
     * Perform a waterfall with 2 tasks
     *
     * @since    0.2.0
     */
    public static <T2, TRES> CompletableFuture<FlowResult<TRES>> waterfall(Supplier<CompletableFuture<T2>> first, Function<T2, CompletableFuture<TRES>> second) {
        // Start the first task
        return first.get().thenCompose(result -> {
            // Then start the second task
            return second.apply(result)
                    .thenApply(result2 -> new FlowResult<>(null, result2))
                    .exceptionally(ex -> new FlowResult<>(ex, null));
        });
    }

    /**
     * Perform a waterfall with 3 tasks.
     * Each task only gets the result from the previous task.
     *
     * @since    0.2.0
     */
    public static <T2, T3, TRES> CompletableFuture<FlowResult<TRES>> waterfall(Supplier<CompletableFuture<T2>> first, Function<T2, CompletableFuture<T3>> second, Function<T3, CompletableFuture<TRES>> third) {
        // Start the first task
        return first.get().thenCompose(result -> {
            // Then start the second task
            return second.apply(result)
                    .thenCompose(result2 -> {
                        // Then start the third task
                        return third.apply(result2)
                                .thenApply(result3 -> new FlowResult<>(null, result3))
                                .exceptionally(ex -> new FlowResult<>(ex, null));
                    })
                    .exceptionally(ex -> new FlowResult<>(ex, null));
        });
    }

    /**
     * Perform a waterfall with 4 tasks.
     * Each task only gets the result from the previous task.
     *
     * @since    0.2.0
     */
    public static <T2, T3, T4, TRES> CompletableFuture<FlowResult<TRES>> waterfall(Supplier<CompletableFuture<T2>> first, Function<T2, CompletableFuture<T3>> second, Function<T3, CompletableFuture<T4>> third, Function<T4, CompletableFuture<TRES>> fourth) {
        // Start the first task
        return first.get().thenCompose(result -> {
            // Then start the second task
            return second.apply(result)
                    .thenCompose(result2 -> {
                        // Then start the third task
                        return third.apply(result2)
                                .thenCompose(result3 -> {
                                    // Then start the fourth task
                                    return fourth.apply(result3)
                                            .thenApply(result4 -> new FlowResult<>(null, result4))
                                            .exceptionally(ex -> new FlowResult<>(ex, null));
                                })
                                .exceptionally(ex -> new FlowResult<>(ex, null));
                    })
                    .exceptionally(ex -> new FlowResult<>(ex, null));
        });
    }

    /**
     * Perform all the given tasks in series
     *
     * @since    0.2.0
     */
    @SafeVarargs
    public static CompletableFuture<FlowResult<Void>> parallel(Supplier<CompletableFuture<?>>... runnables) {

        // Starting with an already completed future
        CompletableFuture<Void> result = CompletableFuture.completedFuture(null);
        CompletableFuture<FlowResult<Void>> finalResult = new CompletableFuture<>();

        for (Supplier<CompletableFuture<?>> runnable : runnables) {
            result = result.thenCompose(ignored -> runnable.get()
                    .thenAccept(ignored2 -> {
                        // No specific action needed on success of each
                    })
                    .exceptionally(ex -> {
                        // Complete the finalResult with the exception and stop further processing
                        finalResult.complete(new FlowResult<>(ex, null));
                        return null; // Required to comply with exceptionally
                    }));
        }

        // Once all tasks are complete (or if no exception triggered), complete finalResult with empty Optional
        result.thenRun(() -> {
            if (!finalResult.isDone()) {
                finalResult.complete(new FlowResult<>(null, null));
            }
        });

        // Returning the CompletableFuture that will eventually contain Optional<Throwable>
        return finalResult;
    }

    /**
     * Get all the future results
     *
     * @since    0.2.0
     */
    public static <T> void done(Collection<CompletableFuture<T>> futures, Consumer<List<T>> callback) {

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        allFutures.thenAccept(v -> {
            List<T> results = new ArrayList<>();
            for (CompletableFuture<T> future : futures) {
                results.add(future.join());
            }

            try {
                callback.accept(results);
            } catch (Throwable t) {
                GlitchGuru.registerThrowable(t);
            }
        });
    }

    /**
     * Perform the given task for each entry in the list.
     * Do this in series
     *
     * @since    0.2.0
     */
    public static <T> CompletableFuture<?> series(Collection<T> entries, Function<T, CompletableFuture<?>> task) {

        // Start with a completed future to use as the initial point for chaining
        CompletableFuture<?> future = CompletableFuture.completedFuture(null);

        // Chain each task to be completed in series
        for (T entry : entries) {
            future = future.thenCompose(v -> task.apply(entry));
        }

        return future;
    }

    /**
     * Perform the given task for each entry in the list.
     * Do this in parallel
     *
     * @since    0.2.0
     */
    public static <T> CompletableFuture<Void> parallel(Collection<T> entries, Function<T, CompletableFuture<?>> task) {
        // Create a list of CompletableFutures for all tasks
        Collection<CompletableFuture<?>> futures = entries.stream()
                .map(task)
                .toList();

        // Combine all futures into a single CompletableFuture
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * An async flow result
     *
     * @since    0.2.0
     */
    public static class FlowResult<T> {
        private Throwable throwable;
        private T result = null;
        public FlowResult(Throwable throwable, T result) {
            this.throwable = throwable;
            this.result = result;
        }

        public boolean hasError() {
            return this.throwable != null;
        }

        public Throwable getError() {
            return this.throwable;
        }

        public boolean hasResult() {
            return this.result != null;
        }

        public T getResult() {
            return this.result;
        }
    }

    /**
     * Private timer task class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static class WrapperTimerTask extends TimerTask {

        private final TickRunnable runnable;

        public WrapperTimerTask(TickRunnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            this.runnable.run();
        }
    }
}
