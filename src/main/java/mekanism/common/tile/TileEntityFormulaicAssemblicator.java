package mekanism.common.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismBlock;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.assemblicator.RecipeFormula;
import mekanism.common.inventory.IInventorySlotHolder;
import mekanism.common.inventory.InventorySlotHelper;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FormulaInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

//TODO: 1.14, Fix dupe bug that probably has to do with container/how it "fake fills" (attempt to drag single item after placing it in center slot)
public class TileEntityFormulaicAssemblicator extends TileEntityMekanism implements ISideConfiguration, IConfigCardAccess {

    private static final NonNullList<ItemStack> EMPTY_LIST = NonNullList.create();
    public static final int SLOT_UPGRADE = 0;
    public static final int SLOT_ENERGY = 1;
    public static final int SLOT_FORMULA = 2;
    public static final int SLOT_INPUT_FIRST = 3;
    public static final int SLOT_INPUT_LAST = 20;
    public static final int SLOT_OUTPUT_FIRST = 21;
    public static final int SLOT_OUTPUT_LAST = 26;
    public static final int SLOT_CRAFT_MATRIX_FIRST = 27;
    public static final int SLOT_CRAFT_MATRIX_LAST = 35;

    public CraftingInventory dummyInv = MekanismUtils.getDummyCraftingInv();

    public int BASE_TICKS_REQUIRED = 40;

    public int ticksRequired = BASE_TICKS_REQUIRED;

    public int operatingTicks;

    public boolean autoMode = false;

    public boolean isRecipe = false;

    public boolean stockControl = false;
    public boolean needsOrganize = true; //organize on load

    public int pulseOperations;

    public RecipeFormula formula;
    private Optional<ICraftingRecipe> cachedRecipe = Optional.empty();
    private NonNullList<ItemStack> lastRemainingItems = EMPTY_LIST;

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    public ItemStack lastFormulaStack = ItemStack.EMPTY;
    public boolean needsFormulaUpdate = false;
    public ItemStack lastOutputStack = ItemStack.EMPTY;

    private List<IInventorySlot> craftingGridSlots;
    private List<InputInventorySlot> inputSlots;
    private List<OutputInventorySlot> outputSlots;

    public TileEntityFormulaicAssemblicator() {
        super(MekanismBlock.FORMULAIC_ASSEMBLICATOR);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        //TODO: This needs to be rethought (Especially because the slots ids are wrong due to not having the upgrade slot be in it
        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GRAY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{SLOT_INPUT_FIRST, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                                                                                                             16, 17, 18, 19, SLOT_INPUT_LAST}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{SLOT_OUTPUT_FIRST, 22, 23, 24, 25, SLOT_OUTPUT_LAST}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{SLOT_ENERGY}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{0, 0, 0, 3, 1, 2});
        configComponent.setInputConfig(TransmissionType.ENERGY);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        //return configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).availableSlots;
        //TODO: Some way to tie slots to a config component? So that we can filter by the config component?
        // This can probably be done by letting the configurations know the relative side information?
        craftingGridSlots = new ArrayList<>();
        inputSlots = new ArrayList<>();
        outputSlots = new ArrayList<>();

        InventorySlotHelper.Builder builder = InventorySlotHelper.Builder.forSide(this::getDirection);
        builder.addSlot(EnergyInventorySlot.discharge(152, 76));
        builder.addSlot(FormulaInventorySlot.at(6, 26));
        for (int slotY = 0; slotY < 2; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                InputInventorySlot inputSlot = InputInventorySlot.at(stack -> {
                    //Is item valid
                    if (formula == null) {
                        return true;
                    }
                    //TODO: CLEAN THIS UP/FIX as some of this logic should probably be in
                    List<Integer> indices = formula.getIngredientIndices(world, stack);
                    if (indices.size() > 0) {
                        if (stockControl) {
                            int filled = 0;
                            for (InputInventorySlot stockSlot : inputSlots) {
                                ItemStack slotStack = stockSlot.getStack();
                                if (!slotStack.isEmpty()) {
                                    if (formula.isIngredientInPos(world, slotStack, indices.get(0))) {
                                        filled++;
                                    }
                                }
                            }
                            return filled < indices.size() * 2;
                        }
                        return true;
                    }
                    return false;
                }, 8 + slotX * 18, 98 + slotY * 18);
                builder.addSlot(inputSlot);
                inputSlots.add(inputSlot);
            }
        }
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 3; slotX++) {
                //TODO: Make sure that automation cannot extract from this slot, and cannot insert into it
                //TODO: Also previously this had canTakeStack and isEnabled be the same as the !autoMode for the slot impl
                IInventorySlot craftingSlot = BasicInventorySlot.at(item -> !autoMode, 26 + slotX * 18, 17 + slotY * 18);
                builder.addSlot(craftingSlot);
                craftingGridSlots.add(craftingSlot);
            }
        }

        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 2; slotX++) {
                OutputInventorySlot outputSlot = OutputInventorySlot.at(116 + slotX * 18, 17 + slotY * 18);
                builder.addSlot(outputSlot);
                outputSlots.add(outputSlot);
            }
        }
        return builder.build();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            checkFormula();
            recalculateRecipe();
        }
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            if (formula != null && stockControl && needsOrganize) {
                needsOrganize = false;
                organizeStock();
            }
            ChargeUtils.discharge(SLOT_ENERGY, this);
            if (getControlType() != RedstoneControl.PULSE) {
                pulseOperations = 0;
            } else if (MekanismUtils.canFunction(this)) {
                pulseOperations++;
            }
            checkFormula();
            if (autoMode && formula == null) {
                toggleAutoMode();
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
                    } else if (getEnergy() >= getEnergyPerTick()) {
                        operatingTicks++;
                        setEnergy(getEnergy() - getEnergyPerTick());
                    }
                } else {
                    operatingTicks = 0;
                }
            } else {
                operatingTicks = 0;
            }
        }
    }

    private void checkFormula() {
        RecipeFormula prev = formula;
        ItemStack formulaStack = getStackInSlot(SLOT_FORMULA);
        if (!formulaStack.isEmpty() && formulaStack.getItem() instanceof ItemCraftingFormula) {
            if (formula == null || lastFormulaStack != formulaStack) {
                loadFormula();
            }
        } else {
            formula = null;
        }
        if (prev != formula) {
            needsFormulaUpdate = true;
        }

        lastFormulaStack = formulaStack;
    }

    public void loadFormula() {
        ItemStack formulaStack = getStackInSlot(SLOT_FORMULA);
        ItemCraftingFormula formulaItem = (ItemCraftingFormula) formulaStack.getItem();
        if (formulaItem.getInventory(formulaStack) != null && !formulaItem.isInvalid(formulaStack)) {
            RecipeFormula recipe = new RecipeFormula(world, formulaItem.getInventory(formulaStack));
            if (recipe.isValidFormula(world)) {
                if (formula != null && !formula.isFormulaEqual(world, recipe)) {
                    formula = recipe;
                    operatingTicks = 0;
                } else if (formula == null) {
                    formula = recipe;
                }
            } else {
                formula = null;
                formulaItem.setInvalid(formulaStack, true);
            }
        } else {
            formula = null;
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        recalculateRecipe();
    }

    private void recalculateRecipe() {
        if (world != null && !isRemote()) {
            if (formula == null) {
                //Should always be 9 for the size
                for (int i = 0; i <= craftingGridSlots.size(); i++) {
                    //TODO: Do we really need to be copying it here
                    dummyInv.setInventorySlotContents(i, craftingGridSlots.get(i).getStack().copy());
                }

                lastRemainingItems = EMPTY_LIST;

                if (!cachedRecipe.isPresent() || !cachedRecipe.get().matches(dummyInv, world)) {
                    //TODO: Check other places CraftingManager was
                    cachedRecipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, dummyInv, world);
                }
                if (cachedRecipe.isPresent()) {
                    lastOutputStack = cachedRecipe.get().getCraftingResult(dummyInv);
                    lastRemainingItems = cachedRecipe.get().getRemainingItems(dummyInv);
                } else {
                    lastOutputStack = MekanismUtils.findRepairRecipe(dummyInv, world);
                }
                isRecipe = !lastOutputStack.isEmpty();
            } else {
                isRecipe = formula.matches(world, craftingGridSlots);
                if (isRecipe) {
                    lastOutputStack = formula.recipe.getCraftingResult(dummyInv);
                    lastRemainingItems = formula.recipe.getRemainingItems(dummyInv);
                } else {
                    lastOutputStack = ItemStack.EMPTY;
                }
            }
            needsOrganize = true;
        }
    }

    private boolean doSingleCraft() {
        //Should always be 9 for the size
        for (int i = 0; i <= craftingGridSlots.size(); i++) {
            //TODO: Do we really need to be copying it here
            dummyInv.setInventorySlotContents(i, craftingGridSlots.get(i).getStack().copy());
        }
        recalculateRecipe();

        ItemStack output = lastOutputStack;
        if (!output.isEmpty() && tryMoveToOutput(output, false) && (lastRemainingItems.isEmpty() || lastRemainingItems.stream().allMatch(it -> it.isEmpty() || tryMoveToOutput(it, false)))) {
            tryMoveToOutput(output, true);
            for (ItemStack remainingItem : lastRemainingItems) {
                if (!remainingItem.isEmpty()) {
                    tryMoveToOutput(remainingItem, true);
                }
            }

            for (IInventorySlot craftingSlot : craftingGridSlots) {
                if (!craftingSlot.isEmpty()) {
                    craftingSlot.shrinkStack(1);
                }
            }
            if (formula != null) {
                moveItemsToGrid();
            }
            markDirty();
            return true;
        }
        return false;
    }

    private boolean craftSingle() {
        if (formula != null) {
            boolean canOperate = true;
            if (!formula.matches(getWorld(), craftingGridSlots)) {
                canOperate = moveItemsToGrid();
            }
            if (canOperate) {
                return doSingleCraft();
            }
        } else {
            return doSingleCraft();
        }
        return false;
    }

    private boolean moveItemsToGrid() {
        boolean ret = true;
        for (int i = 0; i <= craftingGridSlots.size(); i++) {
            IInventorySlot recipeSlot = craftingGridSlots.get(i);
            ItemStack recipeStack = recipeSlot.getStack();
            if (formula.isIngredientInPos(world, recipeStack, i)) {
                continue;
            }
            if (!recipeStack.isEmpty()) {
                //Update recipeStack as well so we can check if it is empty without having to get it again
                recipeSlot.setStack(recipeStack = tryMoveToInput(recipeStack));
                markDirty();
                if (!recipeStack.isEmpty()) {
                    ret = false;
                }
            } else {
                boolean found = false;
                for (int j = inputSlots.size() - 1; j >= 0; j--) {
                    //The stack stored in the stock inventory
                    InputInventorySlot stockSlot = inputSlots.get(j);
                    ItemStack stockStack = stockSlot.getStack();
                    if (!stockStack.isEmpty() && formula.isIngredientInPos(world, stockStack, i)) {
                        recipeSlot.setStack(StackUtils.size(stockStack, 1));
                        if (stockSlot.shrinkStack(1) != 1) {
                            //TODO: Print error that something went wrong
                        }
                        markDirty();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ret = false;
                }
            }
        }
        return ret;
    }

    private void craftAll() {
        while (craftSingle()) {
        }
    }

    private void moveItemsToInput(boolean forcePush) {
        for (int i = 0; i <= craftingGridSlots.size(); i++) {
            IInventorySlot recipeSlot = craftingGridSlots.get(i);
            ItemStack recipeStack = recipeSlot.getStack();
            if (!recipeStack.isEmpty() && (forcePush || (formula != null && !formula.isIngredientInPos(getWorld(), recipeStack, i)))) {
                recipeSlot.setStack(tryMoveToInput(recipeStack));
            }
        }
        markDirty();
    }

    private void toggleAutoMode() {
        if (autoMode) {
            operatingTicks = 0;
            autoMode = false;
        } else if (formula != null) {
            moveItemsToInput(false);
            autoMode = true;
        }
        markDirty();
    }

    private void toggleStockControl() {
        if (!isRemote() && formula != null) {
            stockControl = !stockControl;
            if (stockControl) {
                organizeStock();
            }
        }
    }

    private void organizeStock() {
        int inputSlotCount = inputSlots.size();
        for (int j = 0; j < inputSlotCount; j++) {
            IInventorySlot compareSlot = inputSlots.get(j);
            for (int i = inputSlotCount - 1; i > j; i--) {
                IInventorySlot stockSlot = inputSlots.get(i);
                ItemStack stockStack = stockSlot.getStack();
                if (!stockStack.isEmpty()) {
                    ItemStack compareStack = compareSlot.getStack();
                    if (compareStack.isEmpty()) {
                        compareSlot.setStack(stockStack);
                        stockSlot.setStack(ItemStack.EMPTY);
                        markDirty();
                        return;
                    }
                    int maxCompareSize = compareSlot.getLimit();
                    if (compareStack.getCount() < maxCompareSize) {
                        if (InventoryUtils.areItemsStackable(stockStack, compareStack)) {
                            int newCount = compareStack.getCount() + stockStack.getCount();
                            int newCompareSize = Math.min(maxCompareSize, newCount);
                            if (compareSlot.setStackSize(newCompareSize) != newCompareSize) {
                                //TODO: Print error
                            }
                            int newStockSize = Math.max(0, newCount - maxCompareSize);
                            if (stockSlot.setStackSize(newStockSize) != newStockSize) {
                                //TODO: Print error
                            }
                            markDirty();
                            return;
                        }
                    }
                }
            }
        }
    }

    private ItemStack tryMoveToInput(ItemStack stack) {
        stack = stack.copy();
        for (InputInventorySlot stockSlot : inputSlots) {
            ItemStack stockStack = stockSlot.getStack();
            if (stockStack.isEmpty()) {
                stockSlot.setStack(stack);
                return ItemStack.EMPTY;
            }
            int stockMaxSize = stockSlot.getLimit();
            if (InventoryUtils.areItemsStackable(stack, stockStack) && stockStack.getCount() < stockMaxSize) {
                int toUse = Math.min(stack.getCount(), stockMaxSize - stockStack.getCount());
                if (stockSlot.growStack(toUse) != toUse) {
                    //TODO: Print error that something went wrong
                }
                stack.shrink(toUse);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    private boolean tryMoveToOutput(ItemStack stack, boolean doMove) {
        stack = stack.copy();
        for (OutputInventorySlot outputSlot : outputSlots) {
            ItemStack outputStack = outputSlot.getStack();
            if (outputStack.isEmpty()) {
                if (doMove) {
                    outputSlot.setStack(stack);
                }
                return true;
            }
            int outputMaxSize = outputSlot.getLimit();
            if (InventoryUtils.areItemsStackable(stack, outputStack) && outputStack.getCount() < outputMaxSize) {
                int toUse = Math.min(stack.getCount(), outputMaxSize - outputStack.getCount());
                if (doMove) {
                    if (outputSlot.growStack(toUse) != toUse) {
                        //TODO: Print error that something went wrong
                    }
                }
                stack.shrink(toUse);
                if (stack.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void encodeFormula() {
        ItemStack formulaStack = getStackInSlot(SLOT_FORMULA);
        if (!formulaStack.isEmpty() && formulaStack.getItem() instanceof ItemCraftingFormula) {
            ItemCraftingFormula item = (ItemCraftingFormula) formulaStack.getItem();
            if (item.getInventory(formulaStack) == null) {
                RecipeFormula formula = new RecipeFormula(world, craftingGridSlots);
                if (formula.isValidFormula(world)) {
                    item.setInventory(formulaStack, formula.input);
                    markDirty();
                }
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        autoMode = nbtTags.getBoolean("autoMode");
        operatingTicks = nbtTags.getInt("operatingTicks");
        pulseOperations = nbtTags.getInt("pulseOperations");
        stockControl = nbtTags.getBoolean("stockControl");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean("autoMode", autoMode);
        nbtTags.putInt("operatingTicks", operatingTicks);
        nbtTags.putInt("pulseOperations", pulseOperations);
        nbtTags.putBoolean("stockControl", stockControl);
        return nbtTags;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                toggleAutoMode();
            } else if (type == 1) {
                encodeFormula();
            } else if (type == 2) {
                craftSingle();
            } else if (type == 3) {
                craftAll();
            } else if (type == 4) {
                if (formula != null) {
                    moveItemsToGrid();
                } else {
                    moveItemsToInput(true);
                }
            } else if (type == 5) {
                toggleStockControl();
            }
            return;
        }

        super.handlePacketData(dataStream);

        if (isRemote()) {
            autoMode = dataStream.readBoolean();
            operatingTicks = dataStream.readInt();
            isRecipe = dataStream.readBoolean();
            stockControl = dataStream.readBoolean();
            if (dataStream.readBoolean()) {
                if (dataStream.readBoolean()) {
                    NonNullList<ItemStack> inv = NonNullList.withSize(9, ItemStack.EMPTY);
                    for (int i = 0; i < 9; i++) {
                        if (dataStream.readBoolean()) {
                            inv.set(i, dataStream.readItemStack());
                        }
                    }
                    formula = new RecipeFormula(getWorld(), inv);
                } else {
                    formula = null;
                }
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(autoMode);
        data.add(operatingTicks);
        data.add(isRecipe);
        data.add(stockControl);
        if (needsFormulaUpdate) {
            data.add(true);
            if (formula != null) {
                data.add(true);
                for (int i = 0; i < 9; i++) {
                    if (!formula.input.get(i).isEmpty()) {
                        data.add(true);
                        data.add(formula.input.get(i));
                    } else {
                        data.add(false);
                    }
                }
            } else {
                data.add(false);
            }
        } else {
            data.add(false);
        }
        needsFormulaUpdate = false;
        return data;
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    public TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public Direction getOrientation() {
        return getDirection();
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
            setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage()));
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        return configComponent.isCapabilityDisabled(capability, side, getDirection()) || super.isCapabilityDisabled(capability, side);
    }
}