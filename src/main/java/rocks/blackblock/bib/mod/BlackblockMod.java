package rocks.blackblock.bib.mod;

import com.mojang.serialization.codecs.PrimitiveCodec;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import rocks.blackblock.bib.util.BibItem;
import rocks.blackblock.bib.util.BibSound;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
    private final Map<Identifier, Item> items = new HashMap<>();
    private final Map<Identifier, Block> blocks = new HashMap<>();
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

    /**
     * Register a tag key
     */
    public <T> TagKey<T> registerTagKey(RegistryKey<? extends Registry<T>> registry, String path) {
        return TagKey.of(registry, this.sharedId(path));
    }

    /**
     * Register a tag key of items
     */
    public TagKey<Item> registerItemTagKey(String path) {
        return registerTagKey(RegistryKeys.ITEM, path);
    }

    /**
     * Register an item with plain settings
     *
     * @param   item_name   The name of the item (without namespace)
     * @param   factory     The item instance creator
     */
    public <T extends Item> T registerItem(String item_name, Function<Item.Settings, T> factory) {
        return registerItem(item_name, factory, BibItem.createItemSettings());
    }

    /**
     * Register a plain item with plain settings
     *
     * @param   item_name   The name of the item (without namespace)
     */
    public Item registerItem(String item_name) {
        return registerItem(item_name, Item::new, BibItem.createItemSettings());
    }

    /**
     * Register an item under the shared `blackblock` namespace
     *
     * @param   item_name   The name of the item (without namespace)
     * @param   item        The item instance to register
     */
    public <T extends Item> T registerItem(String item_name, Function<Item.Settings, T> factory, Item.Settings settings) {

        Identifier item_id = this.sharedId(item_name);
        var key = RegistryKey.of(RegistryKeys.ITEM, item_id);

        T item = factory.apply(settings.registryKey(key));
        this.items.put(item_id, item);

        return Registry.register(Registries.ITEM, item_id, item);
    }

    /**
     * Register a block under the `blackblock` namespace
     *
     * @param   block_name   The name of the block (without namespace)
     * @param   block        The block instance to register
     */
    public <T extends Block> T registerBlock(String block_name, Function<AbstractBlock.Settings, T> factory, AbstractBlock.Settings settings) {
        Identifier block_id = this.sharedId(block_name);
        var key = RegistryKey.of(RegistryKeys.BLOCK, block_id);
        T block = factory.apply(settings.registryKey(key));
        this.blocks.put(block_id, block);

        return Registry.register(Registries.BLOCK, block_id, block);
    }

    /**
     * Register a block under the `blackblock` namespace and also create a generic block item for it
     *
     * @param   block_name   The name of the block (without namespace)
     * @param   block        The block instance to register
     */
    public <T extends Block> T registerBlockAndItem(String block_name, Function<AbstractBlock.Settings, T> factory, AbstractBlock.Settings settings) {
        T registered_block = registerBlock(block_name, factory, settings);
        registerBlockItem(block_name, registered_block);
        return registered_block;
    }

    /**
     * Register an item for the given block
     *
     * @param   block_name   The name of the block (without namespace)
     * @param   block        The block instance to register
     */
    public BlockItem registerBlockItem(String block_name, Block block) {
        return registerBlockItem(block_name, block, new Item.Settings());
    }

    /**
     * Register an item for the given block
     *
     * @param   block_name   The name of the block (without namespace)
     * @param   block        The block instance to register
     */
    public BlockItem registerBlockItem(String block_name, Block block, Item.Settings item_settings) {
        BlockItem item = registerItem(block_name, settings -> new BlockItem(block, settings), item_settings);
        return item;
    }

    /**
     * Register a block entity under the `blackblock` namespace
     *
     * @param   block_entity_name   The name of the block entity (without namespace)
     * @param   factory             The block entity factory
     * @param   block               The main block
     */
    public <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String block_entity_name, FabricBlockEntityTypeBuilder.Factory<? extends T> factory, Block block) {
        Identifier identifier = this.sharedId(block_entity_name);
        FabricBlockEntityTypeBuilder<T> builder = FabricBlockEntityTypeBuilder.create(factory, block);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, identifier, builder.build());
    }
}
