package rocks.blackblock.bib.util;

/**
 * Library class for certain mathematical operations
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public abstract class BibMath {

    private static final int ATAN2_BITS = 8;
    private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
    private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
    private static final int ATAN2_COUNT = ATAN2_MASK + 1;
    private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);
    private static final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);
    private static final float[] ATAN2_MAP = new float[ATAN2_COUNT];

    static {
        for (int i = 0; i < ATAN2_DIM; i++) {
            for (int j = 0; j < ATAN2_DIM; j++) {
                float x0 = (float) i / ATAN2_DIM;
                float y0 = (float) j / ATAN2_DIM;

                ATAN2_MAP[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
            }
        }
    }

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
     * Calculate the inverse square root of a number very fast but less accurate.
     * In most cases, this is only slightly faster than doing `1 / Math.sqrt(x)`.
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
     * Calculate the arc tangent of a number very fast but less accurate.
     * Profiled against Math.atan2 and MathHelper.atan2:
     * A lot faster than Math.atan2, but only sometimes faster than MathHelper.atan2
     *
     * @since 0.2.0
     */
    public static float fastAtan2(double y, double x) {
        float add, mul;

        if (x < 0.0f)  {
            if (y < 0.0f) {
                x = -x;
                y = -y;

                mul = 1.0f;
            } else{
                x = -x;
                mul = -1.0f;
            }

            add = -3.141592653f;
        } else {
            if (y < 0.0f) {
                y = -y;
                mul = -1.0f;
            } else {
                mul = 1.0f;
            }

            add = 0.0f;
        }

        double invDiv = 1.0f / (((x < y) ? y : x) * INV_ATAN2_DIM_MINUS_1);

        int xi = (int) (x * invDiv);
        int yi = (int) (y * invDiv);

        return (ATAN2_MAP[yi * ATAN2_DIM + xi] + add) * mul;
    }
}
