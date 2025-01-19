package rocks.blackblock.bib.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import rocks.blackblock.bib.monitor.GlitchGuru;
import rocks.blackblock.bib.runnable.Pledge;
import rocks.blackblock.bib.runnable.TickRunnable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Library for helping with control flow
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibFlow {

    // The main timer
    private static Timer FLOW_TIMER = new Timer("BibFlow", true);

    // The main server thread
    private static final Thread MAIN_SERVER_THREAD = Thread.currentThread();

    // Server chunk executors per world
    private static final Map<RegistryKey<World>, ThreadExecutor<Runnable>> WORLD_CHUNK_EXECUTORS = new HashMap<>();

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
     * Register a chunk thread executor
     *
     * @since    0.2.0
     */
    @ApiStatus.Internal
    public static void registerWorldChunkExecutor(ServerWorld world, ThreadExecutor<Runnable> executor) {
        WORLD_CHUNK_EXECUTORS.put(world.getRegistryKey(), executor);
    }

    /**
     * Perform the given task on the given world's rcelated chunk thread
     * @since    0.2.0
     */
    public static CompletableFuture<Void> onWorldChunkThread(World world, Runnable runnable) {
        return onWorldChunkThread(world.getRegistryKey(), runnable);
    }

    /**
     * Perform the given task on the given world's related chunk thread
     * @since    0.2.0
     */
    public static CompletableFuture<Void> onWorldChunkThread(RegistryKey<World> world_key, Runnable runnable) {

        ThreadExecutor<Runnable> executor = WORLD_CHUNK_EXECUTORS.get(world_key);

        if (executor == null) {
            throw new RuntimeException("No world chunk executor registered for " + world_key);
        }

        return executor.submit(runnable);
    }

    /**
     * Do something within a certain amount of ticks.
     * This will be executed on the main server thread.
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
     * Schedule something on the existing timer thread
     * @since    0.2.0
     */
    public static void setInterval(Runnable runnable, long delay_in_ms) {
        FLOW_TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, delay_in_ms, delay_in_ms);
    }

    /**
     * Do something within a certain amount of ms.
     * This will not be executed on the main server thread.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static TickRunnable onMsTimeout(Runnable runnable, long delay_in_ms) {

        TickRunnable instance = new TickRunnable(runnable, 0);

        FLOW_TIMER.schedule(new WrapperTimerTask(instance), delay_in_ms);

        return instance;
    }

    /**
     * Run the given runnable on a new timer thread
     * after the given delay in ms
     *
     * @since    0.2.0
     */
    public static CompletableFuture<Void> onTimerThread(Runnable runnable, long delay_in_ms) {

        CompletableFuture<Void> result = new CompletableFuture<>();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    result.completeExceptionally(t);
                    timer.cancel();
                    return;
                }

                result.complete(null);
                timer.cancel();
            }
        }, delay_in_ms);

        return result;
    }

    /**
     * Observe something every X ms while the instance exists
     * @since    0.2.0
     */
    public static void onIntervalWhileReferenced(Runnable runnable, Object object, long interval_in_ms) {

        if (object == null) {
            return;
        }

        var timer = new Timer(true);
        var ref = new WeakReference<>(object);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (ref.get() != null) {
                    runnable.run();
                } else {
                    timer.cancel();
                }
            }
        }, interval_in_ms, interval_in_ms);
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
    public static <T> void done(Collection<? extends CompletionStage<T>> stages, Consumer<List<T>> callback) {

        List<CompletableFuture<T>> futures = new ArrayList<>(stages.size());
        futures = stages.stream().map(CompletionStage::toCompletableFuture).toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        List<CompletableFuture<T>> finalFutures = futures;
        allFutures.thenAccept(v -> {
            List<T> results = new ArrayList<>();
            for (CompletableFuture<T> future : finalFutures) {
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
    public static <T> Pledge<?> series(Collection<T> entries, Function<T, CompletionStage<?>> task) {

        // Start with a completed future to use as the initial point for chaining
        CompletableFuture<?> future = CompletableFuture.completedFuture(null);

        // Chain each task to be completed in series
        for (T entry : entries) {
            future = future.thenCompose(v -> task.apply(entry));
        }

        return Pledge.from(future);
    }

    /**
     * Perform the given task for each entry in the list.
     * Do this in parallel
     *
     * @since    0.2.0
     */
    public static <T> Pledge<Void> parallel(Collection<T> entries, Function<T, CompletionStage<?>> task) {
        // Create a list of CompletableFutures for all tasks
        var futures = entries.stream()
                .map(task)
                .map(CompletionStage::toCompletableFuture)
                .toList();

        // Combine all futures into a single CompletableFuture
        return Pledge.from(CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])));
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

    /**
     * A small on-the-fly profiler
     */
    public static class Profiler {

        private final String name;
        private final List<ProfilerPair> timings = new ArrayList<>();
        private long total = 0;
        private long startTime;
        private long endTime;

        public Profiler(String name) {
            this.name = name;
            this.startTime = System.currentTimeMillis();
        }

        private void addPair(ProfilerPair pair) {
            this.timings.add(pair);
            this.endTime = System.currentTimeMillis();
        }

        public void run(String name, Runnable runnable) {

            var start = System.currentTimeMillis();
            runnable.run();
            var end = System.currentTimeMillis();
            var dur = end - start;
            this.total += dur;

            this.addPair(new ProfilerPair(name, dur));
        }

        public <T> T run(String name, Supplier<T> runnable) {

            var start = System.currentTimeMillis();
            var result = runnable.get();
            var end = System.currentTimeMillis();
            var dur = end - start;
            this.total += dur;

            this.addPair(new ProfilerPair(name, dur));

            return result;
        }

        public long getTotalDuration() {
            return this.total;
        }

        public List<ProfilerPair> getTimings() {
            return this.timings;
        }

        public void print() {
            BibLog.log("Profiler result of '" + this.name + "'");

            this.timings.forEach(pair -> {
                BibLog.log("  »",pair.name(), "took", pair.ms(), "ms");
            });

            long total = this.endTime - this.startTime;

            BibLog.log("  Sum of runs:", this.total, "ms");
            BibLog.log("  Total time:", total, "ms");
        }
    }

    private record ProfilerPair(String name, Long ms) {}
}
