package mekanism.common.content.boiler;

import javax.annotation.Nonnull;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class BoilerSteamTank extends BoilerTank {

    public BoilerSteamTank(TileEntityBoilerCasing tileEntity) {
        super(tileEntity);
    }

    @Override
    @Nonnull
    public FluidStack getFluid() {
        return multiblock.structure != null ? multiblock.structure.steamStored : FluidStack.EMPTY;
    }

    @Override
    public void setFluid(@Nonnull FluidStack stack) {
        if (multiblock.structure != null) {
            multiblock.structure.steamStored = stack;
        }
    }

    @Override
    public int getCapacity() {
        return multiblock.structure != null ? multiblock.structure.steamVolume * BoilerUpdateProtocol.STEAM_PER_TANK : 0;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return stack.getFluid().getTags().contains(new ResourceLocation("forge", "steam"));
    }
}