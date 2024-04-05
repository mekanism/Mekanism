package mekanism.common.tile.interfaces.chemical;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.SimpleDynamicChemicalHandler.SimpleDynamicInfusionHandler;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.InfusionHandlerManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface IInfusionTile extends IInfusionTracker {

    @Nullable
    InfusionHandlerManager getInfusionManager();

    /**
     * @apiNote This should not be overridden, or directly called except for initial creation
     */
    @Nullable
    default InfusionHandlerManager getInitialInfusionManager(IContentsListener listener) {
        IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> initialInfusionTanks = getInitialInfusionTanks(listener);
        if (initialInfusionTanks == null) {
            return null;
        }
        return new InfusionHandlerManager(initialInfusionTanks, new SimpleDynamicInfusionHandler(this::getInfusionTanks, listener));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    default IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(IContentsListener listener) {
        return null;
    }

    /**
     * @apiNote This should not be overridden
     */
    default boolean canHandleInfusion() {
        InfusionHandlerManager infusionManager = getInfusionManager();
        return infusionManager != null && infusionManager.canHandle();
    }

    /**
     * @apiNote This should not be overridden
     */
    @Override
    default List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
        InfusionHandlerManager infusionManager = getInfusionManager();
        return infusionManager != null ? infusionManager.getContainers(side) : Collections.emptyList();
    }
}