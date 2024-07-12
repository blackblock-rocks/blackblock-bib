package rocks.blackblock.bib.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import rocks.blackblock.bib.interfaces.HasItemIcon;

@Mixin(value = EntityType.class, priority = 10)
public class EntityTypeMixin implements HasItemIcon {

    /**
     * Get the item icon of the entity.
     * This is their spawn egg by default
     *
     * @since 0.2.0
     */
    @Override
    public @Nullable Item getItemIcon() {
        return SpawnEggItem.forEntity((EntityType<?>) (Object) this);
    }
}
