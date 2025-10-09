package rocks.blackblock.bib.mixin.dfu;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataFixerBuilder.class)
public interface DataFixerBuilderAccessor {
    @Accessor(value = "schemas", remap = false)
    Int2ObjectSortedMap<Schema> bb$getSchemas();
}
