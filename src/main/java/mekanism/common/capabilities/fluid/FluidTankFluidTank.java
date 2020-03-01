package mekanism.common.capabilities.fluid;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

//TODO: FluidHandler - Add support for fluid tank stacking
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

    @Override
    public FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
        if (isCreative && isEmpty() && action.execute() && automationType != AutomationType.EXTERNAL) {
            //If a player manually inserts into a creative tank (or internally, via a FluidInventorySlot), that is empty we need to allow setting the type,
            // Note: We check that it is not external insertion because an empty creative tanks acts as a "void" for automation
            FluidStack simulatedRemainder = super.insert(stack, Action.SIMULATE, automationType);
            if (simulatedRemainder.isEmpty()) {
                //If we are able to insert it then set perform the action of setting it to full
                setStackUnchecked(new FluidStack(stack, getCapacity()));
            }
            return simulatedRemainder;
        }
        return super.insert(stack, action.combine(!isCreative), automationType);
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


    //TODO: FluidHandler - make sure to make growStack increase the one above
    //TODO: FluidHandler - FIXME
    @Override
    @Deprecated
    public int fill(FluidStack resource, FluidAction action) {
        //TODO: FluidHandler - this was for the implementation in the fluid tank's fill fluid inventory slot method
        int filled = fill(resource, action);
        //Push the fluid upwards
        if (filled < resource.getAmount() && !tile.getActive()) {
            TileEntityFluidTank aboveTile = MekanismUtils.getTileEntity(TileEntityFluidTank.class, tile.getWorld(), tile.getPos().up());
            //Except if the above tank is creative as then weird things happen
            if (aboveTile != null && aboveTile.tier != FluidTankTier.CREATIVE) {
                filled += tile.pushUp(new FluidStack(resource, resource.getAmount() - filled), action);
            }
        }
        return filled;
    }

    @Deprecated
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        //TODO: FluidHandler, this was from the fluid tanks world fill thing
        if (isCreative) {
            return resource.getAmount();
        }
        int filled = fill(resource, fluidAction);
        if (filled < resource.getAmount() && !tile.getActive()) {
            filled += tile.pushUp(new FluidStack(resource, resource.getAmount() - filled), fluidAction);
        }
        if (filled > 0 && from == Direction.UP) {
            if (tile.valve == 0) {
                tile.needsPacket = true;
            }
            tile.valve = 20;
            tile.valveFluid = new FluidStack(resource, 1);
        }
        return filled;
    }

    @Deprecated
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        TileEntity belowTile = MekanismUtils.getTileEntity(tile.getWorld(), tile.getPos().down());
        if (from == Direction.DOWN && tile.getActive() && !(belowTile instanceof TileEntityFluidTank)) {
            return false;
        }
        if (isCreative) {
            return true;
        }
        if (tile.getActive() && belowTile instanceof TileEntityFluidTank) { // Only fill if tanks underneath have same fluid.
            return isEmpty() ? ((TileEntityFluidTank) belowTile).fluidTank.canFill(Direction.UP, fluid) : getFluid().isFluidEqual(fluid);
        }
        return isEmpty() || getFluid().isFluidEqual(fluid);
    }

    @Deprecated
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        return !tile.getActive() || from != Direction.DOWN;
    }
}