package rocks.blackblock.bib.interfaces;

import net.minecraft.util.math.random.Random;

import java.util.UUID;

/**
 * Extension of Minecraft's own Random interface
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public interface IsBlackblockRandom extends Random {

    /**
     * Get the next int within the given bounds
     * @since 0.2.0
     */
    int nextInt(int origin, int bound);

    /**
     * Get the next double within the given upper bound
     * @since 0.2.0
     */
    double nextDouble(double bound);

    /**
     * Get the next double within the given bounds
     * @since 0.2.0
     */
    double nextDouble(double origin, double bound);

    /**
     * Get the next float within the given upper bound
     * @since 0.2.0
     */
    float nextFloat(float bound);

    /**
     * Get the next float within the given bounds
     * @since 0.2.0
     */
    float nextFloat(float origin, float bound);

    /**
     * Get the next Gaussian value
     * @since 0.2.0
     */
    float nextGaussian(float mean, float deviation);

    /**
     * Get the next UUID value
     * @since 0.2.0
     */
    UUID nextUUID();
}
