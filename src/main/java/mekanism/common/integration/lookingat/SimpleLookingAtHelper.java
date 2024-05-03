package mekanism.common.integration.lookingat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

public abstract class SimpleLookingAtHelper implements LookingAtHelper {

    public final List<ILookingAtElement> elements;

    public SimpleLookingAtHelper() {
        this(new ArrayList<>());
    }

    public SimpleLookingAtHelper(List<ILookingAtElement> elements) {
        this.elements = elements;
    }

    @Override
    public void addText(Component text) {
        elements.add(new TextElement(text));
    }

    @Override
    public void addEnergyElement(EnergyElement element) {
        elements.add(element);
    }

    @Override
    public void addFluidElement(FluidElement element) {
        elements.add(element);
    }

    @Override
    public void addChemicalElement(ChemicalElement element) {
        elements.add(element);
    }
}