package rocks.blackblock.bib.interfaces;

import net.minecraft.entity.EntityType;

/**
 * Extension interface for TrialSpawnerLogic to set entity types directly
 */
public interface TrialSpawnerLogicExtension {
    void bib$setEntityTypeDirectly(EntityType<?> entityType);
}