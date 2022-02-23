package mekanism.common.inventory.container.slot;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class VirtualCraftingOutputSlot extends VirtualInventoryContainerSlot implements IHasExtraData {

    @Nonnull
    private final QIOCraftingWindow craftingWindow;
    /**
     * @apiNote For use on client side to store if we can craft or not. On the server side we check it directly
     */
    private boolean canCraft;
    private int amountCrafted;

    public VirtualCraftingOutputSlot(BasicInventorySlot slot, @Nullable SlotOverlay slotOverlay, Consumer<ItemStack> uncheckedSetter,
          @Nonnull QIOCraftingWindow craftingWindow) {
        super(slot, craftingWindow.getWindowData(), slotOverlay, uncheckedSetter);
        this.craftingWindow = craftingWindow;
    }

    @Override
    public boolean canMergeWith(@Nonnull ItemStack stack) {
        //Don't allow double-clicking to pickup stacks from the output slot
        return false;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        //Short circuit to avoid looking through the various predicates
        return false;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(@Nonnull ItemStack stack, Action action) {
        //Short circuit don't allow inserting into the output slot
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack remove(int amount) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }
        //Simulate extraction to not actually modify the slot
        // Note: In theory even though we are "simulating" here instead of actually changing how much is
        // in the slot, this shouldn't be a problem or be a risk of duplication, as there are slots like
        // the MerchantResultSlot which effectively do the same thing. They do it slightly differently
        // by taking it and then just setting the contents again, but effectively it is just returning
        // a copy so if mods cause any duplication glitches because of how we handle this, then in theory
        // they probably also cause duplication glitches with some of vanilla's slots as well.
        // Note: We use the slot's actual amount instead of the passed in one, so that we ensure we properly
        // craft everything from the output stack instead of only producing part of the output
        IInventorySlot slot = getInventorySlot();
        ItemStack extracted = slot.extractItem(slot.getCount(), Action.SIMULATE, AutomationType.MANUAL);
        //Adjust amount crafted by the amount that would have actually been extracted
        amountCrafted += extracted.getCount();
        return extracted;
    }

    /**
     * @implNote We override this similar to how {@link net.minecraft.inventory.container.CraftingResultSlot} does, but this never actually ends up getting called for our
     * slots.
     */
    @Override
    protected void onQuickCraft(@Nonnull ItemStack stack, int amount) {
        amountCrafted += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void onSwapCraft(int numItemsCrafted) {
        amountCrafted += numItemsCrafted;
    }

    @Nonnull
    @Override
    public ItemStack onTake(@Nonnull PlayerEntity player, @Nonnull ItemStack stack) {
        ItemStack result = craftingWindow.performCraft(player, stack, amountCrafted);
        amountCrafted = 0;
        return result;
    }

    @Override
    public boolean mayPickup(@Nonnull PlayerEntity player) {
        if (player.level.isClientSide || !(player instanceof ServerPlayerEntity)) {
            return canCraft && super.mayPickup(player);
        }
        return craftingWindow.canViewRecipe((ServerPlayerEntity) player) && super.mayPickup(player);
    }

    @Nonnull
    @Override
    public ItemStack getStackToRender() {
        return canCraft ? super.getStackToRender() : ItemStack.EMPTY;
    }

    @Nonnull
    public ItemStack shiftClickSlot(@Nonnull PlayerEntity player, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots) {
        //Perform the craft in the crafting window. This handles moving the stacks to the proper inventory slots
        craftingWindow.performCraft(player, hotBarSlots, mainInventorySlots);
        // afterwards we want to "stop" crafting as our window determines how much a shift click should produce
        // so even though we may still have an output in the slot, we return empty here so that vanilla's loop
        // it performs for shift clicking, doesn't cause us to craft as much as we are able to.
        return ItemStack.EMPTY;
    }

    @Override
    public void addTrackers(PlayerEntity player, Consumer<ISyncableData> tracker) {
        if (player.level.isClientSide || !(player instanceof ServerPlayerEntity)) {
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
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            tracker.accept(SyncableBoolean.create(() -> craftingWindow.canViewRecipe(serverPlayer), value -> canCraft = value));
        }
    }
}