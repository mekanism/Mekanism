package mekanism.common.attachments.containers.energy;

import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.block.attribute.AttributeEnergy;
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
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedResistiveEnergyContainer extends ComponentBackedEnergyContainer {

    public static ComponentBackedResistiveEnergyContainer create(ItemAccess attachedAccess) {
        return new ComponentBackedResistiveEnergyContainer(attachedAccess);
    }

    private ComponentBackedResistiveEnergyContainer(ItemAccess attachedAccess) {
        super(attachedAccess, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), MekanismUtils::calculateUsage);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong() {
        return AttributeEnergy.STORAGE_MULTIPLIER * getEnergyPerTick();
    }

    public int getEnergyPerTick() {
        return attachedAccess.getResource().getOrDefault(MekanismDataComponents.ENERGY_USAGE, TileEntityResistiveHeater.BASE_USAGE);
    }

    private void updateEnergyUsage(int energyUsage, @Nullable TransactionContext transaction) {
        ItemResource resource = attachedAccess.getResource();
        //Ensure the backing item has not somehow become empty
        if (!resource.isEmpty()) {
            //Note: The attached access should handle snapshotting the backing stack
            ItemAccessUtils.exchange(attachedAccess, resource.with(MekanismDataComponents.ENERGY_USAGE, energyUsage), transaction);
            //Note: We don't have to clamp the energy as all of our call sites call a method which sets the energy afterward anyway
        }
    }

    @Override
    public void copyContents(IEnergyContainer other, @Nullable TransactionContext transaction) {
        if (other instanceof ResistiveHeaterEnergyContainer otherContainer) {
            updateEnergyUsage(otherContainer.getEnergyPerTick(), transaction);
        } else if (other instanceof ComponentBackedResistiveEnergyContainer otherContainer) {
            updateEnergyUsage(otherContainer.getEnergyPerTick(), transaction);
        }
        super.copyContents(other, transaction);
    }

    @Override
    public void serialize(ValueOutput output) {
        super.serialize(output);
        output.putInt(SerializationConstants.ENERGY_USAGE, getEnergyPerTick());
    }

    @Override
    public void deserialize(ValueInput input) {
        input.getInt(SerializationConstants.ENERGY_USAGE).ifPresent(energy -> updateEnergyUsage(energy, null));
        super.deserialize(input);
    }
}