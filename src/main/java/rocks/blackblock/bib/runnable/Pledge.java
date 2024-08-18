package rocks.blackblock.bib.runnable;

import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.monitor.GlitchGuru;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Future-like class
 *
 * @since    0.2.0
 */
@SuppressWarnings({"unused", "unchecked"})
public class Pledge<T> implements Future<T>, CompletionStage<T> {

    private State state = State.PENDING;
    private T result = null;
    private Throwable error = null;
    private List<DoneCallback<T>> done_callbacks = null;
    private List<Consumer<T>> consumers = null;
    private List<Pair<Pledge<?>, Composer<T, ?>>> composers = null;

    public Pledge() {}

    public Pledge(T result) {
        this.state = State.SUCCESS;
        this.result = result;
    }

    /**
     * Create a pledge that is immediately rejected
     *
     * @since    0.2.0
     */
    public static <T> Pledge<T> rejected(Throwable error) {
        Pledge<T> pledge = new Pledge<>();
        pledge.reject(error);
        return pledge;
    }

    /**
     * Create a pledge that is immediately resolved
     *
     * @since    0.2.0
     */
    public static <T> Pledge<T> resolved(T result) {
        Pledge<T> pledge = new Pledge<>();
        pledge.resolve(result);
        return pledge;
    }

    /**
     * Resolve with the given completion stage
     *
     * @since    0.2.0
     */
    public static <T> Pledge<T> from(CompletionStage<T> stage) {
        Pledge<T> pledge = new Pledge<>();
        stage.thenAccept(pledge::resolveWithResult);
        stage.exceptionally(throwable -> {
            pledge.reject(throwable);
            return null;
        });
        return pledge;
    }

    /**
     * Resolve with null
     *
     * @since    0.2.0
     */
    public void resolve() {
        this.resolveWithResult(null);
    }

    /**
     * Resolve the pledge with the given result
     *
     * @since    0.2.0
     */
    public void resolve(T result) {
        this.resolveWithResult(result);
    }

    /**
     * Resolve the pledge with the given result
     *
     * @since    0.2.0
     */
    public void resolveWithResult(T result) {
        if (this.state != State.PENDING) {
            return;
        }

        this.state = State.SUCCESS;
        this.result = result;
        this.performAll();
    }

    /**
     * Resolve with another pledge
     *
     * @since    0.2.0
     */
    public void resolve(Pledge<T> result) {
        this.resolveWithPledge(result);
    }

    /**
     * Resolve with another pledge
     *
     * @since    0.2.0
     */
    public void resolveWithPledge(Pledge<T> result) {

        if (this.state != State.PENDING) {
            return;
        }

        if (result.state == State.SUCCESS) {
            this.state = State.SUCCESS;
            this.result = result.result;
        } else if (result.state == State.FAILURE) {
            this.state = State.FAILURE;
            this.error = result.error;
        } else {

            result.done((error1, result1) -> {
                if (error1 != null) {
                    this.reject(error1);
                } else {
                    this.resolveUnsafe(result1);
                }
            });

            return;
        }

        this.performAll();
    }

    /**
     * Do an unsafe resolve
     *
     * @since    0.2.0
     */
    private void resolveUnsafe(Object result) {
        this.resolve((T) result);
    }

    /**
     * Reject the pledge with the given error
     *
     * @since    0.2.0
     */
    public void reject(Throwable error) {

        if (this.state != State.PENDING) {
            return;
        }

        if (error == null) {
            error = new RuntimeException("Rejected Pledge with null error");
        }

        GlitchGuru.registerThrowable(error, "Rejected Pledge");

        this.state = State.FAILURE;
        this.error = error;
        this.performAll();
    }

    /**
     * Add a then consumer
     *
     * @since    0.2.0
     */
    public void then(Consumer<T> consumer) {

        if (this.state == State.SUCCESS) {
            this.performConsumer(consumer);
            return;
        }

        if (this.consumers == null) {
            this.consumers = new ArrayList<>();
        }

        this.consumers.add(consumer);
    }

    /**
     * Add a then composer
     *
     * @since    0.2.0
     */
    public <U> Pledge<U> thenCompose(Composer<T, U> composer) {

        if (this.state == State.SUCCESS) {
            return composer.compose(null, this.result);
        }

        Pledge<U> intermediate_pledge = new Pledge<>();

        if (this.composers == null) {
            this.composers = new ArrayList<>();
        }

        Pair<Pledge<?>, Composer<T, ?>> pair = new Pair<>(intermediate_pledge, composer);

        this.composers.add(pair);

        return intermediate_pledge;
    }

    /**
     * Add a done callback.
     * Executes it immediately if the pledge is already done.
     *
     * @since    0.2.0
     */
    public void done(DoneCallback<T> callback) {

        if (this.state == State.SUCCESS) {
            this.performDoneCallback(callback);
            return;
        }

        if (this.done_callbacks == null) {
            this.done_callbacks = new ArrayList<>();
        }

        this.done_callbacks.add(callback);
    }

    /**
     * Perform all the things that need calling
     *
     * @since    0.2.0
     */
    protected void performAll() {
        this.performDoneCallbacks();
        this.performConsumers();
        this.performComposers();
    }

    /**
     * Perform a composer pair
     *
     * @since    0.2.0
     */
    protected void performComposer(Pair<Pledge<?>, Composer<T, ?>> pair) {

        Pledge<?> pledge = pair.getLeft();
        Composer<T, ?> composer = pair.getRight();

        try {
            Pledge<?> result = composer.compose(this.error, this.result);

            result.done((error1, result1) -> {
                if (error1 != null) {
                    pledge.reject(error1);
                } else {
                    pledge.resolveUnsafe(result1);
                }
            });

        } catch (Throwable t) {
            GlitchGuru.registerThrowable(t, "Failed to perform composer");
        }
    }

    /**
     * Perform all the composers
     *
     * @since    0.2.0
     */
    protected void performComposers() {

        if (this.composers == null) {
            return;
        }

        List<Pair<Pledge<?>, Composer<T, ?>>> composers = this.composers;
        this.composers = null;

        for (Pair<Pledge<?>, Composer<T, ?>> pair : composers) {
            this.performComposer(pair);
        }

        if (this.composers != null) {
            this.performComposers();
        }
    }

    /**
     * Perform all the consumers
     *
     * @since    0.2.0
     */
    protected void performConsumers() {

        if (this.consumers == null) {
            return;
        }

        List<Consumer<T>> consumers = this.consumers;
        this.consumers = null;

        for (Consumer<T> consumer : consumers) {
            this.performConsumer(consumer);
        }

        if (this.consumers != null) {
            this.performConsumers();
        }
    }

    /**
     * Perform all the done callbacks
     *
     * @since    0.2.0
     */
    protected void performDoneCallbacks() {

        if (this.done_callbacks == null) {
            return;
        }

        List<DoneCallback<T>> callbacks = this.done_callbacks;
        this.done_callbacks = null;

        for (DoneCallback<T> callback : callbacks) {
            this.performDoneCallback(callback);
        }

        if (this.done_callbacks != null) {
            this.performDoneCallbacks();
        }
    }

    /**
     * Perform the given "then" consumer
     *
     * @since    0.2.0
     */
    protected void performConsumer(Consumer<T> consumer) {
        try {
            consumer.accept(this.result);
        } catch (Throwable t) {
            GlitchGuru.registerThrowable(t, "Failed to perform consumer");
        }
    }

    /**
     * Perform the given callback
     *
     * @since    0.2.0
     */
    protected void performDoneCallback(DoneCallback<T> callback) {
        try {
            callback.onDone(this.error, this.result);
        } catch (Throwable t) {
            GlitchGuru.registerThrowable(t, "Failed to perform done callback");
        }
    }

    @Override
    public boolean cancel(boolean b) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return this.state == State.CANCELLED;
    }

    @Override
    public boolean isDone() {

        if (this.state == State.SUCCESS) {
            return true;
        }

        if (this.state == State.FAILURE) {
            return true;
        }

        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {

        while (!this.isDone()) {
            LockSupport.parkNanos("waiting for result", 100000L);
        }

        return this.result;
    }

    @Override
    public T get(long l, @NotNull TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.get();
    }

    @Override
    public <U> Pledge<U> thenApply(Function<? super T, ? extends U> function) {

        Pledge<U> pledge = new Pledge<>();

        this.then(t -> {
            var result = function.apply(t);
            pledge.resolve(result);
        });

        return pledge;
    }

    @Override
    public <U> Pledge<U> thenApplyAsync(Function<? super T, ? extends U> function) {
        return this.thenApply(function);
    }

    @Override
    public <U> Pledge<U> thenApplyAsync(Function<? super T, ? extends U> function, Executor executor) {
        return this.thenApply(function);
    }

    @Override
    public Pledge<Void> thenAccept(Consumer<? super T> consumer) {

        Pledge<Void> pledge = new Pledge<>();

        this.then(t -> {
            consumer.accept(t);
            pledge.resolve((Void) null);
        });

        return pledge;
    }

    @Override
    public Pledge<Void> thenAcceptAsync(Consumer<? super T> consumer) {
        return this.thenAccept(consumer);
    }

    @Override
    public Pledge<Void> thenAcceptAsync(Consumer<? super T> consumer, Executor executor) {
        return this.thenAccept(consumer);
    }

    @Override
    public Pledge<Void> thenRun(Runnable runnable) {

        Pledge<Void> pledge = new Pledge<>();

        this.then(t -> {
            runnable.run();
            pledge.resolve((Void) null);
        });

        return pledge;
    }

    @Override
    public Pledge<Void> thenRunAsync(Runnable runnable) {
        return this.thenRun(runnable);
    }

    @Override
    public Pledge<Void> thenRunAsync(Runnable runnable, Executor executor) {
        return this.thenRun(runnable);
    }

    @Override
    public <U, V> Pledge<V> thenCombine(CompletionStage<? extends U> completionStage, BiFunction<? super T, ? super U, ? extends V> biFunction) {

        Pledge<V> pledge = new Pledge<>();

        this.then(t -> {
            completionStage.thenAccept(u -> {
                var result = biFunction.apply(t, u);
                pledge.resolve(result);
            });
        });

        return pledge;
    }

    @Override
    public <U, V> Pledge<V> thenCombineAsync(CompletionStage<? extends U> completionStage, BiFunction<? super T, ? super U, ? extends V> biFunction) {
        return this.thenCombine(completionStage, biFunction);
    }

    @Override
    public <U, V> Pledge<V> thenCombineAsync(CompletionStage<? extends U> completionStage, BiFunction<? super T, ? super U, ? extends V> biFunction, Executor executor) {
        return this.thenCombine(completionStage, biFunction);
    }

    @Override
    public <U> Pledge<Void> thenAcceptBoth(CompletionStage<? extends U> completionStage, BiConsumer<? super T, ? super U> biConsumer) {

        Pledge<Void> pledge = new Pledge<>();

        this.then(t -> {
            completionStage.thenAccept(u -> {
                biConsumer.accept(t, u);
                pledge.resolve((Void) null);
            });
        });

        return pledge;
    }

    @Override
    public <U> Pledge<Void> thenAcceptBothAsync(CompletionStage<? extends U> completionStage, BiConsumer<? super T, ? super U> biConsumer) {
        return this.thenAcceptBoth(completionStage, biConsumer);
    }

    @Override
    public <U> Pledge<Void> thenAcceptBothAsync(CompletionStage<? extends U> completionStage, BiConsumer<? super T, ? super U> biConsumer, Executor executor) {
        return this.thenAcceptBoth(completionStage, biConsumer);
    }

    @Override
    public Pledge<Void> runAfterBoth(CompletionStage<?> completionStage, Runnable runnable) {

        Pledge<Void> pledge = new Pledge<>();

        this.then(t -> {
            completionStage.thenRun(runnable).thenRun(() -> {
                pledge.resolve((Void) null);
            });
        });

        return pledge;
    }

    @Override
    public Pledge<Void> runAfterBothAsync(CompletionStage<?> completionStage, Runnable runnable) {
        return this.runAfterBoth(completionStage, runnable);
    }

    @Override
    public Pledge<Void> runAfterBothAsync(CompletionStage<?> completionStage, Runnable runnable, Executor executor) {
        return this.runAfterBoth(completionStage, runnable);
    }

    @Override
    public <U> Pledge<U> applyToEither(CompletionStage<? extends T> completionStage, Function<? super T, U> function) {

        Pledge<U> pledge = new Pledge<>();

        this.race(this, completionStage).then(result -> {
            var applied = function.apply(result);
            pledge.resolve(applied);
        });

        return pledge;
    }

    private <U> Pledge<U> race(CompletionStage<? extends U> stage_one, CompletionStage<? extends U> stage_two) {

        Pledge<U> pledge = new Pledge<>();

        stage_one.thenAccept(pledge::resolveWithResult);
        stage_two.thenAccept(pledge::resolveWithResult);

        return pledge;
    }

    @Override
    public <U> Pledge<U> applyToEitherAsync(CompletionStage<? extends T> completionStage, Function<? super T, U> function) {
        return this.applyToEither(completionStage, function);
    }

    @Override
    public <U> Pledge<U> applyToEitherAsync(CompletionStage<? extends T> completionStage, Function<? super T, U> function, Executor executor) {
        return this.applyToEither(completionStage, function);
    }

    @Override
    public Pledge<Void> acceptEither(CompletionStage<? extends T> completionStage, Consumer<? super T> consumer) {

        Pledge<Void> pledge = new Pledge<>();

        this.race(this, completionStage).then(result -> {
            consumer.accept(result);
            pledge.resolve((Void) null);
        });

        return pledge;
    }

    @Override
    public Pledge<Void> acceptEitherAsync(CompletionStage<? extends T> completionStage, Consumer<? super T> consumer) {
        return this.acceptEither(completionStage, consumer);
    }

    @Override
    public Pledge<Void> acceptEitherAsync(CompletionStage<? extends T> completionStage, Consumer<? super T> consumer, Executor executor) {
        return this.acceptEither(completionStage, consumer);
    }

    @Override
    public Pledge<Void> runAfterEither(CompletionStage<?> completionStage, Runnable runnable) {

        Pledge<Void> pledge = new Pledge<>();

        this.race(this, completionStage).then(result -> {
            runnable.run();
            pledge.resolve((Void) null);
        });

        return pledge;
    }

    @Override
    public Pledge<Void> runAfterEitherAsync(CompletionStage<?> completionStage, Runnable runnable) {

        Pledge<Void> pledge = new Pledge<>();

        this.race(this, completionStage).then(result -> {
            runnable.run();
            pledge.resolve((Void) null);
        });

        return pledge;
    }

    @Override
    public Pledge<Void> runAfterEitherAsync(CompletionStage<?> completionStage, Runnable runnable, Executor executor) {
        return this.runAfterEither(completionStage, runnable);
    }

    @Override
    public <U> Pledge<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> function) {

        Pledge<U> pledge = new Pledge<>();

        this.then(result -> {
            CompletionStage<U> stage = function.apply(result);
            stage.thenAccept(pledge::resolveWithResult);
        });

        return pledge;
    }

    @Override
    public <U> Pledge<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> function) {
        return this.thenCompose(function);
    }

    @Override
    public <U> Pledge<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> function, Executor executor) {
        return this.thenCompose(function);
    }

    @Override
    public <U> Pledge<U> handle(BiFunction<? super T, Throwable, ? extends U> biFunction) {

        Pledge<U> pledge = new Pledge<>();

        this.done((error, result) -> {

            var applied = biFunction.apply(result, error);

            pledge.resolve(applied);
        });

        return pledge;
    }

    @Override
    public <U> Pledge<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> biFunction) {
        return this.handle(biFunction);
    }

    @Override
    public <U> Pledge<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> biFunction, Executor executor) {
        return this.handle(biFunction);
    }

    @Override
    public Pledge<T> whenComplete(BiConsumer<? super T, ? super Throwable> biConsumer) {

        Pledge<T> pledge = new Pledge<>();

        this.done((error, result) -> {
            biConsumer.accept(result, error);

            if (error != null) {
                pledge.reject(error);
                return;
            }

            pledge.resolve(result);
        });

        return pledge;
    }

    @Override
    public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> biConsumer) {
        return this.whenComplete(biConsumer);
    }

    @Override
    public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> biConsumer, Executor executor) {
        return this.whenComplete(biConsumer);
    }

    @Override
    public CompletionStage<T> exceptionally(Function<Throwable, ? extends T> function) {

        Pledge<T> pledge = new Pledge<>();

        this.done((error, result) -> {
            if (error != null) {
                pledge.resolve(function.apply(error));
                return;
            }

            pledge.resolve(result);
        });

        return pledge;
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {

        CompletableFuture<T> future = new CompletableFuture<>();

        this.done((error, result) -> {
            if (error != null) {
                future.completeExceptionally(error);
                return;
            }

            future.complete(result);
        });

        return future;
    }

    /**
     * A composer interface
     *
     * @since    0.2.0
     */
    @FunctionalInterface
    public interface Composer<T, U> {
        Pledge<U> compose(Throwable error, T result);
    }

    /**
     * Done callback interface
     *
     * @since    0.2.0
     */
    @FunctionalInterface
    public interface DoneCallback<T> {
        void onDone(@Nullable Throwable error, @Nullable T result);
    }

    /**
     * Pledge states
     *
     * @since    0.2.0
     */
    public enum State {
        PENDING,
        CANCELLED,
        SUCCESS,
        FAILURE
    }
}
