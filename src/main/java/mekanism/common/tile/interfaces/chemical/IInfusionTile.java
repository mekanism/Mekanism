package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.chemical.InfusionHandlerManager;
import net.minecraft.util.Direction;

public interface IInfusionTile {

    @Nonnull
    InfusionHandlerManager getInfusionManager();

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
    @Nonnull
    default List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
        return getInfusionManager().getContainers(side);
    }
}