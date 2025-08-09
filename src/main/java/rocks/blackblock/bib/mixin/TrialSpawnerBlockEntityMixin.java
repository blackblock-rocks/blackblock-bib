package rocks.blackblock.bib.mixin;

import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rocks.blackblock.bib.interfaces.TrialSpawnerEntitySetter;
import rocks.blackblock.bib.interfaces.TrialSpawnerLogicExtension;

@Mixin(TrialSpawnerBlockEntity.class)
public abstract class TrialSpawnerBlockEntityMixin implements TrialSpawnerEntitySetter {
    
    @Shadow
    public abstract TrialSpawnerLogic getSpawner();
    
    @Override
    @SuppressWarnings("unchecked")
    public void bib$setEntityTypeWithoutWorld(EntityType<?> entityType) {
        TrialSpawnerLogic spawner = this.getSpawner();
        // This cast will work at runtime due to TrialSpawnerLogicMixin
        ((TrialSpawnerLogicExtension) (Object) spawner).bib$setEntityTypeDirectly(entityType);
    }
}