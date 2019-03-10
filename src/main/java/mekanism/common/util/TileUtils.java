package mekanism.common.util;

import io.netty.buffer.ByteBuf;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.base.TileNetworkList;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.network.ByteBufUtils;

//TODO: Move this and factor out the parts into proper classes. This is mainly just temp to make organization not as needed
public class TileUtils {
    public static void addTankData(TileNetworkList data, GasTank tank) {
        if (tank.getGas() != null) {
            data.add(true);
            data.add(tank.getGas().getGas().getID());
            data.add(tank.getStored());
        } else {
            data.add(false);
        }
    }

    public static void addTankData(TileNetworkList data, FluidTank tank) {
        if (tank.getFluid() != null) {
            data.add(true);
            data.add(FluidRegistry.getFluidName(tank.getFluid()));
            data.add(tank.getFluidAmount());
        } else {
            data.add(false);
        }
    }

    public static void readTankData(ByteBuf dataStream, GasTank tank) {
        if (dataStream.readBoolean()) {
            tank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
        } else {
            tank.setGas(null);
        }
    }

    public static void readTankData(ByteBuf dataStream, FluidTank tank) {
        if (dataStream.readBoolean()) {
            tank.setFluid(new FluidStack(FluidRegistry.getFluid(ByteBufUtils.readUTF8String(dataStream)), dataStream.readInt()));
        } else {
            tank.setFluid(null);
        }
    }

    //Returns true if it entered the if statement, basically for use by TileEntityGasTank
    public static boolean receiveGas(ItemStack stack, GasTank tank) {
        if (!stack.isEmpty() && (tank.getGas() == null || tank.getStored() < tank.getMaxGas())) {
            tank.receive(GasUtils.removeGas(stack, tank.getGasType(), tank.getNeeded()), true);
            return true;
        }
        return false;
    }

    public static void drawGas(ItemStack stack, GasTank tank) {
        drawGas(stack, tank, true);
    }

    public static void drawGas(ItemStack stack, GasTank tank, boolean doDraw) {
        if (!stack.isEmpty() && tank.getGas() != null) {
            tank.draw(GasUtils.addGas(stack, tank.getGas()), doDraw);
        }
    }

    public static void emitGas(TileEntityBasicBlock tile, GasTank tank, int gasOutput) {
        emitGas(tile, tank, gasOutput, MekanismUtils.getRight(tile.facing));
    }

    public static void emitGas(TileEntityBasicBlock tile, GasTank tank, int gasOutput, EnumFacing facing) {
        if (tank.getGas() != null) {
            GasStack toSend = new GasStack(tank.getGas().getGas(), Math.min(tank.getStored(), gasOutput));
            tank.draw(GasUtils.emit(toSend, tile, ListUtils.asList(facing)), true);
        }
    }
}