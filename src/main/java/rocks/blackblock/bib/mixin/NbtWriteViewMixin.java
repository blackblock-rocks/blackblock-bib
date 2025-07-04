package rocks.blackblock.bib.mixin;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NbtWriteView.class)
public interface NbtWriteViewMixin {

    @Invoker("<init>")
    static NbtWriteView bb$createNbtWriteView(ErrorReporter reporter, DynamicOps<NbtElement> ops, NbtCompound nbt) {
        throw new AssertionError("Mixin failed to apply");
    }

    @Accessor("nbt")
    NbtCompound bb$getNbt();

}
