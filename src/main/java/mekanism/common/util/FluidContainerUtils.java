package mekanism.common.util;

import javax.annotation.Nonnull;
import net.minecraftforge.fluids.FluidStack;

//TODO: FluidHandler - Remove
@Deprecated
public final class FluidContainerUtils {

    public static boolean canDrain(@Nonnull FluidStack tankFluid, @Nonnull FluidStack drainFluid) {
        return !tankFluid.isEmpty() && (drainFluid.isEmpty() || tankFluid.isFluidEqual(drainFluid));
    }
}