package mekanism.common.tile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.content.tank.TankCache;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TileEntityDynamicTank extends TileEntityMultiblock<SynchronizedTankData> implements IFluidContainerManager {

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
        if (isRemote()) {
            if (clientHasStructure && isRendering) {
                if (structure != null) {
                    float targetScale = (float) structure.fluidStored.getAmount() / clientCapacity;
                    if (Math.abs(prevScale - targetScale) > 0.01) {
                        prevScale = (9 * prevScale + targetScale) / 10;
                    }
                }
            } else {
                for (ValveData data : valveViewing) {
                    TileEntityDynamicTank tile = MekanismUtils.getTileEntity(TileEntityDynamicTank.class, getWorld(), data.location.getPos());
                    if (tile != null) {
                        tile.clientHasStructure = false;
                    }
                }
                valveViewing.clear();
            }
        } else if (structure != null) {
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
                structure.prevFluid = structure.fluidStored.isEmpty() ? FluidStack.EMPTY : structure.fluidStored.copy();
                manageInventory();
            }
        }
    }

    public void manageInventory() {
        int needed = (structure.volume * TankUpdateProtocol.FLUID_PER_TANK) - structure.fluidStored.getAmount();
        List<IInventorySlot> inventorySlots = structure.getInventorySlots();
        //TODO: No magic numbers??
        IInventorySlot inputSlot = inventorySlots.get(0);
        if (FluidContainerUtils.isFluidContainer(inputSlot.getStack())) {
            structure.fluidStored = FluidContainerUtils.handleContainerItem(this, structure.editMode, structure.fluidStored, needed, inputSlot, inventorySlots.get(1));
            Mekanism.packetHandler.sendUpdatePacket(this);
        }
    }

    @Override
    public boolean onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!player.func_225608_bj_() && structure != null) {
            if (manageInventory(player, hand, stack)) {
                player.inventory.markDirty();
                sendPacketToRenderer();
                return true;
            }
            return openGui(player);
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
            data.add(structure.editMode);
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
        if (isRemote()) {
            if (clientHasStructure) {
                clientCapacity = dataStream.readInt();
                structure.editMode = dataStream.readEnumValue(ContainerEditMode.class);
                structure.fluidStored = TileUtils.readFluidStack(dataStream);

                if (isRendering) {
                    int size = dataStream.readInt();
                    valveViewing.clear();
                    for (int i = 0; i < size; i++) {
                        ValveData data = new ValveData();
                        data.location = Coord4D.read(dataStream);
                        data.side = Direction.byIndex(dataStream.readInt());
                        valveViewing.add(data);
                        TileEntityDynamicTank tile = MekanismUtils.getTileEntity(TileEntityDynamicTank.class, getWorld(), data.location.getPos());
                        if (tile != null) {
                            tile.clientHasStructure = true;
                        }
                    }
                }
            }
        }
    }

    public int getScaledFluidLevel(long i) {
        if (clientCapacity == 0 || structure.fluidStored.isEmpty()) {
            return 0;
        }
        return (int) (structure.fluidStored.getAmount() * i / clientCapacity);
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

    public boolean manageInventory(PlayerEntity player, Hand hand, ItemStack itemStack) {
        if (structure == null) {
            return false;
        }

        ItemStack copyStack = StackUtils.size(itemStack, 1);
        return new LazyOptionalHelper<>(FluidUtil.getFluidHandler(copyStack)).getIfPresentElse(
              handler -> new LazyOptionalHelper<>(FluidUtil.getFluidContained(copyStack)).getIfPresentElseDo(
                    itemFluid -> {
                        int stored = structure.fluidStored.getAmount();
                        int needed = (structure.volume * TankUpdateProtocol.FLUID_PER_TANK) - stored;
                        if (!structure.fluidStored.isEmpty() && !structure.fluidStored.isFluidEqual(itemFluid)) {
                            return false;
                        }
                        boolean filled = false;
                        FluidStack drained = handler.drain(needed, player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                        ItemStack container = handler.getContainer();

                        if (container.getCount() == 0) {
                            container = ItemStack.EMPTY;
                        }
                        if (!drained.isEmpty()) {
                            if (player.isCreative()) {
                                filled = true;
                            } else if (!container.isEmpty()) {
                                if (itemStack.getCount() == 1) {
                                    player.setHeldItem(hand, container);
                                    filled = true;
                                } else if (player.inventory.addItemStackToInventory(container)) {
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
                                if (structure.fluidStored.isEmpty()) {
                                    structure.fluidStored = drained;
                                } else {
                                    structure.fluidStored.setAmount(structure.fluidStored.getAmount() + drained.getAmount());
                                }
                                return true;
                            }
                        }
                        return false;
                    },
                    () -> {
                        if (!structure.fluidStored.isEmpty()) {
                            int filled = handler.fill(structure.fluidStored, player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                            ItemStack container = handler.getContainer();
                            if (filled > 0) {
                                if (player.isCreative()) {
                                    structure.fluidStored.setAmount(structure.fluidStored.getAmount() - filled);
                                } else if (itemStack.getCount() == 1) {
                                    structure.fluidStored.setAmount(structure.fluidStored.getAmount() - filled);
                                    player.setHeldItem(hand, container);
                                } else if (itemStack.getCount() > 1 && player.inventory.addItemStackToInventory(container)) {
                                    structure.fluidStored.setAmount(structure.fluidStored.getAmount() - filled);
                                    itemStack.shrink(1);
                                }
                                if (structure.fluidStored.getAmount() == 0) {
                                    structure.fluidStored = FluidStack.EMPTY;
                                }
                                return true;
                            }
                        }
                        return false;
                    }),
              false
        );
    }
}