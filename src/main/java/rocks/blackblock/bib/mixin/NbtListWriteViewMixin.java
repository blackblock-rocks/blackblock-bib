package rocks.blackblock.bib.mixin;

import net.minecraft.nbt.NbtList;
import net.minecraft.storage.NbtWriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NbtWriteView.NbtListView.class)
public interface NbtListWriteViewMixin {

    @Accessor("list")
    NbtList bb$getNbtList();
}
