package mekanism.common.integration.lookingat.jade;

import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.LookingAtHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.neoforged.neoforge.fluids.FluidStack;

public class JadeLookingAtHelper implements LookingAtHelper {

    static final String CHEMICAL_STACK = "chemical";
    static final String TEXT = "text";

    private final ListTag data = new ListTag();

    @Override
    public void addText(Component text) {
        //TODO - 1.20.4: Once Jade updates to run with neo's networking changes test to make sure this works properly using nbt instead of json as string
        ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, text).result().ifPresent(tag -> {
            CompoundTag textData = new CompoundTag();
            textData.put(TEXT, tag);
            data.add(textData);
        });
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