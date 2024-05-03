package mekanism.common.integration.lookingat;

import net.minecraft.network.chat.Component;

public interface LookingAtHelper {

    void addText(Component text);

    void addEnergyElement(EnergyElement element);

    void addFluidElement(FluidElement element);

    void addChemicalElement(ChemicalElement element);
}