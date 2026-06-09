package mekanism.common.tile.qio;

import java.util.Optional;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.container.sync.SyncableResource;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.redstone.Orientation.SideBias;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class TileEntityQIORedstoneAdapter extends TileEntityQIOComponent {

    public static final ModelProperty<Boolean> EMITTING = new ModelProperty<>();

    private ItemResource itemType = ItemResource.EMPTY;
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
        if (freq == null || itemType.isEmpty()) {
            return 0;
        } else if (fuzzy) {
            return freq.getTypesForItem(itemType.getItem()).stream().mapToLong(freq::getStored).sum();
        }
        return freq.getStored(itemType);
    }

    public void handleStackChange(ItemStack stack) {
        itemType = ItemResource.of(stack);
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
    protected boolean onUpdateServer(ServerLevel level, @Nullable QIOFrequency frequency) {
        boolean needsUpdate = super.onUpdateServer(level, frequency);
        long stored = getStored(frequency);
        boolean hasStored = stored > 0 && stored >= count;
        boolean shouldEmit = hasStored != inverted;
        if (isEmitting != shouldEmit) {
            isEmitting = shouldEmit;
            needsUpdate = true;
            //Update redstone on sides except the back
            //TODO - 26.1 check Orientation
            Direction frontDirection = getDirection();
            level.updateNeighborsAtExceptFromFacing(getBlockPos(), getBlockState().getBlock(), getOppositeDirection(), Orientation.of(RelativeSide.TOP.getDirection(frontDirection), frontDirection, SideBias.LEFT));
        }
        return needsUpdate;
    }

    @Override
    public void writeSustainedData(ValueOutput output) {
        super.writeSustainedData(output);
        if (!itemType.isEmpty()) {
            output.store(SerializationConstants.SINGLE_ITEM, ItemResource.CODEC, itemType);
        }
        output.putLong(SerializationConstants.AMOUNT, count);
        output.putBoolean(SerializationConstants.FUZZY, fuzzy);
        output.putBoolean(SerializationConstants.INVERSE, inverted);
    }

    @Override
    public void readSustainedData(ValueInput input) {
        super.readSustainedData(input);
        itemType = input.read(SerializationConstants.SINGLE_ITEM, ItemResource.CODEC).orElse(ItemResource.EMPTY);
        count = input.getLongOr(SerializationConstants.AMOUNT, count);
        fuzzy = input.getBooleanOr(SerializationConstants.FUZZY, fuzzy);
        inverted = input.getBooleanOr(SerializationConstants.INVERSE, inverted);
    }

    @Override
    public ModelData getModelData() {
        return ModelData.of(EMITTING, this.isEmitting);
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        output.putBoolean(SerializationConstants.EMITTING, isEmitting);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);
        boolean emitting = input.getBooleanOr(SerializationConstants.EMITTING, isEmitting);
        if (isEmitting != emitting) {
            isEmitting = emitting;
            updateModelData();
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.ITEM_TARGET, itemType);
        builder.set(MekanismDataComponents.LONG_AMOUNT, count);
        builder.set(MekanismDataComponents.FUZZY, fuzzy);
        builder.set(MekanismDataComponents.INVERSE, inverted);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        itemType = input.getOrDefault(MekanismDataComponents.ITEM_TARGET, ItemResource.EMPTY);
        count = input.getOrDefault(MekanismDataComponents.LONG_AMOUNT, count);
        fuzzy = input.getOrDefault(MekanismDataComponents.FUZZY, fuzzy);
        inverted = input.getOrDefault(MekanismDataComponents.INVERSE, inverted);
    }

    @ComputerMethod(nameOverride = "getTargetItem")
    public ItemResource getItemType() {
        return itemType;
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
        container.track(SyncableResource.createItem(this::getItemType, value -> itemType = value));
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
    void setTargetItem(Identifier itemName) throws ComputerException {
        validateSecurityIsPublic();
        Optional<Holder.Reference<Item>> item = BuiltInRegistries.ITEM.get(itemName);
        if (item.isEmpty() || item.get().value() == Items.AIR) {
            throw new ComputerException("Target item '%s' could not be found. If you are trying to clear it consider using clearTargetItem instead.", itemName);
        }
        handleStackChange(new ItemStack(item.get()));
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
