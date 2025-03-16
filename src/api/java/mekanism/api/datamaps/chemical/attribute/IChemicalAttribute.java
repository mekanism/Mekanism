package mekanism.api.datamaps.chemical.attribute;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Represents a chemical attribute.
 * @since 10.7.11
 */
public interface IChemicalAttribute {

    /**
     * Converts this attribute into a legacy variant.
     */
    @Internal
    @SuppressWarnings("removal")
    mekanism.api.chemical.attribute.ChemicalAttribute toLegacyAttribute();

    /**
     * If this returns true, chemicals possessing this attribute will not be accepted by any prefab handlers by default unless validated.
     *
     * @return if chemicals with this attribute require validation before being accepted
     */
    default boolean needsValidation() {
        return false;
    }

    /**
     * Add text components to this chemical attribute's tooltip.
     *
     * @param context Current tooltip context
     * @param tooltips List of tooltips to add to.
     * @param tooltipFlag Flag representing if advanced tooltips are to be shown.
     */
    default void collectTooltips(TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
    }
}