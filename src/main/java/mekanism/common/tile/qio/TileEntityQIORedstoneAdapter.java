package mekanism.common.tile.qio;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityQIORedstoneAdapter extends TileEntityQIOComponent {

    public static final ModelProperty<Boolean> POWERING_PROPERTY = new ModelProperty<>();

    private boolean prevPowering;
    private HashedItem itemType = null;
    private long count = 0;
    private long clientStoredCount = 0;

    public TileEntityQIORedstoneAdapter() {
        super(MekanismBlocks.QIO_REDSTONE_ADAPTER);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
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
    public CompoundNBT getConfigurationData(PlayerEntity player) {
        CompoundNBT data = super.getConfigurationData(player);
        if (itemType != null) {
            data.put(NBTConstants.SINGLE_ITEM, itemType.getStack().save(new CompoundNBT()));
        }
        data.putLong(NBTConstants.AMOUNT, count);
        return data;
    }

    @Override
    public void setConfigurationData(PlayerEntity player, CompoundNBT data) {
        super.setConfigurationData(player, data);
        NBTUtils.setItemStackIfPresent(data, NBTConstants.SINGLE_ITEM, item -> itemType = HashedItem.create(item));
        NBTUtils.setLongIfPresent(data, NBTConstants.AMOUNT, value -> count = value);
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
    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.load(state, nbtTags);
        NBTUtils.setItemStackIfPresent(nbtTags, NBTConstants.SINGLE_ITEM, item -> itemType = HashedItem.create(item));
        NBTUtils.setLongIfPresent(nbtTags, NBTConstants.AMOUNT, value -> count = value);
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT nbtTags) {
        super.save(nbtTags);
        if (itemType != null) {
            nbtTags.put(NBTConstants.SINGLE_ITEM, itemType.getStack().save(new CompoundNBT()));
        }
        nbtTags.putLong(NBTConstants.AMOUNT, count);
        return nbtTags;
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