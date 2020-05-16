package mekanism.common.capabilities.proxy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.energy.ISidedStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
    public FloatingLong getEnergy(int container) {
        return energyHandler.getEnergy(container, side);
    }

    @Override
    public void setEnergy(int container, FloatingLong energy) {
        if (!readOnly) {
            energyHandler.setEnergy(container, energy, side);
        }
    }

    @Override
    public FloatingLong getMaxEnergy(int container) {
        return energyHandler.getMaxEnergy(container, side);
    }

    @Override
    public FloatingLong getNeededEnergy(int container) {
        return energyHandler.getNeededEnergy(container, side);
    }

    @Override
    public FloatingLong insertEnergy(int container, FloatingLong amount, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? amount : energyHandler.insertEnergy(container, amount, side, action);
    }

    @Override
    public FloatingLong extractEnergy(int container, FloatingLong amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? FloatingLong.ZERO : energyHandler.extractEnergy(container, amount, side, action);
    }

    @Override
    public FloatingLong insertEnergy(FloatingLong amount, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? amount : energyHandler.insertEnergy(amount, side, action);
    }

    @Override
    public FloatingLong extractEnergy(FloatingLong amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? FloatingLong.ZERO : energyHandler.extractEnergy(amount, side, action);
    }
}