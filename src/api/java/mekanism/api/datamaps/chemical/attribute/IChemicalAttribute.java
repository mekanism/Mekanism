package mekanism.api.datamaps.chemical.attribute;

import java.util.function.Consumer;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import net.minecraft.network.chat.Component;

//TODO - 1.21: Docs
public interface IChemicalAttribute {

    @Deprecated
    ChemicalAttribute toLegacyAttribute();

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
     * @param adder Method reference to add tooltips to be displayed.
     */
    default void collectTooltips(Consumer<Component> adder) {
    }
}