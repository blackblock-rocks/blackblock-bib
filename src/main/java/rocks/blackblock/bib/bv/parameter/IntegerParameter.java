package rocks.blackblock.bib.bv.parameter;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.text.Text;
import rocks.blackblock.bib.bv.value.BvInteger;
import rocks.blackblock.bib.bv.value.BvMap;
import rocks.blackblock.bib.command.CommandLeaf;

/**
 * An integer parameter
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class IntegerParameter extends TweakParameter<BvInteger> {

    /**
     * Initialize the parameter
     *
     * @since 0.1.0
     */
    public IntegerParameter(String name) {
        super(name);
    }

    /**
     * Return the class of the contained type
     *
     * @since 0.1.0
     */
    @Override
    public Class<BvInteger> getContainedTypeClass() {
        return BvInteger.class;
    }

    /**
     * Configure the "set" part of the commands
     *
     * @since    0.1.0
     */
    public CommandLeaf configureSetLeaf(CommandLeaf set_leaf) {

        CommandLeaf value_leaf = set_leaf.getChild("value");
        value_leaf.setType(IntegerArgumentType.integer());

        value_leaf.onExecute(command_context -> {

            int value = IntegerArgumentType.getInteger(command_context, "value");

            BvMap data_context = this.getDataContext(command_context);
            var succeeded = this.setInRootMap(data_context, BvInteger.of(value));

            if (succeeded) {
                return 1;
            }

            command_context.getSource().sendError(Text.literal("Failed to set value"));

            return 0;
        });

        return value_leaf;
    }
}
