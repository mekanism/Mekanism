package mekanism.common.attachments.containers.energy;

import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.ResistiveHeaterEnergyContainer;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedResistiveEnergyContainer extends ComponentBackedEnergyContainer {

    private static final FloatingLongSupplier SUPPLIES_ZERO = () -> FloatingLong.ZERO;

    public static ComponentBackedResistiveEnergyContainer create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int containerIndex) {
        return new ComponentBackedResistiveEnergyContainer(attachedTo, containerIndex);
    }

    private ComponentBackedResistiveEnergyContainer(ItemStack attachedTo, int containerIndex) {
        super(attachedTo, containerIndex, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue, SUPPLIES_ZERO, SUPPLIES_ZERO);
    }

    @Override
    public FloatingLong getMaxEnergy() {
        return getEnergyPerTick().multiply(ResistiveHeaterEnergyContainer.USAGE_MULTIPLIER);
    }

    private FloatingLong getRate() {
        return getMaxEnergy().multiply(0.005);
    }

    @Override
    protected FloatingLong getInsertRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? FloatingLong.MAX_VALUE : getRate();
    }

    @Override
    protected FloatingLong getExtractRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? FloatingLong.MAX_VALUE : getRate();
    }

    private FloatingLong getEnergyPerTick() {
        return attachedTo.getOrDefault(MekanismDataComponents.ENERGY_USAGE, TileEntityResistiveHeater.BASE_USAGE);
    }

    private void updateEnergyUsage(FloatingLong energyUsage) {
        attachedTo.set(MekanismDataComponents.ENERGY_USAGE, energyUsage);
        //Clamp the energy
        setEnergy(getEnergy());
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = super.serializeNBT(provider);
        nbt.putString(SerializationConstants.ENERGY_USAGE, getEnergyPerTick().toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        NBTUtils.setFloatingLongIfPresent(nbt, SerializationConstants.ENERGY_USAGE, this::updateEnergyUsage);
        super.deserializeNBT(provider, nbt);
    }
}