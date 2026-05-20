package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.IContainerHolder;
import mekanism.common.capabilities.holder.MekContainerHelper;
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
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityFormulaicAssemblicator extends TileEntityConfigurableMachine implements IHasMode {

    public static final Predicate<ItemResource> FORMULA_SLOT_VALIDATOR = MekanismItems.CRAFTING_FORMULA::is;
    private static final NonNullList<ItemStack> EMPTY_LIST = NonNullList.create();

    private static final int BASE_TICKS_REQUIRED = 2 * SharedConstants.TICKS_PER_SECOND;

    private int ticksRequired = BASE_TICKS_REQUIRED;
    private int operatingTicks;
    private boolean usedEnergy = false;
    private boolean autoMode = false;
    private boolean isRecipe = false;
    private boolean stockControl = false;
    private boolean needsOrganize = true; //organize on load
    private boolean canTryToMove = true; //allow trying to move on load
    private final ItemResource[] stockControlMap = new ItemResource[18];

    private int pulseOperations;

    @NotNull
    public RecipeFormula formula = RecipeFormula.EMPTY;
    @Nullable
    private RecipeHolder<CraftingRecipe> cachedRecipe = null;
    @SyntheticComputerMethod(getter = "getExcessRemainingItems")
    NonNullList<ItemStack> lastRemainingItems = EMPTY_LIST;

    private ItemResource lastFormulaStack = ItemResource.EMPTY;
    private ItemStack lastOutputStack = ItemStack.EMPTY;

    private MachineEnergyContainer<TileEntityFormulaicAssemblicator> energyContainer;
    private List<IInventorySlot> craftingGridSlots;
    private List<IInventorySlot> inputSlots;
    private List<IInventorySlot> outputSlots;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFormulaItem", docPlaceholder = "formula slot")
    BasicInventorySlot formulaSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityFormulaicAssemblicator(BlockPos pos, BlockState state) {
        super(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, pos, state);
        configComponent.setupItemIOConfig(inputSlots, outputSlots, energySlot, false);
        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            //Expose formula slot via extra
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, formulaSlot));
        }
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
    }

    @NotNull
    @Override
    protected IContainerHolder<IEnergyContainer> getInitialEnergyContainers(IContentsListener listener) {
        MekContainerHelper<IEnergyContainer> builder = MekContainerHelper.forSideWithEnergyConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        craftingGridSlots = new ArrayList<>();
        inputSlots = new ArrayList<>();
        outputSlots = new ArrayList<>();
        IContentsListener inputSlotChanged = () -> {
            listener.onContentsChanged();
            //If an input slot changes allow trying to move items to the crafting grid again as potentially we have something that can be moved
            // and if we have stock control enabled, allow attempting to re-organize the inventory
            needsOrganize = stockControl;
            canTryToMove = true;
        };
        IContentsListener listenAndRecheckRecipe = () -> {
            listener.onContentsChanged();
            recalculateRecipe();
        };

        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSideWithItemConfig(this);
        //If the formula slot changes we want to make sure to recheck the recipe
        builder.addContainer(formulaSlot = BasicInventorySlot.at(FORMULA_SLOT_VALIDATOR, listenAndRecheckRecipe, 6, 26, 1))
              .setSlotOverlay(SlotOverlay.FORMULA);
        for (int slotY = 0; slotY < 2; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                int index = slotY * 9 + slotX;
                InputInventorySlot inputSlot = InputInventorySlot.at(itemType -> {
                    //Is item valid
                    if (formula.isEmpty()) {
                        return true;
                    } else if (!formula.valid()) {
                        return false;
                    } else if (stockControl) {
                        ItemResource stockItem = stockControlMap[index];
                        if (!stockItem.isEmpty()) {
                            return stockItem.equals(itemType);
                        }
                    }
                    return formula.isValidIngredient(level, itemType);
                }, ConstantPredicates.alwaysTrue(), inputSlotChanged, 8 + slotX * 18, 98 + slotY * 18);
                inputSlots.add(builder.addContainer(inputSlot));
            }
        }
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 3; slotX++) {
                //If a crafting slot changes then we want to make sure that we recheck the recipe
                IInventorySlot craftingSlot = FormulaicCraftingSlot.at(this::getAutoMode, listenAndRecheckRecipe, 26 + slotX * 18, 17 + slotY * 18);
                craftingGridSlots.add(builder.addContainer(craftingSlot));
            }
        }
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 2; slotX++) {
                OutputInventorySlot outputSlot = OutputInventorySlot.at(listener, 116 + slotX * 18, 17 + slotY * 18);
                outputSlots.add(builder.addContainer(outputSlot));
            }
        }
        //Add the energy slot after adding the other slots so that it has the lowest priority in shift clicking
        builder.addContainer(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 152, 76));
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
            if (!formula.isEmpty() && stockControl) {
                //Ensure stock control is loaded before our first tick in case something inserting ticks before our first tick
                // and inserts into the wrong slots
                buildStockControlMap();
            }
        }
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
            //Invalidate the cached recipe and recalculate
            cachedRecipe = null;
            recalculateRecipe();
        }
        if (!formula.isEmpty() && stockControl && needsOrganize) {
            buildStockControlMap();
            organizeStock();
            //Mark as no longer needing to organize after organizing it so that it rearranging things doesn't cause it to organize again
            needsOrganize = false;
        }
        energySlot.fillContainerOrConvert();
        if (getControlType() != RedstoneControl.PULSE) {
            pulseOperations = 0;
        } else if (canFunction()) {
            pulseOperations++;
        }
        checkFormula();
        if (autoMode && formula.isEmpty()) {
            nextMode();
        }

        long clientEnergyUsed = 0L;
        if (autoMode && !formula.isEmpty() && ((getControlType() == RedstoneControl.PULSE && pulseOperations > 0) || canFunction())) {
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
                    long energyPerTick = energyContainer.getEnergyPerTick();
                    try (Transaction transaction = Transaction.openRoot()) {
                        if (energyContainer.extract(energyPerTick, transaction, AutomationType.INTERNAL) == energyPerTick) {
                            clientEnergyUsed = energyPerTick;
                            transaction.commit();
                            operatingTicks++;
                        }
                    }
                }
            } else {
                operatingTicks = 0;
            }
        } else {
            operatingTicks = 0;
        }
        usedEnergy = clientEnergyUsed > 0L;
        return sendUpdatePacket;
    }

    private void checkFormula() {
        ItemResource formulaStack = formulaSlot.resource();
        FormulaAttachment attachment = formulaStack.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY);
        if (!attachment.isEmpty() && !attachment.invalid()) {
            if (formula.isEmpty() || !lastFormulaStack.equals(formulaStack)) {
                formula = loadFormula(formulaStack, attachment);
            }
        } else {
            formula = RecipeFormula.EMPTY;
        }
        //Note: Because loading ends up overriding the set stack, we can't just use our stored variable
        // and have to look it back up instead
        lastFormulaStack = formulaSlot.resource();
    }

    //Note: Assumes attachment is not invalid
    private RecipeFormula loadFormula(ItemResource formulaStack, FormulaAttachment attachment) {
        RecipeFormula recipe = RecipeFormula.create(level, attachment);
        if (recipe.valid()) {
            if (!formula.isEmpty() && !formula.equals(recipe)) {
                //If we are going from one formula to a different one, reset the operating ticks
                // Note: We don't reset the ticks if we are going from none to having one, as we
                // want to persist how many ticks we had when we load from save
                operatingTicks = 0;
            }
            return recipe;
        }
        formulaSlot.setContents(formulaStack.with(MekanismDataComponents.FORMULA_HOLDER, attachment.asInvalid()), formulaSlot.amountAsLong(), null);
        return RecipeFormula.EMPTY;
    }

    private void recalculateRecipe() {
        if (level != null && !isRemote()) {
            boolean wasRecipe = isRecipe;
            ItemStack previousOutput = lastOutputStack;
            NonNullList<ItemStack> previousRemaining = lastRemainingItems;
            if (hasValidFormula()) {
                RecipeHolder<CraftingRecipe> recipe = formula.recipe();
                if (recipe == null) {
                    isRecipe = false;
                    lastOutputStack = ItemStack.EMPTY;
                } else {
                    //Should always be a 3x3 grid for the size
                    CraftingInput input = MekanismUtils.getCraftingInputSlots(3, 3, craftingGridSlots, true).input();
                    isRecipe = recipe.value().matches(input, level);
                    if (isRecipe) {
                        lastOutputStack = recipe.value().assemble(input);
                        lastRemainingItems = recipe.value().getRemainingItems(input);
                    } else {
                        //TODO: Do we need to clear the last remaining items?
                        lastOutputStack = ItemStack.EMPTY;
                    }
                }
            } else {
                //Should always be 9 for the size
                CraftingInput craftingInput = MekanismUtils.getCraftingInputSlots(3, 3, craftingGridSlots, true).input();
                lastRemainingItems = EMPTY_LIST;
                if (cachedRecipe == null || !cachedRecipe.value().matches(craftingInput, level)) {
                    cachedRecipe = MekanismRecipeType.getRecipeFor(RecipeType.CRAFTING, craftingInput, level).orElse(null);
                }
                if (cachedRecipe == null) {
                    lastOutputStack = ItemStack.EMPTY;
                } else {
                    lastOutputStack = cachedRecipe.value().assemble(craftingInput);
                    //Note: Because we don't currently do any replacement of remaining items, we don't need to keep track of where the recipe
                    // was positioned for purposes of replacing things with the remaining items
                    lastRemainingItems = cachedRecipe.value().getRemainingItems(craftingInput);
                }
                isRecipe = !lastOutputStack.isEmpty();
            }
            boolean recipeChanged = false;
            if (isRecipe != wasRecipe || !ItemStack.matches(lastOutputStack, previousOutput) || lastRemainingItems.size() != previousRemaining.size()) {
                recipeChanged = true;
            } else {
                for (int i = 0; i < lastRemainingItems.size(); i++) {
                    if (!ItemStack.matches(lastRemainingItems.get(i), previousRemaining.get(i))) {
                        recipeChanged = true;
                        break;
                    }
                }
            }
            if (recipeChanged) {
                needsOrganize = true;
                canTryToMove = true;
            }
        }
    }

    private boolean doSingleCraft() {
        if (lastOutputStack.isEmpty()) {
            return false;
        }
        ItemResource output = ItemResource.of(lastOutputStack);
        int outputAmount = lastOutputStack.count();
        try (Transaction transaction = Transaction.openRoot()) {
            if (!tryMoveToOutput(output, outputAmount, transaction)) {
                //Can't fit it all, bail and revert changes
                return false;
            }
            for (ItemStack remainingItem : lastRemainingItems) {
                //TODO: Check if it matters that we are not actually updating the list of remaining items?
                // The better solution would be to not allow continuing until we moved output AND all remaining items
                // instead of trying to move all at once??
                //TODO - 26.1: validate we don't have to clear the list anywhere
                if (!remainingItem.isEmpty() && !tryMoveToOutput(ItemResource.of(remainingItem), remainingItem.count(), transaction)) {
                    //Can't fit it all, bail and revert changes
                    return false;
                }
            }
            for (IInventorySlot craftingSlot : craftingGridSlots) {
                if (!craftingSlot.isEmpty()) {
                    int extracted = craftingSlot.extract(craftingSlot.resource(), 1, transaction, AutomationType.INTERNAL);
                    if (extracted == 0) {
                        //Something went horribly wrong when removing the inputs from the input slots, bail and revert changes
                        return false;
                    }
                }
            }
            transaction.commit();
        }
        if (!formula.isEmpty()) {
            moveItemsToGrid();
        }
        return true;
    }

    public boolean craftSingle() {
        boolean canOperate = true;
        if (!formula.isEmpty() && !formula.matches(getLevel(), craftingGridSlots)) {
            canOperate = moveItemsToGrid();
        }
        return canOperate && doSingleCraft();
    }

    private boolean moveItemsToGrid() {
        if (!canTryToMove) {
            return false;
        }
        boolean canOperate = true;
        for (int i = 0; i < craftingGridSlots.size(); i++) {
            IInventorySlot recipeSlot = craftingGridSlots.get(i);
            if (!formula.isIngredientInPos(level, recipeSlot.resource(), i)) {
                if (!tryMoveToGrid(recipeSlot, i)) {
                    canOperate = false;
                }
            }
        }
        if (!canOperate) {
            //If we failed to move items, then we know none of the currently stored items are valid for the recipe,
            // so we can skip trying to move them until something changes
            canTryToMove = false;
        }
        return canOperate;
    }

    private boolean tryMoveToGrid(IInventorySlot recipeSlot, int i) {
        ItemResource resource = recipeSlot.resource();
        int stored = recipeSlot.amountAsInt();
        try (Transaction transaction = Transaction.openRoot()) {
            if (!resource.isEmpty()) {
                //If the current input doesn't match, start by moving it to the input slots
                int extracted = recipeSlot.extract(resource, stored, transaction, AutomationType.INTERNAL);
                if (extracted < stored) {
                    //Cannot extract from the slot, mark that we failed to handle at least one of the slots, and continue onto the next one
                    // Theoretically this if statement should never be true as it always returns true for if extracting is allowed
                    return false;
                }
                int inserted = InventoryUtils.insertItem(inputSlots, resource, stored, transaction, AutomationType.INTERNAL);
                if (inserted < stored) {
                    //Failed to insert the removed contents into the input slots, so mark that we failed to handle at least one of the slots,
                    // and continue onto the next one
                    return false;
                }
            }
            //Commit being able to move the item out of the crafting grid so that even if we are unable to find a replacement stack,
            // then the UI is able to display the expected type instead of it being covered by the invalid one
            transaction.commit();
        }
        //Note: If we haven't returned and thus rolled back our transaction due to failure, that means the recipe slot should be empty here
        Object2BooleanMap<ItemResource> checkedTypes = new Object2BooleanOpenHashMap<>();
        for (IInventorySlot stockSlot : inputSlots) {
            //The stack stored in the stock inventory
            if (!stockSlot.isEmpty()) {
                ItemResource stockType = stockSlot.resource();
                //If we already checked this stack type for being valid in the recipe for this position, we can skip checking it again
                boolean isValidIngredient;
                if (checkedTypes.containsKey(stockType)) {
                    isValidIngredient = checkedTypes.getBoolean(stockType);
                } else {
                    isValidIngredient = formula.isIngredientInPos(level, stockType, i);
                    //Mark whether that type of item is valid for the ingredient
                    checkedTypes.put(stockType, isValidIngredient);
                }
                if (isValidIngredient) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        int extracted = stockSlot.extract(stockType, 1, transaction, AutomationType.INTERNAL);
                        if (extracted == 0) {
                            //Continue to next slot if for some reason we were unable to extract the contents from it
                            // (theoretically this should not be possible with the predicates we define on the stock slots)
                            continue;
                        } else if (recipeSlot.insert(stockType, 1, transaction, AutomationType.INTERNAL) == 1) {
                            //If we were able to extract from the stock slot and insert into the recipe slot, commit our transaction
                            // and return true for being able to operate
                            transaction.commit();
                            return true;
                        }
                        //Otherwise we continue to try and see if any of our other types work
                        //Note: We also mark the type as false, as we aren't actually able to insert it into the slot
                        // so then even if it would be valid for the recipe, it isn't actually valid
                        // (Due to the predicates for our slots, I don't think this should ever be the case)
                        checkedTypes.put(stockType, false);
                    }
                }
            }
        }
        //We didn't find a stack to replace it with, that means we won't be able to operate on our recipe
        return false;
    }

    public void craftAll() {
        //TODO: Can we somehow optimize this, maybe by moving multiple items at once
        while (craftSingle()) {
        }
    }

    public void fillGrid() {
        if (!formula.isEmpty()) {
            moveItemsToGrid();
        }
    }

    public void emptyGrid() {
        if (formula.isEmpty()) {
            moveItemsToInput(true);
        }
    }

    private void moveItemsToInput(boolean forcePush) {
        for (int i = 0; i < craftingGridSlots.size(); i++) {
            IInventorySlot recipeSlot = craftingGridSlots.get(i);
            if (recipeSlot.isEmpty()) {
                continue;
            }
            ItemResource resource = recipeSlot.resource();
            if (forcePush || !formula.isEmpty() && !formula.isIngredientInPos(getLevel(), resource, i)) {
                try (Transaction transaction = Transaction.openRoot()) {
                    int inserted = InventoryUtils.insertItem(inputSlots, resource, recipeSlot.amountAsInt(), transaction, AutomationType.INTERNAL);
                    if (inserted > 0 && recipeSlot.extract(resource, inserted, transaction, AutomationType.INTERNAL) == inserted) {
                        //If we are able to fully extract from the recipe slot the amount that we inserted into the input slots
                        // then commit the change. We rely on the fact that our recipe slot should always be able to extract
                        // so the limiting factor of this should be what can be inserted into the input slots
                        transaction.commit();
                    }
                }
            }
        }
    }

    @Override
    public void nextMode() {
        if (autoMode) {
            operatingTicks = 0;
            autoMode = false;
            markForSave();
        } else if (!formula.isEmpty()) {
            moveItemsToInput(false);
            autoMode = true;
            markForSave();
        }
    }

    @Override
    public void previousMode() {
        //We only have two modes just flip it
        nextMode();
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
        if (!isRemote() && !formula.isEmpty()) {
            stockControl = !stockControl;
            if (stockControl) {
                organizeStock();
            }
            //We either just organized, so can mark it as not actually needing organize in case things changed,
            // or we don't want to organize so if we had a queued organization we can just remove it
            needsOrganize = false;
        }
    }

    private void organizeStock() {
        if (formula.isEmpty()) {
            return;
        }
        // build map of what items we have to organize
        // Note: We keep track of the order so that it is more consistent
        Object2IntMap<ItemResource> storedMap = new Object2IntLinkedOpenHashMap<>();
        for (IInventorySlot inputSlot : inputSlots) {
            if (!inputSlot.isEmpty()) {
                storedMap.mergeInt(inputSlot.resource(), inputSlot.amountAsInt(), Integer::sum);
            }
        }
        // place items into respective controlled slots
        IntSet unused = new IntArraySet(stockControlMap.length);
        for (int i = 0; i < inputSlots.size(); i++) {
            ItemResource itemType = stockControlMap[i];
            if (itemType == null) {
                unused.add(i);
            } else {
                IInventorySlot slot = inputSlots.get(i);
                int stored = storedMap.getInt(itemType);
                if (stored > 0) {
                    int count = Math.min(itemType.getMaxStackSize(), stored);
                    if (count == stored) {
                        storedMap.removeInt(itemType);
                    } else {
                        storedMap.put(itemType, stored - count);
                    }
                    slot.setContents(itemType, count, null);
                } else {
                    //If we don't have the item stored anymore (already filled all previous slots with it),
                    // then we need to empty the slot as the items in it has been moved to a more "optimal" slot
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
                slot.setEmpty();
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

    //TODO - 26.1: Replace this with transactionally moving things around?
    private boolean setSlotIfChanged(Object2IntMap<ItemResource> storedMap, IInventorySlot inputSlot) {
        boolean empty = false;
        ObjectIterator<Object2IntMap.Entry<ItemResource>> iterator = Object2IntMaps.fastIterator(storedMap);
        Object2IntMap.Entry<ItemResource> next = iterator.next();
        ItemResource itemType = next.getKey();
        int stored = next.getIntValue();
        int count = Math.min(itemType.getMaxStackSize(), stored);
        if (count == stored) {
            iterator.remove();
            empty = storedMap.isEmpty();
        } else {
            next.setValue(stored - count);
        }
        inputSlot.setContents(itemType, count, null);
        return empty;
    }

    private void buildStockControlMap() {
        if (formula.isEmpty()) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            int j = i * 2;
            ItemResource itemType = ItemResource.of(formula.getInputStack(i));
            stockControlMap[j] = itemType;
            stockControlMap[j + 1] = itemType;
        }
    }

    private boolean tryMoveToOutput(ItemResource itemType, int amount, TransactionContext transaction) {
        //Try to insert the item (simulating as needed), and overwrite our local reference to point to the remainder
        // We can then continue on to the next slot if we did not fit it all and try to insert it.
        // The logic is relatively simple due to only having one stack we are trying to insert, so we don't have to worry
        // about the fact the slot doesn't actually get updated if we simulated, and then is invalid for the next simulation
        int inserted = InventoryUtils.insertItem(outputSlots, itemType, amount, transaction, AutomationType.INTERNAL);
        return inserted == amount;
    }

    public void encodeFormula() {
        if (formulaSlot.isEmpty()) {
            return;
        }
        ItemResource currentResource = formulaSlot.resource();
        FormulaAttachment formulaAttachment = currentResource.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY);
        if (formulaAttachment.isEmpty()) {
            RecipeFormula formula = RecipeFormula.create(level, craftingGridSlots);
            if (formula.valid()) {
                formulaSlot.setContents(currentResource.with(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.create(formula)), formulaSlot.amountAsLong(), null);
            }
        }
    }

    @Override
    public void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        autoMode = input.getBooleanOr(SerializationConstants.AUTO, autoMode);
        operatingTicks = input.getIntOr(SerializationConstants.PROGRESS, operatingTicks);
        pulseOperations = input.getIntOr(SerializationConstants.PULSE, pulseOperations);
        stockControl = input.getBooleanOr(SerializationConstants.STOCK_CONTROL, stockControl);
    }

    @Override
    public void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean(SerializationConstants.AUTO, autoMode);
        output.putInt(SerializationConstants.PROGRESS, operatingTicks);
        output.putInt(SerializationConstants.PULSE, pulseOperations);
        output.putBoolean(SerializationConstants.STOCK_CONTROL, stockControl);
    }

    @Override
    public boolean supportsMode(RedstoneControl mode) {
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

    public boolean usedEnergy() {
        return usedEnergy;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::getAutoMode, value -> autoMode = value));
        container.track(SyncableInt.create(this::getOperatingTicks, value -> operatingTicks = value));
        container.track(SyncableInt.create(this::getTicksRequired, value -> ticksRequired = value));
        container.track(SyncableBoolean.create(this::hasRecipe, value -> isRecipe = value));
        container.track(SyncableBoolean.create(this::getStockControl, value -> stockControl = value));
        container.track(SyncableBoolean.create(this::usedEnergy, value -> usedEnergy = value));
        for (int i = 0; i < 9; i++) {
            int index = i;
            container.track(SyncableItemStack.create(() -> formula.getInputStack(index), stack -> formula = formula.withStack(getLevel(), index, stack)));
        }
    }

    @ComputerMethod
    public boolean hasValidFormula() {
        return !formula.isEmpty() && formula.valid();
    }

    //Methods relating to IComputerTile
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getCraftingInputSlot", docPlaceholder = "crafting input slot")
    IInventorySlot getCraftingInputSlot(int slot) throws ComputerException {
        if (slot < 0 || slot >= craftingGridSlots.size()) {
            throw new ComputerException("Crafting Input Slot '%d' is out of bounds, must be between 0 and %d.", slot, craftingGridSlots.size());
        }
        return craftingGridSlots.get(slot);
    }

    @ComputerMethod
    int getCraftingOutputSlots() {
        return outputSlots.size();
    }

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getCraftingOutputSlot", docPlaceholder = "crafting output slot")
    IInventorySlot getCraftingOutputSlot(int slot) throws ComputerException {
        int size = getCraftingOutputSlots();
        if (slot < 0 || slot >= size) {
            throw new ComputerException("Crafting Output Slot '%d' is out of bounds, must be between 0 and %d.", slot, size);
        }
        return outputSlots.get(slot);
    }

    @ComputerMethod(nameOverride = "getSlots")
    int computerGetSlots() {
        return inputSlots.size();
    }

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getItemInSlot", docPlaceholder = "input slot")
    IInventorySlot getCorrespondingSlot(int slot) throws ComputerException {
        int size = computerGetSlots();
        if (slot < 0 || slot >= size) {
            throw new ComputerException("Slot '%d' is out of bounds, must be between 0 and %d.", slot, size);
        }
        return inputSlots.get(slot);
    }

    @ComputerMethod(nameOverride = "encodeFormula", requiresPublicSecurity = true, methodDescription = "Requires an unencoded formula in the formula slot and a valid recipe")
    void computerEncodeFormula() throws ComputerException {
        validateSecurityIsPublic();
        FormulaAttachment formulaAttachment = formulaSlot.resource().getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY);
        if (formulaAttachment.isEmpty()) {
            throw new ComputerException("No formula found.");
        } else if (hasValidFormula() || formulaAttachment.hasItems()) {
            throw new ComputerException("Formula has already been encoded.");
        } else if (!hasRecipe()) {
            throw new ComputerException("Encoding formulas require that there is a valid recipe to actually encode.");
        }
        encodeFormula();
    }

    @ComputerMethod(nameOverride = "emptyGrid", requiresPublicSecurity = true, methodDescription = "Requires auto mode to be disabled")
    void computerEmptyGrid() throws ComputerException {
        validateSecurityIsPublic();
        if (autoMode) {
            throw new ComputerException("Emptying the grid requires Auto-Mode to be disabled.");
        }
        emptyGrid();
    }

    @ComputerMethod(nameOverride = "fillGrid", requiresPublicSecurity = true, methodDescription = "Requires auto mode to be disabled")
    void computerFillGrid() throws ComputerException {
        validateSecurityIsPublic();
        if (autoMode) {
            throw new ComputerException("Filling the grid requires Auto-Mode to be disabled.");
        }
        fillGrid();
    }

    private void validateCanCraft() throws ComputerException {
        validateSecurityIsPublic();
        if (!hasRecipe()) {
            throw new ComputerException("Unable to perform craft as there is currently no matching recipe in the grid.");
        } else if (autoMode) {
            throw new ComputerException("Unable to perform craft as Auto-Mode is enabled.");
        }
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires recipe and auto mode to be disabled")
    void craftSingleItem() throws ComputerException {
        validateCanCraft();
        craftSingle();
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires recipe and auto mode to be disabled")
    void craftAvailableItems() throws ComputerException {
        validateCanCraft();
        craftAll();
    }

    private void validateHasValidFormula(String operation) throws ComputerException {
        validateSecurityIsPublic();
        if (!hasValidFormula()) {
            throw new ComputerException("%s requires a valid formula.", operation);
        }
    }

    @ComputerMethod(nameOverride = "getStockControl", requiresPublicSecurity = true, methodDescription = "Requires valid encoded formula")
    boolean computerGetStockControl() throws ComputerException {
        validateHasValidFormula("Stock Control");
        return getStockControl();
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires valid encoded formula")
    void setStockControl(boolean mode) throws ComputerException {
        validateHasValidFormula("Stock Control");
        if (stockControl != mode) {
            toggleStockControl();
        }
    }

    @ComputerMethod(nameOverride = "getAutoMode", requiresPublicSecurity = true, methodDescription = "Requires valid encoded formula")
    boolean computerGetAutoMode() throws ComputerException {
        validateHasValidFormula("Auto-Mode");
        return getAutoMode();
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires valid encoded formula")
    void setAutoMode(boolean mode) throws ComputerException {
        validateHasValidFormula("Auto-Mode");
        if (autoMode != mode) {
            nextMode();
        }
    }
    //End methods IComputerTile
}
