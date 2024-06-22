package rocks.blackblock.bib.interaction;

import rocks.blackblock.bib.util.BibBlock;

/**
 * BlockEntities with this interface will have a
 * custom method to handle an interaction
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public interface InternalBibCustomInteractionHandler {
    void handleBlockInteraction(BibBlock.Interaction interaction);
}
