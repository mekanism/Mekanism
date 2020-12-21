package mekanism.common.inventory.container;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.slot.ArmorSlot;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.IInsertableSlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.inventory.container.slot.OffhandSlot;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.ISyncableData.DirtyType;
import mekanism.common.inventory.container.sync.SyncableBlockPos;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloat;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.container.sync.SyncableShort;
import mekanism.common.inventory.container.sync.chemical.SyncableChemicalStack;
import mekanism.common.inventory.container.sync.chemical.SyncableGasStack;
import mekanism.common.inventory.container.sync.chemical.SyncableInfusionStack;
import mekanism.common.inventory.container.sync.chemical.SyncablePigmentStack;
import mekanism.common.inventory.container.sync.chemical.SyncableSlurryStack;
import mekanism.common.inventory.container.sync.list.SyncableList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.network.container.PacketUpdateContainerBatch;
import mekanism.common.network.container.property.PropertyData;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

public abstract class MekanismContainer extends Container implements ISecurityContainer {

    public static final int BASE_Y_OFFSET = 84;

    @Nullable
    protected final PlayerInventory inv;
    protected final List<InventoryContainerSlot> inventoryContainerSlots = new ArrayList<>();
    protected final List<ArmorSlot> armorSlots = new ArrayList<>();
    protected final List<MainInventorySlot> mainInventorySlots = new ArrayList<>();
    protected final List<HotBarSlot> hotBarSlots = new ArrayList<>();
    protected final List<OffhandSlot> offhandSlots = new ArrayList<>();
    private final List<ISyncableData> trackedData = new ArrayList<>();
    private final Map<Object, List<ISyncableData>> specificTrackedData = new Object2ObjectOpenHashMap<>();

    protected MekanismContainer(ContainerTypeRegistryObject<?> type, int id, @Nullable PlayerInventory inv) {
        super(type.getContainerType(), id);
        this.inv = inv;
    }

    @Nonnull
    @Override
    protected Slot addSlot(@Nonnull Slot slot) {
        //Manually handle the code that is in super.addSlot so that we do not end up adding extra elements to
        // inventoryItemStacks as we handle the tracking/sync changing via the below track call. This way we are
        // able to minimize the amount of overhead that we end up with due to keeping track of the stack in SyncableItemStack
        slot.slotNumber = inventorySlots.size();
        inventorySlots.add(slot);
        track(SyncableItemStack.create(slot::getStack, slot::putStack));
        if (slot instanceof InventoryContainerSlot) {
            inventoryContainerSlots.add((InventoryContainerSlot) slot);
        } else if (slot instanceof ArmorSlot) {
            armorSlots.add((ArmorSlot) slot);
        } else if (slot instanceof MainInventorySlot) {
            mainInventorySlots.add((MainInventorySlot) slot);
        } else if (slot instanceof HotBarSlot) {
            hotBarSlots.add((HotBarSlot) slot);
        } else if (slot instanceof OffhandSlot) {
            offhandSlots.add((OffhandSlot) slot);
        }
        //TODO: Should we add a warning if it is not one of the above
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

    public void startTracking(Object key, ISpecificContainerTracker tracker) {
        List<ISyncableData> list = tracker.getSpecificSyncableData();
        list.forEach(this::track);
        specificTrackedData.put(key, list);
    }

    public void stopTracking(Object key) {
        List<ISyncableData> list = specificTrackedData.get(key);
        if (list != null) {
            list.forEach(trackedData::remove);
            specificTrackedData.remove(key);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        //Is this the proper default
        //TODO - 10.1: Re-evaluate this and maybe add in some distance based checks??
        return true;
    }

    @Override
    public boolean canMergeSlot(@Nonnull ItemStack stack, @Nonnull Slot slot) {
        if (slot instanceof IInsertableSlot) {
            return ((IInsertableSlot) slot).canMergeWith(stack) && super.canMergeSlot(stack, slot);
        }
        return super.canMergeSlot(stack, slot);
    }

    @Override
    public void onContainerClosed(@Nonnull PlayerEntity player) {
        super.onContainerClosed(player);
        closeInventory(player);
    }

    protected void closeInventory(@Nonnull PlayerEntity player) {
    }

    protected void openInventory(@Nonnull PlayerInventory inv) {
    }

    protected int getInventoryYOffset() {
        return BASE_Y_OFFSET;
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
        for (int slotY = 0; slotY < PlayerInventory.getHotbarSize(); slotY++) {
            addSlot(createHotBarSlot(inv, slotY, xOffset + slotY * 18, yOffset));
        }
    }

    protected HotBarSlot createHotBarSlot(@Nonnull PlayerInventory inv, int index, int x, int y) {
        return new HotBarSlot(inv, index, x, y);
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
    public ItemStack transferStackInSlot(@Nonnull PlayerEntity player, int slotID) {
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
            stackToInsert = insertItem(armorSlots, stackToInsert, true);
            stackToInsert = insertItem(hotBarSlots, stackToInsert, true);
            stackToInsert = insertItem(mainInventorySlots, stackToInsert, true);
            //If we still have any left then input into the empty stacks in the order of main inventory -> hot bar
            // Note: Even though we are doing the main inventory, we still need to do both, ignoring empty then not instead of
            // just directly inserting into the main inventory, in case there are empty slots before the one we can stack with
            stackToInsert = insertItem(armorSlots, stackToInsert, false);
            stackToInsert = insertItem(hotBarSlots, stackToInsert, false);
            stackToInsert = insertItem(mainInventorySlots, stackToInsert, false);
        } else {
            //We are in the main inventory or the hot bar
            //Start by trying to insert it into the tile's inventory slots, first attempting to stack with other items
            stackToInsert = insertItem(inventoryContainerSlots, stackToInsert, true);
            if (slotStack.getCount() == stackToInsert.getCount()) {
                //Then as long as if we still have the same number of items (failed to insert), try to insert it into the tile's inventory slots allowing for empty items
                stackToInsert = insertItem(inventoryContainerSlots, stackToInsert, false);
                if (slotStack.getCount() == stackToInsert.getCount()) {
                    //Else if we failed to do that also, try transferring to armor inventory, main inventory or the hot bar, depending which one we currently are in
                    if (currentSlot instanceof ArmorSlot || currentSlot instanceof OffhandSlot) {
                        stackToInsert = insertItem(hotBarSlots, stackToInsert, true);
                        stackToInsert = insertItem(mainInventorySlots, stackToInsert, true);
                        stackToInsert = insertItem(hotBarSlots, stackToInsert, false);
                        stackToInsert = insertItem(mainInventorySlots, stackToInsert, false);
                    } else if (currentSlot instanceof MainInventorySlot) {
                        stackToInsert = insertItem(armorSlots, stackToInsert, false);
                        stackToInsert = insertItem(hotBarSlots, stackToInsert, true);
                        stackToInsert = insertItem(hotBarSlots, stackToInsert, false);
                    } else if (currentSlot instanceof HotBarSlot) {
                        stackToInsert = insertItem(armorSlots, stackToInsert, false);
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
        return transferSuccess(currentSlot, player, slotStack, stackToInsert);
    }

    //TODO: JAVADOC?
    //Returns remainder, don't modify inserted stack
    @Nonnull
    protected <SLOT extends Slot & IInsertableSlot> ItemStack insertItem(List<SLOT> slots, @Nonnull ItemStack stack, boolean ignoreEmpty) {
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

    @Nonnull
    protected ItemStack transferSuccess(@Nonnull Slot currentSlot, @Nonnull PlayerEntity player, @Nonnull ItemStack slotStack, @Nonnull ItemStack stackToInsert) {
        int difference = slotStack.getCount() - stackToInsert.getCount();
        currentSlot.decrStackSize(difference);
        ItemStack newStack = StackUtils.size(slotStack, difference);
        currentSlot.onTake(player, newStack);
        return newStack;
    }

    //Start container sync management
    public void track(ISyncableData data) {
        trackedData.add(data);
    }

    @Nonnull
    @Override
    protected IntReferenceHolder trackInt(@Nonnull IntReferenceHolder referenceHolder) {
        //Override vanilla's int tracking so that if for some reason this method gets called for our container
        // it properly adds it to our tracking
        track(SyncableInt.create(referenceHolder::get, referenceHolder::set));
        return referenceHolder;
    }

    public void trackArray(boolean[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableBoolean.create(arrayIn, i));
        }
    }

    public void trackArray(byte[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableByte.create(arrayIn, i));
        }
    }

    public void trackArray(double[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableDouble.create(arrayIn, i));
        }
    }

    public void trackArray(float[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableFloat.create(arrayIn, i));
        }
    }

    public void trackArray(int[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableInt.create(arrayIn, i));
        }
    }

    public void trackArray(long[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableLong.create(arrayIn, i));
        }
    }

    public void trackArray(short[] arrayIn) {
        for (int i = 0; i < arrayIn.length; i++) {
            track(SyncableShort.create(arrayIn, i));
        }
    }

    public void handleWindowProperty(short property, boolean value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableBoolean) {
            ((SyncableBoolean) data).set(value);
        }
    }

    public void handleWindowProperty(short property, byte value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableByte) {
            ((SyncableByte) data).set(value);
        }
    }

    public void handleWindowProperty(short property, short value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableShort) {
            ((SyncableShort) data).set(value);
        } else if (data instanceof SyncableFloatingLong) {
            ((SyncableFloatingLong) data).setDecimal(value);
        }
    }

    public void handleWindowProperty(short property, int value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableInt) {
            ((SyncableInt) data).set(value);
        } else if (data instanceof SyncableEnum) {
            ((SyncableEnum<?>) data).set(value);
        } else if (data instanceof SyncableFluidStack) {
            ((SyncableFluidStack) data).set(value);
        } else if (data instanceof SyncableItemStack) {
            ((SyncableItemStack) data).set(value);
        }
    }

    public void handleWindowProperty(short property, long value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableLong) {
            ((SyncableLong) data).set(value);
        } else if (data instanceof SyncableChemicalStack) {
            ((SyncableChemicalStack<?, ?>) data).set(value);
        }
    }

    public void handleWindowProperty(short property, float value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableFloat) {
            ((SyncableFloat) data).set(value);
        }
    }

    public void handleWindowProperty(short property, double value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableDouble) {
            ((SyncableDouble) data).set(value);
        }
    }

    public void handleWindowProperty(short property, @Nonnull ItemStack value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableItemStack) {
            ((SyncableItemStack) data).set(value);
        }
    }

    public void handleWindowProperty(short property, @Nonnull FluidStack value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableFluidStack) {
            ((SyncableFluidStack) data).set(value);
        }
    }

    public void handleWindowProperty(short property, @Nullable BlockPos value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableBlockPos) {
            ((SyncableBlockPos) data).set(value);
        }
    }

    public <STACK extends ChemicalStack<?>> void handleWindowProperty(short property, @Nonnull STACK value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableGasStack && value instanceof GasStack) {
            ((SyncableGasStack) data).set((GasStack) value);
        } else if (data instanceof SyncableInfusionStack && value instanceof InfusionStack) {
            ((SyncableInfusionStack) data).set((InfusionStack) value);
        } else if (data instanceof SyncablePigmentStack && value instanceof PigmentStack) {
            ((SyncablePigmentStack) data).set((PigmentStack) value);
        } else if (data instanceof SyncableSlurryStack && value instanceof SlurryStack) {
            ((SyncableSlurryStack) data).set((SlurryStack) value);
        }
    }

    public <FREQUENCY extends Frequency> void handleWindowProperty(short property, @Nullable FREQUENCY value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableFrequency) {
            ((SyncableFrequency<FREQUENCY>) data).set(value);
        }
    }

    public void handleWindowProperty(short property, @Nonnull FloatingLong value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableFloatingLong) {
            ((SyncableFloatingLong) data).set(value);
        }
    }

    public <TYPE> void handleWindowProperty(short property, @Nonnull List<TYPE> value) {
        ISyncableData data = trackedData.get(property);
        if (data instanceof SyncableList) {
            ((SyncableList<TYPE>) data).set(value);
        }
    }

    @Override
    public void detectAndSendChanges() {
        //Note: We do not call super.detectAndSendChanges() because we intercept and track the
        // stack changes and int changes ourselves. This allows for us to have more accurate syncing
        // and also batch various sync packets
        if (!listeners.isEmpty()) {
            //Only check tracked data for changes if we actually have any listeners
            List<PropertyData> dirtyData = new ArrayList<>();
            for (short i = 0; i < trackedData.size(); i++) {
                ISyncableData data = trackedData.get(i);
                DirtyType dirtyType = data.isDirty();
                if (dirtyType != DirtyType.CLEAN) {
                    dirtyData.add(data.getPropertyData(i, dirtyType));
                }
            }
            int size = dirtyData.size();
            if (size == 1) {
                //If we only have a single element send a type specific packet to reduce overhead of
                // having to include type and count
                sendChange(dirtyData.get(0).getSinglePacket((short) windowId));
            } else if (size > 1) {
                sendChange(new PacketUpdateContainerBatch((short) windowId, dirtyData));
            }
        }
    }

    private <MSG> void sendChange(MSG packet) {
        for (IContainerListener listener : listeners) {
            if (listener instanceof ServerPlayerEntity) {
                Mekanism.packetHandler.sendTo(packet, (ServerPlayerEntity) listener);
            }
        }
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        boolean alreadyHas = listeners.contains(listener);
        super.addListener(listener);
        if (!alreadyHas && listener instanceof ServerPlayerEntity) {
            //Send all contents to the listener when it first gets added
            List<PropertyData> dirtyData = new ArrayList<>();
            for (short i = 0; i < trackedData.size(); i++) {
                dirtyData.add(trackedData.get(i).getPropertyData(i, DirtyType.DIRTY));
            }
            int size = dirtyData.size();
            if (size == 1) {
                //If we only have a single element send a type specific packet to reduce overhead of
                // having to include type and count
                Mekanism.packetHandler.sendTo(dirtyData.get(0).getSinglePacket((short) windowId), (ServerPlayerEntity) listener);
            } else if (size > 1) {
                Mekanism.packetHandler.sendTo(new PacketUpdateContainerBatch((short) windowId, dirtyData), (ServerPlayerEntity) listener);
            }
        }
    }
    //End container sync management

    public interface ISpecificContainerTracker {

        List<ISyncableData> getSpecificSyncableData();
    }
}