package rocks.blackblock.bib.util;

import org.jetbrains.annotations.ApiStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

/**
 * Library class for working with time & dates
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibTime {

    public static LocalDate LOCAL_DATE = LocalDate.now();
    public static LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now();
    public static boolean IS_AROUND_HALLOWEEN = false;

    /**
     * Initialize the time class
     * @since    0.2.0
     */
    @ApiStatus.Internal
    public static void initialize() {
        // Update the time every minute
        BibFlow.setInterval(BibTime::updateTime, 60 * 1000);

        // Update the date every 5 minutes
        BibFlow.setInterval(BibTime::updateDate, 5 * 60 * 1000);
    }

    /**
     * Update the date
     * @since    0.2.0
     */
    private static void updateDate() {
        LOCAL_DATE = LocalDate.now();

        int day_of_month = LOCAL_DATE.get(ChronoField.DAY_OF_MONTH);
        int month_of_year = LOCAL_DATE.get(ChronoField.MONTH_OF_YEAR);
        IS_AROUND_HALLOWEEN = month_of_year == 10 && day_of_month >= 20 || month_of_year == 11 && day_of_month <= 3;
    }

    /**
     * Update the time
     * @since    0.2.0
     */
    private static void updateTime() {
        LOCAL_DATE_TIME = LocalDateTime.now();
    }
}
