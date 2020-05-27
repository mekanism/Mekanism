package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import javax.annotation.Nullable;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicSlurryHandler;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.SlurryHandlerManager;
import net.minecraft.util.Direction;

@MethodsReturnNonnullByDefault
public interface ISlurryTile extends ISlurryTracker {

    SlurryHandlerManager getSlurryManager();

    /**
     * @apiNote This should not be overridden, or directly called except for initial creation
     */
    default SlurryHandlerManager getInitialSlurryManager() {
        return new SlurryHandlerManager(getInitialSlurryTanks(), new DynamicSlurryHandler(this::getSlurryTanks, this::extractSlurryCheck, this::insertSlurryCheck, this));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    default IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks() {
        return null;
    }

    /**
     * @apiNote This should not be overridden
     */
    default boolean canHandleSlurry() {
        return getSlurryManager().canHandle();
    }

    /**
     * @apiNote This should not be overridden
     */
    @Override
    default List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
        return getSlurryManager().getContainers(side);
    }

    default boolean extractSlurryCheck(int tank, @Nullable Direction side) {
        return true;
    }

    default boolean insertSlurryCheck(int tank, @Nullable Direction side) {
        return true;
    }
}