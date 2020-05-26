package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.pigment.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.chemical.dynamic.DynamicPigmentHandler;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.chemical.PigmentHandlerManager;
import net.minecraft.util.Direction;

@MethodsReturnNonnullByDefault
public interface IPigmentTile {

    @Nonnull
    PigmentHandlerManager getPigmentManager();

    /**
     * @apiNote This should not be overridden, or directly called except for initial creation
     */
    default PigmentHandlerManager getInitialPigmentManager(@Nonnull Runnable onContentsChanged) {
        DynamicPigmentHandler pigmentHandler = new DynamicPigmentHandler(this::getPigmentManager, onContentsChanged);
        return new PigmentHandlerManager(getInitialPigmentTanks(pigmentHandler), pigmentHandler);
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    default IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(@Nonnull IMekanismPigmentHandler handler) {
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