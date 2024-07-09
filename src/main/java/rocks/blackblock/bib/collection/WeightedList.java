package rocks.blackblock.bib.collection;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A generic list implementation that associates weights with elements.
 * This allows for weighted random selection of elements, where items with higher weights
 * have a proportionally higher chance of being selected.
 *
 * <p>This class is not thread-safe. If multiple threads access a WeightedList instance
 * concurrently, and at least one of the threads modifies the list structurally, it must
 * be synchronized externally.</p>
 *
 * @param <T> the type of elements maintained by this list
 *
 * @author Jelle De Loecker <jelle@elevenways.be>
 * @since  0.2.0
 */
@SuppressWarnings({"unused"})
public class WeightedList<T> implements Iterable<WeightedList.WeightedEntry<T>> {

    protected final ArrayList<WeightedEntry<T>> entries;
    private double totalWeight = 0;
    protected final Random random;

    // Default constructor
    public WeightedList() {
        this(new Random());
    }

    // Constructor with custom Random
    public WeightedList(Random random) {
        this(16, random);
    }

    // Constructor with initial capacity
    public WeightedList(int initialCapacity) {
        this(initialCapacity, new Random());
    }

    // Constructor with both initial capacity and custom Random
    public WeightedList(int initialCapacity, Random random) {
        this.entries = new ArrayList<>(initialCapacity);
        this.random = random;
    }

    public void add(T value, double weight) {
        if (weight <= 0) throw new IllegalArgumentException("Weight must be positive");
        entries.add(new WeightedEntry<>(value, weight));
        totalWeight += weight;
    }

    public boolean remove(T value) {
        boolean removed = false;
        Iterator<WeightedEntry<T>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            WeightedEntry<T> entry = iterator.next();
            if (Objects.equals(entry.value, value)) {
                totalWeight -= entry.weight;
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    public T getRandom() {
        if (entries.isEmpty()) {
            return null;
        }

        double r = random.nextDouble() * totalWeight;
        for (WeightedEntry<T> entry : entries) {
            r -= entry.weight;
            if (r <= 0) {
                return entry.value;
            }
        }

        // This should never happen, but just in case of rounding errors
        return entries.get(entries.size() - 1).value;
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public void trimToSize() {
        this.entries.trimToSize();
    }

    public List<T> values() {
        List<T> values = new ArrayList<>(this.entries.size());

        for (var entry : this.entries) {
            values.add(entry.value);
        }

        return values;
    }

    @NotNull
    @Override
    public Iterator<WeightedEntry<T>> iterator() {
        return this.entries.iterator();
    }

    public record WeightedEntry<T>(T value, double weight) {}
}
