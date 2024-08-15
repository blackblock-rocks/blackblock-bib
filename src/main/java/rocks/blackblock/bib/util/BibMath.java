package rocks.blackblock.bib.util;

/**
 * Library class for certain mathematical operations
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public abstract class BibMath {

    private BibMath() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Gets the least significant set bit of the input.
     * Uses a bitwise trick for efficiency.
     * @since 0.2.0
     */
    public static int getTrailingBit(final int n) {
        return -n & n;
    }

    /**
     * Gets the least significant set bit of the input for long integers.
     * Uses a bitwise trick for efficiency.
     * @since 0.2.0
     */
    public static long getTrailingBit(final long n) {
        return -n & n;
    }

    /**
     * Calculate the inverse square root of a number very fast but less accurate
     * @since 0.2.0
     */
    public static double fastInverseSqrt(double x) {
        double d = 0.5 * x;
        long l = Double.doubleToRawLongBits(x);
        l = 6910469410427058090L - (l >> 1);
        x = Double.longBitsToDouble(l);
        return x * (1.5 - d * x * x);
    }

    /**
     * Calculate a square root of a number very fast but less accurate
     * @since 0.2.0
     */
    public static double fastSqrt(double x) {
        return x * fastInverseSqrt(x);
    }
}
