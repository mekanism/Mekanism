package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import javax.annotation.Nullable;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicGasHandler;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.GasHandlerManager;
import net.minecraft.util.Direction;

@MethodsReturnNonnullByDefault
public interface IGasTile extends IGasTracker {

    GasHandlerManager getGasManager();

    /**
     * @apiNote This should not be overridden, or directly called except for initial creation
     */
    default GasHandlerManager getInitialGasManager() {
        return new GasHandlerManager(getInitialGasTanks(), new DynamicGasHandler(this::getGasTanks, this::extractGasCheck, this::insertGasCheck, this));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
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
    @Override
    default List<IGasTank> getGasTanks(@Nullable Direction side) {
        return getGasManager().getContainers(side);
    }

    default boolean extractGasCheck(int tank, @Nullable Direction side) {
        return true;
    }

    default boolean insertGasCheck(int tank, @Nullable Direction side) {
        return true;
    }
}