package mekanism.common.tile;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.FluidTankFluidTank;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.upgrade.FluidTankUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityFluidTank extends TileEntityMekanism implements IConfigurable, IFluidContainerManager {

    public FluidTankFluidTank fluidTank;

    private ContainerEditMode editMode = ContainerEditMode.BOTH;

    public FluidTankTier tier;

    public int valve;
    @Nonnull
    public FluidStack valveFluid = FluidStack.EMPTY;

    public float prevScale;

    private boolean needsPacket;

    private FluidInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;

    private boolean updateClientLight = false;

    public TileEntityFluidTank(IBlockProvider blockProvider) {
        super(blockProvider);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockType(), FluidTankTier.class);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(fluidTank = FluidTankFluidTank.create(this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(inputSlot = FluidInventorySlot.input(fluidTank, this, 146, 19));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 146, 51));
        inputSlot.setSlotOverlay(SlotOverlay.INPUT);
        outputSlot.setSlotOverlay(SlotOverlay.OUTPUT);
        return builder.build();
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (updateClientLight) {
            WorldUtils.recheckLighting(world, pos);
            updateClientLight = false;
        }
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (valve > 0) {
            valve--;
            if (valve == 0) {
                valveFluid = FluidStack.EMPTY;
                needsPacket = true;
            }
        }

        float scale = MekanismUtils.getScale(prevScale, fluidTank);
        if (scale != prevScale) {
            if (prevScale == 0 || scale == 0) {
                //If it was empty and no longer is, or wasn't empty and now is empty we want to recheck the block lighting
                // as the fluid may have changed and have a light value
                //TODO: Do we want to only bother doing this if the fluid *does* have a light value attached?
                //TODO: Do we even need this on the sever side of things
                WorldUtils.recheckLighting(world, pos);
            }
            prevScale = scale;
            needsPacket = true;
        }
        inputSlot.handleTank(outputSlot, editMode);
        if (getActive()) {
            FluidUtils.emit(EnumSet.of(Direction.DOWN), fluidTank, this, tier.getOutput());
        }
        if (needsPacket) {
            sendUpdatePacket();
            needsPacket = false;
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt(NBTConstants.EDIT_MODE, editMode.ordinal());
        return nbtTags;
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.EDIT_MODE, ContainerEditMode::byIndexStatic, mode -> editMode = mode);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    @Nonnull
    @Override
    public FluidStack insertFluid(int tank, @Nonnull FluidStack stack, @Nullable Direction side, @Nonnull Action action) {
        FluidStack remainder = super.insertFluid(tank, stack, side, action);
        if (side == Direction.UP && action.execute() && remainder.getAmount() < stack.getAmount() && !isRemote()) {
            if (valve == 0) {
                needsPacket = true;
            }
            valve = 20;
            valveFluid = new FluidStack(stack, 1);
        }
        return remainder;
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            setActive(!getActive());
            World world = getWorld();
            if (world != null) {
                world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 1);
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Override
    public ContainerEditMode getContainerEditMode() {
        return editMode;
    }

    @Override
    public void nextMode() {
        editMode = editMode.getNext();
        markDirty(false);
    }

    @Override
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof FluidTankUpgradeData) {
            FluidTankUpgradeData data = (FluidTankUpgradeData) upgradeData;
            redstone = data.redstone;
            inputSlot.setStack(data.inputSlot.getStack());
            outputSlot.setStack(data.outputSlot.getStack());
            editMode = data.editMode;
            fluidTank.setStack(data.stored);
            for (ITileComponent component : getComponents()) {
                component.read(data.components);
            }
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public FluidTankUpgradeData getUpgradeData() {
        return new FluidTankUpgradeData(redstone, inputSlot, outputSlot, editMode, fluidTank.getFluid(), getComponents());
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(ContainerEditMode::byIndexStatic, ContainerEditMode.BOTH, () -> editMode, value -> editMode = value));
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.put(NBTConstants.FLUID_STORED, fluidTank.getFluid().writeToNBT(new CompoundNBT()));
        updateTag.put(NBTConstants.VALVE, valveFluid.writeToNBT(new CompoundNBT()));
        updateTag.putFloat(NBTConstants.SCALE, prevScale);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, fluid -> fluidTank.setStack(fluid));
        NBTUtils.setFluidStackIfPresent(tag, NBTConstants.VALVE, fluid -> valveFluid = fluid);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevScale = scale);
        //Set the client's light to update just in case the value changed
        //TODO: Do we want to only bother doing this if the fluid *does* have a light value attached?
        updateClientLight = true;
    }
}