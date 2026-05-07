package mekanism.common.tile.qio;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.attachments.containers.item.AttachedItems;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowOutputInventorySlot;
import mekanism.common.network.to_client.qio.BulkQIOData;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIODashboard extends TileEntityQIOComponent implements IQIOCraftingWindowHolder {

    /**
     * @apiNote This is only not final for purposes of being able to assign it in presetVariables so that we can use it in getInitialInventory.
     */
    private QIOCraftingWindow[] craftingWindows;
    private boolean insertIntoFrequency = true;
    private boolean recipesChecked = false;

    public TileEntityQIODashboard(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QIO_DASHBOARD, pos, state);
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        craftingWindows = new QIOCraftingWindow[MAX_CRAFTING_WINDOWS];
        for (byte tableIndex = 0; tableIndex < craftingWindows.length; tableIndex++) {
            //Note: We don't bother passing a special listener as:
            // a. We don't support comparators
            // b. If we did it would be of items which this would already be
            craftingWindows[tableIndex] = new QIOCraftingWindow(this, tableIndex);
        }
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        //TODO - 1.18: Re-evaluate/make an improved performance ItemHandlerManager that uses this method
        // that is for read only slots instead of actually exposing slots to various sides
        InventorySlotHelper builder = InventorySlotHelper.readOnly();
        for (QIOCraftingWindow craftingWindow : craftingWindows) {
            for (int slot = 0; slot < 9; slot++) {
                builder.addSlot(craftingWindow.getInputSlot(slot));
            }
            builder.addSlot(craftingWindow.getOutputSlot());
        }
        return builder.build();
    }

    @Override
    public void applyInventorySlots(DataComponentGetter input, List<IInventorySlot> slots, AttachedItems attachedItems) {
        List<ItemStack> stacks = attachedItems.containers();
        int size = stacks.size();
        if (size == slots.size()) {
            for (int i = 0; i < size; i++) {
                IInventorySlot slot = slots.get(i);
                if (slot instanceof CraftingWindowOutputInventorySlot) {
                    slot.setEmpty();
                } else {
                    ItemStack stack = stacks.get(i);
                    ItemResource itemType = ItemResource.of(stack);
                    int amount = stack.count();
                    if (slot instanceof BasicInventorySlot basicSlot) {
                        basicSlot.setContentsUnchecked(itemType, amount);
                    } else {
                        slot.setContents(itemType, amount);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public AttachedItems collectInventorySlots(DataComponentMap.Builder builder, List<IInventorySlot> slots) {
        boolean hasNonEmpty = false;
        List<ItemStack> stacks = new ArrayList<>(slots.size());
        for (IInventorySlot slot : slots) {
            ItemStack stack;
            if (slot instanceof CraftingWindowOutputInventorySlot) {
                stack = ItemStack.EMPTY;
            } else {
                stack = slot.getResource().toStack(slot.amount());
            }
            stacks.add(stack);
            if (!stack.isEmpty()) {
                hasNonEmpty = true;
            }
        }
        return hasNonEmpty ? new AttachedItems(stacks) : null;
    }

    @Override
    protected boolean onUpdateServer(@Nullable QIOFrequency frequency) {
        boolean needsUpdate = super.onUpdateServer(frequency);
        if (CommonWorldTickHandler.flushTagAndRecipeCaches || !recipesChecked) {
            //If we need to update the recipes because of a reload or if we just haven't checked the recipes yet
            // after loading, as there was no world set yet, refresh the recipes
            recipesChecked = true;
            for (QIOCraftingWindow craftingWindow : craftingWindows) {
                craftingWindow.invalidateRecipe();
            }
        }
        return needsUpdate;
    }

    @Override
    public void encodeExtraContainerData(RegistryFriendlyByteBuf buffer) {
        super.encodeExtraContainerData(buffer);
        BulkQIOData.encodeToPacket(buffer, getFrequency());
    }

    @Override
    public QIOCraftingWindow[] getCraftingWindows() {
        return craftingWindows;
    }

    @Nullable
    @Override
    public QIOFrequency getFrequency() {
        return getQIOFrequency();
    }

    @Override
    public void writeSustainedData(@NotNull ValueOutput output) {
        super.writeSustainedData(output);
        output.putBoolean(SerializationConstants.INSERT_INTO_FREQUENCY, insertIntoFrequency);
    }

    @Override
    public void readSustainedData(@NotNull ValueInput input) {
        super.readSustainedData(input);
        insertIntoFrequency = input.getBooleanOr(SerializationConstants.INSERT_INTO_FREQUENCY, insertIntoFrequency);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.INSERT_INTO_FREQUENCY, insertIntoFrequency);
    }

    @Override
    protected void applyImplicitComponents(@NotNull DataComponentGetter input) {
        super.applyImplicitComponents(input);
        insertIntoFrequency = input.getOrDefault(MekanismDataComponents.INSERT_INTO_FREQUENCY, insertIntoFrequency);
    }

    public boolean shiftClickIntoFrequency() {
        return insertIntoFrequency;
    }

    public void toggleShiftClickDirection() {
        this.insertIntoFrequency = !insertIntoFrequency;
        markForSave();
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::shiftClickIntoFrequency, value -> insertIntoFrequency = value));
    }

    //Methods relating to IComputerTile
    private void validateWindow(int window) throws ComputerException {
        if (window < 0 || window >= craftingWindows.length) {
            throw new ComputerException("Window '%d' is out of bounds, must be between 0 and %d.", window, craftingWindows.length);
        }
    }

    //TODO - 26.1: Re-evaluate how we want to handle exposing this to computer integration
    //@WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getCraftingInput", docPlaceholder = "crafting input slot")
    IInventorySlot getCraftingInputSlot(int window, int slot) throws ComputerException {
        validateWindow(window);
        if (slot < 0 || slot >= 9) {
            throw new ComputerException("Slot '%d' is out of bounds, must be between 0 and 9.", slot);
        }
        return craftingWindows[window].getInputSlot(slot);
    }

    //TODO - 26.1: Re-evaluate how we want to handle exposing this to computer integration
    //@WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getCraftingOutput", docPlaceholder = "crafting output slot")
    IInventorySlot getCraftingOutputSlot(int window) throws ComputerException {
        validateWindow(window);
        return craftingWindows[window].getOutputSlot();
    }
    //End methods IComputerTile
}