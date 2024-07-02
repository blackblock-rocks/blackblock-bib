package rocks.blackblock.bib.bv.parameter;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.text.Text;
import rocks.blackblock.bib.bv.value.BvMap;
import rocks.blackblock.bib.bv.value.BvString;
import rocks.blackblock.bib.command.CommandLeaf;

/**
 * A string parameter
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class StringParameter extends TweakParameter<BvString> {

    /**
     * Initialize the parameter
     *
     * @since 0.2.0
     */
    public StringParameter(String name) {
        super(name);
    }

    /**
     * Return the class of the contained type
     *
     * @since    0.2.0
     */
    @Override
    public Class<BvString> getContainedTypeClass() {
        return BvString.class;
    }

    /**
     * Configure the "set" part of the commands
     *
     * @since    0.2.0
     */
    public CommandLeaf configureSetLeaf(CommandLeaf set_leaf) {

        CommandLeaf value_leaf = set_leaf.getChild("value");
        value_leaf.setType(StringArgumentType.string());

        value_leaf.onExecute(command_context -> {

            String value = StringArgumentType.getString(command_context, "value");

            BvMap data_context = this.getDataContext(command_context);
            var succeeded = this.setInRootMap(data_context, BvString.of(value));

            if (succeeded) {
                return 1;
            }

            command_context.getSource().sendError(Text.literal("Failed to set value"));

            return 0;
        });

        return value_leaf;
    }

}
