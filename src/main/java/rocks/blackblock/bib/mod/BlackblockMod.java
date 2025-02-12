package rocks.blackblock.bib.mod;

import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import rocks.blackblock.bib.util.BibSound;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for other mods to do mod-specific stuff
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.3.0
 */
@SuppressWarnings("unused")
public class BlackblockMod {

    private static final Map<String, BlackblockMod> INSTANCES = new HashMap<>();
    private static final String BLACKBLOCK_NAMESPACE = "blackblock";
    private final String full_mod_namespace;

    /**
     * Initialize the class for a specific mod.
     * The full-mod-id should be something like "blackblock-belongings"
     */
    private BlackblockMod(String full_mod_namespace) {
        this.full_mod_namespace = full_mod_namespace;
    }

    /**
     * Get or create a mod instance
     */
    public static BlackblockMod get(String full_mod_namespace) {
        return INSTANCES.computeIfAbsent(full_mod_namespace, BlackblockMod::new);
    }

    /**
     * Create an Identifier in the Blackblock namespace
     */
    public Identifier sharedId(String path) {
        return Identifier.of(BLACKBLOCK_NAMESPACE, path);
    }

    /**
     * Create a mod-specific ID
     */
    public Identifier localId(String path) {
        return Identifier.of(this.full_mod_namespace, path);
    }

    /**
     * Register a sound
     */
    public SoundEvent registerSound(String path) {
        return BibSound.register(this.localId(path));
    }

    /**
     * Register a data-component
     * (under the Blackblock namespace)
     */
    public <T> ComponentType<T> registerDataComponent(String path, ComponentType<T> component) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                this.sharedId(path),
                component
        );
    }

    /**
     * Register a data-component using the given codec
     * (under the Blackblock namespace)
     */
    public <T> ComponentType<T> registerDataComponent(String path, PrimitiveCodec<T> codec) {
        return this.registerDataComponent(path, ComponentType.<T>builder().codec(codec).build());
    }
}
