package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.chemical.SlurryHandlerManager;
import net.minecraft.util.Direction;

public interface ISlurryTile {

    @Nonnull
    SlurryHandlerManager getSlurryManager();

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
    @Nonnull
    default List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
        return getSlurryManager().getContainers(side);
    }
}