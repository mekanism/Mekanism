package mekanism.common.tile.interfaces.chemical;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.capabilities.chemical.dynamic.SimpleDynamicChemicalHandler.SimpleDynamicSlurryHandler;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.SlurryHandlerManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface ISlurryTile extends ISlurryTracker {

    @Nullable
    SlurryHandlerManager getSlurryManager();

    /**
     * @apiNote This should not be overridden, or directly called except for initial creation
     */
    @Nullable
    default SlurryHandlerManager getInitialSlurryManager(IContentsListener listener) {
        IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> initialSlurryTanks = getInitialSlurryTanks(listener);
        if (initialSlurryTanks == null) {
            return null;
        }
        return new SlurryHandlerManager(initialSlurryTanks, new SimpleDynamicSlurryHandler(this::getSlurryTanks, listener));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    default IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener) {
        return null;
    }

    /**
     * @apiNote This should not be overridden
     */
    default boolean canHandleSlurry() {
        SlurryHandlerManager slurryManager = getSlurryManager();
        return slurryManager != null && slurryManager.canHandle();
    }

    /**
     * @apiNote This should not be overridden
     */
    @Override
    default List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
        SlurryHandlerManager slurryManager = getSlurryManager();
        return slurryManager != null ? slurryManager.getContainers(side) : Collections.emptyList();
    }
}