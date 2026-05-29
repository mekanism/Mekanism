package mekanism.common.attachments.containers.energy;

import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.ResistiveHeaterEnergyContainer;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class ComponentBackedResistiveEnergyContainer extends ComponentBackedEnergyContainer {

    public static ComponentBackedResistiveEnergyContainer create(ItemAccess attachedAccess, int containerIndex) {
        return new ComponentBackedResistiveEnergyContainer(attachedAccess, containerIndex);
    }

    private ComponentBackedResistiveEnergyContainer(ItemAccess attachedAccess, int containerIndex) {
        super(attachedAccess, containerIndex, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), ConstantPredicates.ZERO, ConstantPredicates.ZERO_LONG);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacity() {
        return MathUtils.multiplyClamped(getEnergyPerTick(), ResistiveHeaterEnergyContainer.USAGE_MULTIPLIER);
    }

    private int getRate() {
        return MekanismUtils.calculateUsage(this.capacity());
    }

    @Override
    protected int getInsertionRate(AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType.isManual() ? Integer.MAX_VALUE : getRate();
    }

    @Override
    protected int getExtractionRate(AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType.isManual() ? Integer.MAX_VALUE : getRate();
    }

    public int getEnergyPerTick() {
        return attachedAccess.getResource().getOrDefault(MekanismDataComponents.ENERGY_USAGE, TileEntityResistiveHeater.BASE_USAGE);
    }

    private void updateEnergyUsage(int energyUsage) {
        ItemResource resource = attachedAccess.getResource();
        //Ensure the backing item has not somehow become empty
        if (!resource.isEmpty()) {
            //Note: The attached access should handle snapshotting the backing stack
            ItemAccessUtils.exchange(attachedAccess, resource.with(MekanismDataComponents.ENERGY_USAGE, energyUsage), null);
            //Note: We don't have to clamp the energy as all of our call sites call a method which sets the energy afterward anyway
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
        output.putInt(SerializationConstants.ENERGY_USAGE, getEnergyPerTick());
    }

    @Override
    public void deserialize(ValueInput input) {
        input.getInt(SerializationConstants.ENERGY_USAGE).ifPresent(this::updateEnergyUsage);
        super.deserialize(input);
    }
}