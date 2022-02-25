package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.lib.inventory.TileTransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.upgrade.BinUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityBin extends TileEntityMekanism implements IConfigurable {

    public int addTicks = 0;
    public int removeTicks = 0;
    private int delayTicks;

    private BinTier tier;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getStored")
    private BinInventorySlot binSlot;

    public TileEntityBin(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockType(), BinTier.class);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(binSlot = BinInventorySlot.create(listener, tier));
        return builder.build();
    }

    public BinTier getTier() {
        return tier;
    }

    public int getItemCount() {
        return binSlot.getCount();
    }

    public BinInventorySlot getBinSlot() {
        return binSlot;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        addTicks = Math.max(0, addTicks - 1);
        removeTicks = Math.max(0, removeTicks - 1);
        delayTicks = Math.max(0, delayTicks - 1);
        if (delayTicks == 0) {
            if (getActive()) {
                BlockEntity tile = WorldUtils.getTileEntity(getLevel(), getBlockPos().below());
                TileTransitRequest request = new TileTransitRequest(this, Direction.DOWN);
                request.addItem(binSlot.getBottomStack(), 0);
                TransitResponse response;
                if (tile instanceof TileEntityLogisticalTransporterBase transporter) {
                    response = transporter.getTransmitter().insert(this, request, null, true, 0);
                } else {
                    response = request.addToInventory(tile, Direction.DOWN, 0, false);
                }
                if (!response.isEmpty() && tier != BinTier.CREATIVE) {
                    int sendingAmount = response.getSendingAmount();
                    MekanismUtils.logMismatchedStackSize(binSlot.shrinkStack(sendingAmount, Action.EXECUTE), sendingAmount);
                }
                delayTicks = 10;
            }
        } else {
            delayTicks--;
        }
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        setActive(!getActive());
        Level world = getLevel();
        if (world != null) {
            world.playSound(null, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), SoundEvents.UI_BUTTON_CLICK, SoundSource.BLOCKS, 0.3F, 1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onRightClick(Player player) {
        return InteractionResult.PASS;
    }

    @Override
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof BinUpgradeData data) {
            redstone = data.redstone();
            binSlot.setStack(data.binSlot().getStack());
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public BinUpgradeData getUpgradeData() {
        return new BinUpgradeData(redstone, getBinSlot());
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (level != null && !isRemote()) {
            sendUpdatePacket();
        }
    }

    @Nonnull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        updateTag.put(NBTConstants.ITEM, binSlot.serializeNBT());
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setCompoundIfPresent(tag, NBTConstants.ITEM, nbt -> binSlot.deserializeNBT(nbt));
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private int getCapacity() {
        return binSlot.getLimit(binSlot.getStack());
    }
    //End methods IComputerTile
}