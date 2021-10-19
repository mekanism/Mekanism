package mekanism.common.tile.qio;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityQIORedstoneAdapter extends TileEntityQIOComponent implements ISustainedData {

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

    public void handleCountChange(long count) {
        if (this.count != count) {
            this.count = count;
            markDirty(false);
        }
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        boolean powering = isPowering();
        if (powering != prevPowering) {
            World world = getLevel();
            if (world != null) {
                world.updateNeighborsAt(getBlockPos(), getBlockType());
            }
            prevPowering = powering;
            sendUpdatePacket();
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withInitial(POWERING_PROPERTY, prevPowering).build();
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (itemType != null) {
            ItemDataUtils.setCompound(itemStack, NBTConstants.SINGLE_ITEM, itemType.getStack().save(new CompoundNBT()));
        }
        ItemDataUtils.setLong(itemStack, NBTConstants.AMOUNT, count);
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, NBTConstants.SINGLE_ITEM, NBT.TAG_COMPOUND)) {
            itemType = HashedItem.create(ItemStack.of(ItemDataUtils.getCompound(itemStack, NBTConstants.SINGLE_ITEM)));
        }
        count = ItemDataUtils.getLong(itemStack, NBTConstants.AMOUNT);
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.SINGLE_ITEM, NBTConstants.SINGLE_ITEM);
        remap.put(NBTConstants.AMOUNT, NBTConstants.AMOUNT);
        return remap;
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
        WorldUtils.updateBlock(getLevel(), getBlockPos(), getBlockState());
    }

    @Override
    protected void loadGeneralPersistentData(CompoundNBT data) {
        super.loadGeneralPersistentData(data);
        NBTUtils.setItemStackIfPresent(data, NBTConstants.SINGLE_ITEM, item -> itemType = HashedItem.create(item));
        NBTUtils.setLongIfPresent(data, NBTConstants.AMOUNT, value -> count = value);
    }

    @Override
    protected void addGeneralPersistentData(CompoundNBT data) {
        super.addGeneralPersistentData(data);
        if (itemType != null) {
            data.put(NBTConstants.SINGLE_ITEM, itemType.getStack().save(new CompoundNBT()));
        }
        data.putLong(NBTConstants.AMOUNT, count);
    }

    @ComputerMethod(nameOverride = "getTargetItem")
    public ItemStack getItemType() {
        return itemType != null ? itemType.getStack() : ItemStack.EMPTY;
    }

    @ComputerMethod(nameOverride = "getTriggerAmount")
    public long getCount() {
        return count;
    }

    public long getStoredCount() {
        return clientStoredCount;
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

    //Methods relating to IComputerTile
    @ComputerMethod
    private void clearTargetItem() throws ComputerException {
        validateSecurityIsPublic();
        handleStackChange(ItemStack.EMPTY);
    }

    @ComputerMethod
    private void setTargetItem(ResourceLocation itemName) throws ComputerException {
        validateSecurityIsPublic();
        Item item = ForgeRegistries.ITEMS.getValue(itemName);
        if (item == null || item == Items.AIR) {
            throw new ComputerException("Target item '%s' could not be found. If you are trying to clear it consider using clearTargetItem instead.", itemName);
        }
        handleStackChange(new ItemStack(item));
    }

    @ComputerMethod
    private void setTriggerAmount(long amount) throws ComputerException {
        validateSecurityIsPublic();
        if (amount < 0) {
            throw new ComputerException("Trigger amount cannot be negative. Received: %d", amount);
        }
        handleCountChange(amount);
    }
    //End methods IComputerTile
}