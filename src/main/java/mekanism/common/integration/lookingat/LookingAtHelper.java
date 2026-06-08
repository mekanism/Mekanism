package mekanism.common.integration.lookingat;

import net.minecraft.network.chat.Component;

public interface LookingAtHelper {

    void addText(Component text);

    void addEnergyElement(EnergyElement element);

    void addFluidElement(FluidElement element);

    void addChemicalElement(ChemicalElement element);

    default void addElement(ILookingAtElement element) {
        switch (element) {
            case EnergyElement energyElement -> addEnergyElement(energyElement);
            case ChemicalElement chemicalElement -> addChemicalElement(chemicalElement);
            case FluidElement fluidElement -> addFluidElement(fluidElement);
            case TextElement textElement -> addText(textElement.text());
        }
    }
}