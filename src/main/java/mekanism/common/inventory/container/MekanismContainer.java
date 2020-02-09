package mekanism.common.inventory.container;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.IInsertableSlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFloat;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.container.sync.SyncableShort;
import mekanism.common.network.container.PacketUpdateContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public abstract class MekanismContainer extends Container {

    @Nullable
    protected final PlayerInventory inv;
    protected final List<InventoryContainerSlot> inventoryContainerSlots = new ArrayList<>();
    protected final List<MainInventorySlot> mainInventorySlots = new ArrayList<>();
    protected final List<HotBarSlot> hotBarSlots = new ArrayList<>();
    private final List<ISyncableData<?>> trackedData = new ArrayList<>();

    protected MekanismContainer(ContainerTypeRegistryObject<?> type, int id, @Nullable PlayerInventory inv) {
        super(type.getContainerType(), id);
        this.inv = inv;
    }

    @Nonnull
    @Override
    protected Slot addSlot(Slot slot) {
        slot = super.addSlot(slot);
        if (slot instanceof InventoryContainerSlot) {
            inventoryContainerSlots.add((InventoryContainerSlot) slot);
        } else if (slot instanceof MainInventorySlot) {
            mainInventorySlots.add((MainInventorySlot) slot);
        } else if (slot instanceof HotBarSlot) {
            hotBarSlots.add((HotBarSlot) slot);
        }
        //TODO: Should we add a warning if it is not one of the above. Would currently get thrown by personal chest item
        return slot;
    }

    /**
     * Adds slots and opens, must be called at end of extending classes constructors
     */
    protected void addSlotsAndOpen() {
        addSlots();
        if (inv != null) {
            addInventorySlots(inv);
            openInventory(inv);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        //Is this the proper default
        return true;
    }

    @Override
    public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);
        closeInventory(player);
    }

    protected void closeInventory(PlayerEntity player) {
    }

    protected void openInventory(@Nonnull PlayerInventory inv) {
    }

    protected int getInventoryYOffset() {
        return 84;
    }

    protected int getInventoryXOffset() {
        return 8;
    }

    protected void addInventorySlots(@Nonnull PlayerInventory inv) {
        if (this instanceof IEmptyContainer) {
            //Don't include the player's inventory slots
            return;
        }
        int yOffset = getInventoryYOffset();
        int xOffset = getInventoryXOffset();
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                addSlot(new MainInventorySlot(inv, slotX + slotY * 9 + 9, xOffset + slotX * 18, yOffset + slotY * 18));
            }
        }
        yOffset += 58;
        for (int slotY = 0; slotY < 9; slotY++) {
            addSlot(new HotBarSlot(inv, slotY, xOffset + slotY * 18, yOffset));
        }
    }

    protected void addSlots() {
    }

    /**
     * {@inheritDoc}
     *
     * @return The contents in this slot AFTER transferring items away.
     */
    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        //TODO: Do we need any special handling to have this not do anything if we don't have an inventory or are an empty container?
        // Probably not given we then won't have any slots to actually add
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot == null || !currentSlot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack slotStack = currentSlot.getStack();
        ItemStack stackToInsert = slotStack;
        if (currentSlot instanceof InventoryContainerSlot) {
            //Insert into stacks that already contain an item in the order hot bar -> main inventory
            stackToInsert = insertItem(hotBarSlots, stackToInsert, true);
            stackToInsert = insertItem(mainInventorySlots, stackToInsert, true);
            //If we still have any left then input into the empty stacks in the order of main inventory -> hot bar
            // Note: Even though we are doing the main inventory, we still need to do both, ignoring empty then not instead of
            // just directly inserting into the main inventory, in case there are empty slots before the one we can stack with
            stackToInsert = insertItem(mainInventorySlots, stackToInsert, false);
            stackToInsert = insertItem(hotBarSlots, stackToInsert, false);
        } else {
            //We are in the main inventory or the hot bar
            //Start by trying to insert it into the tile's inventory slots, first attempting to stack with other items
            stackToInsert = insertItem(inventoryContainerSlots, stackToInsert, true);
            if (slotStack.getCount() == stackToInsert.getCount()) {
                //Then as long as if we still have the same number of items (failed to insert), try to insert it into the tile's inventory slots allowing for empty items
                stackToInsert = insertItem(inventoryContainerSlots, stackToInsert, false);
                if (slotStack.getCount() == stackToInsert.getCount()) {
                    //Else if we failed to do that also, try transferring to the main inventory or the hot bar, depending which one we currently are in
                    if (currentSlot instanceof MainInventorySlot) {
                        stackToInsert = insertItem(hotBarSlots, stackToInsert, true);
                        stackToInsert = insertItem(hotBarSlots, stackToInsert, false);
                    } else if (currentSlot instanceof HotBarSlot) {
                        stackToInsert = insertItem(mainInventorySlots, stackToInsert, true);
                        stackToInsert = insertItem(mainInventorySlots, stackToInsert, false);
                    } else {
                        //TODO: Should we add a warning message so we can find out if we ever end up here. (Given we should never end up here anyways)
                    }
                }
            }
        }
        if (stackToInsert.getCount() == slotStack.getCount()) {
            //If nothing changed then return that fact
            return ItemStack.EMPTY;
        }
        //Otherwise decrease the stack by the amount we inserted, and return it as a new stack for what is now in the slot
        int difference = slotStack.getCount() - stackToInsert.getCount();
        currentSlot.decrStackSize(difference);
        ItemStack newStack = StackUtils.size(slotStack, difference);
        currentSlot.onTake(player, newStack);
        return newStack;
    }

    //TODO: JAVADOC?
    //Returns remainder, don't modify inserted stack
    @Nonnull
    private <SLOT extends Slot & IInsertableSlot> ItemStack insertItem(List<SLOT> slots, @Nonnull ItemStack stack, boolean ignoreEmpty) {
        if (stack.isEmpty()) {
            //Skip doing anything if the stack is already empty.
            // Makes it easier to chain calls, rather than having to check if the stack is empty after our previous call
            return stack;
        }
        for (SLOT slot : slots) {
            if (ignoreEmpty && slot.getStack().isEmpty()) {
                //Skip checking empty stacks if we want to ignore them
                continue;
            }
            stack = slot.insertItem(stack, Action.EXECUTE);
            if (stack.isEmpty()) {
                break;
            }
        }
        return stack;
    }

    //Start container sync management
    //TODO: Maybe add some custom typed ones (such as maybe for fluids/gases) and or remove some of the ones we don't use
    protected void track(ISyncableData<?> data) {
        trackedData.add(data);
    }

    protected void trackArray(boolean[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableBoolean.create(arrayIn, i));
        }
    }

    protected void trackArray(byte[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableByte.create(arrayIn, i));
        }
    }

    protected void trackArray(double[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableDouble.create(arrayIn, i));
        }
    }

    protected void trackArray(float[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableFloat.create(arrayIn, i));
        }
    }

    protected void trackArray(int[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableInt.create(arrayIn, i));
        }
    }

    protected void trackArray(long[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableLong.create(arrayIn, i));
        }
    }

    protected void trackArray(short[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableShort.create(arrayIn, i));
        }
    }

    public void handleWindowProperty(short property, boolean value) {
        ISyncableData<?> data = trackedData.get(property);
        if (data instanceof SyncableBoolean) {
            ((SyncableBoolean) data).set(value);
        }
    }

    public void handleWindowProperty(short property, byte value) {
        ISyncableData<?> data = trackedData.get(property);
        if (data instanceof SyncableByte) {
            ((SyncableByte) data).set(value);
        }
    }

    public void handleWindowProperty(short property, short value) {
        ISyncableData<?> data = trackedData.get(property);
        if (data instanceof SyncableShort) {
            ((SyncableShort) data).set(value);
        }
    }

    public void handleWindowProperty(short property, int value) {
        ISyncableData<?> data = trackedData.get(property);
        if (data instanceof SyncableInt) {
            ((SyncableInt) data).set(value);
        }
    }

    public void handleWindowProperty(short property, long value) {
        ISyncableData<?> data = trackedData.get(property);
        if (data instanceof SyncableLong) {
            ((SyncableLong) data).set(value);
        }
    }

    public void handleWindowProperty(short property, float value) {
        ISyncableData<?> data = trackedData.get(property);
        if (data instanceof SyncableFloat) {
            ((SyncableFloat) data).set(value);
        }
    }

    public void handleWindowProperty(short property, double value) {
        ISyncableData<?> data = trackedData.get(property);
        if (data instanceof SyncableDouble) {
            ((SyncableDouble) data).set(value);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!listeners.isEmpty()) {
            for (short i = 0; i < trackedData.size(); i++) {
                ISyncableData<?> data = trackedData.get(i);
                if (data.isDirty()) {
                    //TODO: Make a packet for batching the updates together
                    PacketUpdateContainer<?> updatePacket = null;
                    for (IContainerListener listener : listeners) {
                        if (listener instanceof ServerPlayerEntity) {
                            if (updatePacket == null) {
                                //Lazy get the update packet to send
                                updatePacket = data.getUpdatePacket((short) windowId, i);
                            }
                            Mekanism.packetHandler.sendTo(updatePacket, (ServerPlayerEntity) listener);
                        }
                    }
                }
            }
        }
    }
    //End container sync management
}