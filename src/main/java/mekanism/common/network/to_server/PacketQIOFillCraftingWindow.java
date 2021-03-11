package mekanism.common.network.to_server;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOCraftingTransferHelper.SingularHashedItemSource;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.ItemHandlerHelper;

public class PacketQIOFillCraftingWindow implements IMekanismPacket {

    private final Byte2ObjectMap<SingularHashedItemSource> sources;
    private final ResourceLocation recipeID;
    private final boolean maxTransfer;

    public PacketQIOFillCraftingWindow(ResourceLocation recipeID, boolean maxTransfer, Byte2ObjectMap<SingularHashedItemSource> sources) {
        this.recipeID = recipeID;
        this.sources = sources;
        this.maxTransfer = maxTransfer;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player != null && player.containerMenu instanceof QIOItemViewerContainer) {
            QIOItemViewerContainer container = (QIOItemViewerContainer) player.containerMenu;
            byte selectedCraftingGrid = container.getSelectedCraftingGrid(player.getUUID());
            if (selectedCraftingGrid == -1) {
                Mekanism.logger.warn("Received transfer request from: {}, but they do not currently have a crafting window open.", player);
            } else {
                Optional<? extends IRecipe<?>> optionalIRecipe = player.level.getRecipeManager().byKey(recipeID);
                if (optionalIRecipe.isPresent()) {
                    IRecipe<?> recipe = optionalIRecipe.get();
                    if (recipe instanceof ICraftingRecipe) {
                        transferRecipe(container, (ICraftingRecipe) recipe, selectedCraftingGrid, player);
                    } else {
                        Mekanism.logger.warn("Received transfer request from: {}, but the type ({}) of the specified recipe was not a crafting recipe.",
                              player, recipe.getClass());
                    }
                } else {
                    Mekanism.logger.warn("Received transfer request from: {}, but could not find specified recipe.", player);
                }
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeResourceLocation(recipeID);
        buffer.writeBoolean(maxTransfer);
        //Cast to byte as this should always be at most 9
        buffer.writeByte((byte) sources.size());
        for (Byte2ObjectMap.Entry<SingularHashedItemSource> entry : sources.byte2ObjectEntrySet()) {
            //Target Slot
            buffer.writeByte(entry.getByteKey());
            //Source slot
            SingularHashedItemSource source = entry.getValue();
            byte sourceSlot = source.getSlot();
            //We "cheat" here by just writing the source slot regardless of if we are in the crafting window, main inventory, or QIO
            // as then we can use the not a valid value as indication that we have a UUID following for QIO source, and otherwise we
            // get away with not having to write some sort of identifier for which type of data we are transferring
            buffer.writeByte(sourceSlot);
            if (sourceSlot == -1) {
                //If we don't actually have a source slot, that means we need to write the UUID
                // as it is being transferred out of the QIO
                UUID qioSource = source.getQioSource();
                if (qioSource == null) {
                    throw new IllegalStateException("Invalid QIO crafting window transfer source.");
                }
                buffer.writeUUID(qioSource);
            }
        }
    }

    public static PacketQIOFillCraftingWindow decode(PacketBuffer buffer) {
        ResourceLocation recipeID = buffer.readResourceLocation();
        boolean maxTransfer = buffer.readBoolean();
        byte slotCount = buffer.readByte();
        Byte2ObjectMap<SingularHashedItemSource> sources = new Byte2ObjectArrayMap<>(slotCount);
        for (byte slot = 0; slot < slotCount; slot++) {
            byte targetSlot = buffer.readByte();
            byte sourceSlot = buffer.readByte();
            if (sourceSlot == -1) {
                sources.put(targetSlot, new SingularHashedItemSource(buffer.readUUID()));
            } else {
                sources.put(targetSlot, new SingularHashedItemSource(sourceSlot));
            }
        }
        return new PacketQIOFillCraftingWindow(recipeID, maxTransfer, sources);
    }

    private void transferRecipe(QIOItemViewerContainer container, ICraftingRecipe recipe, byte selectedCraftingGrid, PlayerEntity player) {
        QIOFrequency frequency = container.getFrequency();
        CraftingInventory dummy = MekanismUtils.getDummyCraftingInv();
        QIOCraftingWindow craftingWindow = container.getCraftingWindow(selectedCraftingGrid);
        List<HotBarSlot> hotBarSlots = container.getHotBarSlots();
        List<MainInventorySlot> mainInventorySlots = container.getMainInventorySlots();
        for (Byte2ObjectMap.Entry<SingularHashedItemSource> entry : sources.byte2ObjectEntrySet()) {
            ItemStack stack;
            SingularHashedItemSource source = entry.getValue();
            byte slot = source.getSlot();
            if (slot == -1) {
                UUID qioSource = source.getQioSource();
                if (qioSource == null) {
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, with no valid source.", player, recipeID);
                    return;
                } else if (frequency == null) {
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, with a QIO source but no selected frequency.", player, recipeID);
                    return;
                }
                HashedItem storedItem = frequency.getTypeByUUID(qioSource);
                if (storedItem == null) {
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, could not find stored item with UUID: {}.", player, recipeID, qioSource);
                    return;
                }
                stack = storedItem.getStack();
            } else if (slot >= 0 && slot < 9 + PlayerInventory.getSelectionSize() + 27) {
                if (slot < 9) {
                    //Crafting Window
                    CraftingWindowInventorySlot inputSlot = craftingWindow.getInputSlot(slot);
                    //Note: This isn't a super accurate validation of if we can take the stack or not, given in theory we
                    // always should be able to, but we have this check that mimics our implementation here just in case
                    if (inputSlot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).isEmpty()) {
                        Mekanism.logger.warn("Received transfer request from: {}, for: {}, with a request to take from crafting window slot: {}, "
                                             + "but that slot cannot be taken from.", player, recipeID, slot);
                        return;
                    }
                    stack = inputSlot.getStack();
                } else if (slot < 9 + PlayerInventory.getSelectionSize()) {
                    //Hotbar
                    int actualSlot = slot - 9;
                    if (actualSlot >= hotBarSlots.size()) {
                        //Something went wrong, shouldn't happen even with an invalid packet
                        Mekanism.logger.warn("Received transfer request from: {}, for: {}, could not find hotbar slot: {}.", player, recipeID, actualSlot);
                        return;
                    }
                    HotBarSlot hotbarSlot = hotBarSlots.get(actualSlot);
                    if (hotbarSlot.mayPickup(player)) {
                        stack = hotbarSlot.getItem();
                    } else {
                        Mekanism.logger.warn("Received transfer request from: {}, for: {}, with a request to take from hotbar slot: {}, "
                                             + "but that slot cannot be taken from.", player, recipeID, actualSlot);
                        return;
                    }
                } else {
                    //Main inventory
                    int actualSlot = slot - 9 - PlayerInventory.getSelectionSize();
                    if (actualSlot >= mainInventorySlots.size()) {
                        //Something went wrong, shouldn't happen even with an invalid packet
                        Mekanism.logger.warn("Received transfer request from: {}, for: {}, could not find main inventory slot: {}.", player, recipeID, actualSlot);
                        return;
                    }
                    MainInventorySlot mainInventorySlot = mainInventorySlots.get(actualSlot);
                    if (mainInventorySlot.mayPickup(player)) {
                        stack = mainInventorySlot.getItem();
                    } else {
                        Mekanism.logger.warn("Received transfer request from: {}, for: {}, with a request to take from main inventory slot: {}, "
                                             + "but that slot cannot be taken from.", player, recipeID, actualSlot);
                        return;
                    }
                }
            } else {
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, with an invalid slot id: {}.", player, recipeID, slot);
                return;
            }
            if (stack.isEmpty()) {
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, for an empty slot: {}.", player, recipeID, slot);
                return;
            }
            byte targetSlot = entry.getByteKey();
            if (targetSlot < 0 || targetSlot >= 9) {
                Mekanism.logger.warn("Received transfer request from: {}, for: {}, with an invalid target slot id: {}.", player, recipeID, targetSlot);
                return;
            }
            dummy.setItem(targetSlot, stack);
        }
        if (!recipe.matches(dummy, player.level)) {
            Mekanism.logger.warn("Received transfer request from: {}, but source items aren't valid for the requested recipe: {}.", player, recipeID);
            return;
        }
        //TODO - 10.1: Figure out how we will validate if there is room to do all that before we actually do so?
        // Realistically we will probably need to do it "twice", once by simulating, once by not
        // (maybe we can do a similar thing to how the TransportManager simulates insertion)
        // And if our simulation is screwed up and we are wrong, then we will have to dump whatever excess items we had on the ground?
        //TODO - 10.1: Support the maxTransfer flag, and make sure to validate the other slots we are grabbing from that we didn't validate when checking the recipe is valid
        // We will need to pass the proper data to transferItems, but I believe once we get simulation working, we can probably relatively easily extend it so that it
        // also calculates how much will come from each slot and how much we can even handle in the recipe.
        // One short circuit that probably will be worth doing is if one of the inputs to the recipe is not stackable, then we can just treat maxTransfer as false
        transferItems(frequency, craftingWindow, player, hotBarSlots, mainInventorySlots);
    }

    private void transferItems(@Nullable QIOFrequency frequency, QIOCraftingWindow craftingWindow, PlayerEntity player, List<HotBarSlot> hotBarSlots,
          List<MainInventorySlot> mainInventorySlots) {
        //Extract items that will be put into the crafting window
        Byte2ObjectMap<ItemStack> targetContents = new Byte2ObjectArrayMap<>(sources.size());
        for (Byte2ObjectMap.Entry<SingularHashedItemSource> entry : sources.byte2ObjectEntrySet()) {
            SingularHashedItemSource source = entry.getValue();
            byte slot = source.getSlot();
            if (slot == -1) {
                UUID qioSource = source.getQioSource();
                if (qioSource == null || frequency == null) {
                    //Fixes null warnings, we already validate this isn't an issue above
                    throw new IllegalStateException("Invalid QIO source or frequency");
                }
                HashedItem storedItem = frequency.getTypeByUUID(qioSource);
                if (storedItem == null) {
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, could not find stored item with UUID: {}. "
                                         + "This likely means that more of it was requested than is stored.", player, recipeID, qioSource);
                    return;
                }
                ItemStack stack = frequency.removeByType(storedItem, 1);
                if (stack.isEmpty()) {
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, but could not extract item: {} with nbt: {} from the QIO.", player,
                          recipeID, storedItem.getStack().getItem(), storedItem.getStack().getTag());
                    return;
                }
                targetContents.put(entry.getByteKey(), stack);
            } else if (slot < 9) {
                //Crafting Window
                CraftingWindowInventorySlot inputSlot = craftingWindow.getInputSlot(slot);
                ItemStack stack = inputSlot.extractItem(1, Action.EXECUTE, AutomationType.MANUAL);
                if (stack.isEmpty()) {
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, could not extract item from crafting window slot: {}. "
                                         + "This likely means that more of it was requested than is stored.", player, recipeID, slot);
                    return;
                }
                targetContents.put(entry.getByteKey(), stack);
            } else if (slot < 9 + PlayerInventory.getSelectionSize()) {
                //Hotbar
                int actualSlot = slot - 9;
                HotBarSlot hotbarSlot = hotBarSlots.get(actualSlot);
                ItemStack stack = hotbarSlot.remove(1);
                if (stack.isEmpty()) {
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, could not extract item from hotbar window slot: {}. "
                                         + "This likely means that more of it was requested than is stored.", player, recipeID, actualSlot);
                    return;
                }
                targetContents.put(entry.getByteKey(), stack);
            } else {
                //Main inventory
                int actualSlot = slot - 9 - PlayerInventory.getSelectionSize();
                MainInventorySlot mainInventorySlot = mainInventorySlots.get(actualSlot);
                ItemStack stack = mainInventorySlot.remove(1);
                if (stack.isEmpty()) {
                    Mekanism.logger.warn("Received transfer request from: {}, for: {}, could not extract item from main inventory window slot: {}. "
                                         + "This likely means that more of it was requested than is stored.", player, recipeID, actualSlot);
                    return;
                }
                targetContents.put(entry.getByteKey(), stack);
            }
        }
        //Extract what items are still
        Byte2ObjectMap<ItemStack> remainingCraftingGridContents = new Byte2ObjectArrayMap<>(9);
        for (byte i = 0; i < 9; i++) {
            CraftingWindowInventorySlot inputSlot = craftingWindow.getInputSlot(i);
            if (!inputSlot.isEmpty()) {
                ItemStack stack = inputSlot.extractItem(inputSlot.getCount(), Action.EXECUTE, AutomationType.MANUAL);
                if (!stack.isEmpty()) {
                    remainingCraftingGridContents.put(i, stack);
                } else {
                    //TODO - 10.1: Catch the case when it can fail to extract in the simulation stage
                }
            }
        }
        //Insert items for the crafting window into it
        for (Byte2ObjectMap.Entry<ItemStack> entry : targetContents.byte2ObjectEntrySet()) {
            CraftingWindowInventorySlot inputSlot = craftingWindow.getInputSlot(entry.getByteKey());
            ItemStack remainder = inputSlot.insertItem(entry.getValue(), Action.EXECUTE, AutomationType.MANUAL);
            if (!remainder.isEmpty()) {
                //TODO - 10.1: Catch this error during the simulation stage?
            }
        }
        //Put the items that were in the crafting window in the player's inventory
        for (Byte2ObjectMap.Entry<ItemStack> entry : remainingCraftingGridContents.byte2ObjectEntrySet()) {
            //TODO: Can we somehow batch this, maybe by keeping it as <HashedItem, CombinedStack, Set<FromSlots>>
            ItemStack stack = entry.getValue();
            //Insert into player's inventory
            stack = MekanismContainer.insertItem(hotBarSlots, stack, true);
            stack = MekanismContainer.insertItem(mainInventorySlots, stack, true);
            stack = MekanismContainer.insertItem(hotBarSlots, stack, false);
            stack = MekanismContainer.insertItem(mainInventorySlots, stack, false);
            if (!stack.isEmpty()) {
                //If we couldn't insert it all, try recombining with the slots they were in the crafting window
                // (only if the type matches though)
                CraftingWindowInventorySlot inputSlot = craftingWindow.getInputSlot(entry.getByteKey());
                ItemStack stored = inputSlot.getStack();
                if (ItemHandlerHelper.canItemStacksStack(stored, stack)) {
                    stack = inputSlot.insertItem(stack, Action.EXECUTE, AutomationType.MANUAL);
                }
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
                        Mekanism.logger.warn("Received transfer request from: {}, for: {}, and was unable to fit all contents that were in the crafting window "
                                             + "into the player's inventory/QIO system; dropping items by player.", player, recipeID);
                        //TODO - 10.1: Make sure we don't get to this point by having accurate simulation
                    }
                }
            }
        }
    }
}