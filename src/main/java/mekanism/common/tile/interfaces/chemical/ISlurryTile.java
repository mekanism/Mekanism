package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.slurry.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.chemical.dynamic.DynamicSlurryHandler;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.chemical.SlurryHandlerManager;
import net.minecraft.util.Direction;

@MethodsReturnNonnullByDefault
public interface ISlurryTile extends ISlurryTracker {

    SlurryHandlerManager getSlurryManager();

    /**
     * @apiNote This should not be overridden, or directly called except for initial creation
     */
    default SlurryHandlerManager getInitialSlurryManager() {
        DynamicSlurryHandler slurryHandler = new DynamicSlurryHandler(this::getSlurryManager, this::onContentsChanged);
        return new SlurryHandlerManager(getInitialSlurryTanks(slurryHandler), slurryHandler);
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    default IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(@Nonnull IMekanismSlurryHandler handler) {
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
}