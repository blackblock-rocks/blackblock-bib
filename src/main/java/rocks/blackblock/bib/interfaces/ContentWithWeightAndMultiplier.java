package rocks.blackblock.bib.interfaces;

/**
 * Let an instance have a weight and a multiplier
 *
 * @author  Jelle De Loecker   <jelle@elevenways.be>
 * @since   0.2.0
 */
public interface ContentWithWeightAndMultiplier<T> extends HasWeight {

    /**
     * Get the held content
     * @since   0.2.0
     */
    T getContent();

    /**
     * Get the multiplier
     * @since   0.2.0
     */
    double getMultiplier();

    /**
     * Get the original weight
     * @since   0.2.0
     */
    int getOriginalWeight();

    /**
     * Apply the calculation
     * @since   0.2.0
     */
    default int applyMultiplierToOriginalWeight() {
        return (int) Math.ceil(this.getOriginalWeight() * 10000 * this.getMultiplier());
    }

    /**
     * Default implementation
     */
    abstract class Default<T> implements ContentWithWeightAndMultiplier<T> {

        protected T content;
        protected int original_weight;
        protected double multiplier = 1.0;
        protected Integer adjusted_weight = null;

        /**
         * Create a new instance
         * @since   0.2.0
         */
        public Default(T content, int original_weight) {
            this.content = content;
            this.original_weight = original_weight;
        }

        /**
         * Get the held content
         * @since   0.2.0
         */
        @Override
        public T getContent() {
            return this.content;
        }

        /**
         * Get the multiplier
         * @since   0.2.0
         */
        @Override
        public double getMultiplier() {
            return this.multiplier;
        }

        /**
         * Get the original weight
         * @since   0.2.0
         */
        @Override
        public int getOriginalWeight() {
            return this.original_weight;
        }

        /**
         * Get the adjusted weight
         * @since   0.2.0
         */
        @Override
        public int getWeight() {

            if (this.adjusted_weight == null) {
                this.adjusted_weight = this.applyMultiplierToOriginalWeight();
            }

            return this.adjusted_weight;
        }
    }
}
