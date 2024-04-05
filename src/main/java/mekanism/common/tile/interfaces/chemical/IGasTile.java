package mekanism.common.tile.interfaces.chemical;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.SimpleDynamicChemicalHandler.SimpleDynamicGasHandler;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.GasHandlerManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface IGasTile extends IGasTracker {

    @Nullable
    GasHandlerManager getGasManager();

    /**
     * @apiNote This should not be overridden, or directly called except for initial creation
     */
    @Nullable
    default GasHandlerManager getInitialGasManager(IContentsListener listener) {
        IChemicalTankHolder<Gas, GasStack, IGasTank> initialGasTanks = getInitialGasTanks(listener);
        if (initialGasTanks == null) {
            return null;
        }
        return new GasHandlerManager(initialGasTanks, new SimpleDynamicGasHandler(this::getGasTanks, listener));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    default IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        return null;
    }

    /**
     * @apiNote This should not be overridden
     */
    default boolean canHandleGas() {
        GasHandlerManager gasManager = getGasManager();
        return gasManager != null && gasManager.canHandle();
    }

    /**
     * @apiNote This should not be overridden
     */
    @Override
    default List<IGasTank> getGasTanks(@Nullable Direction side) {
        GasHandlerManager gasManager = getGasManager();
        return gasManager != null ? gasManager.getContainers(side) : Collections.emptyList();
    }
}