package mekanism.common.tile.qio;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class TileEntityQIORedstoneAdapter extends TileEntityQIOComponent {

    public static final ModelProperty<Boolean> POWERING_PROPERTY = new ModelProperty<>();

    private boolean prevPowering;
    private HashedItem itemType = null;
    private long count = 0;
    private long clientStoredCount = 0;

    public TileEntityQIORedstoneAdapter() {
        super(MekanismBlocks.QIO_REDSTONE_ADAPTER);
    }

    public boolean isPowering() {
        if (isRemote()) {
            return prevPowering;
        }
        QIOFrequency freq = getQIOFrequency();
        if (freq != null && itemType != null) {
            long stored = freq.getStored(itemType);
            return stored > 0 && stored >= count;
        }
        return false;
    }

    public void handleStackChange(ItemStack stack) {
        itemType = stack.isEmpty() ? null : HashedItem.create(stack);
        markDirty(false);
    }

    public void handleCountChange(int count) {
        this.count = count;
        markDirty(false);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        boolean powering = isPowering();
        if (powering != prevPowering) {
            World world = getWorld();
            if (world != null) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType());
            }
            prevPowering = powering;
            sendUpdatePacket();
        }

        if (world.getGameTime() % 10 == 0) {
            setActive(getQIOFrequency() != null);
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withInitial(POWERING_PROPERTY, prevPowering).build();
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putBoolean(NBTConstants.ACTIVE, prevPowering);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        prevPowering = tag.getBoolean(NBTConstants.ACTIVE);
        requestModelDataUpdate();
        WorldUtils.updateBlock(getWorld(), getPos());
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        NBTUtils.setItemStackIfPresent(nbtTags, NBTConstants.SINGLE_ITEM, item -> itemType = HashedItem.create(item));
        NBTUtils.setLongIfPresent(nbtTags, NBTConstants.AMOUNT, value -> count = value);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
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

    public long getStoredCount() {
        return clientStoredCount;
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableItemStack.create(this::getItemType, value -> {
            if (value.isEmpty()) {
                itemType = null;
            } else {
                itemType = HashedItem.create(value);
            }
        }));
        container.track(SyncableLong.create(this::getCount, value -> count = value));
        container.track(SyncableLong.create(() -> {
            QIOFrequency freq = getQIOFrequency();
            return freq != null && itemType != null ? freq.getStored(itemType) : 0;
        }, value -> clientStoredCount = value));
    }
}
