package mekanism.generators.common.tile.reactor;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.IHeatTransfer;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.MekanismGases;
import mekanism.common.MekanismLang;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.text.BooleanStateDisplay.InputOutput;
import mekanism.generators.common.GeneratorsBlock;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

//TODO: Allow reactor controller inventory slot to be interacted with via the port again
public class TileEntityReactorPort extends TileEntityReactorBlock implements IFluidHandlerWrapper, IGasHandler, IHeatTransfer, IConfigurable {

    public boolean fluidEject;

    public TileEntityReactorPort() {
        super(GeneratorsBlock.REACTOR_PORT);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        fluidEject = nbtTags.getBoolean("fluidEject");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean("fluidEject", fluidEject);
        return nbtTags;
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    @Override
    public void onUpdate() {
        if (changed) {
            World world = getWorld();
            if (world != null) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType());
            }
        }

        super.onUpdate();
        if (!isRemote()) {
            CableUtils.emit(this);
            if (fluidEject && getReactor() != null && !getReactor().getSteamTank().getFluid().isEmpty()) {
                IFluidTank tank = getReactor().getSteamTank();
                EmitUtils.forEachSide(getWorld(), getPos(), EnumSet.allOf(Direction.class), (tile, side) -> {
                    if (!(tile instanceof TileEntityReactorPort)) {
                        CapabilityUtils.getCapabilityHelper(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()).ifPresent(handler -> {
                            if (PipeUtils.canFill(handler, tank.getFluid())) {
                                tank.drain(handler.fill(tank.getFluid(), FluidAction.EXECUTE), FluidAction.EXECUTE);
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return getReactor().getWaterTank().fill(resource, fluidAction);
    }

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        return getReactor().getSteamTank().drain(maxDrain, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return getReactor() != null && !fluidEject && fluid.getFluid() == Fluids.WATER;
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        return getReactor() != null && (fluid.isEmpty() || MekanismFluids.STEAM.fluidMatches(fluid));
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        if (getReactor() == null) {
            return PipeUtils.EMPTY;
        }
        return new IFluidTank[]{getReactor().getWaterTank(), getReactor().getSteamTank()};
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        if (getReactor() != null) {
            //TODO: Move this stuff to a recipe type
            if (stack.getType() == MekanismGases.DEUTERIUM.getGas()) {
                return getReactor().getDeuteriumTank().fill(stack, action);
            } else if (stack.getType() == MekanismGases.TRITIUM.getGas()) {
                return getReactor().getTritiumTank().fill(stack, action);
            } else if (stack.getType() == MekanismGases.FUSION_FUEL.getGas()) {
                return getReactor().getFuelTank().fill(stack, action);
            }
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        return GasStack.EMPTY;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        //TODO: Move this stuff to a recipe type
        return type == MekanismGases.DEUTERIUM.getGas() || type == MekanismGases.TRITIUM.getGas() || type == MekanismGases.FUSION_FUEL.getGas();
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return getReactor() != null ? new GasTankInfo[]{getReactor().getDeuteriumTank(), getReactor().getTritiumTank(), getReactor().getFuelTank()} : IGasHandler.NONE;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public double getEnergy() {
        if (getReactor() == null) {
            return 0;
        }
        return getReactor().getBufferedEnergy();
    }

    @Override
    public void setEnergy(double energy) {
        if (getReactor() != null) {
            getReactor().setBufferedEnergy(energy);
        }
    }

    @Override
    public double getMaxEnergy() {
        if (getReactor() == null) {
            return 0;
        }
        return getReactor().getBufferSize();
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return true;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return false;
    }

    @Override
    public double getMaxOutput() {
        return Integer.MAX_VALUE;
    }

    @Override
    public double getTemp() {
        if (getReactor() != null) {
            return getReactor().getTemp();
        }
        return 0;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return 5;
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        if (getReactor() != null) {
            return getReactor().getInsulationCoefficient(side);
        }
        return 0;
    }

    @Override
    public void transferHeatTo(double heat) {
        if (getReactor() != null) {
            getReactor().transferHeatTo(heat);
        }
    }

    @Override
    public double[] simulateHeat() {
        return HeatUtils.simulate(this);
    }

    @Override
    public double applyTemperatureChange() {
        if (getReactor() != null) {
            return getReactor().applyTemperatureChange();
        }
        return 0;
    }

    @Nullable
    @Override
    public IHeatTransfer getAdjacent(Direction side) {
        TileEntity adj = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        if (adj instanceof TileEntityReactorBlock) {
            return null;
        }
        return CapabilityUtils.getCapabilityHelper(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()).getValue();
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return getReactor() == null;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            boolean prevEject = fluidEject;
            fluidEject = dataStream.readBoolean();
            if (prevEject != fluidEject) {
                World world = getWorld();
                if (world != null) {
                    MekanismUtils.updateBlock(world, getPos());
                }
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(fluidEject);
        return data;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            fluidEject = !fluidEject;
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                  GeneratorsLang.REACTOR_PORT_EJECT.translateColored(EnumColor.GRAY, InputOutput.of(!fluidEject, true))));
            Mekanism.packetHandler.sendUpdatePacket(this);
            markDirty();
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }
}