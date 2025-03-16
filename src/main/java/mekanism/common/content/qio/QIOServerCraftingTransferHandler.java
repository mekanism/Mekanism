package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMaps;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOCraftingTransferHelper.BaseSimulatedInventory;
import mekanism.common.content.qio.QIOCraftingTransferHelper.SingularHashedItemSource;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.InsertableSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.to_server.qio.PacketQIOFillCraftingWindow;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.Nullable;

/**
 * Used for the server side transfer handling by the {@link PacketQIOFillCraftingWindow}
 */
public class QIOServerCraftingTransferHandler {

    private final QIOCraftingWindow craftingWindow;
    private final ResourceLocation recipeID;
    private final Player player;
    @Nullable
    private final QIOFrequency frequency;
    private final boolean rejectToInventory;
    private final List<HotBarSlot> hotBarSlots;
    private final List<MainInventorySlot> mainInventorySlots;

    private final Byte2ObjectMap<SlotData> availableItems = new Byte2ObjectOpenHashMap<>();
    private final Map<UUID, FrequencySlotData> frequencyAvailableItems = new HashMap<>();
    private final NonNullList<ItemStack> recipeToTest = NonNullList.withSize(9, ItemStack.EMPTY);

    public static void tryTransfer(QIOItemViewerContainer container, byte selectedCraftingGrid, boolean rejectToInventory, Player player, ResourceLocation recipeID,
          CraftingRecipe recipe, Byte2ObjectMap<List<SingularHashedItemSource>> sources) {
        QIOServerCraftingTransferHandler transferHandler = new QIOServerCraftingTransferHandler(container, selectedCraftingGrid, rejectToInventory, player, recipeID);
        transferHandler.tryTransfer(recipe, sources);
    }

    private QIOServerCraftingTransferHandler(QIOItemViewerContainer container, byte selectedCraftingGrid, boolean rejectToInventory, Player player, ResourceLocation recipeID) {
        this.player = player;
        this.recipeID = recipeID;
        this.frequency = container.getFrequency();
        this.rejectToInventory = rejectToInventory;
        this.craftingWindow = container.getCraftingWindow(selectedCraftingGrid);
        this.hotBarSlots = container.getHotBarSlots();
        this.mainInventorySlots = container.getMainInventorySlots();
    }

    private void tryTransfer(CraftingRecipe recipe, Byte2ObjectMap<List<SingularHashedItemSource>> sources) {
        //Calculate what items are available inside the crafting window and if they can be extracted as we will
        // need to be able to extract the contents afterwards anyway
        for (byte slot = 0; slot < 9; slot++) {
            IInventorySlot inputSlot = craftingWindow.getInputSlot(slot);
            if (!inputSlot.isEmpty()) {
                ItemStack available = inputSlot.extractItem(inputSlot.getCount(), Action.SIMULATE, AutomationType.INTERNAL);
                if (available.getCount() < inputSlot.getCount()) {
                    //TODO: Eventually it would be nice if we added in some support so that if an item is staying put in its crafting slot
                    // we don't actually need to do any validation of if it can be extracted from when it will just end up in the same spot anyways
                    // but for now this isn't that major of a concern as our slots don't actually have any restrictions on them in regards to extracting
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, and was unable to extract all items from crafting input slot: {}.",
                          player, recipeID, slot);
                    return;
                }
                availableItems.put(slot, new SlotData(available));
            }
        }
        for (ObjectIterator<Byte2ObjectMap.Entry<List<SingularHashedItemSource>>> iterator = Byte2ObjectMaps.fastIterator(sources); iterator.hasNext(); ) {
            Byte2ObjectMap.Entry<List<SingularHashedItemSource>> entry = iterator.next();
            byte targetSlot = entry.getByteKey();
            if (targetSlot < 0 || targetSlot >= 9) {
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, with an invalid target slot id: {}.", player, recipeID, targetSlot);
                return;
            }
            int stackSize = 0;
            List<SingularHashedItemSource> singleSources = entry.getValue();
            for (Iterator<SingularHashedItemSource> iter = singleSources.iterator(); iter.hasNext(); ) {
                SingularHashedItemSource source = iter.next();
                byte slot = source.getSlot();
                int used;
                if (slot == -1) {
                    used = simulateQIOSource(targetSlot, source.getQioSource(), source.getUsed(), stackSize);
                } else {
                    used = simulateSlotSource(targetSlot, slot, source.getUsed(), stackSize);
                }
                if (used == -1) {
                    //Error occurred and was logged, exit
                    return;
                } else if (used == 0) {
                    //Unable to use any of this source due to it not stacking with an earlier one for example
                    // remove this source
                    iter.remove();
                } else {
                    if (used < source.getUsed()) {
                        //If we used less than we were expected to (most likely due to stack sizes) then we need
                        // to decrease the amount of the source being used
                        source.setUsed(used);
                    }
                    stackSize += used;
                }
            }
            if (singleSources.isEmpty()) {
                //There should always be at least one (the first source) that didn't get removed, but in case something went wrong,
                // and it got removed anyway, then we catch it here and fail
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, that had no valid sources, this should not be possible.", player, recipeID);
                return;
            }
            ItemStack resultItem = recipeToTest.get(targetSlot);
            if (!resultItem.isEmpty() && resultItem.getMaxStackSize() < stackSize) {
                //Note: This should never happen as if it would happen it should be caught in the above simulation and have the amount used reduced to not happen
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, that tried to transfer more items into: {} than can stack ({}) in one slot.",
                      player, recipeID, targetSlot, resultItem.getMaxStackSize());
                return;
            }
        }
        CraftingInput dummy = MekanismUtils.getCraftingInput(3, 3, recipeToTest, true).input();
        if (!recipe.matches(dummy, player.level())) {
            Mekanism.logger.warn("Received transfer request from: {}, but source items aren't valid for the requested recipe: {}.", player, recipeID);
        } else if (!hasRoomToShuffle()) {
            //Note: Uses debug logging level as there are a couple cases this might not be 100% accurate on the client side
            Mekanism.logger.debug("Received transfer request from: {}, but there is not enough room to shuffle items around for the requested recipe: {}.",
                  player, recipeID);
        } else {
            transferItems(sources);
        }
    }

    /**
     * Simulates transferring an item from the QIO into a recipe target slot
     *
     * @return {@code -1} if an error occurred, and we should bail, otherwise the amount that should be actually used.
     */
    private int simulateQIOSource(byte targetSlot, UUID qioSource, int used, int currentStackSize) {
        if (qioSource == null) {
            return fail("Received transfer request from: {}, for: {}, with no valid source.", player, recipeID);
        }
        FrequencySlotData slotData = frequencyAvailableItems.get(qioSource);
        if (slotData == null) {
            if (frequency == null) {
                return fail("Received transfer request from: {}, for: {}, with a QIO source but no selected frequency.", player, recipeID);
            }
            HashedItem storedItem = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(qioSource);
            if (storedItem == null) {
                return fail("Received transfer request from: {}, for: {}, for item with unknown UUID: {}.", player, recipeID, qioSource);
            }
            long stored = frequency.getStoredByHash(storedItem);
            slotData = stored == 0 ? FrequencySlotData.EMPTY : new FrequencySlotData(storedItem, stored);
            frequencyAvailableItems.put(qioSource, slotData);
        }
        return addStackToRecipe(targetSlot, slotData, used, (byte) -1, currentStackSize);
    }

    /**
     * Simulates transferring an item from an inventory slot into a recipe target slot
     *
     * @return {@code -1} if an error occurred, and we should bail, otherwise the amount that should be actually used.
     */
    private int simulateSlotSource(byte targetSlot, byte slot, int used, int currentStackSize) {
        if (slot < 0 || slot >= 9 + Inventory.getSelectionSize() + 27) {
            return fail("Received transfer request from: {}, for: {}, with an invalid slot id: {}.", player, recipeID, slot);
        }
        SlotData slotData = availableItems.get(slot);
        if (slotData == null) {
            if (slot < 9) {
                //If our known available items don't contain the slot, and it is a crafting window slot,
                // fail as we already looked up all the items that we have available in the crafting slots
                return fail("Received transfer request from: {}, for: {}, with a request to take from crafting window slot: {}, but that slot cannot be taken from.",
                      player, recipeID, slot);
            }
            InsertableSlot inventorySlot;
            if (slot < 9 + Inventory.getSelectionSize()) {
                //Hotbar
                int actualSlot = slot - 9;
                if (actualSlot >= hotBarSlots.size()) {
                    //Something went wrong, shouldn't happen even with an invalid packet
                    return fail("Received transfer request from: {}, for: {}, could not find hotbar slot: {}.", player, recipeID, actualSlot);
                }
                inventorySlot = hotBarSlots.get(actualSlot);
                if (!inventorySlot.mayPickup(player)) {
                    return fail("Received transfer request from: {}, for: {}, with a request to take from hotbar slot: {}, but that slot cannot be taken from.",
                          player, recipeID, actualSlot);
                }
            } else {
                //Main inventory
                int actualSlot = slot - 9 - Inventory.getSelectionSize();
                if (actualSlot >= mainInventorySlots.size()) {
                    //Something went wrong, shouldn't happen even with an invalid packet
                    return fail("Received transfer request from: {}, for: {}, could not find main inventory slot: {}.", player, recipeID, actualSlot);
                }
                inventorySlot = mainInventorySlots.get(actualSlot);
                if (!inventorySlot.mayPickup(player)) {
                    return fail("Received transfer request from: {}, for: {}, with a request to take from main inventory slot: {}, but that slot cannot be taken from.",
                          player, recipeID, actualSlot);
                }
            }
            slotData = inventorySlot.hasItem() ? new SlotData(inventorySlot.getItem()) : SlotData.EMPTY;
            availableItems.put(slot, slotData);
        }
        return addStackToRecipe(targetSlot, slotData, used, slot, currentStackSize);
    }

    /**
     * Simulates transferring an item into a recipe target slot and adds it to the recipe in a given position.
     *
     * @return {@code -1} if an error occurred, and we should bail, otherwise the amount that should be actually used.
     */
    private int addStackToRecipe(byte targetSlot, ItemData slotData, int used, byte sourceSlot, int currentStackSize) {
        if (slotData.isEmpty()) {
            if (sourceSlot == -1) {
                return fail("Received transfer request from: {}, for: {}, for an item that isn't stored in the frequency.", player, recipeID);
            }
            return fail("Received transfer request from: {}, for: {}, for an empty slot: {}.", player, recipeID, sourceSlot);
        } else if (slotData.getAvailable() < used) {
            if (sourceSlot == -1) {
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, but the QIO frequency only had {} remaining items instead of the expected: {}. "
                                     + "Attempting to continue by only using the available number of items.", player, recipeID, slotData.getAvailable(), used);
            } else {
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, but slot: {} only had {} remaining items instead of the expected: {}. "
                                     + "Attempting to continue by only using the available number of items.", player, recipeID, sourceSlot, slotData.getAvailable(), used);
            }
            used = slotData.getAvailable();
        }
        ItemStack currentRecipeTarget = recipeToTest.get(targetSlot);
        ItemStack slotStack = slotData.getStack();
        if (currentRecipeTarget.isEmpty()) {
            int max = slotStack.getMaxStackSize();
            if (used > max) {
                //This should never happen unless the player has an oversized stack in their inventory
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, but the item being moved can only stack to: {} but a stack of size: {} was "
                                     + "being moved. Attempting to continue by only using as many items as can be stacked.", player, recipeID, max, used);
                used = max;
            }
            //We copy the stack in case any mods do dumb things in their recipes and would end up mutating our stacks that shouldn't be mutated by accident
            recipeToTest.set(targetSlot, slotStack.copy());
        } else if (!ItemStack.isSameItemSameComponents(currentRecipeTarget, slotStack)) {
            //If our stack can't stack with the item we already are going to put in the slot, fail "gracefully"
            //Note: debug level because this may happen due to not knowing all NBT
            Mekanism.logger.debug("Received transfer request from: {}, for: {}, but found items for target slot: {} cannot stack. "
                                  + "Attempting to continue by skipping the additional stack.", player, recipeID, targetSlot);
            return 0;
        } else {
            int max = currentRecipeTarget.getMaxStackSize();
            int needed = max - currentStackSize;
            if (used > needed) {
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, but moving the requested amount of: {} would cause the output stack to past "
                                     + "its max stack size ({}). Attempting to continue by only using as many items as can be stacked.", player, recipeID, used, max);
                used = needed;
            }
        }
        slotData.simulateUse(used);
        return used;
    }

    /**
     * @implNote As it simplifies the logic (and is what we had initially written), this simulates if we can shuffle with the player inventory before checking the
     * frequency. (I believe this is also more efficient than doing the simulated checks against the frequency)
     */
    private boolean hasRoomToShuffle() {
        //Map used to keep track of inputs while also merging identical inputs, so we can cut down
        // on how many times we have to check if things can stack
        Object2IntMap<HashedItem> leftOverInput = new Object2IntArrayMap<>(9);
        for (byte inputSlot = 0; inputSlot < 9; inputSlot++) {
            SlotData inputSlotData = availableItems.get(inputSlot);
            if (inputSlotData != null && inputSlotData.getAvailable() > 0) {
                //If there was an item in the slot and there still is we need to see if we have room for it anywhere
                //Note: We can just make the hashed item be raw as the stack does not get modified, and we don't persist this map
                leftOverInput.mergeInt(HashedItem.raw(inputSlotData.getStack()), inputSlotData.getAvailable(), Integer::sum);
            }
        }
        if (!leftOverInput.isEmpty()) {
            //If we have any leftover inputs in the crafting inventory, then get a simulated view of what the player's inventory
            // will look like after things are changed
            BaseSimulatedInventory simulatedInventory = new BaseSimulatedInventory(hotBarSlots, mainInventorySlots) {
                @Override
                protected int getRemaining(int slot, ItemStack currentStored) {
                    SlotData slotData = availableItems.get((byte) (slot + 9));
                    return slotData == null ? currentStored.getCount() : slotData.getAvailable();
                }
            };
            Object2IntMap<HashedItem> stillLeftOver = simulatedInventory.shuffleInputs(leftOverInput, frequency != null);
            if (stillLeftOver == null) {
                //If we have remaining items and no frequency then we don't have room to shuffle
                return false;
            }
            if (!stillLeftOver.isEmpty() && frequency != null) {
                //If we still have left over things try adding them to the frequency
                // Note: We validate the frequency is not null, even though it shouldn't be null if we have anything still left over
                //We start by doing a "precheck" to see if it is potentially even possible to fit the contents in based on type counts
                //Note: We calculate these numbers as a difference so that it is easier to make sure none of the numbers accidentally overflow
                int availableItemTypes = frequency.getTotalItemTypeCapacity() - frequency.getTotalItemTypes(false);
                long availableItemSpace = frequency.getTotalItemCountCapacity() - frequency.getTotalItemCount();
                for (FrequencySlotData slotData : frequencyAvailableItems.values()) {
                    //Free up however much space as we used of the item
                    availableItemSpace += slotData.getUsed();
                    if (slotData.getAvailable() == 0) {
                        //If we used all that is available, we need to also free up an item type
                        //Note: Given we can't use as much as a full integer as we have nine slots that stack up to a max of Item.ABSOLUTE_MAX_STACK_SIZE
                        // if we get down to zero then we know that we actually used it all, and it isn't just the case that we
                        // think we are at zero because of clamping a long to an int
                        availableItemTypes++;
                    }
                }
                for (ObjectIterator<Object2IntMap.Entry<HashedItem>> iterator = Object2IntMaps.fastIterator(stillLeftOver); iterator.hasNext(); ) {
                    Object2IntMap.Entry<HashedItem> entry = iterator.next();
                    availableItemSpace -= entry.getIntValue();
                    if (availableItemSpace <= 0) {
                        //No room for all our items, fail
                        return false;
                    }
                    if (frequency.isStoring(entry.getKey())) {
                        //It is stored, check to make sure it isn't a type we are removing fully
                        UUID uuid = QIOGlobalItemLookup.INSTANCE.getUUIDForType(entry.getKey());
                        if (uuid != null) {
                            FrequencySlotData slotData = frequencyAvailableItems.get(uuid);
                            if (slotData != null && slotData.getAvailable() == 0) {
                                // if it is, then we need to reclaim the item type as being available
                                availableItemTypes--;
                                if (availableItemTypes <= 0) {
                                    //Not enough room for types
                                    return false;
                                }
                            }
                        }
                    } else {
                        //It is not stored, we need to use an item type up
                        availableItemTypes--;
                        if (availableItemTypes <= 0) {
                            //Not enough room for types
                            return false;
                        }
                    }
                }
                Collection<QIODriveData> drives = frequency.getAllDrives();
                List<SimulatedQIODrive> simulatedDrives = new ArrayList<>(drives.size());
                for (QIODriveData drive : drives) {
                    simulatedDrives.add(new SimulatedQIODrive(drive));
                }
                for (Map.Entry<UUID, FrequencySlotData> entry : frequencyAvailableItems.entrySet()) {
                    FrequencySlotData slotData = entry.getValue();
                    HashedItem type = slotData.getType();
                    if (type != null) {
                        //If there is something actually stored in the frequency for this UUID, we need to try and remove it from our simulated drives
                        int toRemove = slotData.getUsed();
                        for (SimulatedQIODrive drive : simulatedDrives) {
                            toRemove = drive.remove(type, toRemove);
                            if (toRemove == 0) {
                                break;
                            }
                        }
                    }
                }
                for (ObjectIterator<Object2IntMap.Entry<HashedItem>> iterator = Object2IntMaps.fastIterator(stillLeftOver); iterator.hasNext(); ) {
                    Object2IntMap.Entry<HashedItem> entry = iterator.next();
                    HashedItem item = entry.getKey();
                    int toAdd = entry.getIntValue();
                    //Start by trying to add to ones it can stack with
                    for (SimulatedQIODrive drive : simulatedDrives) {
                        toAdd = drive.add(item, toAdd, true);
                        if (toAdd == 0) {
                            break;
                        }
                    }
                    //Note: Ideally the adding to empty slots would be done afterwards for keeping it as compact as possible
                    // but due to how our actual adding to the slots works, the way it is here ends up actually having a more
                    // accurate simulation result
                    if (toAdd > 0) {
                        //And then add to empty slots if we couldn't add it all in a way that stacks
                        for (SimulatedQIODrive drive : simulatedDrives) {
                            toAdd = drive.add(item, toAdd, false);
                            if (toAdd == 0) {
                                break;
                            }
                        }
                        if (toAdd > 0) {
                            //There are some items we can't fit anywhere, fail
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void transferItems(Byte2ObjectMap<List<SingularHashedItemSource>> sources) {
        SelectedWindowData windowData = craftingWindow.getWindowData();
        //Extract items that will be put into the crafting window
        Byte2ObjectMap<ItemStack> targetContents = new Byte2ObjectArrayMap<>(sources.size());
        for (ObjectIterator<Byte2ObjectMap.Entry<List<SingularHashedItemSource>>> iterator = Byte2ObjectMaps.fastIterator(sources); iterator.hasNext(); ) {
            Byte2ObjectMap.Entry<List<SingularHashedItemSource>> entry = iterator.next();
            for (SingularHashedItemSource source : entry.getValue()) {
                byte slot = source.getSlot();
                ItemStack stack;
                if (slot == -1) {
                    UUID qioSource = source.getQioSource();
                    //Neither the source nor the frequency can be null here as we validated that during simulation
                    HashedItem storedItem = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(qioSource);
                    if (storedItem == null) {
                        bail(targetContents, "Received transfer request from: {}, for: {}, for item with unknown UUID: {}.", player, recipeID, qioSource);
                        return;
                    } else if (!frequency.isStoring(storedItem)) {
                        bail(targetContents, "Received transfer request from: {}, for: {}, could not find stored item with UUID: {}. "
                                             + "This likely means that more of it was requested than is stored.", player, recipeID, qioSource);
                        return;
                    }
                    stack = frequency.removeByType(storedItem, source.getUsed());
                    if (stack.isEmpty()) {
                        bail(targetContents, "Received transfer request from: {}, for: {}, but could not extract item: {} from the QIO.",
                              player, recipeID, storedItem);
                        return;
                    } else if (stack.getCount() < source.getUsed()) {
                        Mekanism.logger.warn("Received transfer request from: {}, for: {}, but was unable to extract the expected amount: {} of item: {} from the QIO. "
                                             + "This should not be possible as it should have been caught during simulation. Attempting to continue anyways with the actual "
                                             + "extracted amount of {}.", player, recipeID, source.getUsed(), storedItem, stack.getCount());
                    }
                } else {
                    int actualSlot;
                    String slotType;
                    if (slot < 9) {//Crafting Window
                        actualSlot = slot;
                        slotType = "crafting window";
                        stack = craftingWindow.getInputSlot(slot).extractItem(source.getUsed(), Action.EXECUTE, AutomationType.MANUAL);
                    } else if (slot < 9 + Inventory.getSelectionSize()) {//Hotbar
                        actualSlot = slot - 9;
                        slotType = "hotbar";
                        stack = hotBarSlots.get(actualSlot).remove(source.getUsed());
                    } else {//Main inventory
                        actualSlot = slot - 9 - Inventory.getSelectionSize();
                        slotType = "main inventory";
                        stack = mainInventorySlots.get(actualSlot).remove(source.getUsed());
                    }
                    if (stack.isEmpty()) {
                        bail(targetContents, "Received transfer request from: {}, for: {}, could not extract item from {} slot: {}. "
                                             + "This likely means that more of it was requested than is stored.", player, recipeID, slotType, actualSlot);
                        return;
                    } else if (stack.getCount() < source.getUsed()) {
                        Mekanism.logger.warn("Received transfer request from: {}, for: {}, but was unable to extract the expected amount: {} from {} slot: {}. "
                                             + "This should not be possible as it should have been caught during simulation. Attempting to continue anyways with the "
                                             + "actual extracted amount of {}.", player, recipeID, source.getUsed(), slotType, actualSlot, stack.getCount());
                    }
                }
                byte targetSlot = entry.getByteKey();
                if (targetContents.containsKey(targetSlot)) {
                    ItemStack existing = targetContents.get(targetSlot);
                    if (ItemStack.isSameItemSameComponents(existing, stack)) {
                        int needed = existing.getMaxStackSize() - existing.getCount();
                        if (stack.getCount() <= needed) {
                            existing.grow(stack.getCount());
                        } else {
                            existing.grow(needed);
                            //Note: We can safely modify the stack as all our ways of extracting return a new stack
                            stack.shrink(needed);
                            Mekanism.logger.warn("Received transfer request from: {}, for: {}, but contents could not fully fit into target slot: {}. "
                                                 + "This should not be able to happen, returning excess stack, and attempting to continue.", player, recipeID, targetSlot);
                            returnItem(stack, windowData);
                        }
                    } else {
                        Mekanism.logger.warn("Received transfer request from: {}, for: {}, but contents could not stack into target slot: {}. "
                                             + "This should not be able to happen, returning extra stack, and attempting to continue.", player, recipeID, targetSlot);
                        returnItem(stack, windowData);
                    }
                } else {
                    //Note: We can safely modify the stack as all our ways of extracting return a new stack
                    targetContents.put(targetSlot, stack);
                }
            }
        }
        //Extract what items are still in the window
        Byte2ObjectMap<ItemStack> remainingCraftingGridContents = new Byte2ObjectArrayMap<>(9);
        for (byte slot = 0; slot < 9; slot++) {
            IInventorySlot inputSlot = craftingWindow.getInputSlot(slot);
            if (!inputSlot.isEmpty()) {
                ItemStack stack = inputSlot.extractItem(inputSlot.getCount(), Action.EXECUTE, AutomationType.MANUAL);
                if (!stack.isEmpty()) {
                    remainingCraftingGridContents.put(slot, stack);
                } else {
                    bail(targetContents, remainingCraftingGridContents, "Received transfer request from: {}, for: {}, but failed to remove items from crafting "
                                                                        + "input slot: {}. This should not be possible as it should have been caught by an earlier check.",
                          player, recipeID, slot);
                    return;
                }
            }
        }
        //Insert items for the crafting window into it
        for (ObjectIterator<Byte2ObjectMap.Entry<ItemStack>> iterator = Byte2ObjectMaps.fastIterator(targetContents); iterator.hasNext(); ) {
            Byte2ObjectMap.Entry<ItemStack> entry = iterator.next();
            byte targetSlot = entry.getByteKey();
            IInventorySlot inputSlot = craftingWindow.getInputSlot(targetSlot);
            ItemStack remainder = inputSlot.insertItem(entry.getValue(), Action.EXECUTE, AutomationType.MANUAL);
            if (remainder.isEmpty()) {
                //If it was fully inserted, remove the entry from what we have left to deal with
                iterator.remove();
            } else {
                // otherwise, update the stack for what is remaining and also print a warning as this should have been caught earlier,
                // as we then will handle any remaining contents at the end (though we shouldn't have any)
                // Note: We need to use put, as entry#setValue is not supported in fastutil maps
                targetContents.put(targetSlot, remainder);
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, but was unable to fully insert it into the {} crafting input slot. "
                                     + "This should not be possible as it should have been caught during simulation. Attempting to continue anyways.",
                      player, recipeID, targetSlot);
            }
        }
        //Put the items that were in the crafting window in the player's inventory
        for (ObjectIterator<Byte2ObjectMap.Entry<ItemStack>> iterator = Byte2ObjectMaps.fastIterator(remainingCraftingGridContents); iterator.hasNext(); ) {
            Byte2ObjectMap.Entry<ItemStack> entry = iterator.next();
            ItemStack stack = entry.getValue();
            if (rejectToInventory) {
                //If we prioritize inserting back into the player's inventory, start by doing so
                stack = returnItemToInventory(stack, windowData);
            }
            if (!stack.isEmpty()) {
                //If we couldn't insert it all, try recombining with the slots they were in the crafting window
                // (only if the type matches though)
                IInventorySlot inputSlot = craftingWindow.getInputSlot(entry.getByteKey());
                if (ItemStack.isSameItemSameComponents(inputSlot.getStack(), stack)) {
                    stack = inputSlot.insertItem(stack, Action.EXECUTE, AutomationType.MANUAL);
                }
                if (!stack.isEmpty()) {
                    //If we couldn't insert it, then try to put the remaining items in the frequency
                    if (frequency != null) {
                        stack = frequency.addItem(stack);
                    }
                    if (!rejectToInventory) {
                        //If we didn't already try to insert it into the player's inventory, then try to do so
                        stack = returnItemToInventory(stack, windowData);
                    }
                    if (!stack.isEmpty()) {
                        //If we couldn't insert it all, either because there was no frequency or it didn't have room for it all
                        // drop it as the player, and print a warning as ideally we should never have been able to get to this
                        // point as our simulation should have marked it as invalid
                        // Note: In theory we should never get to this point due to having accurate simulations ahead of time
                        player.drop(stack, false);
                        Mekanism.logger.warn("Received transfer request from: {}, for: {}, initially targeting the player's inventory: {}, and was unable to fit "
                                             + "all contents that were in the crafting window into the player's inventory/QIO system; dropping items by player.",
                              player, recipeID, rejectToInventory);
                    }
                }
            }
        }
        if (!targetContents.isEmpty()) {
            //If we have any contents we wanted to move remaining try to return them, in theory
            // this should never happen but in case it does make sure we don't void any items
            bail(targetContents, "Received transfer request from: {}, for: {}, but ended up with {} items that could not be transferred into "
                                 + "the proper crafting grid slot. This should not be possible as it should have been caught during simulation.", player, recipeID,
                  targetContents.size());
        }
    }

    /**
     * Bails out if something went horribly wrong and didn't get caught by simulations, and send the various items back to the inventory.
     */
    private void bail(Byte2ObjectMap<ItemStack> targetContents, String format, Object... args) {
        bail(targetContents, Byte2ObjectMaps.emptyMap(), format, args);
    }

    /**
     * Bails out if something went horribly wrong and didn't get caught by simulations, and send the various items back to the inventory.
     */
    private void bail(Byte2ObjectMap<ItemStack> targetContents, Byte2ObjectMap<ItemStack> remainingCraftingGridContents, String format, Object... args) {
        Mekanism.logger.warn(format, args);
        SelectedWindowData windowData = craftingWindow.getWindowData();
        for (ItemStack stack : targetContents.values()) {
            //We don't attempt to try and return the contents being moved to the crafting inventory to their original slots
            // as we don't keep track of that data and in theory unless something goes majorly wrong we should never end
            // up bailing anyways
            //TODO: Eventually we may want to try and make it first try to return to the same slots it came from but it doesn't matter that much
            returnItem(stack, windowData);
        }
        //Put the items that were in the crafting window in the player's inventory
        for (ObjectIterator<Byte2ObjectMap.Entry<ItemStack>> iterator = Byte2ObjectMaps.fastIterator(remainingCraftingGridContents); iterator.hasNext(); ) {
            Byte2ObjectMap.Entry<ItemStack> entry = iterator.next();
            ItemStack stack = entry.getValue();
            IInventorySlot inputSlot = craftingWindow.getInputSlot(entry.getByteKey());
            if (ItemStack.isSameItemSameComponents(inputSlot.getStack(), stack)) {
                stack = inputSlot.insertItem(stack, Action.EXECUTE, AutomationType.MANUAL);
                if (stack.isEmpty()) {
                    continue;
                }
            }
            returnItem(stack, windowData);
        }
    }

    /**
     * Tries to reinsert the stack into the player's inventory, and then if there is any remaining items tries to insert them into the frequency if there is one and if
     * not just drops them by the player.
     */
    private void returnItem(ItemStack stack, @Nullable SelectedWindowData windowData) {
        //Insert into player's inventory
        stack = returnItemToInventory(stack, windowData);
        if (!stack.isEmpty()) {
            //If we couldn't insert it, then try to put the remaining items in the frequency
            if (frequency != null) {
                stack = frequency.addItem(stack);
            }
            if (!stack.isEmpty()) {
                //If we couldn't insert it all, either because there was no frequency or it didn't have room for it all
                // drop it as the player, and print a warning as ideally we should never have been able to get to this
                // point as our simulation should have marked it as invalid
                player.drop(stack, false);
            }
        }
    }

    /**
     * Tries to reinsert the stack into the player's inventory in the order of hotbar, then main inventory; checks for stacks it can combine with before filling empty
     * ones.
     *
     * @return Remaining stack that couldn't be inserted.
     */
    private ItemStack returnItemToInventory(ItemStack stack, @Nullable SelectedWindowData windowData) {
        stack = MekanismContainer.insertItem(hotBarSlots, stack, true, windowData);
        stack = MekanismContainer.insertItem(mainInventorySlots, stack, true, windowData);
        stack = MekanismContainer.insertItem(hotBarSlots, stack, false, windowData);
        return MekanismContainer.insertItem(mainInventorySlots, stack, false, windowData);
    }

    /**
     * Helper to combine a WARN level log message and returning {@code -1} to represent failure in methods that use this.
     *
     * @return {@code -1}
     */
    private int fail(String format, Object... args) {
        Mekanism.logger.warn(format, args);
        return -1;
    }

    private static class SimulatedQIODrive {

        /**
         * Pointer to the actual map from the real QIODrive. Do not modify this map, it is mainly to reduce the need for doing potentially massive map copies.
         */
        private final Object2LongMap<HashedItem> sourceItemMap;
        private Set<HashedItem> removedTypes;
        private int availableItemTypes;
        private long availableItemSpace;

        public SimulatedQIODrive(QIODriveData sourceDrive) {
            this.sourceItemMap = sourceDrive.getItemMap();
            this.availableItemSpace = sourceDrive.getCountCapacity() - sourceDrive.getTotalCount();
            this.availableItemTypes = sourceDrive.getTypeCapacity() - sourceDrive.getTotalTypes();
        }

        public int remove(HashedItem item, int count) {
            long stored = sourceItemMap.getOrDefault(item, 0);
            if (stored == 0) {
                return count;
            }
            if (stored <= count) {
                //If we have less stored then we are trying to remove
                if (removedTypes == null) {
                    removedTypes = new HashSet<>();
                }
                // remove the type, and refund the amount of space we get from removing it
                removedTypes.add(item);
                availableItemTypes++;
                availableItemSpace += stored;
                return count - (int) stored;
            }
            availableItemSpace += count;
            return 0;
        }

        public int add(HashedItem item, int count, boolean mustContain) {
            if (availableItemSpace == 0) {
                //No space, fail
                return count;
            }
            //Note: We don't need to accurately keep track of the item types we add as we only have it happening once,
            // and if we fill it up on the first go around then we would be skipping it from there being no space available
            boolean contains = sourceItemMap.containsKey(item) && (removedTypes == null || !removedTypes.contains(item));
            if (mustContain != contains) {
                //If we don't have the item and are only adding if we do, or vice versa, just return we didn't add anything
                return count;
            }
            if (!contains) {
                if (availableItemTypes == 0) {
                    //If we don't contain it and no space for new item types, fail
                    return count;
                }
                //If we don't contain it, we need to reduce our type count
                availableItemTypes--;
            }
            if (count < availableItemSpace) {
                //We can fit all of it
                availableItemSpace -= count;
                return 0;
            }
            //We can't fit it all so use up all the space we can
            count -= (int) availableItemSpace;
            availableItemSpace = 0;
            return count;
        }
    }

    private abstract static class ItemData {

        private int available;

        protected ItemData(int available) {
            this.available = available;
        }

        public abstract boolean isEmpty();

        public int getAvailable() {
            return available;
        }

        public void simulateUse(int used) {
            available -= used;
        }

        /**
         * @apiNote Don't mutate this stack
         */
        protected abstract ItemStack getStack();
    }

    private static class SlotData extends ItemData {

        public static final SlotData EMPTY = new SlotData(ItemStack.EMPTY, 0);

        /**
         * @apiNote Don't mutate this stack
         */
        private final ItemStack stack;

        public SlotData(ItemStack stack) {
            this(stack, stack.getCount());
        }

        protected SlotData(ItemStack stack, int available) {
            super(available);
            this.stack = stack;
        }

        @Override
        public boolean isEmpty() {
            return this == EMPTY || stack.isEmpty();
        }

        @Override
        public ItemStack getStack() {
            return stack;
        }
    }

    private static class FrequencySlotData extends ItemData {

        public static final FrequencySlotData EMPTY = new FrequencySlotData(null, 0);

        /**
         * @apiNote Don't mutate this stack
         */
        private final HashedItem type;
        private int used;

        public FrequencySlotData(HashedItem type, long stored) {
            //Clamp to int as with how many slots we are filling even though the frequency may have more than
            // a certain amount stored, we can never need that many for usage, so we can save some extra memory
            super(MathUtils.clampToInt(stored));
            this.type = type;
        }

        @Override
        public boolean isEmpty() {
            return this == EMPTY || type == null;
        }

        @Override
        public ItemStack getStack() {
            return type == null ? ItemStack.EMPTY : type.getInternalStack();
        }

        @Override
        public void simulateUse(int used) {
            super.simulateUse(used);
            this.used += used;
        }

        public int getUsed() {
            return used;
        }

        public HashedItem getType() {
            return type;
        }
    }
}