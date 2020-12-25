package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
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
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class TileEntityBin extends TileEntityMekanism implements IConfigurable {

    public int addTicks = 0;

    private int delayTicks;

    private BinTier tier;

    private BinInventorySlot binSlot;

    public TileEntityBin(IBlockProvider blockProvider) {
        super(blockProvider);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockType(), BinTier.class);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(binSlot = BinInventorySlot.create(this, tier));
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
        delayTicks = Math.max(0, delayTicks - 1);
        if (delayTicks == 0) {
            if (getActive()) {
                TileEntity tile = WorldUtils.getTileEntity(getWorld(), getPos().down());
                TileTransitRequest request = new TileTransitRequest(this, Direction.DOWN);
                request.addItem(binSlot.getBottomStack(), 0);
                TransitResponse response;
                if (tile instanceof TileEntityLogisticalTransporterBase) {
                    response = ((TileEntityLogisticalTransporterBase) tile).getTransmitter().insert(this, request, null, true, 0);
                } else {
                    response = request.addToInventory(tile, Direction.DOWN, false);
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
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        setActive(!getActive());
        World world = getWorld();
        if (world != null) {
            world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 1);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Override
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof BinUpgradeData) {
            BinUpgradeData data = (BinUpgradeData) upgradeData;
            redstone = data.redstone;
            binSlot.setStack(data.binSlot.getStack());
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
        if (world != null && !isRemote()) {
            sendUpdatePacket();
        }
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.put(NBTConstants.ITEM, binSlot.serializeNBT());
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setCompoundIfPresent(tag, NBTConstants.ITEM, nbt -> binSlot.deserializeNBT(nbt));
    }
}