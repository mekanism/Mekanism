package mekanism.common.util;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.TileNetworkList;
import mekanism.api.chemical.ChemicalTank;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.infuse.InfusionTank;
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

    public static void addTankData(TileNetworkList data, ChemicalTank tank) {
        if (tank.isEmpty()) {
            data.add(EMPTY_TAG_COMPOUND);
        } else {
            data.add(tank.getStack().write(new CompoundNBT()));
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

    //TODO: Convert read/write to use GasStack.readFromPacket(dataStream) helper methods for both fluids, gases, and infusion types?
    public static void readTankData(PacketBuffer dataStream, GasTank tank) {
        tank.setStack(GasStack.readFromNBT(dataStream.readCompoundTag()));
    }

    public static void readTankData(PacketBuffer dataStream, InfusionTank tank) {
        tank.setStack(InfusionStack.readFromNBT(dataStream.readCompoundTag()));
    }

    public static void readTankData(PacketBuffer dataStream, FluidTank tank) {
        tank.setFluid(readFluidStack(dataStream));
    }

    public static FluidStack readFluidStack(PacketBuffer dataStream) {
        return FluidStack.loadFluidStackFromNBT(dataStream.readCompoundTag());
    }

    //TODO: when removing receiveGas and drawGas also remove the no longer used methods from GasUtils
    //Returns true if it entered the if statement, basically for use by TileEntityGasTank
    public static boolean receiveGas(ItemStack stack, GasTank tank) {
        if (!stack.isEmpty() && (tank.isEmpty() || tank.getStored() < tank.getCapacity())) {
            tank.fill(GasUtils.removeGas(stack, tank.getType(), tank.getNeeded()), Action.EXECUTE);
            return true;
        }
        return false;
    }

    /**
     * @return True if gas was removed
     */
    public static boolean drawGas(ItemStack stack, GasTank tank, Action action) {
        if (!stack.isEmpty() && !tank.isEmpty()) {
            return !tank.drain(GasUtils.addGas(stack, tank.getStack()), action).isEmpty();
        }
        return false;
    }

    public static void emitGas(TileEntityMekanism tile, GasTank tank, int gasOutput, Direction facing) {
        if (!tank.isEmpty()) {
            GasStack toSend = new GasStack(tank.getStack(), Math.min(tank.getStored(), gasOutput));
            tank.drain(GasUtils.emit(toSend, tile, EnumSet.of(facing)), Action.EXECUTE);
        }
    }
}