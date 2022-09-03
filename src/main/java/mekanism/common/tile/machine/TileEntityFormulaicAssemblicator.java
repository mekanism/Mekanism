package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.assemblicator.RecipeFormula;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FormulaicCraftingSlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityFormulaicAssemblicator extends TileEntityConfigurableMachine implements IHasMode {

    private static final NonNullList<ItemStack> EMPTY_LIST = NonNullList.create();
    private static final Predicate<@NotNull ItemStack> formulaSlotValidator = stack -> stack.getItem() instanceof ItemCraftingFormula;

    private static final int BASE_TICKS_REQUIRED = 40;

    private final CraftingContainer dummyInv = MekanismUtils.getDummyCraftingInv();

    private int ticksRequired = BASE_TICKS_REQUIRED;
    private int operatingTicks;
    private boolean autoMode = false;
    private boolean isRecipe = false;
    private boolean stockControl = false;
    private boolean needsOrganize = true; //organize on load
    private final HashedItem[] stockControlMap = new HashedItem[18];

    private int pulseOperations;

    public RecipeFormula formula;
    @Nullable
    private CraftingRecipe cachedRecipe = null;
    @SyntheticComputerMethod(getter = "getExcessRemainingItems")
    private NonNullList<ItemStack> lastRemainingItems = EMPTY_LIST;

    private ItemStack lastFormulaStack = ItemStack.EMPTY;
    private ItemStack lastOutputStack = ItemStack.EMPTY;

    private MachineEnergyContainer<TileEntityFormulaicAssemblicator> energyContainer;
    private List<IInventorySlot> craftingGridSlots;
    private List<IInventorySlot> inputSlots;
    private List<IInventorySlot> outputSlots;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFormulaItem")
    private BasicInventorySlot formulaSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityFormulaicAssemblicator(BlockPos pos, BlockState state) {
        super(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
        configComponent.setupItemIOConfig(inputSlots, outputSlots, energySlot, false);
        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            //Expose formula slot via extra
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, formulaSlot));
            itemConfig.setDefaults();
        }
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        craftingGridSlots = new ArrayList<>();
        inputSlots = new ArrayList<>();
        outputSlots = new ArrayList<>();
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(formulaSlot = BasicInventorySlot.at(formulaSlotValidator, listener, 6, 26))
              .setSlotOverlay(SlotOverlay.FORMULA);
        for (int slotY = 0; slotY < 2; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                int index = slotY * 9 + slotX;
                InputInventorySlot inputSlot = InputInventorySlot.at(stack -> {
                    //Is item valid
                    if (formula == null) {
                        return true;
                    }
                    IntList indices = formula.getIngredientIndices(level, stack);
                    if (!indices.isEmpty()) {
                        HashedItem stockItem = stockControlMap[index];
                        if (!stockControl || stockItem == null) {
                            return true;
                        }
                        return ItemHandlerHelper.canItemStacksStack(stockItem.getStack(), stack);
                    }
                    return false;
                }, BasicInventorySlot.alwaysTrue, listener, 8 + slotX * 18, 98 + slotY * 18);
                builder.addSlot(inputSlot);
                inputSlots.add(inputSlot);
            }
        }
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 3; slotX++) {
                IInventorySlot craftingSlot = FormulaicCraftingSlot.at(this::getAutoMode, listener, 26 + slotX * 18, 17 + slotY * 18);
                builder.addSlot(craftingSlot);
                craftingGridSlots.add(craftingSlot);
            }
        }
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 2; slotX++) {
                OutputInventorySlot outputSlot = OutputInventorySlot.at(listener, 116 + slotX * 18, 17 + slotY * 18);
                builder.addSlot(outputSlot);
                outputSlots.add(outputSlot);
            }
        }
        //Add the energy slot after adding the other slots so that it has the lowest priority in shift clicking
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 152, 76));
        return builder.build();
    }

    public BasicInventorySlot getFormulaSlot() {
        return formulaSlot;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            checkFormula();
            recalculateRecipe();
            if (formula != null && stockControl) {
                //Ensure stock control is loaded before our first tick in case something inserting ticks before our first tick
                // and inserts into the wrong slots
                buildStockControlMap();
            }
        }
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
            //Invalidate the cached recipe and recalculate
            cachedRecipe = null;
            recalculateRecipe();
        }
        if (formula != null && stockControl && needsOrganize) {
            needsOrganize = false;
            buildStockControlMap();
            organizeStock();
        }
        energySlot.fillContainerOrConvert();
        if (getControlType() != RedstoneControl.PULSE) {
            pulseOperations = 0;
        } else if (MekanismUtils.canFunction(this)) {
            pulseOperations++;
        }
        checkFormula();
        if (autoMode && formula == null) {
            nextMode();
        }

        if (autoMode && formula != null && ((getControlType() == RedstoneControl.PULSE && pulseOperations > 0) || MekanismUtils.canFunction(this))) {
            boolean canOperate = true;
            if (!isRecipe) {
                canOperate = moveItemsToGrid();
            }
            if (canOperate) {
                isRecipe = true;
                if (operatingTicks >= ticksRequired) {
                    if (doSingleCraft()) {
                        operatingTicks = 0;
                        if (pulseOperations > 0) {
                            pulseOperations--;
                        }
                    }
                } else {
                    FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
                    if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                        energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                        operatingTicks++;
                    }
                }
            } else {
                operatingTicks = 0;
            }
        } else {
            operatingTicks = 0;
        }
    }

    private void checkFormula() {
        ItemStack formulaStack = formulaSlot.getStack();
        if (!formulaStack.isEmpty() && formulaStack.getItem() instanceof ItemCraftingFormula) {
            if (formula == null || lastFormulaStack != formulaStack) {
                loadFormula();
            }
        } else {
            formula = null;
        }
        lastFormulaStack = formulaStack;
    }

    private void loadFormula() {
        ItemStack formulaStack = formulaSlot.getStack();
        ItemCraftingFormula formulaItem = (ItemCraftingFormula) formulaStack.getItem();
        if (formulaItem.isInvalid(formulaStack)) {
            formula = null;
            return;
        }
        NonNullList<ItemStack> formulaInventory = formulaItem.getInventory(formulaStack);
        if (formulaInventory == null) {
            formula = null;
        } else {
            RecipeFormula recipe = new RecipeFormula(level, formulaInventory);
            if (recipe.isValidFormula()) {
                if (formula == null) {
                    formula = recipe;
                } else if (!formula.isFormulaEqual(recipe)) {
                    formula = recipe;
                    operatingTicks = 0;
                }
            } else {
                formula = null;
                formulaItem.setInvalid(formulaStack, true);
            }
        }
    }

    @Override
    protected void setChanged(boolean updateComparator) {
        super.setChanged(updateComparator);
        //TODO: Should this be changed to being in onContentsChanged instead of setChanged?
        recalculateRecipe();
    }

    private void recalculateRecipe() {
        if (level != null && !isRemote()) {
            if (formula == null || !formula.isValidFormula()) {
                //Should always be 9 for the size
                for (int i = 0; i < craftingGridSlots.size(); i++) {
                    dummyInv.setItem(i, StackUtils.size(craftingGridSlots.get(i).getStack(), 1));
                }
                lastRemainingItems = EMPTY_LIST;
                if (cachedRecipe == null || !cachedRecipe.matches(dummyInv, level)) {
                    cachedRecipe = MekanismRecipeType.getRecipeFor(RecipeType.CRAFTING, dummyInv, level).orElse(null);
                }
                if (cachedRecipe == null) {
                    lastOutputStack = ItemStack.EMPTY;
                } else {
                    lastOutputStack = cachedRecipe.assemble(dummyInv);
                    lastRemainingItems = cachedRecipe.getRemainingItems(dummyInv);
                }
                isRecipe = !lastOutputStack.isEmpty();
            } else {
                isRecipe = formula.matches(level, craftingGridSlots);
                if (isRecipe) {
                    lastOutputStack = formula.assemble();
                    lastRemainingItems = formula.getRemainingItems();
                } else {
                    lastOutputStack = ItemStack.EMPTY;
                }
            }
            needsOrganize = true;
        }
    }

    private boolean doSingleCraft() {
        recalculateRecipe();
        ItemStack output = lastOutputStack;
        if (!output.isEmpty() && tryMoveToOutput(output, Action.SIMULATE) &&
            (lastRemainingItems.isEmpty() || lastRemainingItems.stream().allMatch(it -> it.isEmpty() || tryMoveToOutput(it, Action.SIMULATE)))) {
            tryMoveToOutput(output, Action.EXECUTE);
            //TODO: Fix this as I believe if things overlap there is a chance it won't work properly.
            // For example if there are multiple stacks of dirt in remaining and we have room for one stack, but given we only check one stack at a time...)
            for (ItemStack remainingItem : lastRemainingItems) {
                if (!remainingItem.isEmpty()) {
                    //TODO: Check if it matters that we are not actually updating the list of remaining items?
                    // The better solution would be to not allow continuing until we moved output AND all remaining items
                    // instead of trying to move all at once??
                    tryMoveToOutput(remainingItem, Action.EXECUTE);
                }
            }

            for (IInventorySlot craftingSlot : craftingGridSlots) {
                if (!craftingSlot.isEmpty()) {
                    MekanismUtils.logMismatchedStackSize(craftingSlot.shrinkStack(1, Action.EXECUTE), 1);
                }
            }
            if (formula != null) {
                moveItemsToGrid();
            }
            markForSave();
            return true;
        }
        return false;
    }

    public boolean craftSingle() {
        if (formula == null) {
            return doSingleCraft();
        }
        boolean canOperate = true;
        if (!formula.matches(getLevel(), craftingGridSlots)) {
            canOperate = moveItemsToGrid();
        }
        if (canOperate) {
            return doSingleCraft();
        }
        return false;
    }

    private boolean moveItemsToGrid() {
        boolean ret = true;
        for (int i = 0; i < craftingGridSlots.size(); i++) {
            IInventorySlot recipeSlot = craftingGridSlots.get(i);
            ItemStack recipeStack = recipeSlot.getStack();
            if (formula.isIngredientInPos(level, recipeStack, i)) {
                continue;
            }
            if (recipeStack.isEmpty()) {
                boolean found = false;
                for (int j = inputSlots.size() - 1; j >= 0; j--) {
                    //The stack stored in the stock inventory
                    IInventorySlot stockSlot = inputSlots.get(j);
                    if (!stockSlot.isEmpty()) {
                        ItemStack stockStack = stockSlot.getStack();
                        if (formula.isIngredientInPos(level, stockStack, i)) {
                            recipeSlot.setStack(StackUtils.size(stockStack, 1));
                            MekanismUtils.logMismatchedStackSize(stockSlot.shrinkStack(1, Action.EXECUTE), 1);
                            markForSave();
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    ret = false;
                }
            } else {
                //Update recipeStack as well, so we can check if it is empty without having to get it again
                recipeSlot.setStack(recipeStack = tryMoveToInput(recipeStack));
                markForSave();
                if (!recipeStack.isEmpty()) {
                    ret = false;
                }
            }
        }
        return ret;
    }

    public void craftAll() {
        while (craftSingle()) {
        }
    }

    public void moveItems() {
        if (formula == null) {
            moveItemsToInput(true);
        } else {
            moveItemsToGrid();
        }
    }

    private void moveItemsToInput(boolean forcePush) {
        for (int i = 0; i < craftingGridSlots.size(); i++) {
            IInventorySlot recipeSlot = craftingGridSlots.get(i);
            ItemStack recipeStack = recipeSlot.getStack();
            if (!recipeStack.isEmpty() && (forcePush || (formula != null && !formula.isIngredientInPos(getLevel(), recipeStack, i)))) {
                recipeSlot.setStack(tryMoveToInput(recipeStack));
            }
        }
        markForSave();
    }

    @Override
    public void nextMode() {
        if (autoMode) {
            operatingTicks = 0;
            autoMode = false;
            markForSave();
        } else if (formula != null) {
            moveItemsToInput(false);
            autoMode = true;
            markForSave();
        }
    }

    @ComputerMethod
    public boolean hasRecipe() {
        return isRecipe;
    }

    @ComputerMethod(nameOverride = "getRecipeProgress")
    public int getOperatingTicks() {
        return operatingTicks;
    }

    @ComputerMethod
    public int getTicksRequired() {
        return ticksRequired;
    }

    public boolean getStockControl() {
        return stockControl;
    }

    public boolean getAutoMode() {
        return autoMode;
    }

    public void toggleStockControl() {
        if (!isRemote() && formula != null) {
            stockControl = !stockControl;
            if (stockControl) {
                organizeStock();
            }
        }
    }

    private void organizeStock() {
        if (formula == null) {
            return;
        }
        // build map of what items we have to organize
        Object2IntMap<HashedItem> storedMap = new Object2IntOpenHashMap<>();
        for (IInventorySlot inputSlot : inputSlots) {
            ItemStack stack = inputSlot.getStack();
            if (!stack.isEmpty()) {
                HashedItem hashed = HashedItem.create(stack);
                storedMap.put(hashed, storedMap.getOrDefault(hashed, 0) + stack.getCount());
            }
        }
        // place items into respective controlled slots
        IntSet unused = new IntOpenHashSet();
        for (int i = 0; i < inputSlots.size(); i++) {
            HashedItem hashedItem = stockControlMap[i];
            if (hashedItem == null) {
                unused.add(i);
            } else if (storedMap.containsKey(hashedItem)) {
                int stored = storedMap.getInt(hashedItem);
                int count = Math.min(hashedItem.getStack().getMaxStackSize(), stored);
                if (count == stored) {
                    storedMap.removeInt(hashedItem);
                } else {
                    storedMap.put(hashedItem, stored - count);
                }
                setSlotIfChanged(inputSlots.get(i), hashedItem, count);
            } else {
                //If we don't have the item stored anymore (already filled all previous slots with it),
                // then we need to empty the slot as the items in it has been moved to a more "optimal" slot
                //Note: We only set them to empty if they are not already empty to avoid onContentsChanged being called
                // Technically our default implementation doesn't fire onContentsChanged if the stack was already empty
                // but this is not an API contract
                IInventorySlot slot = inputSlots.get(i);
                if (!slot.isEmpty()) {
                    slot.setEmpty();
                }
            }
        }
        // if we still have items, first try to add remaining items to known unused (non-controlled) slots
        boolean empty = storedMap.isEmpty();
        for (int i : unused) {
            IInventorySlot slot = inputSlots.get(i);
            if (empty) {
                //If we don't have any more items to sort, clear all the other slots that we haven't set something in
                //Note: We only set them to empty if they are not already empty to avoid onContentsChanged being called
                // Technically our default implementation doesn't fire onContentsChanged if the stack was already empty
                // but this is not an API contract
                if (!slot.isEmpty()) {
                    slot.setEmpty();
                }
            } else {
                empty = setSlotIfChanged(storedMap, slot);
            }
        }
        if (empty) {
            //If we are empty exit
            return;
        }
        // if we still have items, just add them to any slots that are still empty
        for (IInventorySlot inputSlot : inputSlots) {
            if (inputSlot.isEmpty()) {
                if (setSlotIfChanged(storedMap, inputSlot)) {
                    //Exit all items accounted for
                    return;
                }
            }
        }
        if (!storedMap.isEmpty()) {
            Mekanism.logger.error("Critical error: Formulaic Assemblicator had items left over after organizing stock. Impossible!");
        }
    }

    private boolean setSlotIfChanged(Object2IntMap<HashedItem> storedMap, IInventorySlot inputSlot) {
        boolean empty = false;
        Object2IntMap.Entry<HashedItem> next = storedMap.object2IntEntrySet().iterator().next();
        HashedItem item = next.getKey();
        int stored = next.getIntValue();
        int count = Math.min(item.getStack().getMaxStackSize(), stored);
        if (count == stored) {
            storedMap.removeInt(item);
            empty = storedMap.isEmpty();
        } else {
            next.setValue(stored - count);
        }
        setSlotIfChanged(inputSlot, item, count);
        return empty;
    }

    private static void setSlotIfChanged(IInventorySlot slot, HashedItem item, int count) {
        ItemStack stack = item.createStack(count);
        if (!ItemStack.matches(slot.getStack(), stack)) {
            slot.setStack(stack);
        }
    }

    private void buildStockControlMap() {
        if (formula == null) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            int j = i * 2;
            ItemStack stack = formula.getInputStack(i);
            if (stack.isEmpty()) {
                stockControlMap[j] = null;
                stockControlMap[j + 1] = null;
            } else {
                HashedItem hashedItem = HashedItem.create(stack);
                stockControlMap[j] = hashedItem;
                stockControlMap[j + 1] = hashedItem;
            }
        }
    }

    private ItemStack tryMoveToInput(ItemStack stack) {
        for (IInventorySlot stockSlot : inputSlots) {
            stack = stockSlot.insertItem(stack, Action.EXECUTE, AutomationType.INTERNAL);
            if (stack.isEmpty()) {
                //We fit it all, just break and return that we have no remainder
                break;
            }
        }
        return stack;
    }

    private boolean tryMoveToOutput(ItemStack stack, Action action) {
        for (IInventorySlot outputSlot : outputSlots) {
            //Try to insert the item (simulating as needed), and overwrite our local reference to point ot the remainder
            // We can then continue on to the next slot if we did not fit it all and try to insert it.
            // The logic is relatively simple due to only having one stack we are trying to insert, so we don't have to worry
            // about the fact the slot doesn't actually get updated if we simulated, and then is invalid for the next simulation
            stack = outputSlot.insertItem(stack, action, AutomationType.INTERNAL);
            if (stack.isEmpty()) {
                break;
            }
        }
        return stack.isEmpty();
    }

    public void encodeFormula() {
        if (!formulaSlot.isEmpty()) {
            ItemStack formulaStack = formulaSlot.getStack();
            if (formulaStack.getItem() instanceof ItemCraftingFormula item && item.getInventory(formulaStack) == null) {
                RecipeFormula formula = new RecipeFormula(level, craftingGridSlots);
                if (formula.isValidFormula()) {
                    item.setInventory(formulaStack, formula.input);
                    markForSave();
                }
            }
        }
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        autoMode = nbt.getBoolean(NBTConstants.AUTO);
        operatingTicks = nbt.getInt(NBTConstants.PROGRESS);
        pulseOperations = nbt.getInt(NBTConstants.PULSE);
        stockControl = nbt.getBoolean(NBTConstants.STOCK_CONTROL);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        nbtTags.putBoolean(NBTConstants.AUTO, autoMode);
        nbtTags.putInt(NBTConstants.PROGRESS, operatingTicks);
        nbtTags.putInt(NBTConstants.PULSE, pulseOperations);
        nbtTags.putBoolean(NBTConstants.STOCK_CONTROL, stockControl);
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        }
    }

    @NotNull
    @Override
    public List<Component> getInfo(@NotNull Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    public MachineEnergyContainer<TileEntityFormulaicAssemblicator> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::getAutoMode, value -> autoMode = value));
        container.track(SyncableInt.create(this::getOperatingTicks, value -> operatingTicks = value));
        container.track(SyncableInt.create(this::getTicksRequired, value -> ticksRequired = value));
        container.track(SyncableBoolean.create(this::hasRecipe, value -> isRecipe = value));
        container.track(SyncableBoolean.create(this::getStockControl, value -> stockControl = value));
        container.track(SyncableBoolean.create(() -> formula != null, hasFormula -> {
            if (hasFormula) {
                if (formula == null && isRemote()) {
                    //If we are on the client (which we should be when setting anyway) and we don't have a formula yet
                    // but should, then create an empty formula
                    formula = new RecipeFormula(getLevel(), NonNullList.withSize(9, ItemStack.EMPTY));
                }
            } else {
                formula = null;
            }
        }));
        for (int i = 0; i < 9; i++) {
            int index = i;
            container.track(SyncableItemStack.create(() -> formula == null ? ItemStack.EMPTY : formula.input.get(index), stack -> {
                if (!stack.isEmpty() && formula == null && isRemote()) {
                    //If we are on the client (which we should be when setting anyway) and we don't have a formula yet
                    // but should, then create an empty formula. Also make sure it isn't just us trying to clear the formula slot
                    formula = new RecipeFormula(getLevel(), NonNullList.withSize(9, ItemStack.EMPTY));
                }
                if (formula != null) {
                    formula.setStack(getLevel(), index, stack);
                }
            }));
        }
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private ItemStack getCraftingInputSlot(int slot) throws ComputerException {
        if (slot < 0 || slot >= craftingGridSlots.size()) {
            throw new ComputerException("Crafting Input Slot '%d' is out of bounds, must be between 0 and %d.", slot, craftingGridSlots.size());
        }
        return craftingGridSlots.get(slot).getStack();
    }

    @ComputerMethod
    private int getCraftingOutputSlots() {
        return outputSlots.size();
    }

    @ComputerMethod
    private ItemStack getCraftingOutputSlot(int slot) throws ComputerException {
        int size = getCraftingOutputSlots();
        if (slot < 0 || slot >= size) {
            throw new ComputerException("Crafting Output Slot '%d' is out of bounds, must be between 0 and %d.", slot, size);
        }
        return outputSlots.get(slot).getStack();
    }

    @ComputerMethod
    private boolean hasValidFormula() {
        return formula != null && formula.isValidFormula();
    }

    @ComputerMethod(nameOverride = "getSlots")
    private int computerGetSlots() {
        return inputSlots.size();
    }

    @ComputerMethod
    private ItemStack getItemInSlot(int slot) throws ComputerException {
        int size = computerGetSlots();
        if (slot < 0 || slot >= size) {
            throw new ComputerException("Slot '%d' is out of bounds, must be between 0 and %d.", slot, size);
        }
        return inputSlots.get(slot).getStack();
    }

    @ComputerMethod(nameOverride = "encodeFormula")
    private void computerEncodeFormula() throws ComputerException {
        validateSecurityIsPublic();
        ItemStack formulaStack = formulaSlot.getStack();
        if (formulaStack.isEmpty() || !(formulaStack.getItem() instanceof ItemCraftingFormula craftingFormula)) {
            throw new ComputerException("No formula found.");
        } else if (formula != null && formula.isValidFormula() || craftingFormula.getInventory(formulaStack) != null) {
            throw new ComputerException("Formula has already been encoded.");
        } else if (!hasRecipe()) {
            throw new ComputerException("Encoding formulas require that there is a valid recipe to actually encode.");
        }
        encodeFormula();
    }

    @ComputerMethod
    private void fillOrEmptyGrid() throws ComputerException {
        validateSecurityIsPublic();
        if (autoMode) {
            throw new ComputerException("Filling/Emptying the grid requires Auto-Mode to be disabled.");
        }
        moveItems();
    }

    private void validateCanCraft() throws ComputerException {
        validateSecurityIsPublic();
        if (!hasRecipe()) {
            throw new ComputerException("Unable to perform craft as there is currently no matching recipe in the grid.");
        } else if (autoMode) {
            throw new ComputerException("Unable to perform craft as Auto-Mode is enabled.");
        }
    }

    @ComputerMethod
    private void craftSingleItem() throws ComputerException {
        validateCanCraft();
        craftSingle();
    }

    @ComputerMethod
    private void craftAvailableItems() throws ComputerException {
        validateCanCraft();
        craftAll();
    }

    private void validateHasValidFormula(String operation) throws ComputerException {
        validateSecurityIsPublic();
        if (!hasValidFormula()) {
            throw new ComputerException("%s requires a valid formula.", operation);
        }
    }

    @ComputerMethod(nameOverride = "getStockControl")
    private boolean computerGetStockControl() throws ComputerException {
        validateHasValidFormula("Stock Control");
        return getStockControl();
    }

    @ComputerMethod
    private void setStockControl(boolean mode) throws ComputerException {
        validateHasValidFormula("Stock Control");
        if (stockControl != mode) {
            toggleStockControl();
        }
    }

    @ComputerMethod(nameOverride = "getAutoMode")
    private boolean computerGetAutoMode() throws ComputerException {
        validateHasValidFormula("Auto-Mode");
        return getAutoMode();
    }

    @ComputerMethod
    private void setAutoMode(boolean mode) throws ComputerException {
        validateHasValidFormula("Auto-Mode");
        if (autoMode != mode) {
            nextMode();
        }
    }
    //End methods IComputerTile
}