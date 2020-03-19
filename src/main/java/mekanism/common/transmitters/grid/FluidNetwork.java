package mekanism.common.transmitters.grid;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.MekanismLang;
import mekanism.common.base.target.FluidHandlerTarget;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidNetwork extends DynamicNetwork<IFluidHandler, FluidNetwork, FluidStack> implements IMekanismFluidHandler {

    private final List<IExtendedFluidTank> fluidTanks;
    public final VariableCapacityFluidTank fluidTank;

    private int transferDelay;
    public boolean didTransfer;
    private boolean prevTransfer;
    public float fluidScale;
    private int prevStored;
    private int prevTransferAmount;

    public FluidNetwork() {
        fluidTank = VariableCapacityFluidTank.create(this::getCapacity, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue, this);
        fluidTanks = Collections.singletonList(fluidTank);
    }

    public FluidNetwork(Collection<FluidNetwork> networks) {
        this();
        for (FluidNetwork net : networks) {
            if (net != null) {
                adoptTransmittersAndAcceptorsFrom(net);
                net.deregister();
            }
        }
        fluidScale = getScale();
        register();
    }

    @Override
    public void adoptTransmittersAndAcceptorsFrom(FluidNetwork net) {
        if (isRemote()) {
            if (!net.fluidTank.isEmpty() && net.fluidScale > fluidScale) {
                fluidScale = net.fluidScale;
                fluidTank.setStack(net.getBuffer());
                net.fluidScale = 0;
                net.fluidTank.setEmpty();
            }
        } else if (!net.fluidTank.isEmpty()) {
            if (fluidTank.isEmpty()) {
                fluidTank.setStack(net.getBuffer());
            } else if (fluidTank.isFluidEqual(net.fluidTank.getFluid())) {
                int amount = net.fluidTank.getFluidAmount();
                if (fluidTank.growStack(amount, Action.EXECUTE) != amount) {
                    //TODO: Print warning/error
                }
            } else if (net.fluidTank.getFluidAmount() > fluidTank.getFluidAmount()) {
                fluidTank.setStack(net.getBuffer());
            }
            net.fluidTank.setEmpty();
        }
        super.adoptTransmittersAndAcceptorsFrom(net);
    }

    @Nonnull
    @Override
    public FluidStack getBuffer() {
        return fluidTank.getFluid().copy();
    }

    @Override
    public void absorbBuffer(IGridTransmitter<IFluidHandler, FluidNetwork, FluidStack> transmitter) {
        FluidStack fluid = transmitter.getBuffer();
        if (fluid.isEmpty()) {
            return;
        }
        if (fluidTank.isEmpty()) {
            fluidTank.setStack(fluid.copy());
            return;
        }
        //TODO better multiple buffer impl
        if (fluidTank.isFluidEqual(fluid)) {
            int amount = fluid.getAmount();
            if (fluidTank.growStack(amount, Action.EXECUTE) != amount) {
                //TODO: Print warning/error
            }
        }
    }

    @Override
    public void clampBuffer() {
        if (!fluidTank.isEmpty()) {
            int capacity = getCapacity();
            if (fluidTank.getFluidAmount() > capacity) {
                if (fluidTank.setStackSize(capacity, Action.EXECUTE) != capacity) {
                    //TODO: Print warning/error
                }
            }
        }
    }

    private int tickEmit(@Nonnull FluidStack fluidToSend) {
        Set<FluidHandlerTarget> availableAcceptors = new ObjectOpenHashSet<>();
        int totalHandlers = 0;
        Long2ObjectMap<IChunk> chunkMap = new Long2ObjectOpenHashMap<>();
        for (Coord4D coord : possibleAcceptors) {
            EnumSet<Direction> sides = acceptorDirections.get(coord);
            if (sides == null || sides.isEmpty()) {
                continue;
            }
            TileEntity tile = MekanismUtils.getTileEntity(getWorld(), chunkMap, coord);
            if (tile == null) {
                continue;
            }
            FluidHandlerTarget target = new FluidHandlerTarget(fluidToSend);
            for (Direction side : sides) {
                CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side).ifPresent(acceptor -> {
                    if (PipeUtils.canFill(acceptor, fluidToSend)) {
                        target.addHandler(side, acceptor);
                    }
                });
            }
            int curHandlers = target.getHandlers().size();
            if (curHandlers > 0) {
                availableAcceptors.add(target);
                totalHandlers += curHandlers;
            }
        }
        return EmitUtils.sendToAcceptors(availableAcceptors, totalHandlers, fluidToSend.getAmount(), fluidToSend);
    }

    public FluidStack emit(@Nonnull FluidStack stack, Action action) {
        if (stack.isEmpty() || (!fluidTank.isEmpty() && !fluidTank.isFluidEqual(stack))) {
            return stack;
        }
        int toAdd = Math.min(fluidTank.getNeeded(), stack.getAmount());
        if (action.execute()) {
            if (fluidTank.isEmpty()) {
                fluidTank.setStack(new FluidStack(stack, toAdd));
            } else {
                //Otherwise try to grow the stack
                if (fluidTank.growStack(toAdd, Action.EXECUTE) != toAdd) {
                    //TODO: Print warning/error
                }
            }
        }
        return new FluidStack(stack, stack.getAmount() - toAdd);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            prevTransferAmount = 0;
            if (transferDelay == 0) {
                didTransfer = false;
            } else {
                transferDelay--;
            }
            int stored = fluidTank.getFluidAmount();
            if (stored != prevStored) {
                needsUpdate = true;
            }
            prevStored = stored;
            if (didTransfer != prevTransfer || needsUpdate) {
                MinecraftForge.EVENT_BUS.post(new FluidTransferEvent(this, getBuffer(), didTransfer));
                needsUpdate = false;
            }
            prevTransfer = didTransfer;
            if (!fluidTank.isEmpty()) {
                prevTransferAmount = tickEmit(fluidTank.getFluid());
                if (prevTransferAmount > 0) {
                    didTransfer = true;
                    transferDelay = 2;
                }
                if (fluidTank.shrinkStack(prevTransferAmount, Action.EXECUTE) != prevTransferAmount) {
                    //TODO: Print warning/error
                }
            }
        }
    }

    @Override
    public void clientTick() {
        super.clientTick();
        fluidScale = Math.max(fluidScale, getScale());
        if (didTransfer && fluidScale < 1) {
            fluidScale = Math.max(getScale(), Math.min(1, fluidScale + 0.02F));
        } else if (!didTransfer && fluidScale > 0) {
            fluidScale = getScale();
            if (fluidScale == 0) {
                fluidTank.setEmpty();
            }
        }
    }

    public float getScale() {
        if (fluidTank.isEmpty() || fluidTank.getCapacity() == 0) {
            return 0;
        }
        return Math.min(1, (float) fluidTank.getFluidAmount() / fluidTank.getCapacity());
    }

    @Override
    public String toString() {
        return "[FluidNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
    }

    @Override
    public ITextComponent getNeededInfo() {
        return MekanismLang.FLUID_NETWORK_NEEDED.translate((float) fluidTank.getNeeded() / 1_000F);
    }

    @Override
    public ITextComponent getStoredInfo() {
        if (fluidTank.isEmpty()) {
            return MekanismLang.NONE.translate();
        }
        return MekanismLang.NETWORK_MB_STORED.translate(fluidTank.getFluid(), fluidTank.getFluidAmount());
    }

    @Override
    public ITextComponent getFlowInfo() {
        return MekanismLang.NETWORK_MB_PER_TICK.translate(prevTransferAmount);
    }

    @Override
    public boolean isCompatibleWith(FluidNetwork other) {
        return super.isCompatibleWith(other) && (this.fluidTank.isEmpty() || other.fluidTank.isEmpty() || this.fluidTank.isFluidEqual(other.fluidTank.getFluid()));
    }

    @Override
    public boolean compatibleWithBuffer(@Nonnull FluidStack buffer) {
        return super.compatibleWithBuffer(buffer) && (this.fluidTank.isEmpty() || buffer.isEmpty() || this.fluidTank.isFluidEqual(buffer));
    }

    @Override
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.FLUID_NETWORK, transmitters.size(), possibleAcceptors.size());
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Override
    public void onContentsChanged() {
        //TODO: Do we want to mark the network as dirty
    }

    public static class FluidTransferEvent extends Event {

        public final FluidNetwork fluidNetwork;

        public final FluidStack fluidType;
        public final boolean didTransfer;

        public FluidTransferEvent(FluidNetwork network, @Nonnull FluidStack type, boolean did) {
            fluidNetwork = network;
            fluidType = type;
            didTransfer = did;
        }
    }
}