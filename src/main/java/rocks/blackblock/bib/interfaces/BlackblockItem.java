package rocks.blackblock.bib.interfaces;

import rocks.blackblock.bib.util.BibText;

public interface BlackblockItem {
    void appendTooltip(BibText.TooltipBuilder builder);
}
