package rocks.blackblock.bib.mixin;

import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.block.spawner.TrialSpawnerData;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rocks.blackblock.bib.interfaces.TrialSpawnerLogicExtension;

@Mixin(TrialSpawnerLogic.class)
public abstract class TrialSpawnerLogicMixin implements TrialSpawnerLogicExtension {
    
    @Shadow
    private TrialSpawnerLogic.FullConfig fullConfig;
    
    @Shadow
    private TrialSpawnerData data;
    
    @Override
    public void bib$setEntityTypeDirectly(EntityType<?> entityType) {
        // Create new config with the entity type
        this.fullConfig = this.fullConfig.withEntityType(entityType);
        this.data.reset();
    }
}