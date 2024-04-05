package mekanism.common.tile.interfaces.chemical;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.SimpleDynamicChemicalHandler.SimpleDynamicPigmentHandler;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.PigmentHandlerManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface IPigmentTile extends IPigmentTracker {

    @Nullable
    PigmentHandlerManager getPigmentManager();

    /**
     * @apiNote This should not be overridden, or directly called except for initial creation
     */
    @Nullable
    default PigmentHandlerManager getInitialPigmentManager(IContentsListener listener) {
        IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> initialPigmentTanks = getInitialPigmentTanks(listener);
        if (initialPigmentTanks == null) {
            return null;
        }
        return new PigmentHandlerManager(initialPigmentTanks, new SimpleDynamicPigmentHandler(this::getPigmentTanks, listener));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    default IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener) {
        return null;
    }

    /**
     * @apiNote This should not be overridden
     */
    default boolean canHandlePigment() {
        PigmentHandlerManager pigmentManager = getPigmentManager();
        return pigmentManager != null && pigmentManager.canHandle();
    }

    /**
     * @apiNote This should not be overridden
     */
    @Override
    default List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
        PigmentHandlerManager pigmentManager = getPigmentManager();
        return pigmentManager != null ? pigmentManager.getContainers(side) : Collections.emptyList();
    }
}