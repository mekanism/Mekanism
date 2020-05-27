package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import javax.annotation.Nullable;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicInfusionHandler;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.InfusionHandlerManager;
import net.minecraft.util.Direction;

@MethodsReturnNonnullByDefault
public interface IInfusionTile extends IInfusionTracker {

    InfusionHandlerManager getInfusionManager();

    /**
     * @apiNote This should not be overridden, or directly called except for initial creation
     */
    default InfusionHandlerManager getInitialInfusionManager() {
        return new InfusionHandlerManager(getInitialInfusionTanks(), new DynamicInfusionHandler(this::getInfusionTanks, this::extractInfusionCheck, this::insertInfusionCheck, this));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    default IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks() {
        return null;
    }

    /**
     * @apiNote This should not be overridden
     */
    default boolean canHandleInfusion() {
        return getInfusionManager().canHandle();
    }

    /**
     * @apiNote This should not be overridden
     */
    @Override
    default List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
        return getInfusionManager().getContainers(side);
    }

    default boolean extractInfusionCheck(int tank, @Nullable Direction side) {
        return true;
    }

    default boolean insertInfusionCheck(int tank, @Nullable Direction side) {
        return true;
    }
}