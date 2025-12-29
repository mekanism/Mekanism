package mekanism.common.attachments.containers.energy;

import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.ResistiveHeaterEnergyContainer;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedResistiveEnergyContainer extends ComponentBackedEnergyContainer {

    public static ComponentBackedResistiveEnergyContainer create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int containerIndex) {
        return new ComponentBackedResistiveEnergyContainer(attachedTo, containerIndex);
    }

    private ComponentBackedResistiveEnergyContainer(ItemStack attachedTo, int containerIndex) {
        super(attachedTo, containerIndex, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), ConstantPredicates.ZERO_LONG, ConstantPredicates.ZERO_LONG);
    }

    @Override
    public long getMaxEnergy() {
        return MathUtils.multiplyClamped(getEnergyPerTick(), ResistiveHeaterEnergyContainer.USAGE_MULTIPLIER);
    }

    private long getRate() {
        return MekanismUtils.calculateUsage(getMaxEnergy());
    }

    @Override
    protected long getInsertRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? Long.MAX_VALUE : getRate();
    }

    @Override
    protected long getExtractRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? Long.MAX_VALUE : getRate();
    }

    private long getEnergyPerTick() {
        return attachedTo.getOrDefault(MekanismDataComponents.ENERGY_USAGE, TileEntityResistiveHeater.BASE_USAGE);
    }

    private void updateEnergyUsage(long energyUsage) {
        attachedTo.set(MekanismDataComponents.ENERGY_USAGE, energyUsage);
        //Clamp the energy
        setEnergy(getEnergy());
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putLong(SerializationConstants.ENERGY_USAGE, getEnergyPerTick());
    }

    @Override
    public void deserialize(ValueInput input) {
        input.getLong(SerializationConstants.ENERGY_USAGE).ifPresent(this::updateEnergyUsage);
        super.deserialize(input);
    }
}