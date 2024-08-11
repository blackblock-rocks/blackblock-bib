package rocks.blackblock.bib.collection;

/**
 * Class to calculate the average value of numbers
 *
 * @since    0.2.0
 */
public class RollingAverage<T extends Number> {

    private int buffer_size = 20;
    private T[] buffer;
    private int current_index;
    private double sum;
    private int count;

    /**
     * Create a new RollingAverage with a default buffer size of 20
     *
     * @since 0.2.0
     */
    public RollingAverage() {
        this(20);
    }

    /**
     * Create a new RollingAverage with the given buffer size
     *
     * @since 0.2.0
     */
    public RollingAverage(int buffer_size) {
        this.setBufferSize(buffer_size);
    }

    /**
     * Set the buffer size
     *
     * @since 0.2.0
     */
    @SuppressWarnings("unchecked")
    public void setBufferSize(int buffer_size) {
        this.buffer_size = buffer_size;
        this.buffer = (T[]) new Number[buffer_size];
        this.current_index = 0;
        this.sum = 0;
        this.count = 0;
    }

    /**
     * Add a new value to the average
     *
     * @since 0.2.0
     */
    public void addValue(T value) {

        if (value == null) {
            return;
        }

        if (count < buffer_size) {
            // Buffer not full yet
            sum += value.doubleValue();
            buffer[current_index] = value;
            count++;
        } else {
            // Buffer full, replace oldest value
            sum = sum - buffer[current_index].doubleValue() + value.doubleValue();
            buffer[current_index] = value;
        }

        current_index = (current_index + 1) % buffer_size;
    }

    /**
     * Get the average value
     *
     * @since 0.2.0
     */
    public double getAverage() {
        return count > 0 ? sum / count : 0;
    }

    /**
     * Get the current index
     *
     * @since 0.2.0
     */
    public int getCurrentIndex() {
        return current_index;
    }
}
