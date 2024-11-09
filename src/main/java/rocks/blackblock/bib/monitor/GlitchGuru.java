package rocks.blackblock.bib.monitor;

import com.mojang.serialization.DataResult;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.interop.InteropSentry;
import rocks.blackblock.bib.util.BibLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class for tracking errors/performances
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
public class GlitchGuru {

    // The Sentry DSN (endpoint)
    private static String sentry_dsn = null;

    // Has sentry been initialized?
    private static boolean sentry_initialized = false;

    /**
     * Create a new transaction
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static GlitchGuru.Transaction startTransaction(String name) {
        return new GlitchGuru.Transaction(name);
    }

    /**
     * Log a warning
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void captureMessageSilently(StringBuilder message, Level level) {
        captureMessageSilently(message.toString(), level);
    }

    /**
     * Log a warning
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void captureMessageSilently(String message, Level level) {
        if (!GlitchGuru.sentry_initialized) {
            return;
        }

        InteropSentry.captureMessageSilently(message, level);
    }

    /**
     * Set the DSN
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void setSentryOtions(String dsn, double sample_rate) {
        GlitchGuru.sentry_dsn = dsn;

        if (dsn != null) {
            GlitchGuru.sentry_initialized = true;
            InteropSentry.setSentryOtions(dsn, sample_rate);
        }
    }

    /**
     * Register a dataresult error
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void registerThrowable(DataResult.Error<?> error) {

        String message = error.message();

        if (message == null) {
            message = "Unknown error";
        }

        Optional<?> partial = error.resultOrPartial();
        String partial_message = partial.map(value -> ": " + value).orElse("");

        if (!partial_message.isBlank()) {
            message += "\nPartial result: " + partial_message;
        }

        registerThrowable(new RuntimeException(message), error.message());
    }

    /**
     * Register a throwable
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void registerThrowable(Throwable t) {
        registerThrowable(t, null);
    }

    /**
     * Register a throwable
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void registerThrowable(Throwable t, String message) {

        if (message != null) {
            BibLog.log(message, t);
            t.printStackTrace();
        } else {
            BibLog.log("Registering throwable", t);
            t.printStackTrace();
        }

        if (GlitchGuru.sentry_initialized) {
            InteropSentry.captureException(t);
        }
    }

    /**
     * Silently register a throwable
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void registerThrowableSilently(Throwable t, String message) {
        if (GlitchGuru.sentry_initialized) {
            InteropSentry.captureException(t);
        }
    }

    public static class Transaction {

        private Transaction parent = null;
        private String name;
        private InteropSentry.Transaction sentry_transaction = null;
        // The current time
        private final long start_time = System.currentTimeMillis();

        public Transaction(Transaction parent, @NotNull String name) {
            this.parent = parent;
            this.name = name;

            if (GlitchGuru.sentry_initialized) {
                this.sentry_transaction = new InteropSentry.Transaction(parent, name);
            }
        }

        public Transaction(@NotNull String name) {
            this(null, name);
        }

        public Transaction getParent() {
            return this.parent;
        }

        public InteropSentry.Transaction getSentryTransaction() {
            return this.sentry_transaction;
        }

        public InteropSentry.Transaction getSentryTransactionParent() {

            if (this.parent != null) {
                return this.parent.getSentryTransaction();
            }

            return null;
        }

        /**
         * Set some context
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public void setData(String key, Object value) {
            if (this.sentry_transaction != null) {
                this.sentry_transaction.setData(key, value);
            }
        }

        /**
         * Add a throwable to this transaction
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public void addThrowable(Throwable t) {
            if (this.sentry_transaction != null) {
                this.sentry_transaction.addThrowable(t);
            }
        }

        /**
         * Set the throwable of this transaction
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public void setThrowable(Throwable t) {
            if (this.sentry_transaction != null) {
                this.sentry_transaction.setThrowable(t);
            }
        }

        /**
         * Log a message to the transaction (and debug)
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public void log(Object... args) {
            BibLog.outputLevel(Level.INFO, args);
        }

        /**
         * Log a message to the transaction (and print the duration since the start)
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public void logTime(Object... args) {
            long now = System.currentTimeMillis();
            long diff = now - this.start_time;

            List<Object> new_args = new ArrayList<>(List.of(args));
            new_args.add("(");
            new_args.add(diff);
            new_args.add("ms");
            new_args.add(")");

            BibLog.outputLevel(Level.INFO, new_args);
        }

        /**
         * Finish the transaction
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public void finish() {
            if (this.sentry_transaction != null) {
                this.sentry_transaction.finish();
            }
        }
    }

}
