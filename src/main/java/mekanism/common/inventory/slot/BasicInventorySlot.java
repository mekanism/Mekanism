package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.access.InventorySlotItemAccess;
import mekanism.api.resource.BasicResourceContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.warning.ISupportsWarning;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

//TODO: Should we make some sort of "ITickableSlot" or something that lets us tick a bunch of slots at once instead of having to manually call the relevant methods
@NothingNullByDefault
public class BasicInventorySlot extends BasicResourceContainer<ItemResource> implements IInventorySlot {//TODO - 26.1: Docs on how this is similar to ItemStackResourceHandler

    public static BasicInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
        return at(ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    public static BasicInventorySlot at(Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        return at(ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), validator, listener, x, y);
    }

    public static BasicInventorySlot at(@Range(from = 1, to = Long.MAX_VALUE) long capacity, Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        if (capacity < 1) {
            throw new IllegalArgumentException("Slots with a custom capacity must allow at least one item");
        }
        return new BasicInventorySlot(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), validator, listener, x, y);
    }

    public static BasicInventorySlot at(BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        return at(canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    public static BasicInventorySlot at(BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        return new BasicInventorySlot(canExtract, canInsert, validator, listener, x, y);
    }

    //TODO - 26.1: Figure out what automation type this should have, or if it should be hardcoded to automation type internal?
    private final ItemAccess itemAccess = new InventorySlotItemAccess(this, AutomationType.INTERNAL);
    private final int x;
    private final int y;
    protected boolean obeyStackLimit = true;
    private ContainerSlotType slotType = ContainerSlotType.NORMAL;
    @Nullable
    private SlotOverlay slotOverlay;
    @Nullable
    private Consumer<ISupportsWarning<?>> warningAdder;

    protected BasicInventorySlot(BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        this(Item.ABSOLUTE_MAX_STACK_SIZE, canExtract, canInsert, validator, listener, x, y);
    }

    protected BasicInventorySlot(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        super(capacity, canExtract, canInsert, validator, listener);
        this.x = x;
        this.y = y;
    }

    public int getGuiX() {
        return x;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacityAsLong(ItemResource resource) {
        //Note: The below logic gracefully handles when zero is returned from super due to the resource not being valid
        long capacity = super.capacityAsLong(resource);
        return obeyStackLimit && !resource.isEmpty() ? Math.min(capacity, resource.getMaxStackSize()) : capacity;
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        return new InventoryContainerSlot(this, x, y, slotType, slotOverlay, warningAdder);
    }

    public void setSlotType(ContainerSlotType slotType) {
        //TODO - 1.18: Re-evaluate this method as for the most part we now seem to be handling this in GuiMekanism
        // and figuring it out based on the data type; which at the very least means we can probably remove some
        // calls to this. Though there are also some cases where we want to override it where it doesn't now as
        // the fallback sets it to normal basically regardless (see evaporation multiblock and input slots)
        this.slotType = slotType;
    }

    public void tracksWarnings(@Nullable Consumer<ISupportsWarning<?>> warningAdder) {
        this.warningAdder = warningAdder;
    }

    public void setSlotOverlay(@Nullable SlotOverlay slotOverlay) {
        this.slotOverlay = slotOverlay;
    }

    @Nullable
    protected final SlotOverlay getSlotOverlay() {
        return slotOverlay;
    }

    protected final ContainerSlotType getSlotType() {
        return slotType;
    }

    public ItemAccess asItemAccess() {
        return itemAccess;
    }
}