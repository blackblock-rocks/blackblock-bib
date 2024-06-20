package rocks.blackblock.bib.interop;

import io.sentry.ISpan;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.monitor.GlitchGuru;
import rocks.blackblock.bib.util.BibFlow;
import rocks.blackblock.bib.util.BibLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Interop class for working with the Sentry API
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@ApiStatus.Internal()
public final class InteropSentry {

    public static void captureMessageSilently(String message, Level level) {
        SentryLevel sentry_level = null;

        if (level == Level.WARN) {
            sentry_level = SentryLevel.WARNING;
        } else if (level == Level.INFO) {
            sentry_level = SentryLevel.INFO;
        } else if (level == Level.ERROR) {
            sentry_level = SentryLevel.ERROR;
        } else if (level == Level.DEBUG) {
            sentry_level = SentryLevel.DEBUG;
        } else {
            sentry_level = SentryLevel.INFO;
        }

        // Remove all ANSI codes
        message = message.replaceAll("\u001B\\[[;\\d]*m", "");

        Sentry.captureMessage(message, sentry_level);
    }

    public static void setSentryOtions(String dsn, double sample_rate) {
        BibLog.log("Initializing Sentry:", dsn);

        Sentry.init(options -> {
            options.setDsn(dsn);

            // This is for debugging sentry itself
            //options.setDebug(BBLog.DEBUG);

            if (sample_rate > 0) {
                options.setEnableTracing(true);
            }

            options.setSampleRate(sample_rate);
        });
    }

    public static void captureException(Throwable t) {
        Sentry.captureException(t);
    }

    /**
     * A transaction (to log performance & errors)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class Transaction {

        @Nullable
        private final Transaction parent;

        @NotNull
        private String name = null;

        // The main transaction
        private ITransaction sentry_transaction = null;

        // The span
        private ISpan sentry_span = null;

        // The current time
        private final long start_time = System.currentTimeMillis();

        // Has this finished?
        private boolean has_finished = false;

        /**
         * Initialize the transaction
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public Transaction(Transaction parent, @NotNull String name) {
            this.parent = parent;
            this.name = name;
            this.initSentry();

            if (BibLog.DEBUG) {
                this.initDebug();
            }
        }

        /**
         * Initialize the transaction
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public Transaction(GlitchGuru.Transaction parent, @NotNull String name) {

            if (parent != null) {
                this.parent = parent.getSentryTransactionParent();
            } else {
                this.parent = null;
            }

            this.name = name;
            this.initSentry();

            if (BibLog.DEBUG) {
                this.initDebug();
            }
        }

        public Transaction(@NotNull String name) {
            this((Transaction) null, name);
        }

        /**
         * Get the parent transaction
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public Transaction getParent() {
            return this.parent;
        }

        /**
         * Initialize debug logging
         * (Only called when debug is enabled)
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        private void initDebug() {

            if (this.parent == null) {
                BibLog.log("Starting transaction:", this.name);
            } else {
                BibLog.log("Starting sub-transaction:", this.name);
            }

            BibFlow.onMsTimeout(this::printWarningIfNotFinished, 6000);
        }

        /**
         * Print a warning when the transaction has not yet finished
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        private void printWarningIfNotFinished() {
            if (!this.has_finished) {
                this.logTime("Transaction", name, "is taking a long time to finish!");

                BibFlow.onMsTimeout(this::printWarningIfNotFinished, 9000);
            }
        }

        /**
         * Initialize the Sentry.io instances
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        private void initSentry() {
            if (this.parent == null) {
                this.sentry_transaction = Sentry.startTransaction(this.name, "task");
                this.sentry_span = this.sentry_transaction;
            } else {
                this.sentry_span = this.parent.sentry_span.startChild(this.name);
            }
        }

        /**
         * Set some context
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public void setData(String key, Object value) {
            this.sentry_span.setData(key, value);
        }

        /**
         * Add a throwable to this transaction
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public void addThrowable(Throwable t) {
            this.setThrowable(t);
        }

        /**
         * Set the throwable of this transaction
         *
         * @author   Jelle De Loecker <jelle@elevenways.be>
         * @since    0.1.0
         */
        public void setThrowable(Throwable t) {
            if (this.sentry_span != null) {
                this.sentry_span.setThrowable(t);
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

            this.has_finished = true;

            if (this.sentry_span != null) {

                if (BibLog.DEBUG) {
                    BibLog.attention("Finishing transaction " + this.name);
                }

                this.sentry_span.finish();
            }
        }
    }
}
