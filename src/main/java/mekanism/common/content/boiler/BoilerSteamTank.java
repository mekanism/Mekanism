package mekanism.common.content.boiler;

import javax.annotation.Nonnull;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraftforge.fluids.FluidStack;

public class BoilerSteamTank extends BoilerTank {

    public BoilerSteamTank(TileEntityBoilerCasing tile) {
        super(tile);
    }

    @Override
    public int getCapacity() {
        return multiblock.structure == null ? 0 : multiblock.structure.steamVolume * BoilerUpdateProtocol.STEAM_PER_TANK;
    }

    @Override
    public boolean isFluidValid(@Nonnull FluidStack stack) {
        return super.isFluidValid(stack) && stack.getFluid().isIn(MekanismTags.Fluids.STEAM);
    }
}