package rocks.blackblock.bib.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtReadView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(NbtReadView.NbtListReadView.class)
public interface NbtListReadViewMixin {

    @Accessor("nbts")
    List<NbtCompound> bb$getListOfCompounds();

}
