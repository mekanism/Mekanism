package mekanism.common.tile.bin;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBlockProvider;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ILogisticalTransporter;
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
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class TileEntityBin extends TileEntityMekanism implements ISidedInventory, IActiveState, IConfigurable, ITierUpgradeable, IComparatorSupport {

    private static final int[] UPSLOTS = {1};
    private static final int[] DOWNSLOTS = {0};

    public boolean isActive;
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
                if (!bottomStack.isEmpty() && isActive) {
                    TileEntity tile = Coord4D.get(this).offset(EnumFacing.DOWN).getTileEntity(world);
                    ILogisticalTransporter transporter = CapabilityUtils.getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, EnumFacing.UP);
                    TransitResponse response;
                    if (transporter == null) {
                        response = InventoryUtils.putStackInInventory(tile, TransitRequest.getFromStack(bottomStack), EnumFacing.DOWN, false);
                    } else {
                        response = TransporterUtils.insert(this, transporter, TransitRequest.getFromStack(bottomStack), null, true, 0);
                    }
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
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setInteger("itemCount", cacheCount);
        nbtTags.setInteger("tier", tier.ordinal());
        if (!bottomStack.isEmpty()) {
            nbtTags.setTag("bottomStack", bottomStack.writeToNBT(new NBTTagCompound()));
        }
        if (!topStack.isEmpty()) {
            nbtTags.setTag("topStack", topStack.writeToNBT(new NBTTagCompound()));
        }
        if (getItemCount() > 0) {
            nbtTags.setTag("itemType", itemType.writeToNBT(new NBTTagCompound()));
        }
        return nbtTags;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        cacheCount = nbtTags.getInteger("itemCount");
        tier = BinTier.values()[nbtTags.getInteger("tier")];
        bottomStack = new ItemStack(nbtTags.getCompoundTag("bottomStack"));
        topStack = new ItemStack(nbtTags.getCompoundTag("topStack"));
        if (getItemCount() > 0) {
            itemType = new ItemStack(nbtTags.getCompoundTag("itemType"));
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(isActive);
        data.add(getItemCount());
        data.add(tier.ordinal());
        if (getItemCount() > 0) {
            data.add(itemType);
        }
        return data;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            isActive = dataStream.readBoolean();
            clientAmount = dataStream.readInt();
            tier = BinTier.values()[dataStream.readInt()];
            if (clientAmount > 0) {
                itemType = PacketHandler.readStack(dataStream);
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

    @Nonnull
    @Override
    public String getName() {
        return LangUtils.localize(getBlockType().getTranslationKey() + ".Bin" + tier.getBaseTier().getSimpleName() + ".name");
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
        return true;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
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
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        //This is legacy for the sided inventory stuff, using IItemHandler returns a
        // BinItemHandler that does not use this method
        if (side == EnumFacing.UP) {
            return UPSLOTS;
        } else if (side == EnumFacing.DOWN) {
            return DOWNSLOTS;
        }
        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean canInsertItem(int i, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        return isItemValidForSlot(i, itemstack);
    }

    @Override
    public boolean canExtractItem(int i, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        return i == 0 && isValid(itemstack);
    }

    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP;
    }

    @Override
    public boolean getActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        boolean stateChange = getActive() != active;
        if (stateChange) {
            isActive = active;
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
        return MekanismUtils.redstoneLevelFromContents(getItemCount(), getMaxStoredCount());
    }

    public int getMaxStoredCount() {
        return tier.getStorage();
    }

    @Override
    public EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side) {
        setActive(!getActive());
        world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 1);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onRightClick(EntityPlayer player, EnumFacing side) {
        return EnumActionResult.PASS;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.CONFIGURABLE_CAPABILITY || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.cast(this);
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(myItemHandler);
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