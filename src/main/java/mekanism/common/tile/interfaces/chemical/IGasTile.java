package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.chemical.GasHandlerManager;
import net.minecraft.util.Direction;

public interface IGasTile {

    @Nonnull
    GasHandlerManager getGasManager();

    @Nullable
    default IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        return null;
    }

    /**
     * @apiNote This should not be overridden
     */
    default boolean canHandleGas() {
        return getGasManager().canHandle();
    }

    /**
     * @apiNote This should not be overridden
     */
    @Nonnull
    default List<IGasTank> getGasTanks(@Nullable Direction side) {
        return getGasManager().getContainers(side);
    }
}