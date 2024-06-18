package rocks.blackblock.bib.runnable;

import org.jetbrains.annotations.ApiStatus;
import rocks.blackblock.bib.util.BibServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Representing a runnable that should be executed on a certain tick
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
public class TickRunnable {

    // Are there currently any queued runnables?
    private static boolean HAS_QUEUED_RUNNABLES = false;

    // All the currently queued runnables
    private static final List<TickRunnable> QUEUED_RUNNABLES = new ArrayList<>();

    // The actual runnable to eventually execute
    private Runnable runnable;

    // On what tick it should be executed
    private int on_tick;

    // If it has been cancelled
    private boolean cancelled = false;

    // If it has run already
    private boolean has_run = false;

    /**
     * Check the queued runnables
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @ApiStatus.Internal
    public static void checkQueuedRunnables() {

        if (!HAS_QUEUED_RUNNABLES) {
            return;
        }

        int current_tick = BibServer.getTick();

        List<TickRunnable> to_remove = null;
        List<TickRunnable> to_check = new ArrayList<>(QUEUED_RUNNABLES);

        for (TickRunnable runnable : to_check) {
            if (runnable.getOnTick() <= current_tick) {
                if (to_remove == null) {
                    to_remove = new ArrayList<>();
                }

                runnable.run();
                to_remove.add(runnable);
            }
        }

        if (to_remove != null) {
            QUEUED_RUNNABLES.removeAll(to_remove);
        }
    }

    /**
     * Queue the given TickRunnable
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void queueTickRunnable(TickRunnable runnable) {

        if (runnable == null || runnable.on_tick < 0 || runnable.has_run) {
            return;
        }

        if (!HAS_QUEUED_RUNNABLES) {
            HAS_QUEUED_RUNNABLES = true;
        }

        QUEUED_RUNNABLES.add(runnable);
    }

    /**
     * Initialize the runnable
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public TickRunnable(Runnable runnable, int on_tick) {
        this.runnable = runnable;
        this.on_tick = on_tick;
    }

    /**
     * Try to run the runnable
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @return   True if the runnable was run, false if it was cancelled
     */
    public boolean run() {

        if (cancelled) {
            return false;
        }

        this.has_run = true;
        runnable.run();

        return true;
    }

    /**
     * Get the tick this runnable should be executed on
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public int getOnTick() {
        return on_tick;
    }

    /**
     * Cancel the runnable
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @return   True if the runnable was cancelled, false if it already ran
     */
    public boolean cancel() {

        if (has_run) {
            return false;
        }

        this.cancelled = true;

        return true;
    }
}