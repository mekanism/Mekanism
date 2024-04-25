package mekanism.common.integration.lookingat.jade;

import java.util.Optional;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.LookingAtHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.neoforged.neoforge.fluids.FluidStack;

public class JadeLookingAtHelper implements LookingAtHelper {

    static final String CHEMICAL_STACK = "chemical";
    static final String TEXT = "text";

    private final ListTag data = new ListTag();

    @Override
    public void addText(Component text) {
        Optional<Tag> result = ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, text).result();
        if (result.isPresent()) {
            CompoundTag textData = new CompoundTag();
            textData.put(TEXT, result.get());
            data.add(textData);
        }
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
        //TODO - 1.20.5: Providers
        //fluidData.put(NBTConstants.FLUID_STORED, stored.saveOptional(provider));
        fluidData.putInt(NBTConstants.MAX, capacity);
        data.add(fluidData);
    }

    @Override
    public void addChemicalElement(ChemicalStack<?> stored, long capacity) {
        CompoundTag chemicalData = new CompoundTag();
        //TODO - 1.20.5: Providers
        //chemicalData.put(CHEMICAL_STACK, stored.saveOptional(provider));
        chemicalData.putLong(NBTConstants.MAX, capacity);
        data.add(chemicalData);
    }

    public void finalizeData(CompoundTag data) {
        if (!this.data.isEmpty()) {
            data.put(NBTConstants.MEK_DATA, this.data);
        }
    }
}