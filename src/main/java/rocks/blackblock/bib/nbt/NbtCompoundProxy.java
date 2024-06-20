package rocks.blackblock.bib.nbt;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A proxy for NbtCompound
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
public interface NbtCompoundProxy {

    /**
     * Get the NBT element to work with
     *
     * @since    0.1.0
     */
    @NotNull
    NbtCompound getProxiedNbtCompound();

    /**
     * Mark the element as dirty
     *
     * @since    0.1.0
     */
    void markDirty();

    /**
     * Does this contain the given key?
     *
     * @since    0.1.0
     */
    default boolean containsKey(String key) {
        return this.getProxiedNbtCompound().contains(key);
    }

    /**
     * Does this contain a uuid value?
     *
     * @since    0.1.0
     */
    default boolean containsUuid(String key) {
        return this.getProxiedNbtCompound().containsUuid(key);
    }

    /**
     * Get a UUID value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default UUID getUuid(String key) {
        return this.getUuid(key, null);
    }

    /**
     * Get a UUID value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default UUID getUuid(String key, UUID default_value) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (nbt.containsUuid(key)) {
            return nbt.getUuid(key);
        }

        return default_value;
    }

    /**
     * Put a UUID value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default void putUuid(String key, UUID new_value) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (new_value == null) {
            nbt.remove(key);
        } else {
            nbt.putUuid(key, new_value);
        }

        this.markDirty();
    }

    /**
     * Get an integer value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default Integer getInteger(String key) {
        return this.getInteger(key, null);
    }

    /**
     * Get an integer value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default Integer getInteger(String key, Integer default_value) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (nbt.contains(key, NbtElement.INT_TYPE)) {
            return nbt.getInt(key);
        }

        return default_value;
    }

    /**
     * Put an integer value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default void putInteger(String key, Integer new_value) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (new_value == null) {
            nbt.remove(key);
        } else {
            nbt.putInt(key, new_value);
        }

        this.markDirty();
    }

    /**
     * Get a string value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default String getString(String key) {
        return this.getString(key, null);
    }

    /**
     * Get a string value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default String getString(String key, String default_value) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (nbt.contains(key, NbtElement.STRING_TYPE)) {
            return nbt.getString(key);
        }

        return default_value;
    }

    /**
     * Put a string value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default void putString(String key, String new_value) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (new_value == null) {
            nbt.remove(key);
        } else {
            nbt.putString(key, new_value);
        }

        this.markDirty();
    }

    /**
     * Get a boolean value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default Boolean getBoolean(String key) {
        return this.getBoolean(key, null);
    }

    /**
     * Get a boolean value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default Boolean getBoolean(String key, Boolean default_value) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (nbt.contains(key, NbtElement.BYTE_TYPE)) {
            return nbt.getBoolean(key);
        }

        return default_value;
    }

    /**
     * Put a boolean value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default void putBoolean(String key, Boolean new_value) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (new_value == null) {
            nbt.remove(key);
        } else {
            nbt.putBoolean(key, new_value);
        }

        this.markDirty();
    }

    /**
     * Get a long value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default Long getLong(String key) {
        return this.getLong(key, null);
    }

    /**
     * Get a long value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    default Long getLong(String key, Long default_value) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (nbt.contains(key, NbtElement.LONG_TYPE)) {
            return nbt.getLong(key);
        }

        return default_value;
    }

    /**
     * Put a long value into the item stack's NBT data
     *
     * @since    0.1.0
     */
    default void putLong(String key, Long new_value) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (new_value == null) {
            nbt.remove(key);
        } else {
            nbt.putLong(key, new_value);
        }

        this.markDirty();
    }

    /**
     * Get an NBTList value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    @Nullable
    default NbtList getList(String key, int type) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (nbt.contains(key, NbtElement.LIST_TYPE)) {
            return nbt.getList(key, type);
        }

        return null;
    }

    /**
     * Get an NbtCompound value from the item stack's NBT data
     *
     * @since    0.1.0
     */
    @Nullable
    default NbtCompound getCompound(String key) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (nbt.contains(key, NbtElement.COMPOUND_TYPE)) {
            return nbt.getCompound(key);
        }

        return null;
    }

    /**
     * Put an NbtCompound value into the item stack's NBT data
     *
     * @since    0.1.0
     */
    default void putCompound(String key, NbtCompound new_value) {
        this.putElement(key, new_value);
    }

    /**
     * Get an NbtElement
     *
     * @since    0.1.0
     */
    @Nullable
    default NbtElement getElement(String key) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (nbt.contains(key)) {
            return nbt.get(key);
        }

        return null;
    }

    /**
     * Put an NbtElement
     *
     * @since    0.1.0
     */
    default void putElement(String key, NbtElement new_value) {

        NbtCompound nbt = this.getProxiedNbtCompound();

        if (new_value == null) {
            nbt.remove(key);
        } else {
            nbt.put(key, new_value);
        }

        this.markDirty();
    }
}