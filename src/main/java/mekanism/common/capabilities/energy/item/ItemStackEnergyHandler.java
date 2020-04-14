package mekanism.common.capabilities.energy.item;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Helper class for implementing fluid handlers for items
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackEnergyHandler extends ItemCapability implements IMekanismStrictEnergyHandler {

    protected List<IEnergyContainer> energyContainers;

    protected abstract List<IEnergyContainer> getInitialContainers();

    @Override
    protected void init() {
        this.energyContainers = getInitialContainers();
    }

    @Override
    protected void load() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            DataHandlerUtils.readContainers(getEnergyContainers(null), ItemDataUtils.getList(stack, NBTConstants.ENERGY_CONTAINERS));
        }
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            ItemDataUtils.setList(stack, NBTConstants.ENERGY_CONTAINERS, DataHandlerUtils.writeContainers(getEnergyContainers(null)));
        }
    }

    @Override
    public boolean canProcess(Capability<?> capability) {
        return EnergyCompatUtils.isEnergyCapability(capability);
    }

    @Override
    public <T> LazyOptional<T> getMatchingCapability(@Nonnull Capability<T> capability) {
        return EnergyCompatUtils.getEnergyCapability(capability, this);
    }
}