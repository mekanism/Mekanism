package mekanism.common.attachments.containers.energy;

import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.ResistiveHeaterEnergyContainer;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class ComponentBackedResistiveEnergyContainer extends ComponentBackedEnergyContainer {

    public static ComponentBackedResistiveEnergyContainer create(ContainerType<?, ?, ?> ignored, ItemAccess attachedAccess, int containerIndex) {
        return new ComponentBackedResistiveEnergyContainer(attachedAccess, containerIndex);
    }

    private ComponentBackedResistiveEnergyContainer(ItemAccess attachedAccess, int containerIndex) {
        super(attachedAccess, containerIndex, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), ConstantPredicates.ZERO_LONG, ConstantPredicates.ZERO_LONG);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacity() {
        return MathUtils.multiplyClamped(getEnergyPerTick(), ResistiveHeaterEnergyContainer.USAGE_MULTIPLIER);
    }

    private long getRate() {
        return MekanismUtils.calculateUsage(this.capacity());
    }

    @Override
    protected long getInsertionRate(AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType.isManual() ? Long.MAX_VALUE : getRate();
    }

    @Override
    protected long getExtractionRate(AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType.isManual() ? Long.MAX_VALUE : getRate();
    }

    public long getEnergyPerTick() {
        return attachedAccess.getResource().getOrDefault(MekanismDataComponents.ENERGY_USAGE, TileEntityResistiveHeater.BASE_USAGE);
    }

    private void updateEnergyUsage(long energyUsage) {
        try (Transaction transaction = Transaction.openRoot()) {
            //Note: The attached access should handle snapshotting the backing stack
            attachedAccess.exchange(attachedAccess.getResource().with(MekanismDataComponents.ENERGY_USAGE, energyUsage), attachedAccess.getAmount(), transaction);
            //Note: We don't have to clamp the energy as all of our call sites call a method which sets the energy afterward anyway
            transaction.commit();
        }
    }

    @Override
    public void copyContents(IEnergyContainer other) {
        if (other instanceof ResistiveHeaterEnergyContainer otherContainer) {
            updateEnergyUsage(otherContainer.getEnergyPerTick());
        } else if (other instanceof ComponentBackedResistiveEnergyContainer otherContainer) {
            updateEnergyUsage(otherContainer.getEnergyPerTick());
        }
        super.copyContents(other);
    }

    @Override
    public void serialize(ValueOutput output) {
        super.serialize(output);
        output.putLong(SerializationConstants.ENERGY_USAGE, getEnergyPerTick());
    }

    @Override
    public void deserialize(ValueInput input) {
        input.getLong(SerializationConstants.ENERGY_USAGE).ifPresent(this::updateEnergyUsage);
        super.deserialize(input);
    }
}