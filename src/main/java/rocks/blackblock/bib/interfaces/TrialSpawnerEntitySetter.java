package rocks.blackblock.bib.interfaces;

import net.minecraft.entity.EntityType;

/**
 * Interface for setting entity types on trial spawners without requiring a world
 */
public interface TrialSpawnerEntitySetter {
    void bib$setEntityTypeWithoutWorld(EntityType<?> entityType);
}