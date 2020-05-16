package mekanism.api.chemical.attribute;

import java.util.List;
import net.minecraft.util.text.ITextComponent;

/**
 * All chemical attributes should inherit from from this class. No specific implementation is required.
 *
 * @author aidancbrady
 */
public abstract class ChemicalAttribute {

    /**
     * If this returns true, chemicals possessing this attribute will not be accepted by any prefab handlers by default unless validated.
     *
     * @return if chemicals with this attribute require validation before being accepted
     */
    public boolean needsValidation() {
        return false;
    }

    /**
     * Add text components to this chemical attribute's tooltip.
     *
     * @param list list of tooltips to be displayed
     *
     * @return updated list of tooltips
     */
    public List<ITextComponent> addTooltipText(List<ITextComponent> list) {
        return list;
    }
}
