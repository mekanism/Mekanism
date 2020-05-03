package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

public class TileEntityQIORedstoneAdapter extends TileEntityQIOComponent {

    private boolean prevPowering;
    private HashedItem itemType = null;
    private long count = 0;

    public TileEntityQIORedstoneAdapter() {
        super(MekanismBlocks.QIO_REDSTONE_ADAPTER);
    }

    public boolean isPowering() {
        if (isRemote()) {
            return prevPowering;
        }
        QIOFrequency freq = getQIOFrequency();
        if (freq != null && itemType != null) {
            return freq.getStored(itemType) >= count;
        }
        return false;
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
        boolean powering = isPowering();
        if (powering != prevPowering) {
            World world = getWorld();
            if (world != null) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType());
            }
        }
        prevPowering = powering;
        setActive(powering);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setItemStackIfPresent(nbtTags, NBTConstants.SINGLE_ITEM, (item) -> itemType = new HashedItem(item));
        NBTUtils.setLongIfPresent(nbtTags, NBTConstants.AMOUNT, (value) -> count = value);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (itemType != null) {
            nbtTags.put(NBTConstants.SINGLE_ITEM, itemType.getStack().write(new CompoundNBT()));
        }
        nbtTags.putLong(NBTConstants.AMOUNT, count);
        return nbtTags;
    }

    public ItemStack getItemType() {
        return itemType != null ? itemType.getStack() : ItemStack.EMPTY;
    }

    public long getCount() {
        return count;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableItemStack.create(this::getItemType, (value) -> {
            if (value.isEmpty()) {
                itemType = null;
            } else {
                itemType = new HashedItem(value);
            }
        }));
        container.track(SyncableLong.create(this::getCount, (value) -> count = value));
    }
}
