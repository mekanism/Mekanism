package mekanism.common.tile.qio;

import java.util.Map;
import mekanism.api.NBTConstants;
import mekanism.client.model.data.DataBasedModelLoader;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIORedstoneAdapter extends TileEntityQIOComponent {

    @Nullable
    private HashedItem itemType = null;
    private boolean fuzzy;
    private boolean inverted;
    private long count = 0;
    private long clientStoredCount = 0;
    private boolean isEmitting;

    public TileEntityQIORedstoneAdapter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QIO_REDSTONE_ADAPTER, pos, state);
    }

    public int getRedstoneLevel(Direction side) {
        return side != getOppositeDirection() && getActive() && isEmitting ? 15 : 0;
    }

    private long getFreqStored() {
        return getStored(getQIOFrequency());
    }

    private long getStored(@Nullable QIOFrequency freq) {
        if (freq == null || itemType == null) {
            return 0;
        } else if (fuzzy) {
            return freq.getTypesForItem(itemType.getItem()).stream().mapToLong(freq::getStored).sum();
        }
        return freq.getStored(itemType);
    }

    public void handleStackChange(ItemStack stack) {
        itemType = stack.isEmpty() ? null : HashedItem.create(stack);
        markForSave();
    }

    public void handleCountChange(long count) {
        if (this.count != count) {
            this.count = count;
            markForSave();
        }
    }

    public void toggleFuzzyMode() {
        setFuzzyMode(!fuzzy);
    }

    private void setFuzzyMode(boolean fuzzy) {
        if (this.fuzzy != fuzzy) {
            this.fuzzy = fuzzy;
            markForSave();
        }
    }

    public void invertSignal() {
        setSignalInverted(!inverted);
    }

    private void setSignalInverted(boolean inverted) {
        if (this.inverted != inverted) {
            this.inverted = inverted;
            markForSave();
        }
    }

    @Override
    protected boolean onUpdateServer(@Nullable QIOFrequency frequency) {
        boolean needsUpdate = super.onUpdateServer(frequency);
        long stored = getStored(frequency);
        boolean hasStored = stored > 0 && stored >= count;
        boolean shouldEmit = hasStored != inverted;
        if (isEmitting != shouldEmit) {
            isEmitting = shouldEmit;
            needsUpdate = true;
        }
        return needsUpdate;
    }

    @Override
    public void writeSustainedData(CompoundTag dataMap) {
        super.writeSustainedData(dataMap);
        if (itemType != null) {
            dataMap.put(NBTConstants.SINGLE_ITEM, itemType.internalToNBT());
        }
        dataMap.putLong(NBTConstants.AMOUNT, count);
        dataMap.putBoolean(NBTConstants.FUZZY_MODE, fuzzy);
        dataMap.putBoolean(NBTConstants.INVERSE, inverted);
    }

    @Override
    public void readSustainedData(CompoundTag dataMap) {
        super.readSustainedData(dataMap);
        NBTUtils.setItemStackIfPresent(dataMap, NBTConstants.SINGLE_ITEM, item -> itemType = HashedItem.create(item));
        NBTUtils.setLongIfPresent(dataMap, NBTConstants.AMOUNT, value -> count = value);
        NBTUtils.setBooleanIfPresent(dataMap, NBTConstants.FUZZY_MODE, value -> fuzzy = value);
        NBTUtils.setBooleanIfPresent(dataMap, NBTConstants.INVERSE, value -> inverted = value);
    }

    @NotNull
    @Override
    public ModelData getModelData() {
        if (isEmitting) {
            return ModelData.builder().with(DataBasedModelLoader.EMITTING, null).build();
        }
        return super.getModelData();
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        updateTag.putBoolean(NBTConstants.EMITTING, isEmitting);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        super.handleUpdateTag(tag);
        boolean emitting = tag.getBoolean(NBTConstants.EMITTING);
        if (isEmitting != emitting) {
            isEmitting = emitting;
            updateModelData();
        }
    }

    @Override
    public Map<String, Holder<AttachmentType<?>>> getTileDataAttachmentRemap() {
        Map<String, Holder<AttachmentType<?>>> remap = super.getTileDataAttachmentRemap();
        remap.put(NBTConstants.SINGLE_ITEM, MekanismAttachmentTypes.ITEM_TARGET);
        remap.put(NBTConstants.AMOUNT, MekanismAttachmentTypes.LONG_AMOUNT);
        remap.put(NBTConstants.FUZZY_MODE, MekanismAttachmentTypes.FUZZY);
        remap.put(NBTConstants.INVERSE, MekanismAttachmentTypes.INVERSE);
        return remap;
    }

    @Override
    public void writeToStack(ItemStack stack) {
        super.writeToStack(stack);
        if (itemType != null) {
            stack.setData(MekanismAttachmentTypes.ITEM_TARGET, itemType.getInternalStack());
        }
        stack.setData(MekanismAttachmentTypes.LONG_AMOUNT, count);
        stack.setData(MekanismAttachmentTypes.FUZZY, fuzzy);
        stack.setData(MekanismAttachmentTypes.INVERSE, inverted);
    }

    @Override
    public void readFromStack(ItemStack stack) {
        super.readFromStack(stack);
        itemType = stack.getExistingData(MekanismAttachmentTypes.ITEM_TARGET)
              .filter(type -> !type.isEmpty())
              .map(HashedItem::create)
              .orElse(null);
        count = stack.getData(MekanismAttachmentTypes.LONG_AMOUNT);
        fuzzy = stack.getData(MekanismAttachmentTypes.FUZZY);
        inverted = stack.getData(MekanismAttachmentTypes.INVERSE);
    }

    @ComputerMethod(nameOverride = "getTargetItem")
    public ItemStack getItemType() {
        return itemType == null ? ItemStack.EMPTY : itemType.getInternalStack();
    }

    @ComputerMethod(nameOverride = "getTriggerAmount")
    public long getCount() {
        return count;
    }

    @ComputerMethod
    public boolean getFuzzyMode() {
        return fuzzy;
    }

    @ComputerMethod
    public boolean isInverted() {
        return inverted;
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
        container.track(SyncableBoolean.create(this::getFuzzyMode, value -> fuzzy = value));
        container.track(SyncableBoolean.create(this::isInverted, value -> inverted = value));
        container.track(SyncableLong.create(this::getFreqStored, value -> clientStoredCount = value));
    }

    //Methods relating to IComputerTile
    @ComputerMethod(requiresPublicSecurity = true)
    void clearTargetItem() throws ComputerException {
        validateSecurityIsPublic();
        handleStackChange(ItemStack.EMPTY);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setTargetItem(ResourceLocation itemName) throws ComputerException {
        validateSecurityIsPublic();
        Item item = BuiltInRegistries.ITEM.get(itemName);
        if (item == Items.AIR) {
            throw new ComputerException("Target item '%s' could not be found. If you are trying to clear it consider using clearTargetItem instead.", itemName);
        }
        handleStackChange(new ItemStack(item));
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setTriggerAmount(long amount) throws ComputerException {
        validateSecurityIsPublic();
        if (amount < 0) {
            throw new ComputerException("Trigger amount cannot be negative. Received: %d", amount);
        }
        handleCountChange(amount);
    }

    @ComputerMethod(nameOverride = "toggleFuzzyMode", requiresPublicSecurity = true)
    void computerToggleFuzzyMode() throws ComputerException {
        validateSecurityIsPublic();
        toggleFuzzyMode();
    }

    @ComputerMethod(nameOverride = "setFuzzyMode", requiresPublicSecurity = true)
    void computerSetFuzzyMode(boolean fuzzy) throws ComputerException {
        validateSecurityIsPublic();
        setFuzzyMode(fuzzy);
    }

    @ComputerMethod(nameOverride = "invertSignal", requiresPublicSecurity = true)
    void computerInvertSignal() throws ComputerException {
        validateSecurityIsPublic();
        invertSignal();
    }

    @ComputerMethod(nameOverride = "setSignalInverted", requiresPublicSecurity = true)
    void computerSetSignalInverted(boolean inverted) throws ComputerException {
        validateSecurityIsPublic();
        setSignalInverted(inverted);
    }
    //End methods IComputerTile
}
