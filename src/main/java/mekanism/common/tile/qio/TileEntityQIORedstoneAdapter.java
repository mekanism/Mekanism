package mekanism.common.tile.qio;

import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.client.model.data.DataBasedModelLoader;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
            return freq.getTypesForItem(itemType.getItem()).stream().mapToLong(freq::getStoredByHash).sum();
        }
        return freq.getStoredByHash(itemType);
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
            //Update redstone on sides except the back
            level.updateNeighborsAtExceptFromFacing(getBlockPos(), getBlockState().getBlock(), getOppositeDirection());
        }
        return needsUpdate;
    }

    @Override
    public void writeSustainedData(HolderLookup.Provider provider, CompoundTag dataMap) {
        super.writeSustainedData(provider, dataMap);
        if (itemType != null) {
            dataMap.put(SerializationConstants.SINGLE_ITEM, itemType.internalToNBT(provider));
        }
        dataMap.putLong(SerializationConstants.AMOUNT, count);
        dataMap.putBoolean(SerializationConstants.FUZZY, fuzzy);
        dataMap.putBoolean(SerializationConstants.INVERSE, inverted);
    }

    @Override
    public void readSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag dataMap) {
        super.readSustainedData(provider, dataMap);
        NBTUtils.setItemStackIfPresent(provider, dataMap, SerializationConstants.SINGLE_ITEM, item -> itemType = HashedItem.create(item));
        NBTUtils.setLongIfPresent(dataMap, SerializationConstants.AMOUNT, value -> count = value);
        NBTUtils.setBooleanIfPresent(dataMap, SerializationConstants.FUZZY, value -> fuzzy = value);
        NBTUtils.setBooleanIfPresent(dataMap, SerializationConstants.INVERSE, value -> inverted = value);
    }

    @NotNull
    @Override
    public ModelData getModelData() {
        if (isEmitting) {
            return ModelData.of(DataBasedModelLoader.EMITTING, null);
        }
        return super.getModelData();
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.putBoolean(SerializationConstants.EMITTING, isEmitting);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        boolean emitting = tag.getBoolean(SerializationConstants.EMITTING);
        if (isEmitting != emitting) {
            isEmitting = emitting;
            updateModelData();
        }
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.ITEM_TARGET, Optional.ofNullable(itemType));
        builder.set(MekanismDataComponents.LONG_AMOUNT, count);
        builder.set(MekanismDataComponents.FUZZY, fuzzy);
        builder.set(MekanismDataComponents.INVERSE, inverted);
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        itemType = input.getOrDefault(MekanismDataComponents.ITEM_TARGET, Optional.empty()).orElse(null);
        count = input.getOrDefault(MekanismDataComponents.LONG_AMOUNT, count);
        fuzzy = input.getOrDefault(MekanismDataComponents.FUZZY, fuzzy);
        inverted = input.getOrDefault(MekanismDataComponents.INVERSE, inverted);
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
