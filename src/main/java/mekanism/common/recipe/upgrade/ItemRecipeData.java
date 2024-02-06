package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ItemRecipeData implements RecipeUpgradeData<ItemRecipeData> {

    private final List<IInventorySlot> slots;

    ItemRecipeData(ListTag slots) {
        this(readContents(slots));
    }

    ItemRecipeData(List<IInventorySlot> slots) {
        this.slots = slots;
    }

    @Nullable
    @Override
    public ItemRecipeData merge(ItemRecipeData other) {
        List<IInventorySlot> allSlots = new ArrayList<>(slots);
        allSlots.addAll(other.slots);
        return new ItemRecipeData(allSlots);
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (slots.isEmpty()) {
            return true;
        }
        Item item = stack.getItem();
        List<IInventorySlot> stackSlots = new ArrayList<>();
        if (item instanceof ItemBlockPersonalStorage<?>) {
            //Add the slots in the same way we would for a PersonalStorageItemInventory and if we can transfer to the item,
            // we will copy them over directly
            PersonalStorageManager.createSlots(stackSlots::add, BasicInventorySlot.alwaysTrueBi, null);
            return applyToStack(slots, stackSlots, (ListTag toWrite) -> PersonalStorageManager.createInventoryFor(stack, stackSlots));
        }
        boolean isBin = item instanceof ItemBlockBin;
        IItemHandler itemHandler = Capabilities.ITEM.getCapability(stack);
        if (itemHandler != null) {
            for (int i = 0, slots = itemHandler.getSlots(); i < slots; i++) {
                int slot = i;
                stackSlots.add(new DummyInventorySlot(itemHandler.getSlotLimit(slot), itemStack -> itemHandler.isItemValid(slot, itemStack), isBin));
            }
        } else if (item instanceof BlockItem blockItem) {
            TileEntityMekanism tile = getTileFromBlock(blockItem.getBlock());
            if (tile == null || !tile.persistInventory()) {
                //Something went wrong
                return false;
            }
            for (int i = 0, slots = tile.getSlots(); i < slots; i++) {
                int slot = i;
                stackSlots.add(new DummyInventorySlot(tile.getSlotLimit(slot), itemStack -> tile.isItemValid(slot, itemStack), isBin));
            }
        } else if (item instanceof ItemRobit) {
            //Special casing for the robit so that we don't void items from a personal chest when upgrading to a robit
            //Inventory slots
            for (int slotY = 0; slotY < 3; slotY++) {
                for (int slotX = 0; slotX < 9; slotX++) {
                    stackSlots.add(new DummyInventorySlot(BasicInventorySlot.DEFAULT_LIMIT, BasicInventorySlot.alwaysTrue, false));
                }
            }
            //Energy slot
            stackSlots.add(new DummyInventorySlot(BasicInventorySlot.DEFAULT_LIMIT, itemStack -> {
                if (EnergyCompatUtils.hasStrictEnergyHandler(itemStack)) {
                    return true;
                }
                ItemStackToEnergyRecipe foundRecipe = MekanismRecipeType.ENERGY_CONVERSION.getInputCache().findTypeBasedRecipe(null, itemStack);
                return foundRecipe != null && !foundRecipe.getOutput(itemStack).isZero();
            }, false));
            //Smelting input slot
            stackSlots.add(new DummyInventorySlot(BasicInventorySlot.DEFAULT_LIMIT, itemStack -> MekanismRecipeType.SMELTING.getInputCache().containsInput(null, itemStack), false));
            //Smelting output slot
            stackSlots.add(new DummyInventorySlot(BasicInventorySlot.DEFAULT_LIMIT, BasicInventorySlot.alwaysTrue, false));
        } else if (item instanceof IItemSustainedInventory sustainedInventory) {
            //Fallback just save it all
            for (IInventorySlot slot : slots) {
                if (!slot.isEmpty()) {
                    //We have no information about what our item supports, but we have at least some stacks we want to transfer
                    sustainedInventory.setSustainedInventory(DataHandlerUtils.writeContainers(slots), stack);
                    return true;
                }
            }
            return true;
        } else {
            return false;
        }
        return applyToStack(slots, stackSlots, (ListTag toWrite) -> ((IItemSustainedInventory) item).setSustainedInventory(toWrite, stack));
    }

    static boolean applyToStack(List<IInventorySlot> dataSlots, List<IInventorySlot> stackSlots, Consumer<ListTag> stackWriter) {
        return applyToStack(dataSlots, stackSlots, t -> {
            stackWriter.accept(t);
            return true;
        });
    }

    private static boolean applyToStack(List<IInventorySlot> dataSlots, List<IInventorySlot> stackSlots, Predicate<ListTag> stackWriter) {
        if (stackSlots.isEmpty()) {
            return true;
        }
        //TODO: Improve the logic so that it maybe tries multiple different slot combinations
        IMekanismInventory outputHandler = new IMekanismInventory() {
            @NotNull
            @Override
            public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
                return stackSlots;
            }

            @Override
            public void onContentsChanged() {
            }
        };
        if (applyToStack(outputHandler, dataSlots)) {
            if (dataSlots.stream().anyMatch(slot -> !slot.isEmpty())) {
                //We managed to transfer it all into valid slots, so save it to the stack
                return stackWriter.test(DataHandlerUtils.writeContainers(stackSlots));
            }
            return true;
        }
        return false;
    }

    static boolean applyToStack(IMekanismInventory outputHandler, List<IInventorySlot> dataSlots) {
        for (IInventorySlot slot : dataSlots) {
            if (!slot.isEmpty() && !ItemHandlerHelper.insertItemStacked(outputHandler, slot.getStack(), false).isEmpty()) {
                //If we have a remainder something failed so bail
                return false;
            }
        }
        return true;
    }

    public static List<IInventorySlot> readContents(@Nullable ListTag contents) {
        if (contents == null || contents.isEmpty()) {
            return Collections.emptyList();
        }
        int count = DataHandlerUtils.getMaxId(contents, NBTConstants.SLOT);
        List<IInventorySlot> slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            slots.add(new DummyInventorySlot());
        }
        DataHandlerUtils.readContainers(slots, contents);
        return slots;
    }

    private static class DummyInventorySlot extends BasicInventorySlot {

        private DummyInventorySlot() {
            this(Integer.MAX_VALUE, alwaysTrue, true);
        }

        private DummyInventorySlot(int capacity, Predicate<@NotNull ItemStack> validator, boolean isBin) {
            super(capacity, alwaysTrueBi, alwaysTrueBi, validator, null, 0, 0);
            if (isBin) {
                obeyStackLimit = false;
            }
        }
    }
}