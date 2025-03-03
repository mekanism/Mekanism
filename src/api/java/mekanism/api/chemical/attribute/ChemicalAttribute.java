package mekanism.api.chemical.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.datamaps.chemical.attribute.IChemicalAttribute;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;

/**
 * All chemical attributes should inherit from this class. No specific implementation is required.
 *
 * @author aidancbrady
 */
@Deprecated(forRemoval = true, since = "10.7.11")
public abstract class ChemicalAttribute implements IChemicalAttribute {

    /**
     * Add text components to this chemical attribute's tooltip.
     *
     * @param list list of tooltips to be displayed
     *
     * @return updated list of tooltips
     *
     * @deprecated since 10.7.4. Use {@link #collectTooltips(Consumer)} instead.
     */
    @Deprecated(forRemoval = true, since = "10.7.4")
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
    @Deprecated(forRemoval = true, since = "10.7.11")
    public void collectTooltips(Consumer<Component> adder) {
        //TODO - 1.22: When removing this legacy handling, make overriders call super
        List<Component> list = new ArrayList<>();
        addTooltipText(list);
        for (Component component : list) {
            adder.accept(component);
        }
    }

    @Override
    public void collectTooltips(TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
        collectTooltips(tooltips::add);
    }

    @Override
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public final ChemicalAttribute toLegacyAttribute() {
        return this;
    }
}
