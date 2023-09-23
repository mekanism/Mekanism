package mekanism.common.tile.qio;

import java.util.Map;
import mekanism.api.NBTConstants;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIORedstoneAdapter extends TileEntityQIOComponent {

    @Nullable
    private HashedItem itemType = null;
    private boolean fuzzy;
    private long count = 0;
    private long clientStoredCount = 0;

    public TileEntityQIORedstoneAdapter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QIO_REDSTONE_ADAPTER, pos, state);
        delaySupplier = NO_DELAY;
    }

    public int getRedstoneLevel(Direction side) {
        return side != getOppositeDirection() && getActive() ? 15 : 0;
    }

    private long getFreqStored() {
        QIOFrequency freq = getQIOFrequency();
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

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        long stored = getFreqStored();
        setActive(stored > 0 && stored >= count);
    }

    @Override
    public void writeSustainedData(CompoundTag dataMap) {
        super.writeSustainedData(dataMap);
        if (itemType != null) {
            dataMap.put(NBTConstants.SINGLE_ITEM, itemType.internalToNBT());
        }
        dataMap.putLong(NBTConstants.AMOUNT, count);
        dataMap.putBoolean(NBTConstants.FUZZY_MODE, fuzzy);
    }

    @Override
    public void readSustainedData(CompoundTag dataMap) {
        super.readSustainedData(dataMap);
        NBTUtils.setItemStackIfPresent(dataMap, NBTConstants.SINGLE_ITEM, item -> itemType = HashedItem.create(item));
        NBTUtils.setLongIfPresent(dataMap, NBTConstants.AMOUNT, value -> count = value);
        NBTUtils.setBooleanIfPresent(dataMap, NBTConstants.FUZZY_MODE, value -> fuzzy = value);
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = super.getTileDataRemap();
        remap.put(NBTConstants.SINGLE_ITEM, NBTConstants.SINGLE_ITEM);
        remap.put(NBTConstants.AMOUNT, NBTConstants.AMOUNT);
        remap.put(NBTConstants.FUZZY_MODE, NBTConstants.FUZZY_MODE);
        return remap;
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
        Item item = ForgeRegistries.ITEMS.getValue(itemName);
        if (item == null || item == Items.AIR) {
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
    //End methods IComputerTile
}
