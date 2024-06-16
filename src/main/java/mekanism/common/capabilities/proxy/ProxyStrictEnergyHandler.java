package mekanism.common.capabilities.proxy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.ISidedStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.Unsigned;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyStrictEnergyHandler extends ProxyHandler implements IStrictEnergyHandler {

    private final ISidedStrictEnergyHandler energyHandler;

    public ProxyStrictEnergyHandler(ISidedStrictEnergyHandler energyHandler, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.energyHandler = energyHandler;
    }

    @Override
    public int getEnergyContainerCount() {
        return energyHandler.getEnergyContainerCount(side);
    }

    @Override
    public @Unsigned long getEnergy(int container) {
        return energyHandler.getEnergy(container, side);
    }

    @Override
    public void setEnergy(int container, @Unsigned long energy) {
        if (!readOnly) {
            energyHandler.setEnergy(container, energy, side);
        }
    }

    @Override
    public @Unsigned long getMaxEnergy(int container) {
        return energyHandler.getMaxEnergy(container, side);
    }

    @Override
    public @Unsigned long getNeededEnergy(int container) {
        return energyHandler.getNeededEnergy(container, side);
    }

    @Override
    public @Unsigned long insertEnergy(int container, @Unsigned long amount, Action action) {
        return readOnlyInsert() ? amount : energyHandler.insertEnergy(container, amount, side, action);
    }

    @Override
    public @Unsigned long extractEnergy(int container, @Unsigned long amount, Action action) {
        return readOnlyExtract() ? 0L : energyHandler.extractEnergy(container, amount, side, action);
    }

    @Override
    public @Unsigned long insertEnergy(@Unsigned long amount, Action action) {
        return readOnlyInsert() ? amount : energyHandler.insertEnergy(amount, side, action);
    }

    @Override
    public @Unsigned long extractEnergy(@Unsigned long amount, Action action) {
        return readOnlyExtract() ? 0L : energyHandler.extractEnergy(amount, side, action);
    }
}