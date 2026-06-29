package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.capabilities.holder.single.SingleConfigHolder;
import mekanism.common.component.FormulaComponent;
import mekanism.common.component.containers.type.ContainerType;
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
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

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
    private final ItemResource[] stockControlMap = Util.make(new ItemResource[18], map -> Arrays.fill(map, ItemResource.EMPTY));

    private int pulseOperations;

    public RecipeFormula formula = RecipeFormula.EMPTY;
    @Nullable
    private RecipeHolder<CraftingRecipe> cachedRecipe = null;
    @SyntheticComputerMethod(getter = "getExcessRemainingItems")
    NonNullList<ItemStack> lastRemainingItems = EMPTY_LIST;

    private ItemResource lastFormulaStack = ItemResource.EMPTY;
    private ItemStack lastOutputStack = ItemStack.EMPTY;

    @UnknownNullability//Initialized via getInitialEnergyContainer
    private MachineEnergyContainer<TileEntityFormulaicAssemblicator> energyContainer;
    private final List<IInventorySlot> craftingGridSlots;
    private final List<IInventorySlot> inputSlots;
    private final List<IInventorySlot> outputSlots;
    /// For in inserting to input slots and stacking before going to empty slots
    private final IMekanismResourceHandler<ItemResource, IInventorySlot> directInputHandler;
    /// For in inserting to output slots and stacking before going to empty slots
    private final IMekanismResourceHandler<ItemResource, IInventorySlot> directOutputHandler;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFormulaItem", docPlaceholder = "formula slot")
    BasicInventorySlot formulaSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityFormulaicAssemblicator(BlockPos pos, BlockState state) {
        craftingGridSlots = new ArrayList<>();
        inputSlots = new ArrayList<>();
        outputSlots = new ArrayList<>();
        super(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, pos, state);
        configComponent.setupItemIOConfig(inputSlots, outputSlots, energySlot, false);
        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            //Expose formula slot via extra
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, formulaSlot));
        }
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
        directInputHandler = () -> inputSlots;
        directOutputHandler = () -> outputSlots;
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = MachineEnergyContainer.input(this, listener);
        return SingleConfigHolder.energy(energyContainer, this);
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
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
        builder.addContainer(formulaSlot = BasicInventorySlot.at(1, FORMULA_SLOT_VALIDATOR, listenAndRecheckRecipe, 6, 26))
              .setSlotOverlay(SlotOverlay.FORMULA);
        for (int slotY = 0; slotY < 2; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                int index = slotY * 9 + slotX;
                inputSlots.add(builder.addContainer(InputInventorySlot.at((itemType, _) -> {
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
                }, ConstantPredicates.alwaysTrue(), inputSlotChanged, 8 + slotX * 18, 98 + slotY * 18)));
            }
        }
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 3; slotX++) {
                //If a crafting slot changes then we want to make sure that we recheck the recipe
                craftingGridSlots.add(builder.addContainer(FormulaicCraftingSlot.at(this::getAutoMode, listenAndRecheckRecipe, 26 + slotX * 18, 17 + slotY * 18)));
            }
        }
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 2; slotX++) {
                outputSlots.add(builder.addContainer(OutputInventorySlot.at(listener, 116 + slotX * 18, 17 + slotY * 18)));
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
        if (level != null && !level.isClientSide()) {
            checkFormula(level);
            recalculateRecipe();
            if (!formula.isEmpty() && stockControl) {
                //Ensure stock control is loaded before our first tick in case something inserting ticks before our first tick
                // and inserts into the wrong slots
                buildStockControlMap();
            }
        }
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        if (Mekanism.worldTickHandler.flushTagAndRecipeCaches) {
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
        energySlot.fillContainerOrConvert(null);
        if (getControlType() != RedstoneControl.PULSE) {
            pulseOperations = 0;
        } else if (canFunction()) {
            pulseOperations++;
        }
        checkFormula(level);
        if (autoMode && formula.isEmpty()) {
            nextMode();
        }

        int clientEnergyUsed = 0;
        if (autoMode && !formula.isEmpty() && ((getControlType() == RedstoneControl.PULSE && pulseOperations > 0) || canFunction())) {
            boolean canOperate = true;
            if (!isRecipe) {
                canOperate = moveItemsToGrid(level);
            }
            if (canOperate) {
                isRecipe = true;
                if (operatingTicks >= ticksRequired) {
                    if (doSingleCraft(level)) {
                        operatingTicks = 0;
                        if (pulseOperations > 0) {
                            pulseOperations--;
                        }
                    }
                } else {
                    int energyPerTick = energyContainer.getEnergyPerTick();
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
        usedEnergy = clientEnergyUsed > 0;
        return sendUpdatePacket;
    }

    private void checkFormula(Level level) {
        ItemResource formulaStack = formulaSlot.resource();
        FormulaComponent attachment = formulaStack.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaComponent.EMPTY);
        if (!attachment.isEmpty() && !attachment.invalid()) {
            if (formula.isEmpty() || !lastFormulaStack.equals(formulaStack)) {
                formula = loadFormula(level, formulaStack, attachment);
            }
        } else {
            formula = RecipeFormula.EMPTY;
        }
        //Note: Because loading ends up overriding the set stack, we can't just use our stored variable
        // and have to look it back up instead
        lastFormulaStack = formulaSlot.resource();
    }

    //Note: Assumes attachment is not invalid
    private RecipeFormula loadFormula(Level level, ItemResource formulaStack, FormulaComponent attachment) {
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
        if (level != null && !level.isClientSide()) {
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

    private boolean doSingleCraft(Level level) {
        if (lastOutputStack.isEmpty()) {
            return false;
        }
        ItemResource output = ItemResource.of(lastOutputStack);
        int outputAmount = lastOutputStack.count();
        try (Transaction transaction = Transaction.openRoot()) {
            if (directOutputHandler.insert(output, outputAmount, transaction) < outputAmount) {
                //Can't fit it all, bail and revert changes
                return false;
            }
            for (ItemStack remainingItem : lastRemainingItems) {
                //TODO: Check if it matters that we are not actually updating the list of remaining items?
                // The better solution would be to not allow continuing until we moved output AND all remaining items
                // instead of trying to move all at once??
                //TODO - 26.2: validate we don't have to clear the list anywhere
                int remainingAmount = remainingItem.count();
                if (remainingAmount > 0 && directOutputHandler.insert(ItemResource.of(remainingItem), remainingAmount, transaction) < remainingAmount) {
                    //Can't fit it all, bail and revert changes
                    return false;
                }
            }
            for (IInventorySlot craftingSlot : craftingGridSlots) {
                if (!craftingSlot.isEmpty()) {
                    if (craftingSlot.extract(craftingSlot.resource(), 1, transaction, AutomationType.INTERNAL) == 0) {
                        //Something went horribly wrong when removing the inputs from the input slots, bail and revert changes
                        return false;
                    }
                }
            }
            transaction.commit();
        }
        if (!formula.isEmpty()) {
            moveItemsToGrid(level);
        }
        return true;
    }

    public boolean craftSingle(Level level) {
        boolean canOperate = true;
        if (!formula.isEmpty() && !formula.matches(level, craftingGridSlots)) {
            canOperate = moveItemsToGrid(level);
        }
        return canOperate && doSingleCraft(level);
    }

    private boolean moveItemsToGrid(Level level) {
        if (!canTryToMove) {
            return false;
        }
        boolean canOperate = true;
        for (int i = 0; i < craftingGridSlots.size(); i++) {
            IInventorySlot recipeSlot = craftingGridSlots.get(i);
            if (!formula.isIngredientInPos(level, recipeSlot.resource(), i)) {
                if (!tryMoveToGrid(level, recipeSlot, i)) {
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

    private boolean tryMoveToGrid(Level level, IInventorySlot recipeSlot, int i) {
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
                int inserted = directInputHandler.insert(resource, stored, transaction, AutomationType.INTERNAL);
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

    public void craftAll(Level level) {
        //TODO: Can we somehow optimize this, maybe by moving multiple items at once
        while (craftSingle(level)) {
        }
    }

    public void fillGrid(Level level) {
        if (!formula.isEmpty()) {
            moveItemsToGrid(level);
        }
    }

    public void emptyGrid(Level level) {
        if (formula.isEmpty()) {
            moveItemsToInput(level, true);
        }
    }

    private void moveItemsToInput(Level level, boolean forcePush) {
        for (int i = 0; i < craftingGridSlots.size(); i++) {
            IInventorySlot recipeSlot = craftingGridSlots.get(i);
            if (recipeSlot.isEmpty()) {
                continue;
            }
            ItemResource resource = recipeSlot.resource();
            if (forcePush || !formula.isEmpty() && !formula.isIngredientInPos(level, resource, i)) {
                try (Transaction transaction = Transaction.openRoot()) {
                    int inserted = directInputHandler.insert(resource, recipeSlot.amountAsInt(), transaction, AutomationType.INTERNAL);
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
            moveItemsToInput(level, false);
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

    public void toggleStockControl(Level level) {
        if (!level.isClientSide() && !formula.isEmpty()) {
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
        try (Transaction transaction = Transaction.openRoot()) {
            int slotCount = inputSlots.size();
            // build map of what items we have to organize
            // Note: We keep track of the order so that it is more consistent
            Object2IntMap<ItemResource> storedMap = new Object2IntLinkedOpenHashMap<>();
            for (IInventorySlot inputSlot : inputSlots) {
                if (!inputSlot.isEmpty()) {
                    //Track how much of the item is stored
                    storedMap.mergeInt(inputSlot.resource(), inputSlot.amountAsInt(), Integer::sum);
                    // and then clear the contents from the slot, as we will just roll this back if something goes wrong
                    // and change listeners won't be fired if the slot ends up in the same state as it started
                    ContainerType.ITEM.clearContents(inputSlot, transaction);
                }
            }
            List<IInventorySlot> emptySlots = new ArrayList<>(slotCount);
            // place items into respective controlled slots
            for (int i = 0; i < slotCount; i++) {
                IInventorySlot slot = inputSlots.get(i);
                //TODO: Can we make use of the fact the stock control map is now sorted to optimize out having to add it to a map unless it is in the wrong slot?
                ItemResource itemType = stockControlMap[i];
                if (itemType.isEmpty()) {
                    //If this slot is uncontrolled, add it at the start of the list of empty slots
                    emptySlots.addFirst(slot);
                } else {
                    int stored = storedMap.getInt(itemType);
                    if (stored > 0) {
                        int count = Math.min(slot.capacityAsInt(itemType), stored);
                        if (count == stored) {
                            //The item has been fully handled, remove it from the map
                            storedMap.removeInt(itemType);
                        } else {
                            //Decrease how much is in the map by the amount we could take
                            storedMap.put(itemType, stored - count);
                        }
                        slot.setContents(itemType, count, transaction);
                    } else {
                        emptySlots.addLast(slot);
                    }
                }
            }
            for (ObjectIterator<Object2IntMap.Entry<ItemResource>> iterator = Object2IntMaps.fastIterator(storedMap); iterator.hasNext(); ) {
                Object2IntMap.Entry<ItemResource> entry = iterator.next();
                ItemResource itemType = entry.getKey();
                int stored = entry.getIntValue();
                //Iterate until we are out of empty slots or there is nothing still stored and pending
                for (Iterator<IInventorySlot> slotIterator = emptySlots.iterator(); stored > 0 && slotIterator.hasNext(); ) {
                    IInventorySlot slot = slotIterator.next();
                    int count = Math.min(slot.capacityAsInt(itemType), stored);
                    slot.setContents(itemType, count, transaction);
                    //Decrease how much is left by the amount the slot could accept
                    stored -= count;
                    //Remove the slot from the empty slots as it is no longer empty
                    slotIterator.remove();
                }
                if (stored > 0) {
                    Mekanism.logger.warn("Unable to organize stock, could not fit all of the current contents!?");
                    //Exit without committing
                    return;
                }
            }
            transaction.commit();
        }
    }

    //TODO - 26.2: Fix the placing into wrong slot briefly on the client side by ensuring the stock control map is synced to the client?
    private void buildStockControlMap() {
        if (formula.isEmpty()) {
            return;
        }
        int i = 0;
        Object2IntMap<ItemResource> inputs = formula.getInputs();
        for (ObjectIterator<Object2IntMap.Entry<ItemResource>> iterator = Object2IntMaps.fastIterator(inputs); iterator.hasNext(); ) {
            Object2IntMap.Entry<ItemResource> entry = iterator.next();
            ItemResource itemType = entry.getKey();
            int stored = entry.getIntValue();
            //If the item appears multiple times in the recipe, add slots for each time it appears
            while (stored > 0) {
                //Add two slots per item type
                stockControlMap[i++] = itemType;
                stockControlMap[i++] = itemType;
                stored--;
            }
        }
        //Fill the remaining slots' type as empty to treat them as unmanaged
        for (; i < stockControlMap.length; i++) {
            stockControlMap[i] = ItemResource.EMPTY;
        }
    }

    public void encodeFormula() {
        if (formulaSlot.isEmpty()) {
            return;
        }
        ItemResource currentResource = formulaSlot.resource();
        FormulaComponent formulaAttachment = currentResource.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaComponent.EMPTY);
        if (formulaAttachment.isEmpty()) {
            RecipeFormula formula = RecipeFormula.create(level, craftingGridSlots);
            if (formula.valid()) {
                formulaSlot.setContents(currentResource.with(MekanismDataComponents.FORMULA_HOLDER, FormulaComponent.create(formula)), formulaSlot.amountAsLong(), null);
            }
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        autoMode = input.getBooleanOr(SerializationConstants.AUTO, autoMode);
        operatingTicks = input.getIntOr(SerializationConstants.PROGRESS, operatingTicks);
        pulseOperations = input.getIntOr(SerializationConstants.PULSE, pulseOperations);
        stockControl = input.getBooleanOr(SerializationConstants.STOCK_CONTROL, stockControl);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
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

    @Override
    public List<Component> getInfo(Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    public MachineEnergyContainer<TileEntityFormulaicAssemblicator> energyContainer() {
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
            container.track(SyncableItemStack.create(() -> formula.getInputStack(index), stack -> formula = formula.withStack(level, index, stack)));
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
        FormulaComponent formulaAttachment = formulaSlot.resource().getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaComponent.EMPTY);
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
        emptyGrid(validateLevel());
    }

    @ComputerMethod(nameOverride = "fillGrid", requiresPublicSecurity = true, methodDescription = "Requires auto mode to be disabled")
    void computerFillGrid() throws ComputerException {
        validateSecurityIsPublic();
        if (autoMode) {
            throw new ComputerException("Filling the grid requires Auto-Mode to be disabled.");
        }
        fillGrid(validateLevel());
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
        craftSingle(validateLevel());
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires recipe and auto mode to be disabled")
    void craftAvailableItems() throws ComputerException {
        validateCanCraft();
        craftAll(validateLevel());
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
            toggleStockControl(validateLevel());
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
