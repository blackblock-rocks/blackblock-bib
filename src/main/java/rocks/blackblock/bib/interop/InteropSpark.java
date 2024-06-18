package rocks.blackblock.bib.interop;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import org.jetbrains.annotations.ApiStatus;

/**
 * Class to work with Spark
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@ApiStatus.Internal()
public class InteropSpark {

    /**
     * Get the live average tick time
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static double getTickTime() {
        double tick_time = -1;

        Spark spark = SparkProvider.get();
        GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> mspt = spark.mspt();

        if (mspt != null) {
            tick_time = mspt.poll(StatisticWindow.MillisPerTick.SECONDS_10).median();
        }

        if (tick_time != -1) {
            return tick_time;
        }

        return -1;
    }
}
