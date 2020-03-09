package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.base.ContainerEditMode;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.content.tank.TankCache;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityDynamicTank extends TileEntityMultiblock<SynchronizedTankData> implements IFluidContainerManager {

    /**
     * A client-sided set of valves on this tank's structure that are currently active, used on the client for rendering fluids.
     */
    public Set<ValveData> valveViewing = new ObjectOpenHashSet<>();

    public float prevScale;

    public TileEntityDynamicTank() {
        this(MekanismBlocks.DYNAMIC_TANK);
    }

    public TileEntityDynamicTank(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (clientHasStructure && isRendering) {
            if (structure != null) {
                float targetScale = (float) structure.fluidTank.getFluidAmount() / (structure.volume * TankUpdateProtocol.FLUID_PER_TANK);
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
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null) {
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
                structure.prevFluid = structure.fluidTank.isEmpty() ? FluidStack.EMPTY : structure.fluidTank.getFluid().copy();
                List<IInventorySlot> inventorySlots = structure.getInventorySlots(null);
                //TODO: No magic numbers??
                FluidInventorySlot inputSlot = (FluidInventorySlot) inventorySlots.get(0);
                inputSlot.handleTank(inventorySlots.get(1), structure.editMode);
            }
        }
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!player.isShiftKeyDown() && structure != null) {
            if (manageInventory(player, hand, stack)) {
                player.inventory.markDirty();
                sendPacketToRenderer();
                return ActionResultType.SUCCESS;
            }
            return openGui(player);
        }
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    protected SynchronizedTankData getNewStructure() {
        return new SynchronizedTankData(this);
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
            data.add(structure.volume);
            data.add(structure.editMode);
            data.add(structure.fluidTank.getFluid());

            if (isRendering) {
                Set<ValveData> toSend = new ObjectOpenHashSet<>();

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
                structure.volume = dataStream.readInt();
                structure.editMode = dataStream.readEnumValue(ContainerEditMode.class);
                structure.fluidTank.setStack(dataStream.readFluidStack());

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

    @Override
    public ContainerEditMode getContainerEditMode() {
        if (structure == null) {
            return ContainerEditMode.BOTH;
        }
        return structure.editMode;
    }

    @Override
    public void setContainerEditMode(ContainerEditMode mode) {
        if (structure != null) {
            structure.editMode = mode;
        }
    }

    public boolean manageInventory(PlayerEntity player, Hand hand, ItemStack itemStack) {
        if (structure == null) {
            return false;
        }
        ItemStack copyStack = StackUtils.size(itemStack, 1);
        Optional<IFluidHandlerItem> fluidHandlerItem = MekanismUtils.toOptional(FluidUtil.getFluidHandler(copyStack));
        if (fluidHandlerItem.isPresent()) {
            IFluidHandlerItem handler = fluidHandlerItem.get();
            FluidStack fluidInItem;
            if (structure.fluidTank.isEmpty()) {
                //If we don't have a fluid stored try draining in general
                fluidInItem = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
            } else {
                //Otherwise try draining the same type of fluid we have stored
                // We do this to better support multiple tanks in case the fluid we have stored we could pull out of a block's
                // second tank but just asking to drain a specific amount
                fluidInItem = handler.drain(new FluidStack(structure.fluidTank.getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE);
            }
            if (fluidInItem.isEmpty()) {
                if (!structure.fluidTank.isEmpty()) {
                    int filled = handler.fill(structure.fluidTank.getFluid(), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                    ItemStack container = handler.getContainer();
                    if (filled > 0) {
                        boolean removeFluid = false;
                        if (player.isCreative()) {
                            removeFluid = true;
                        } else if (itemStack.getCount() == 1) {
                            removeFluid = true;
                            player.setHeldItem(hand, container);
                        } else if (itemStack.getCount() > 1 && player.inventory.addItemStackToInventory(container)) {
                            removeFluid = true;
                            itemStack.shrink(1);
                        }
                        if (removeFluid) {
                            structure.fluidTank.shrinkStack(filled, Action.EXECUTE);
                        }
                        return true;
                    }
                }
            } else if (structure.fluidTank.isEmpty() || structure.fluidTank.getFluid().isFluidEqual(fluidInItem)) {
                boolean filled = false;
                int stored = structure.fluidTank.getFluidAmount();
                int needed = (structure.volume * TankUpdateProtocol.FLUID_PER_TANK) - stored;
                FluidStack drained = handler.drain(needed, player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                ItemStack container = handler.getContainer();
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
                        if (itemStack.isEmpty()) {
                            player.setHeldItem(hand, ItemStack.EMPTY);
                        }
                        filled = true;
                    }
                    if (filled) {
                        if (structure.fluidTank.isEmpty()) {
                            structure.fluidTank.setStack(drained);
                        } else {
                            structure.fluidTank.growStack(drained.getAmount(), Action.EXECUTE);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        //Disable item handler caps if we are the dynamic tank, don't disable it for the subclassed valve though
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && getType() == MekanismTileEntityTypes.DYNAMIC_TANK.getTileEntityType()) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }
}