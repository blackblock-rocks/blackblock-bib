package rocks.blackblock.bib.debug.logging;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.spi.LoggerContextFactory;

import java.util.List;

/**
 * Setup our deobfuscator appender at the earliest possible time.
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public class DeobfuscatorSetup implements PreLaunchEntrypoint {

    /**
     * Try to load our Tiny Yarn file on startup.
     * If that works: reconfigure Log4j & the System.out stream
     *
     * @since    0.2.0
     */
    @Override
    public void onPreLaunch() {
        String TINY_YARN_PATH = System.getenv("TINY_YARN");

        if (TINY_YARN_PATH == null || TINY_YARN_PATH.isBlank()) {
            return;
        }

        BibYarn.INSTANCE = BibYarn.from(TINY_YARN_PATH);

        if (BibYarn.INSTANCE != null) {
            // Also replace the original System.out output stream
            BibYarn.INSTANCE.setupOutputInterceptor();
        }

        LoggerContextFactory factory = LogManager.getFactory();

        if (factory instanceof Log4jContextFactory log4jfactory) {
            List<LoggerContext> contexts = log4jfactory.getSelector().getLoggerContexts();
            contexts.forEach(this::replaceLogger);
        }
    }

    /**
     * Add our custom filter to each logger context
     *
     * @since    0.2.0
     */
    private void replaceLogger(LoggerContext context) {

        // Get the current log4j configuration
        Configuration config = context.getConfiguration();

        // Get the root logger
        LoggerConfig rootLoggerConfig = config.getRootLogger();

        // We're going to add our custom "filter/reappender" to it
        Log4jDeobfuscatorFilter customFilter = new Log4jDeobfuscatorFilter(Filter.Result.NEUTRAL, Filter.Result.DENY);
        rootLoggerConfig.addFilter(customFilter);

        context.updateLoggers();
    }
}
