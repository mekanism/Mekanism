package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import javax.annotation.Nullable;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicPigmentHandler;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.PigmentHandlerManager;
import net.minecraft.util.Direction;

@MethodsReturnNonnullByDefault
public interface IPigmentTile extends IPigmentTracker {

    PigmentHandlerManager getPigmentManager();

    /**
     * @apiNote This should not be overridden, or directly called except for initial creation
     */
    default PigmentHandlerManager getInitialPigmentManager() {
        return new PigmentHandlerManager(getInitialPigmentTanks(), new DynamicPigmentHandler(this::getPigmentTanks, this::extractPigmentCheck, this::insertPigmentCheck, this));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    default IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks() {
        return null;
    }

    /**
     * @apiNote This should not be overridden
     */
    default boolean canHandlePigment() {
        return getPigmentManager().canHandle();
    }

    /**
     * @apiNote This should not be overridden
     */
    @Override
    default List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
        return getPigmentManager().getContainers(side);
    }

    default boolean extractPigmentCheck(int tank, @Nullable Direction side) {
        return true;
    }

    default boolean insertPigmentCheck(int tank, @Nullable Direction side) {
        return true;
    }
}