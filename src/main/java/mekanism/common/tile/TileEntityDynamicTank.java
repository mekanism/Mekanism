package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityDynamicTank extends TileEntityMultiblock<TankMultiblockData> implements IFluidContainerManager, IValveHandler {

    public TileEntityDynamicTank() {
        this(MekanismBlocks.DYNAMIC_TANK);
        //Disable item handler caps if we are the dynamic tank, don't disable it for the subclassed valve though
        addDisabledCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    public TileEntityDynamicTank(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!player.isSneaking() && getMultiblock().isFormed()) {
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
    public TankMultiblockData createMultiblock() {
        return new TankMultiblockData(this);
    }

    @Override
    public TankUpdateProtocol getFormationProtocol() {
        return new TankUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<TankMultiblockData> getManager() {
        return Mekanism.tankManager;
    }

    @Override
    public ContainerEditMode getContainerEditMode() {
        return getMultiblock().editMode;
    }

    @Override
    public void nextMode() {
        getMultiblock().editMode = getMultiblock().editMode.getNext();
    }

    private boolean manageInventory(PlayerEntity player, Hand hand, ItemStack itemStack) {
        if (!getMultiblock().isFormed()) {
            return false;
        }
        return FluidUtils.handleTankInteraction(player, hand, itemStack, getMultiblock().fluidTank);
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (getMultiblock().isFormed() && isRendering) {
            updateTag.putFloat(NBTConstants.SCALE, getMultiblock().prevScale);
            updateTag.putInt(NBTConstants.VOLUME, getMultiblock().getVolume());
            updateTag.put(NBTConstants.FLUID_STORED, getMultiblock().fluidTank.getFluid().writeToNBT(new CompoundNBT()));
            updateTag.put(NBTConstants.GAS_STORED, getMultiblock().gasTank.getStack().write(new CompoundNBT()));
            writeValves(updateTag);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (getMultiblock().isFormed() && isRendering) {
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> getMultiblock().prevScale = scale);
            NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, value -> getMultiblock().setVolume(value));
            NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, value -> getMultiblock().fluidTank.setStack(value));
            NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> getMultiblock().gasTank.setStack(value));
            readValves(tag);
        }
    }
}