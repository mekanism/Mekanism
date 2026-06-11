package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.UUID;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOCraftingTransferHelper.SingularItemTypeSource;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.slot.TransactionalSlot;
import mekanism.common.network.to_server.qio.PacketQIOFillCraftingWindow;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/// Used for the server side transfer handling by the [PacketQIOFillCraftingWindow]
public class QIOServerCraftingTransferHandler {

    private static final int MAX_NEEDED = QIOCraftingWindow.SLOTS_PER_WINDOW * Item.ABSOLUTE_MAX_STACK_SIZE;

    private final QIOItemViewerContainer container;
    private final QIOCraftingWindow craftingWindow;
    private final Identifier recipeID;
    private final Player player;
    @Nullable
    private final QIOFrequency frequency;
    private final boolean rejectToInventory;

    public static void tryTransfer(QIOItemViewerContainer container, byte selectedCraftingGrid, boolean rejectToInventory, Player player, Identifier recipeID,
          CraftingRecipe recipe, Byte2ObjectMap<List<SingularItemTypeSource>> sources) {
        QIOServerCraftingTransferHandler transferHandler = new QIOServerCraftingTransferHandler(container, selectedCraftingGrid, rejectToInventory, player, recipeID);
        try (Transaction transaction = Transaction.openRoot()) {
            if (transferHandler.transferItems(recipe, sources, transaction)) {
                transaction.commit();
            }
        }
    }

    private QIOServerCraftingTransferHandler(QIOItemViewerContainer container, byte selectedCraftingGrid, boolean rejectToInventory, Player player, Identifier recipeID) {
        this.container = container;
        this.player = player;
        this.recipeID = recipeID;
        this.frequency = container.getFrequency();
        this.rejectToInventory = rejectToInventory;
        this.craftingWindow = container.getCraftingWindow(selectedCraftingGrid);
    }

    private boolean transferItems(CraftingRecipe recipe, Byte2ObjectMap<List<SingularItemTypeSource>> sources, TransactionContext transaction) {
        SelectedWindowData windowData = craftingWindow.getWindowData();
        Object2IntMap<ItemResource> currentlyShuffling = new Object2IntOpenHashMap<>();
        //Extract items that will be put into the crafting window
        Byte2ObjectMap<ItemStack> targetContents = new Byte2ObjectArrayMap<>(sources.size());
        for (ObjectIterator<Byte2ObjectMap.Entry<List<SingularItemTypeSource>>> iterator = Byte2ObjectMaps.fastIterator(sources); iterator.hasNext(); ) {
            Byte2ObjectMap.Entry<List<SingularItemTypeSource>> entry = iterator.next();
            for (SingularItemTypeSource source : entry.getValue()) {
                byte slot = source.getSlot();
                int toUse = source.getUsed();
                if (toUse == 0) {
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, that had empty usage quantities.", player, recipeID);
                    return false;
                } else if (toUse > MAX_NEEDED) {
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, that had requested a single transfer to have: {}, but the max that could ever be needed is: {}",
                          player, recipeID, toUse, MAX_NEEDED);
                    return false;
                }
                try (Transaction subTransaction = Transaction.open(transaction)) {
                    ItemResource itemType;
                    int amountExtracted;
                    if (slot == -1) {
                        if (frequency == null) {
                            Mekanism.logger.warn("Received transfer request from: {}, for: {}, for items from the current frequency, but no frequency was set.", player, recipeID);
                            return false;
                        }
                        UUID qioSource = source.getQioSource();
                        itemType = QIOGlobalItemLookup.instance().getTypeByUUID(qioSource);
                        if (itemType.isEmpty()) {
                            Mekanism.logger.warn("Received transfer request from: {}, for: {}, for item with unknown UUID: {}.", player, recipeID, qioSource);
                            return false;
                        }
                        amountExtracted = frequency.removeByType(itemType, toUse, subTransaction);
                        if (amountExtracted == 0) {
                            Mekanism.logger.warn("Received transfer request from: {}, for: {}, could not find or extract stored item ({}) with UUID: {}. " +
                                                 "This likely means that more of it was requested than is stored.", player, recipeID, itemType, qioSource);
                            return false;
                        } else if (amountExtracted < toUse) {
                            Mekanism.logger.warn("Received transfer request from: {}, for: {}, but was unable to extract the expected amount: {} of item: {} from the QIO. "
                                                 + "Attempting to continue anyway with the actual extracted amount of {}.", player, recipeID, toUse, itemType, amountExtracted);
                        }
                    } else {
                        int actualSlot;
                        String slotType;
                        if (slot < QIOCraftingWindow.SLOTS_PER_WINDOW) {//Crafting Window
                            actualSlot = slot;
                            slotType = "crafting window";
                            IInventorySlot inputSlot = craftingWindow.getInputSlot(slot);
                            itemType = inputSlot.resource();
                            amountExtracted = itemType.isEmpty() ? 0 : inputSlot.extract(itemType, toUse, subTransaction, AutomationType.MANUAL);
                        } else {
                            actualSlot = slot - QIOCraftingWindow.SLOTS_PER_WINDOW;
                            slotType = "player inventory";
                            TransactionalSlot transactionalSlot = container.getPlayerSlot(actualSlot);
                            itemType = ItemResource.of(transactionalSlot.getItem());
                            amountExtracted = itemType.isEmpty() ? 0 : transactionalSlot.extract(player, itemType, toUse, subTransaction);
                        }
                        if (amountExtracted == 0) {
                            Mekanism.logger.warn("Received transfer request from: {}, for: {}, could not extract item from {} slot: {}. "
                                                 + "This likely means that more of it was requested than is stored.", player, recipeID, slotType, actualSlot);
                            return false;
                        } else if (amountExtracted < toUse) {
                            Mekanism.logger.warn("Received transfer request from: {}, for: {}, but was unable to extract the expected amount: {} from {} slot: {}. "
                                                 + "Attempting to continue anyway with the actual extracted amount of {}.", player, recipeID, toUse, slotType, actualSlot, amountExtracted);
                        }
                    }
                    //Add the item to the list of things we are planning to insert into the crafting window
                    byte targetSlot = entry.getByteKey();
                    if (targetContents.containsKey(targetSlot)) {
                        ItemStack existing = targetContents.get(targetSlot);
                        if (!itemType.matches(existing)) {
                            Mekanism.logger.warn("Received transfer request from: {}, for: {}, but contents could not stack into target slot: {}. "
                                                 + "This should not be able to happen, returning extra stack, and attempting to continue.", player, recipeID, targetSlot);
                            //Skip committing the sub transaction so that the stack goes back to where we took it from
                            continue;
                        }
                        int needed = existing.getMaxStackSize() - existing.count();
                        if (amountExtracted <= needed) {
                            existing.grow(amountExtracted);
                        } else {
                            existing.grow(needed);
                            Mekanism.logger.warn("Received transfer request from: {}, for: {}, but contents could not fully fit into target slot: {}. "
                                                 + "This should not be able to happen, attempting to shuffle excess stack and continue.", player, recipeID, targetSlot);
                            //Mark whatever excess didn't fit in the existing stack as currently being shuffled
                            currentlyShuffling.mergeInt(itemType, amountExtracted - needed, Integer::sum);
                        }
                    } else {
                        targetContents.put(targetSlot, itemType.toStack(amountExtracted));
                    }
                    subTransaction.commit();
                }
            }
        }
        //Extract what items are still in the window and insert the desired items
        for (byte slot = 0; slot < QIOCraftingWindow.SLOTS_PER_WINDOW; slot++) {
            //TODO: Eventually it would be nice if we added in some support so that if an item is staying put in its crafting slot
            // we don't actually need to do any validation of if it can be extracted from when it will just end up in the same spot anyway
            // but for now this isn't that major of a concern as our slots don't actually have any restrictions on them in regards to extracting
            IInventorySlot inputSlot = craftingWindow.getInputSlot(slot);
            if (!inputSlot.isEmpty()) {
                ItemResource resource = inputSlot.resource();
                int stored = inputSlot.amountAsInt();
                if (inputSlot.extract(resource, stored, transaction, AutomationType.MANUAL) < stored || !inputSlot.isEmpty()) {
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, and was unable to extract all items from crafting input slot: {}.", player, recipeID, slot);
                    return false;
                }
                currentlyShuffling.mergeInt(resource, stored, Integer::sum);
            }
            //Insert items that we want to end up in this crafting window slot into it
            ItemStack stack = targetContents.getOrDefault(slot, ItemStack.EMPTY);
            if (!stack.isEmpty()) {
                ItemResource resource = ItemResource.of(stack);
                int amountToInsert = stack.count();
                int inserted = inputSlot.insert(resource, amountToInsert, transaction, AutomationType.MANUAL);
                if (inserted < amountToInsert) {
                    //If it was not fully inserted, add it to the map of items that we are currently shuffling and will try to find homes for
                    currentlyShuffling.mergeInt(resource, amountToInsert - inserted, Integer::sum);
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, but was unable to fully insert it into crafting input slot: {}. Attempting to continue.",
                          player, recipeID, slot);
                }
            }
        }
        //Put the items that were in the crafting window in the player's inventory
        Iterable<TransactionalSlot> playerInv = container.getPlayerSlots();
        for (ObjectIterator<Object2IntMap.Entry<ItemResource>> iterator = Object2IntMaps.fastIterator(currentlyShuffling); iterator.hasNext(); ) {
            Object2IntMap.Entry<ItemResource> entry = iterator.next();
            ItemResource itemType = entry.getKey();
            int amountToInsert = entry.getIntValue();
            if (rejectToInventory) {
                //If we prioritize inserting back into the player's inventory, start by doing so
                amountToInsert -= MekanismContainer.insertItem(playerInv, itemType, amountToInsert, transaction, windowData);
                if (amountToInsert == 0) {
                    continue;//If we inserted everything skip to the next item
                }
            }
            //If we couldn't insert it all, try recombining with the slots in the crafting window (only if the type matches though)
            for (byte slot = 0; slot < QIOCraftingWindow.SLOTS_PER_WINDOW; slot++) {
                IInventorySlot inputSlot = craftingWindow.getInputSlot(slot);
                if (itemType.equals(inputSlot.resource())) {
                    amountToInsert -= inputSlot.insert(itemType, amountToInsert, transaction, AutomationType.MANUAL);
                    if (amountToInsert == 0) {
                        break;
                    }
                }
            }
            if (amountToInsert == 0) {
                continue;//If we inserted everything skip to the next item
            } else if (frequency != null) {
                //If we couldn't insert all of it, then try to put the remaining items in the frequency
                amountToInsert -= frequency.addItem(itemType, amountToInsert, transaction);
                if (amountToInsert == 0) {
                    continue;//If we inserted everything skip to the next item
                }
            }
            if (!rejectToInventory) {
                //If we didn't already try to insert it into the player's inventory, then try to do so
                amountToInsert -= MekanismContainer.insertItem(playerInv, itemType, amountToInsert, transaction, windowData);
            }
            if (amountToInsert > 0) {
                //If we couldn't insert it all, either because there was no frequency or it didn't have room for it all,
                // print a warning and abort the transfer
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, but was unable to shuffle items around. Initially targeting the player's inventory: {}, aborting transfer.",
                      player, recipeID, rejectToInventory);
                return false;
            }
        }
        if (!recipe.matches(craftingWindow.asCraftingInput().input(), player.level())) {
            //Double-check the recipe sent is valid for the stored items
            Mekanism.logger.warn("Received transfer request from: {}, but source items aren't valid for the requested recipe: {}.", player, recipeID);
            return false;
        }
        return true;
    }
}