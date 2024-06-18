package rocks.blackblock.bib.util;

import rocks.blackblock.bib.runnable.TickRunnable;

import java.util.Timer;
import java.util.TimerTask;

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
