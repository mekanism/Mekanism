package mekanism.common.capabilities.fluid;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidTankFluidTank extends BasicFluidTank {

    public static FluidTankFluidTank create(TileEntityFluidTank tile) {
        Objects.requireNonNull(tile, "Fluid tank tile entity cannot be null");
        return new FluidTankFluidTank(tile);
    }

    private TileEntityFluidTank tile;
    private boolean isCreative;

    private FluidTankFluidTank(TileEntityFluidTank tile) {
        super(tile.tier.getStorage(), alwaysTrueBi, alwaysTrueBi, alwaysTrue, tile);
        this.tile = tile;
        isCreative = tile.tier == FluidTankTier.CREATIVE;
    }

    //TODO: FluidHandler - Add proper handling to growStack, that then tries to use insertion into above tanks
    // if there was some amount it was unable to fit in this tank?
    @Override
    public FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
        FluidStack remainder;
        if (isCreative && isEmpty() && action.execute() && automationType != AutomationType.EXTERNAL) {
            //If a player manually inserts into a creative tank (or internally, via a FluidInventorySlot), that is empty we need to allow setting the type,
            // Note: We check that it is not external insertion because an empty creative tanks acts as a "void" for automation
            remainder = super.insert(stack, Action.SIMULATE, automationType);
            if (remainder.isEmpty()) {
                //If we are able to insert it then set perform the action of setting it to full
                setStackUnchecked(new FluidStack(stack, getCapacity()));
            }
        } else {
            remainder = super.insert(stack, action.combine(!isCreative), automationType);
        }
        if (!remainder.isEmpty()) {
            //If we have any left over check if we can send it to the tank that is above
            if (!tile.getActive()) {
                TileEntityFluidTank tileAbove = MekanismUtils.getTileEntity(TileEntityFluidTank.class, this.tile.getWorld(), this.tile.getPos().up());
                if (tileAbove != null) {
                    remainder = tileAbove.fluidTank.insert(remainder, action, AutomationType.INTERNAL);
                }
            }
        }
        return remainder;
    }

    @Override
    public FluidStack extract(int amount, Action action, AutomationType automationType) {
        return super.extract(amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(int, Action)}, as both {@link #growStack(int, Action)} and {@link #shrinkStack(int, Action)} are wrapped through
     * this method.
     */
    @Override
    public int setStackSize(int amount, Action action) {
        return super.setStackSize(amount, action.combine(!isCreative));
    }
}