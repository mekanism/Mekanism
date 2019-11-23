package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.TileNetworkList;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityBin extends TileEntityMekanism implements IActiveState, IConfigurable, ITierUpgradeable, IComparatorSupport {

    public int addTicks = 0;

    public int delayTicks;

    public BinTier tier;

    //TODO: Remove this, and just sync the slot??
    public ItemStack clientStack = ItemStack.EMPTY;

    private BinInventorySlot binSlot;

    public TileEntityBin(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void presetVariables() {
        tier = ((BlockBin) getBlockType()).getTier();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(binSlot = BinInventorySlot.create(this, tier));
        return builder.build();
    }

    @Override
    public boolean upgrade(BaseTier upgradeTier) {
        //TODO: Upgrade
        //TODO: Replace tier with next tier?
        /*if (upgradeTier.ordinal() != tier.ordinal() + 1) {
            return false;
        }
        tier = EnumUtils.BIN_TIERS[upgradeTier.ordinal()];
        Mekanism.packetHandler.sendUpdatePacket(this);
        markDirty();*/
        return true;
    }

    public BinTier getTier() {
        return tier;
    }

    public int getItemCount() {
        return binSlot.getStack().getCount();
    }

    public ItemStack getItemType() {
        return binSlot.getStack();
    }

    public BinInventorySlot getBinSlot() {
        return binSlot;
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            addTicks = Math.max(0, addTicks - 1);
            delayTicks = Math.max(0, delayTicks - 1);

            if (delayTicks == 0) {
                if (getActive()) {
                    TileEntity tile = MekanismUtils.getTileEntity(getWorld(), getPos().down());
                    ItemStack bottomStack = binSlot.getBottomStack();
                    TransitResponse response = CapabilityUtils.getCapabilityHelper(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, Direction.UP).getIfPresentElseDo(
                          transporter -> TransporterUtils.insert(this, transporter, TransitRequest.getFromStack(bottomStack), null, true, 0),
                          () -> InventoryUtils.putStackInInventory(tile, TransitRequest.getFromStack(bottomStack), Direction.DOWN, false)
                    );
                    if (!response.isEmpty() && tier != BinTier.CREATIVE) {
                        int sendingAmount = response.getSendingAmount();
                        if (binSlot.shrinkStack(sendingAmount, Action.EXECUTE) != sendingAmount) {
                            //TODO: Print error something went wrong
                        }
                    }
                    delayTicks = 10;
                }
            } else {
                delayTicks--;
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        ItemStack stack = binSlot.getStack();
        //TODO: Just write our own item stack method for over the network/slot sending method
        data.add(stack);
        //Add size so that we can fix it
        data.add(stack.getCount());
        return data;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            clientStack = dataStream.readItemStack();
            //Fix the size as it was packed
            clientStack.setCount(dataStream.readInt());
            MekanismUtils.updateBlock(getWorld(), getPos());
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!isRemote()) {
            MekanismUtils.saveChunk(this);
            Mekanism.packetHandler.sendUpdatePacket(this);
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
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(getItemCount(), binSlot.getLimit(binSlot.getStack()));
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }
}