package mekanism.common.tile;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.base.ContainerEditMode;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.SyncableGasStack;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.HybridInventorySlot;
import mekanism.common.multiblock.IValveHandler;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityDynamicTank extends TileEntityMultiblock<TankMultiblockData> implements IFluidContainerManager, IValveHandler {

    public float prevScale;

    public TileEntityDynamicTank() {
        this(MekanismBlocks.DYNAMIC_TANK);
        //Disable item handler caps if we are the dynamic tank, don't disable it for the subclassed valve though
        addDisabledCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    public TileEntityDynamicTank(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && isRendering) {
            boolean needsPacket = needsValveUpdate();
            List<IInventorySlot> inventorySlots = structure.getInventorySlots(null);
            //TODO: No magic numbers??
            HybridInventorySlot inputSlot = (HybridInventorySlot) inventorySlots.get(0);
            HybridInventorySlot outputSlot = (HybridInventorySlot) inventorySlots.get(1);
            inputSlot.handleTank(outputSlot, structure.editMode);
            inputSlot.drainGasTank();
            outputSlot.fillGasTank();
            float scale = Math.max(MekanismUtils.getScale(prevScale, structure.fluidTank), MekanismUtils.getScale(prevScale, structure.gasTank));
            if (scale != prevScale) {
                needsPacket = true;
                prevScale = scale;
            }
            if (needsPacket) {
                sendUpdatePacket();
            }
        }
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!player.isShiftKeyDown() && structure != null) {
            if (manageInventory(player, hand, stack)) {
                player.inventory.markDirty();
                return ActionResultType.SUCCESS;
            }
            return openGui(player);
        }
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public TankMultiblockData getNewStructure() {
        return new TankMultiblockData(this);
    }

    @Override
    public TankUpdateProtocol getProtocol() {
        return new TankUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<TankMultiblockData> getManager() {
        return Mekanism.tankManager;
    }

    @Override
    public ContainerEditMode getContainerEditMode() {
        if (structure == null) {
            return ContainerEditMode.BOTH;
        }
        return structure.editMode;
    }

    @Override
    public void nextMode() {
        if (structure != null) {
            structure.editMode = structure.editMode.getNext();
        }
    }

    private boolean manageInventory(PlayerEntity player, Hand hand, ItemStack itemStack) {
        if (structure == null) {
            return false;
        }
        return FluidUtils.handleTankInteraction(player, hand, itemStack, structure.fluidTank);
    }

    @Override
    public Collection<ValveData> getValveData() {
        return structure != null ? structure.valves : null;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (structure != null && isRendering) {
            updateTag.putFloat(NBTConstants.SCALE, prevScale);
            updateTag.putInt(NBTConstants.VOLUME, structure.getVolume());
            updateTag.put(NBTConstants.FLUID_STORED, structure.fluidTank.getFluid().writeToNBT(new CompoundNBT()));
            updateTag.put(NBTConstants.GAS_STORED, structure.gasTank.getStack().write(new CompoundNBT()));
            writeValves(updateTag);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (clientHasStructure && isRendering && structure != null) {
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevScale = scale);
            NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, value -> structure.setVolume(value));
            NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, value -> structure.fluidTank.setStack(value));
            NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> structure.gasTank.setStack(value));
            readValves(tag);
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(ContainerEditMode::byIndexStatic, ContainerEditMode.BOTH, this::getContainerEditMode, mode -> {
            if (structure != null) {
                structure.editMode = mode;
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.getVolume(), value -> {
            if (structure != null) {
                structure.setVolume(value);
            }
        }));
        container.track(SyncableFluidStack.create(() -> structure == null ? FluidStack.EMPTY : structure.fluidTank.getFluid(), value -> {
            if (structure != null) {
                structure.fluidTank.setStack(value);
            }
        }));
        container.track(SyncableGasStack.create(() -> structure == null ? GasStack.EMPTY : structure.gasTank.getStack(), value -> {
            if (structure != null) {
                structure.gasTank.setStack(value);
            }
        }));
    }
}