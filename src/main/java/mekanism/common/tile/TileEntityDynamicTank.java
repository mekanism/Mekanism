package mekanism.common.tile;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.base.IBlockProvider;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.content.tank.TankCache;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityDynamicTank extends TileEntityMultiblock<SynchronizedTankData> implements IFluidContainerManager {

    protected static final int[] SLOTS = {0, 1};

    /**
     * A client-sided set of valves on this tank's structure that are currently active, used on the client for rendering fluids.
     */
    public Set<ValveData> valveViewing = new HashSet<>();

    /**
     * The capacity this tank has on the client-side.
     */
    public int clientCapacity;

    public float prevScale;

    public TileEntityDynamicTank() {
        this(MekanismBlock.DYNAMIC_TANK);
    }

    public TileEntityDynamicTank(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) {
            if (clientHasStructure && isRendering) {
                if (structure != null) {
                    float targetScale = (float) (structure.fluidStored != null ? structure.fluidStored.amount : 0) / clientCapacity;
                    if (Math.abs(prevScale - targetScale) > 0.01) {
                        prevScale = (9 * prevScale + targetScale) / 10;
                    }
                }
            } else {
                for (ValveData data : valveViewing) {
                    TileEntityDynamicTank tileEntity = (TileEntityDynamicTank) data.location.getTileEntity(world);
                    if (tileEntity != null) {
                        tileEntity.clientHasStructure = false;
                    }
                }
                valveViewing.clear();
            }
        } else if (structure != null) {
            if (structure.fluidStored != null && structure.fluidStored.amount <= 0) {
                structure.fluidStored = null;
                markDirty();
            }
            if (isRendering) {
                boolean needsValveUpdate = false;
                for (ValveData data : structure.valves) {
                    if (data.activeTicks > 0) {
                        data.activeTicks--;
                    }
                    if (data.activeTicks > 0 != data.prevActive) {
                        needsValveUpdate = true;
                    }
                    data.prevActive = data.activeTicks > 0;
                }
                if (needsValveUpdate || structure.needsRenderUpdate()) {
                    sendPacketToRenderer();
                }
                structure.prevFluid = structure.fluidStored != null ? structure.fluidStored.copy() : null;
                manageInventory();
            }
        }
    }

    public void manageInventory() {
        int needed = (structure.volume * TankUpdateProtocol.FLUID_PER_TANK) - (structure.fluidStored != null ? structure.fluidStored.amount : 0);
        if (FluidContainerUtils.isFluidContainer(structure.inventory.get(0))) {
            structure.fluidStored = FluidContainerUtils.handleContainerItem(this, structure.inventory, structure.editMode, structure.fluidStored, needed, 0, 1, null);
            Mekanism.packetHandler.sendUpdatePacket(this);
        }
    }

    @Override
    public boolean onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!player.isSneaking() && structure != null) {
            if (!manageInventory(player, hand, stack)) {
                Mekanism.packetHandler.sendUpdatePacket(this);
                player.openGui(Mekanism.instance, 18, world, getPos().getX(), getPos().getY(), getPos().getZ());
            } else {
                player.inventory.markDirty();
                sendPacketToRenderer();
            }
            return true;
        }
        return false;
    }

    @Override
    protected SynchronizedTankData getNewStructure() {
        return new SynchronizedTankData();
    }

    @Override
    public TankCache getNewCache() {
        return new TankCache();
    }

    @Override
    protected TankUpdateProtocol getProtocol() {
        return new TankUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<SynchronizedTankData> getManager() {
        return Mekanism.tankManager;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        if (structure != null) {
            data.add(structure.volume * TankUpdateProtocol.FLUID_PER_TANK);
            data.add(structure.editMode.ordinal());
            TileUtils.addFluidStack(data, structure.fluidStored);

            if (isRendering) {
                Set<ValveData> toSend = new HashSet<>();

                for (ValveData valveData : structure.valves) {
                    if (valveData.activeTicks > 0) {
                        toSend.add(valveData);
                    }
                }
                data.add(toSend.size());
                for (ValveData valveData : toSend) {
                    valveData.location.write(data);
                    data.add(valveData.side);
                }
            }
        }
        return data;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
            if (clientHasStructure) {
                clientCapacity = dataStream.readInt();
                structure.editMode = ContainerEditMode.values()[dataStream.readInt()];
                structure.fluidStored = TileUtils.readFluidStack(dataStream);

                if (isRendering) {
                    int size = dataStream.readInt();
                    valveViewing.clear();
                    for (int i = 0; i < size; i++) {
                        ValveData data = new ValveData();
                        data.location = Coord4D.read(dataStream);
                        data.side = Direction.byIndex(dataStream.readInt());
                        valveViewing.add(data);
                        TileEntityDynamicTank tileEntity = (TileEntityDynamicTank) data.location.getTileEntity(world);
                        if (tileEntity != null) {
                            tileEntity.clientHasStructure = true;
                        }
                    }
                }
            }
        }
    }

    public int getScaledFluidLevel(long i) {
        if (clientCapacity == 0 || structure.fluidStored == null) {
            return 0;
        }
        return (int) (structure.fluidStored.amount * i / clientCapacity);
    }

    @Override
    public ContainerEditMode getContainerEditMode() {
        if (structure != null) {
            return structure.editMode;
        }
        return ContainerEditMode.BOTH;
    }

    @Override
    public void setContainerEditMode(ContainerEditMode mode) {
        if (structure == null) {
            return;
        }
        structure.editMode = mode;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    public boolean manageInventory(PlayerEntity player, Hand hand, ItemStack itemStack) {
        if (structure == null) {
            return false;
        }

        ItemStack copyStack = StackUtils.size(itemStack, 1);
        if (FluidContainerUtils.isFluidContainer(itemStack)) {
            IFluidHandlerItem handler = FluidUtil.getFluidHandler(copyStack);
            if (FluidUtil.getFluidContained(copyStack) == null) {
                if (structure.fluidStored != null) {
                    int filled = handler.fill(structure.fluidStored, !player.isCreative());
                    copyStack = handler.getContainer();
                    if (filled > 0) {
                        if (player.isCreative()) {
                            structure.fluidStored.amount -= filled;
                        } else if (itemStack.getCount() == 1) {
                            structure.fluidStored.amount -= filled;
                            player.setHeldItem(hand, copyStack);
                        } else if (itemStack.getCount() > 1 && player.inventory.addItemStackToInventory(copyStack)) {
                            structure.fluidStored.amount -= filled;
                            itemStack.shrink(1);
                        }
                        if (structure.fluidStored.amount == 0) {
                            structure.fluidStored = null;
                        }
                        return true;
                    }
                }
            } else {
                FluidStack itemFluid = FluidUtil.getFluidContained(copyStack);
                int stored = structure.fluidStored != null ? structure.fluidStored.amount : 0;
                int needed = (structure.volume * TankUpdateProtocol.FLUID_PER_TANK) - stored;
                if (structure.fluidStored != null && !structure.fluidStored.isFluidEqual(itemFluid)) {
                    return false;
                }
                boolean filled = false;
                FluidStack drained = handler.drain(needed, !player.isCreative());
                copyStack = handler.getContainer();

                if (copyStack.getCount() == 0) {
                    copyStack = ItemStack.EMPTY;
                }
                if (drained != null) {
                    if (player.isCreative()) {
                        filled = true;
                    } else if (!copyStack.isEmpty()) {
                        if (itemStack.getCount() == 1) {
                            player.setHeldItem(hand, copyStack);
                            filled = true;
                        } else if (player.inventory.addItemStackToInventory(copyStack)) {
                            itemStack.shrink(1);
                            filled = true;
                        }
                    } else {
                        itemStack.shrink(1);
                        if (itemStack.getCount() == 0) {
                            player.setHeldItem(hand, ItemStack.EMPTY);
                        }
                        filled = true;
                    }

                    if (filled) {
                        if (structure.fluidStored == null) {
                            structure.fluidStored = drained;
                        } else {
                            structure.fluidStored.amount += drained.amount;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}