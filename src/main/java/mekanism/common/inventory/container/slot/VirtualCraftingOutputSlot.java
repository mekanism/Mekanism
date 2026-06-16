package mekanism.common.inventory.container.slot;

import java.util.List;
import java.util.function.Consumer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class VirtualCraftingOutputSlot extends VirtualInventoryContainerSlot implements IHasExtraData {

    private final QIOCraftingWindow craftingWindow;
    /// @apiNote For use on client side to store if we can craft or not. On the server side we check it directly
    private boolean canCraft;
    private int amountCrafted;

    public VirtualCraftingOutputSlot(BasicInventorySlot slot, @Nullable SlotOverlay slotOverlay, QIOCraftingWindow craftingWindow) {
        super(slot, craftingWindow.getWindowData(), slotOverlay);
        this.craftingWindow = craftingWindow;
    }

    @Override
    public boolean canMergeWith(ItemStack stack) {
        //Don't allow double-clicking to pickup stacks from the output slot
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        //Short circuit to avoid looking through the various predicates
        return false;
    }

    @Override
    protected boolean allowPartialRemoval() {
        return false;
    }

    @Override
    public int insert(ItemResource itemType, int amount, TransactionContext transaction) {
        //Short circuit don't allow inserting into the output slot
        return 0;
    }

    @Override
    public int extract(Player player, ItemResource itemType, int amount, TransactionContext transaction) {
        IInventorySlot slot = getInventorySlot();
        if (mayPickup(player) && itemType.equals(slot.resource())) {
            int amountPerRecipe = slot.amountAsInt();
            //Return how many full recipe extractions we would be able to do. We don't modify the
            // actual slot as we want it to maintain its contents
            //TODO - 26.2: Do we need to add a snapshot listener for the transaction being committed to increment amountCrafted?
            // I suspect it doesn't matter as we don't actually ever call this method except for vanilla slots that we wrap into ITransactionalSlot
            return (amount / amountPerRecipe) * amountPerRecipe;
        }
        return 0;
    }

    @Override
    public ItemStack remove(int amount) {
        //Note: This method is only called if mayPickup returns true
        if (amount == 0) {
            return ItemStack.EMPTY;
        }
        //"Simulate" extraction to not actually modify the slot by just taking a copy of the slot's stack.
        // This is done rather than simulating to ensure that we properly return the correct stack if it
        // is for a recipe that creates an "over" stacked item. We also ignore the passed in amount so that
        // we ensure we properly craft everything from the output stack instead of only producing part of the output
        // Note: In theory even though we are "simulating" here instead of actually changing how much is
        // in the slot, this shouldn't be a problem or be a risk of duplication, as there are slots like
        // the MerchantResultSlot which effectively do the same thing. They do it slightly differently
        // by taking it and then just setting the contents again, but effectively it is just returning
        // a copy so if mods cause any duplication glitches because of how we handle this, then in theory
        // they probably also cause duplication glitches with some of vanilla's slots as well.
        ItemStack extracted = getStackCopy();
        //Adjust amount crafted by the amount that would have actually been extracted
        amountCrafted += extracted.count();
        //TODO - 26.2: Do we want to scale this up by how many times the full stack could be extracted while still being under amount
        // Unlike #extract, we do need to make sure that it is at least one recipe's worth, even if we don't have to round up for other cases
        return extracted;
    }

    /// @implNote We override this similar to how [net.minecraft.world.inventory.ResultSlot] does, but this never actually ends up getting called for our slots.
    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        amountCrafted += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void onSwapCraft(int numItemsCrafted) {
        //Note: This method is only called if mayPickup returns true
        amountCrafted += numItemsCrafted;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        //Note: This method is only called if mayPickup returns true
        ItemStack result = craftingWindow.performCraft(player, stack, amountCrafted);
        if (!result.isEmpty()) {
            //As vanilla likely called getItem before calling onTake, we need to make sure to reset what StackCopySlot#cachedReturnedStack points at
            // so that when we vanilla calls Slot#setChanged it doesn't then get reverted to the previous stack
            clearCachedReturnStack();
        }
        amountCrafted = 0;
    }

    @Override
    public boolean mayPickup(Player player) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return canCraft && super.mayPickup(player);
        }
        return craftingWindow.canViewRecipe(serverPlayer) && super.mayPickup(player);
    }

    @Override
    public ItemStack getStackCopy() {
        //Note: We check canCraft even on the server side, as we don't have a player context here and as there is only one container per player
        // we can just hackily update the canCraft variable while syncing it to the client
        return canCraft ? super.getStackCopy() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack getStackToRender() {
        return canCraft ? super.getStackToRender() : ItemStack.EMPTY;
    }

    public ItemStack shiftClickSlot(Player player, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots) {
        //Perform the craft in the crafting window. This handles moving the stacks to the proper inventory slots
        // Note: This method is only called if mayPickup returns true
        craftingWindow.performCraft(player, hotBarSlots, mainInventorySlots);
        // afterward we want to "stop" crafting as our window determines how much a shift click should produce
        // so even though we may still have an output in the slot, we return empty here so that vanilla's loop
        // it performs for shift clicking, doesn't cause us to craft as much as we are able to.
        return ItemStack.EMPTY;
    }

    @Override
    public void addTrackers(Player player, Consumer<ISyncableData> tracker) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            //If we are on the client or not a server player entity for some reason return our cached value
            tracker.accept(SyncableBoolean.create(() -> canCraft, value -> canCraft = value));
        } else {
            //Otherwise if we are on the server validate if the player can craft or not
            // Note: We handle syncing the canCraft concept for our slots by just checking if the "owning" player is able to perform
            // the craft. We can get away with doing this as for the most part even server side containers are on a per player basis
            // so there is no harm in hiding/showing the output in the rare cases that another mod may let you hook into viewing an
            // existing container and interact with it.
            //TODO: Eventually if this does turn out to be an issue, it can probably be solved/worked around by using some per player
            // aware syncable boolean. In theory it shouldn't be that hard to create, as sync packet wise we could just have it take
            // two lists:
            // 1. The current one
            // 2. The player specific one
            // and then when encoding it just add the sizes together and pretend they are all part of the first list for purposes of
            // what the client is aware of as the client shouldn't care about them
            //Note: We also update the value of canCraft here while syncing so that we ensure a closer to up to date value on the server
            // for purposes of our getItem and hasItem overrides
            tracker.accept(SyncableBoolean.create(() -> canCraft = craftingWindow.canViewRecipe(serverPlayer), value -> canCraft = value));
        }
    }

    @Override
    public boolean isFake() {
        return true;
    }
}