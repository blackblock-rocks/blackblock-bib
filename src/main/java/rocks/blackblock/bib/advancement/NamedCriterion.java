package rocks.blackblock.bib.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rocks.blackblock.bib.util.BibCodec;

import java.util.Optional;

public class NamedCriterion extends AbstractCriterion<NamedCriterion.Conditions> {
    private final Identifier ID;

    public NamedCriterion(Identifier id) {
        ID = id;
    }

    public Identifier getId() {
        return ID;
    }

    public void trigger(ServerPlayerEntity player, String name) {
        this.trigger(player, (conditions) -> conditions.matches(name));
    }

    /**
     * Return the codec used to unserialize this criterion.
     */
    @Override
    public Codec<NamedCriterion.Conditions> getConditionsCodec() {
        return NamedCriterion.Conditions.CODEC;
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<String> name) implements AbstractCriterion.Conditions {

        /**
         * The actual codec
         */
        public static final Codec<NamedCriterion.Conditions> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                BibCodec.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player").forGetter(Conditions::player),
                BibCodec.createStrictOptionalFieldCodec(Codec.STRING, "name").forGetter(Conditions::name)
        ).apply(instance, Conditions::new));

        public boolean matches(String name) {

            if (this.name.isPresent() && !this.name.get().equals(name)) {
                return false;
            }

            return true;
        }
    }
}
