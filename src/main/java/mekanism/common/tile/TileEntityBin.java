package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBlockProvider;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntityBin extends TileEntityMekanism implements ISidedInventory, IActiveState, IConfigurable, ITierUpgradeable, IComparatorSupport {

    private static final int[] UPSLOTS = {1};
    private static final int[] DOWNSLOTS = {0};

    public int addTicks = 0;

    public int delayTicks;

    public int cacheCount;

    public BinTier tier = BinTier.BASIC;

    public ItemStack itemType = ItemStack.EMPTY;

    public ItemStack topStack = ItemStack.EMPTY;
    public ItemStack bottomStack = ItemStack.EMPTY;

    public int prevCount;

    public int clientAmount;

    private BinItemHandler myItemHandler;

    public TileEntityBin(IBlockProvider blockProvider) {
        super(blockProvider);
        myItemHandler = new BinItemHandler(this);
    }

    @Override
    public boolean upgrade(BaseTier upgradeTier) {
        //TODO: Replace tier with next tier?
        if (upgradeTier.ordinal() != tier.ordinal() + 1) {
            return false;
        }
        tier = BinTier.values()[upgradeTier.ordinal()];
        Mekanism.packetHandler.sendUpdatePacket(this);
        markDirty();
        return true;
    }

    public void sortStacks() {
        if (getItemCount() == 0 || itemType.isEmpty()) {
            itemType = ItemStack.EMPTY;
            topStack = ItemStack.EMPTY;
            bottomStack = ItemStack.EMPTY;
            cacheCount = 0;
            return;
        }

        int count = getItemCount();
        int remain = tier.getStorage() - count;

        if (remain >= itemType.getMaxStackSize()) {
            topStack = ItemStack.EMPTY;
        } else {
            topStack = itemType.copy();
            topStack.setCount(itemType.getMaxStackSize() - remain);
        }

        count -= topStack.getCount();
        bottomStack = itemType.copy();
        bottomStack.setCount(Math.min(itemType.getMaxStackSize(), count));
        count -= bottomStack.getCount();
        cacheCount = count;
    }

    public boolean isValid(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof ItemBlockBin) {
            return false;
        }
        if (itemType.isEmpty()) {
            return true;
        }
        return ItemHandlerHelper.canItemStacksStack(itemType, stack);
    }

    public ItemStack add(ItemStack stack, boolean simulate) {
        if (isValid(stack) && (tier == BinTier.CREATIVE || getItemCount() != tier.getStorage())) {
            if (itemType.isEmpty() && !simulate) {
                setItemType(stack);
            }
            if (tier != BinTier.CREATIVE) {
                if (getItemCount() + stack.getCount() <= tier.getStorage()) {
                    if (!simulate) {
                        setItemCount(getItemCount() + stack.getCount());
                    }
                    return ItemStack.EMPTY;
                } else {
                    ItemStack rejects = stack.copy();
                    rejects.setCount((getItemCount() + stack.getCount()) - tier.getStorage());
                    if (!simulate) {
                        setItemCount(tier.getStorage());
                    }
                    return rejects;
                }
            } else if (!simulate) {
                setItemCount(Integer.MAX_VALUE);
            }
        }
        return stack;
    }

    public ItemStack add(ItemStack stack) {
        return add(stack, false);
    }

    public ItemStack removeStack() {
        if (getItemCount() == 0) {
            return ItemStack.EMPTY;
        }
        return remove(bottomStack.getCount());
    }

    public ItemStack remove(int amount, boolean simulate) {
        if (getItemCount() == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack ret = itemType.copy();
        ret.setCount(Math.min(Math.min(amount, itemType.getMaxStackSize()), getItemCount()));
        if (tier != BinTier.CREATIVE && !simulate) {
            setItemCount(getItemCount() - ret.getCount());
        }
        return ret;
    }

    public ItemStack remove(int amount) {
        return remove(amount, false);
    }

    public int getItemCount() {
        return bottomStack.getCount() + cacheCount + topStack.getCount();
    }

    public void setItemCount(int count) {
        cacheCount = Math.max(0, count);
        topStack = ItemStack.EMPTY;
        bottomStack = ItemStack.EMPTY;
        if (count == 0) {
            setItemType(ItemStack.EMPTY);
        }
        markDirty();
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            addTicks = Math.max(0, addTicks - 1);
            delayTicks = Math.max(0, delayTicks - 1);
            sortStacks();
            if (getItemCount() != prevCount) {
                markDirty();
                MekanismUtils.saveChunk(this);
            }

            if (delayTicks == 0) {
                if (!bottomStack.isEmpty() && getActive()) {
                    TileEntity tile = Coord4D.get(this).offset(Direction.DOWN).getTileEntity(world);
                    TransitResponse response = CapabilityUtils.getCapabilityHelper(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, Direction.UP).getIfPresentElseDo(
                          transporter -> TransporterUtils.insert(this, transporter, TransitRequest.getFromStack(bottomStack), null, true, 0),
                          () -> InventoryUtils.putStackInInventory(tile, TransitRequest.getFromStack(bottomStack), Direction.DOWN, false)
                    );
                    if (!response.isEmpty() && tier != BinTier.CREATIVE) {
                        bottomStack.shrink(response.getSendingAmount());
                        setInventorySlotContents(0, bottomStack);
                    }
                    delayTicks = 10;
                }
            } else {
                delayTicks--;
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("itemCount", cacheCount);
        nbtTags.putInt("tier", tier.ordinal());
        if (!bottomStack.isEmpty()) {
            nbtTags.put("bottomStack", bottomStack.write(new CompoundNBT()));
        }
        if (!topStack.isEmpty()) {
            nbtTags.put("topStack", topStack.write(new CompoundNBT()));
        }
        if (getItemCount() > 0) {
            nbtTags.put("itemType", itemType.write(new CompoundNBT()));
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        cacheCount = nbtTags.getInt("itemCount");
        tier = BinTier.values()[nbtTags.getInt("tier")];
        bottomStack = ItemStack.read(nbtTags.getCompound("bottomStack"));
        topStack = ItemStack.read(nbtTags.getCompound("topStack"));
        if (getItemCount() > 0) {
            itemType = ItemStack.read(nbtTags.getCompound("itemType"));
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(getItemCount());
        data.add(tier.ordinal());
        if (getItemCount() > 0) {
            data.add(itemType);
        }
        return data;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
            clientAmount = dataStream.readInt();
            tier = BinTier.values()[dataStream.readInt()];
            if (clientAmount > 0) {
                itemType = dataStream.readItemStack();
            } else {
                itemType = ItemStack.EMPTY;
            }
            MekanismUtils.updateBlock(world, getPos());
        }
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slotID) {
        if (slotID == 1) {
            return topStack;
        }
        return bottomStack;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int slotID, int amount) {
        if (slotID == 1) {
            return ItemStack.EMPTY;
        } else if (slotID == 0) {
            int toRemove = Math.min(getItemCount(), amount);

            if (toRemove > 0) {
                ItemStack ret = itemType.copy();
                ret.setCount(toRemove);

                setItemCount(getItemCount() - toRemove);

                return ret;
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int slotID) {
        return getStackInSlot(slotID);
    }

    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public void setInventorySlotContents(int i, @Nonnull ItemStack itemstack) {
        if (i == 0) {
            if (getItemCount() == 0) {
                return;
            }
            if (tier != BinTier.CREATIVE) {
                if (itemstack.isEmpty()) {
                    setItemCount(getItemCount() - bottomStack.getCount());
                } else {
                    setItemCount(getItemCount() - (bottomStack.getCount() - itemstack.getCount()));
                }
            }
        } else if (i == 1) {
            if (itemstack.isEmpty()) {
                topStack = ItemStack.EMPTY;
            } else if (isValid(itemstack) && itemstack.getCount() > topStack.getCount() && tier != BinTier.CREATIVE) {
                add(StackUtils.size(itemstack, itemstack.getCount() - topStack.getCount()));
            }
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!world.isRemote) {
            MekanismUtils.saveChunk(this);
            Mekanism.packetHandler.sendUpdatePacket(this);
            prevCount = getItemCount();
            sortStacks();
        }
    }

    public void setItemType(ItemStack stack) {
        if (stack.isEmpty()) {
            itemType = ItemStack.EMPTY;
            cacheCount = 0;
            topStack = ItemStack.EMPTY;
            bottomStack = ItemStack.EMPTY;
            return;
        }
        ItemStack ret = stack.copy();
        ret.setCount(1);
        itemType = ret;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull PlayerEntity player) {
        return true;
    }

    @Override
    public void openInventory(@Nonnull PlayerEntity player) {
    }

    @Override
    public void closeInventory(@Nonnull PlayerEntity player) {
    }

    @Override
    public boolean isItemValidForSlot(int i, @Nonnull ItemStack itemstack) {
        return i == 1 && isValid(itemstack);
    }

    @Override
    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @Override
    public void clear() {
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        //This is legacy for the sided inventory stuff, using IItemHandler returns a
        // BinItemHandler that does not use this method
        if (side == Direction.UP) {
            return UPSLOTS;
        } else if (side == Direction.DOWN) {
            return DOWNSLOTS;
        }
        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean canInsertItem(int i, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        return isItemValidForSlot(i, itemstack);
    }

    @Override
    public boolean canExtractItem(int i, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        return i == 0 && isValid(itemstack);
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
        return MekanismUtils.redstoneLevelFromContents(getItemCount(), getMaxStoredCount());
    }

    public int getMaxStoredCount() {
        return tier.getStorage();
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        setActive(!getActive());
        world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 1);
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
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> myItemHandler));
        }
        return super.getCapability(capability, side);
    }

    private class BinItemHandler implements IItemHandler {

        private TileEntityBin tileEntityBin;

        public BinItemHandler(TileEntityBin tileEntityBin) {
            this.tileEntityBin = tileEntityBin;
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot != 0) {
                return 0;
            }
            return tier.getStorage();
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot != 0 || tileEntityBin.itemType.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return StackUtils.size(tileEntityBin.itemType, tileEntityBin.getItemCount());
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return slot != 0 ? ItemStack.EMPTY : tileEntityBin.add(stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot != 0 ? ItemStack.EMPTY : tileEntityBin.remove(amount, simulate);
        }
    }
}