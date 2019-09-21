package mekanism.common.util;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

//TODO: Move this and factor out the parts into proper classes. This is mainly just temp to make organization not as needed
public class TileUtils {

    // N.B. All the tank I/O functions rely on the fact that an empty NBT Compound is a singular
    // byte and that the Gas/Fluid Stacks initialize to null if they are de-serialized from an
    // empty tag.
    private static final CompoundNBT EMPTY_TAG_COMPOUND = new CompoundNBT();

    public static void addTankData(TileNetworkList data, GasTank tank) {
        if (tank.isEmpty()) {
            data.add(EMPTY_TAG_COMPOUND);
        } else {
            data.add(tank.getGas().write(new CompoundNBT()));
        }
    }

    public static void addTankData(TileNetworkList data, FluidTank tank) {
        addFluidStack(data, tank.getFluid());
    }

    public static void addFluidStack(TileNetworkList data, @Nonnull FluidStack stack) {
        if (!stack.isEmpty()) {
            data.add(stack.writeToNBT(new CompoundNBT()));
        } else {
            data.add(EMPTY_TAG_COMPOUND);
        }
    }

    public static void readTankData(PacketBuffer dataStream, GasTank tank) {
        tank.setGas(GasStack.readFromNBT(dataStream.readCompoundTag()));
    }

    public static void readTankData(PacketBuffer dataStream, FluidTank tank) {
        tank.setFluid(readFluidStack(dataStream));
    }

    public static FluidStack readFluidStack(PacketBuffer dataStream) {
        return FluidStack.loadFluidStackFromNBT(dataStream.readCompoundTag());
    }


    //Returns true if it entered the if statement, basically for use by TileEntityGasTank
    public static boolean receiveGas(ItemStack stack, GasTank tank) {
        if (!stack.isEmpty() && (tank.isEmpty() || tank.getStored() < tank.getMaxGas())) {
            tank.receive(GasUtils.removeGas(stack, tank.getGasType(), tank.getNeeded()), true);
            return true;
        }
        return false;
    }

    /**
     * @return True if gas was removed
     */
    public static boolean drawGas(ItemStack stack, GasTank tank) {
        return drawGas(stack, tank, true);
    }

    public static boolean  drawGas(ItemStack stack, GasTank tank, boolean doDraw) {
        if (!stack.isEmpty() && !tank.isEmpty()) {
            return !tank.draw(GasUtils.addGas(stack, tank.getGas()), doDraw).isEmpty();
        }
        return false;
    }

    public static void emitGas(TileEntityMekanism tile, GasTank tank, int gasOutput, Direction facing) {
        if (!tank.isEmpty()) {
            GasStack toSend = new GasStack(tank.getGas(), Math.min(tank.getStored(), gasOutput));
            tank.draw(GasUtils.emit(toSend, tile, EnumSet.of(facing)), true);
        }
    }
}