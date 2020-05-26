package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.chemical.PigmentHandlerManager;
import net.minecraft.util.Direction;

public interface IPigmentTile {

    @Nonnull
    PigmentHandlerManager getPigmentManager();

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
    @Nonnull
    default List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
        return getPigmentManager().getContainers(side);
    }
}