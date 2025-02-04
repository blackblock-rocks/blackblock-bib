package rocks.blackblock.bib.random;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import rocks.blackblock.bib.interfaces.IsBlackblockRandom;

import java.util.UUID;

/**
 * A XorShiftRandom implementation
 *
 * @author   Gale Patch #0139
 * @author   QCPRummer (of Potatoptimize)
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class XorShiftRandom implements IsBlackblockRandom {

    final double REAL_UNIT_INT = 1.0 / (0x7FFFFFFFL);
    final double REAL_UNIT_UINT = 1.0 / (0xFFFFFFFFL);
    final long Y = 842502087L, Z = 3579807591L, W = 273326509L;
    long x, y, z, w;
    long boolBuffer;
    int boolBufferBits = 0;

    public XorShiftRandom() {
        seed((int) System.currentTimeMillis());
    }

    public XorShiftRandom(int seed) {
        seed(seed);
    }

    @Override
    public double nextDouble(double bound) {
        return (nextLong() >>> 1) / (double) Long.MAX_VALUE * bound;
    }

    @Override
    public double nextDouble(double origin, double bound) {
        return origin + (nextLong() >>> 1) / (double) Long.MAX_VALUE * (bound - origin);
    }

    @Override
    public float nextFloat(float bound) {
        return (nextLong() >>> 1) / (float) Long.MAX_VALUE * bound;
    }

    @Override
    public float nextFloat(float origin, float bound) {
        return origin + (nextLong() >>> 1) / (float) Long.MAX_VALUE * (bound - origin);
    }

    @Override
    public float nextGaussian(float mean, float deviation) {
        return (float) (mean + deviation * nextGaussian());
    }

    @Override
    public UUID nextUUID() {
        long mostSigBits = nextLong();
        long leastSigBits = nextLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    @Override
    public Random split() {
        return this;
    }

    @Override
    public RandomSplitter nextSplitter() {
        return new Splitter(0L, this);
    }

    @Override
    public void setSeed(long seed) {
        seed((int) seed);
    }

    @Override
    public int nextInt() {
        long t = (x ^ (x << 11));
        x = y;
        y = z;
        z = w;
        return (int) (0x7FFFFFFF & (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8))));
    }

    @Override
    public int nextInt(int bound) {
        if (bound < 0) {
            throw new IllegalArgumentException("upperBound must be >=0");
        }

        long t = (x ^ (x << 11));
        x = y;
        y = z;
        z = w;

        return (int) ((REAL_UNIT_INT * (int) (0x7FFFFFFF & (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8))))) * bound);
    }

    @Override
    public int nextInt(int origin, int bound) {
        return this.nextBetween(origin, bound);
    }

    @Override
    public int nextBetween(int min, int max) {
        if (min > max)
            throw new IllegalArgumentException("upperBound must be >=lowerBound");

        long t = (x ^ (x << 11));
        x = y;
        y = z;
        z = w;

        int range = max - min;
        if (range < 0) {
            return min + (int) ((REAL_UNIT_UINT * (double) (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8)))) * (double) ((long) max - (long) min));
        }
        return min + (int) ((REAL_UNIT_INT * (double) (int) (0x7FFFFFFF & (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8))))) * (double) range);
    }

    @Override
    public long nextLong() {
        // This special commit goes to Codium AI for finding a bug that IDEA tells me is completely unnecessary
        return ((long) nextUInt() << 32) + nextUInt();
    }

    @Override
    public boolean nextBoolean() {
        if (boolBufferBits == 0) {
            boolBuffer = nextUInt();
            boolBufferBits = 32;
        }
        boolBuffer >>= 1;
        boolean bit = (boolBuffer & 1) == 0;
        --boolBufferBits;
        return bit;
    }

    @Override
    public float nextFloat() {
        return (float) nextDouble();
    }

    @Override
    public double nextDouble() {
        long t = (x ^ (x << 11));
        x = y;
        y = z;
        z = w;
        return (REAL_UNIT_INT * (int) (0x7FFFFFFF & (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8)))));
    }

    @Override
    public double nextGaussian() {
        long randomBits = nextLong();
        long evenChunks = randomBits & EVEN_CHUNKS;
        long oddChunks = (randomBits & ODD_CHUNKS) >>> 5;
        long sum = chunkSum(evenChunks + oddChunks) - 186;
        return sum / 31.0;
    }

    private long chunkSum(long bits) {
        long sum = bits + (bits >>> 40);
        sum += sum >>> 20;
        sum += sum >>> 10;
        sum &= (1<<10)-1;
        return sum;
    }

    private final long EVEN_CHUNKS = 0x7c1f07c1f07c1fL;
    private final long ODD_CHUNKS  = EVEN_CHUNKS << 5;

    @Override
    public double nextTriangular(double mode, double deviation) {
        double u = nextDouble();
        double squareDev = 2 * deviation;
        double c = (mode - deviation) / (squareDev);
        if (u > c) {
            u = 1.0 - u;
            c = 1.0 - c;
        }
        return mode - deviation + (squareDev) * Math.sqrt(u * c);
    }

    @Override
    public int nextBetweenExclusive(int min, int max) {
        return nextBetween(min + 1, max - 1);
    }

    private long nextUInt() {
        long t = (x ^ (x << 11));
        x = y;
        y = z;
        z = w;
        return (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8))) & (0xFFFFFFFFL);
    }


    private void seed(int seed) {
        x = seed;
        y = Y;
        z = Z;
        w = W;
    }

    public record Splitter(long seed, XorShiftRandom instance) implements RandomSplitter {

        @Override
        public Random split(String seed) {
            int i = seed.hashCode();
            return new XorShiftRandom((int) (i ^ instance.x));
        }

        @Override
        public Random split(long seed) {
            return new XorShiftRandom((int) (seed ^ instance.x));
        }

        @Override
        public Random split(int x, int y, int z) {
            long l = MathHelper.hashCode(x, y, z);
            int m = (int) (l ^ this.seed);
            return new XorShiftRandom(m);
        }

        @Override
        public void addDebugInfo(StringBuilder info) {
            info.append("seed: ").append(instance.x);
        }
    }
}
