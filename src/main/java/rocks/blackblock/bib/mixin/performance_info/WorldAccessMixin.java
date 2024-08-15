package rocks.blackblock.bib.mixin.performance_info;

import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import rocks.blackblock.bib.interfaces.HasPerformanceInfo;

@Mixin(WorldAccess.class)
public interface WorldAccessMixin extends HasPerformanceInfo {
    // Keep the default implementation
}
