package mekanism.common.capabilities.energy.item;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.resolver.EnergyCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

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
        super.init();
        this.energyContainers = getInitialContainers();
    }

    @Override
    protected void load() {
        super.load();
        ItemDataUtils.readContainers(getStack(), NBTConstants.ENERGY_CONTAINERS, getEnergyContainers(null));
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
        ItemDataUtils.writeContainers(getStack(), NBTConstants.ENERGY_CONTAINERS, getEnergyContainers(null));
    }

    private int itemsCount() {
        return this.getStack().getCount();
    }

    @Override
    public FloatingLong getEnergy(int container, @Nullable Direction side) {
        return IMekanismStrictEnergyHandler.super.getEnergy(container, side).multiply(itemsCount());
    }

    @Override
    public void setEnergy(int container, FloatingLong energy, @Nullable Direction side) {
        if (itemsCount() > 0) {
            IMekanismStrictEnergyHandler.super.setEnergy(container, energy.divide(itemsCount()), side);
        }
    }

    @Override
    public FloatingLong getMaxEnergy(int container, @Nullable Direction side) {
        return IMekanismStrictEnergyHandler.super.getMaxEnergy(container, side).multiply(itemsCount());
    }

    @Override
    public FloatingLong insertEnergy(int container, FloatingLong amount, @Nullable Direction side, Action action) {
        int count = itemsCount();
        if (count <= 0) {
            return amount;
        }
        return IMekanismStrictEnergyHandler.super.insertEnergy(container, amount.divide(count), side, action).multiply(count);
    }

    @Override
    public FloatingLong extractEnergy(int container, FloatingLong amount, @Nullable Direction side, Action action) {
        int count = itemsCount();
        if (count <= 0) {
            return FloatingLong.ZERO;
        }
        return IMekanismStrictEnergyHandler.super.extractEnergy(container, amount.divide(count), side, action).multiply(count);
    }

    @Override
    protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
        consumer.accept(new EnergyCapabilityResolver(this));
    }
}