package mekanism.api.chemical.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

/**
 * All chemical attributes should inherit from this class. No specific implementation is required.
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
     *
     * @deprecated since 10.7.4. Use {@link #collectTooltips(Consumer)} instead.
     */
    @Deprecated(since = "10.7.4", forRemoval = true)
    public List<Component> addTooltipText(List<Component> list) {
        return list;
    }

    /**
     * Add text components to this chemical attribute's tooltip.
     *
     * @param adder Method reference to add tooltips to be displayed.
     *
     * @since 10.7.4
     */
    public void collectTooltips(Consumer<Component> adder) {
        //TODO - 1.22: When removing this legacy handling, make overriders call super
        List<Component> list = new ArrayList<>();
        addTooltipText(list);
        for (Component component : list) {
            adder.accept(component);
        }
    }
}
