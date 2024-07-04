package rocks.blackblock.bib.debug;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;

import static org.apache.logging.log4j.core.LoggerContext.getContext;

@Plugin(name = "Log4jDeobfuscatorFilter", category = "Core", elementType = "filter", printObject = true)
public class Log4jDeobfuscatorFilter extends AbstractFilter {

    public static final Marker DEOBFUSCATED_MARKER = MarkerManager.getMarker("DEOBFUSCATED");

    /**
     * Instantiate the filter
     *
     * @since    0.2.0
     */
    public Log4jDeobfuscatorFilter(Result onMatch, Result onMismatch) {
        super(onMatch, onMismatch);
    }

    /**
     * Apply the filter on the given event.
     * This will block all original events & re-append a customized one.
     *
     * @since    0.2.0
     */
    @Override
    public Result filter(LogEvent event) {

        if (event.getMarker() != null && event.getMarker().isInstanceOf(DEOBFUSCATED_MARKER)) {
            // Allow modified events through
            return Result.NEUTRAL;
        }

        this.modifyEventMessageAndReAppend(event);

        return Result.DENY;
    }

    /**
     * Modify the message of the given event and re-append it as a new event
     *
     * @since    0.2.0
     */
    private void modifyEventMessageAndReAppend(LogEvent event) {

        String deobfuscated = BibYarn.INSTANCE.deobfuscateStackTrace(event.getMessage().getFormattedMessage());

        // Create a new LogEvent with the modified message
        LogEvent modifiedEvent = new Log4jLogEvent.Builder(event)
                .setMessage(new SimpleMessage(deobfuscated))
                .setMarker(DEOBFUSCATED_MARKER)
                .build();

        // Forward the modified event to the original appenders
        for (Appender appender : getContext().getConfiguration().getAppenders().values()) {
            appender.append(modifiedEvent);
        }
    }
}