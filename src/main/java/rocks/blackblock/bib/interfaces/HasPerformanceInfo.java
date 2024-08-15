package rocks.blackblock.bib.interfaces;

import rocks.blackblock.bib.util.BibPerf;

/**
 * Get the performance info of something,
 * probably a world
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
public interface HasPerformanceInfo {
    default BibPerf.Info bb$getPerformanceInfo() {
        return null;
    }
}
