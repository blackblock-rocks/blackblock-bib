package rocks.blackblock.bib.collection;

/**
 * A class that will let you get and set the value
 * of a specific instance
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 *
 * @param <T> The type of the instance.
 * @param <V> The type of the value.
 */
@SuppressWarnings({"unused"})
public record PropertyProxy<T, V>(T instance, Getter<V> getter, Setter<V> setter) {

    public boolean isPresent() {
        return this.getValue() != null;
    }

    public boolean isEmpty() {
        return this.getValue() == null;
    }

    public void setValue(V value) {
        this.setter.setValue(value);
    }

    public V getValue() {
        return this.getter.getValue();
    }

    public interface Setter<V> {
        void setValue(V value);
    }

    public interface Getter<V> {
        V getValue();
    }
}