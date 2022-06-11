package mekanism.common.integration.lookingat;

import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

public class HwylaLookingAtHelper implements LookingAtHelper {

    public static final String CHEMICAL_STACK = "chemical";
    public static final String TEXT = "text";

    private final ListTag data = new ListTag();
    @Override
    public void addText(Component text) {
        CompoundTag textData = new CompoundTag();
        textData.putString(TEXT, Component.Serializer.toJson(text));
        data.add(textData);
    }

    @Override
    public void addEnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
        CompoundTag energyData = new CompoundTag();
        energyData.putString(NBTConstants.ENERGY_STORED, energy.toString());
        energyData.putString(NBTConstants.MAX, maxEnergy.toString());
        data.add(energyData);
    }

    @Override
    public void addFluidElement(FluidStack stored, int capacity) {
        CompoundTag fluidData = new CompoundTag();
        fluidData.put(NBTConstants.FLUID_STORED, stored.writeToNBT(new CompoundTag()));
        fluidData.putInt(NBTConstants.MAX, capacity);
        data.add(fluidData);
    }

    @Override
    public void addChemicalElement(ChemicalStack<?> stored, long capacity) {
        CompoundTag chemicalData = new CompoundTag();
        chemicalData.put(CHEMICAL_STACK, stored.write(new CompoundTag()));
        chemicalData.putLong(NBTConstants.MAX, capacity);
        data.add(chemicalData);
    }

    public void finalizeData(CompoundTag data) {
        if (!this.data.isEmpty()) {
            data.put(NBTConstants.MEK_DATA, this.data);
        }
    }
}