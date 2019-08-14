package mekanism.common.tile;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismBlock;
import mekanism.common.SideData;
import mekanism.common.Upgrade;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.assemblicator.RecipeFormula;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
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

public class TileEntityFormulaicAssemblicator extends TileEntityMekanism implements ISideConfiguration, IUpgradeTile, IConfigCardAccess {

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
    private Optional<ICraftingRecipe> cachedRecipe;
    private NonNullList<ItemStack> lastRemainingItems = EMPTY_LIST;

    public TileComponentUpgrade<TileEntityFormulaicAssemblicator> upgradeComponent;
    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    public ItemStack lastFormulaStack = ItemStack.EMPTY;
    public boolean needsFormulaUpdate = false;
    public ItemStack lastOutputStack = ItemStack.EMPTY;

    public TileEntityFormulaicAssemblicator() {
        super(MekanismBlock.FORMULAIC_ASSEMBLICATOR);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GRAY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{SLOT_INPUT_FIRST, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                                                                                                             16, 17, 18, 19, SLOT_INPUT_LAST}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{SLOT_OUTPUT_FIRST, 22, 23, 24, 25, SLOT_OUTPUT_LAST}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{SLOT_ENERGY}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{0, 0, 0, 3, 1, 2});
        configComponent.setInputConfig(TransmissionType.ENERGY);

        upgradeComponent = new TileComponentUpgrade<>(this, SLOT_UPGRADE);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!world.isRemote) {
            checkFormula();
            recalculateRecipe();
        }
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
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
        ItemStack formulaStack = getInventory().get(SLOT_FORMULA);
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
        ItemStack formulaStack = getInventory().get(SLOT_FORMULA);
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
        if (world != null && !world.isRemote) {
            if (formula == null) {
                for (int i = 0; i < 9; i++) {
                    dummyInv.setInventorySlotContents(i, getInventory().get(SLOT_CRAFT_MATRIX_FIRST + i));
                }

                lastRemainingItems = EMPTY_LIST;

                if (!cachedRecipe.isPresent() || !cachedRecipe.get().matches(dummyInv, world)) {
                    //TODO: Check other places CraftingManager was
                    cachedRecipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, dummyInv, world);
                }
                if (cachedRecipe.isPresent()) {
                    lastOutputStack = cachedRecipe.get().getCraftingResult(dummyInv);
                    lastRemainingItems = cachedRecipe.get().getRemainingItems(dummyInv);
                } else {
                    lastOutputStack = MekanismUtils.findRepairRecipe(dummyInv, world);
                }
                isRecipe = !lastOutputStack.isEmpty();
            } else {
                isRecipe = formula.matches(world, getInventory(), SLOT_CRAFT_MATRIX_FIRST);
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
        for (int i = 0; i < 9; i++) {
            dummyInv.setInventorySlotContents(i, getInventory().get(SLOT_CRAFT_MATRIX_FIRST + i));
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

            for (int i = SLOT_CRAFT_MATRIX_FIRST; i <= SLOT_CRAFT_MATRIX_LAST; i++) {
                ItemStack stack = getInventory().get(i);
                if (!stack.isEmpty()) {
                    stack.shrink(1);
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
            if (!formula.matches(world, getInventory(), SLOT_CRAFT_MATRIX_FIRST)) {
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
        for (int i = SLOT_CRAFT_MATRIX_FIRST; i <= SLOT_CRAFT_MATRIX_LAST; i++) {
            ItemStack recipeStack = getInventory().get(i);
            if (formula.isIngredientInPos(world, recipeStack, i - SLOT_CRAFT_MATRIX_FIRST)) {
                continue;
            }
            if (!recipeStack.isEmpty()) {
                //Update recipeStack as well so we can check if it is empty without having to get it again
                getInventory().set(i, recipeStack = tryMoveToInput(recipeStack));
                markDirty();
                if (!recipeStack.isEmpty()) {
                    ret = false;
                }
            } else {
                boolean found = false;
                for (int j = SLOT_INPUT_LAST; j >= SLOT_INPUT_FIRST; j--) {
                    //The stack stored in the stock inventory
                    ItemStack stockStack = getInventory().get(j);
                    if (!stockStack.isEmpty() && formula.isIngredientInPos(world, stockStack, i - SLOT_CRAFT_MATRIX_FIRST)) {
                        getInventory().set(i, StackUtils.size(stockStack, 1));
                        stockStack.shrink(1);
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
        for (int i = SLOT_CRAFT_MATRIX_FIRST; i <= SLOT_CRAFT_MATRIX_LAST; i++) {
            ItemStack recipeStack = getInventory().get(i);
            if (!recipeStack.isEmpty() && (forcePush || (formula != null && !formula.isIngredientInPos(world, recipeStack, i - SLOT_CRAFT_MATRIX_FIRST)))) {
                getInventory().set(i, tryMoveToInput(recipeStack));
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
        if (!world.isRemote && formula != null) {
            stockControl = !stockControl;
            if (stockControl) {
                organizeStock();
            }
        }
    }

    private void organizeStock() {
        for (int j = SLOT_INPUT_FIRST; j <= SLOT_INPUT_LAST; j++) {
            for (int i = SLOT_INPUT_LAST; i > j; i--) {
                ItemStack stockStack = getInventory().get(i);
                if (!stockStack.isEmpty()) {
                    ItemStack compareStack = getInventory().get(j);
                    if (compareStack.isEmpty()) {
                        getInventory().set(j, stockStack);
                        getInventory().set(i, ItemStack.EMPTY);
                        markDirty();
                        return;
                    } else if (compareStack.getCount() < compareStack.getMaxStackSize()) {
                        if (InventoryUtils.areItemsStackable(stockStack, compareStack)) {
                            int newCount = compareStack.getCount() + stockStack.getCount();
                            compareStack.setCount(Math.min(compareStack.getMaxStackSize(), newCount));
                            stockStack.setCount(Math.max(0, newCount - compareStack.getMaxStackSize()));
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
        for (int i = SLOT_INPUT_FIRST; i <= SLOT_INPUT_LAST; i++) {
            ItemStack stockStack = getInventory().get(i);
            if (stockStack.isEmpty()) {
                getInventory().set(i, stack);
                return ItemStack.EMPTY;
            } else if (InventoryUtils.areItemsStackable(stack, stockStack) && stockStack.getCount() < stockStack.getMaxStackSize()) {
                int toUse = Math.min(stack.getCount(), stockStack.getMaxStackSize() - stockStack.getCount());
                stockStack.grow(toUse);
                stack.shrink(toUse);
                if (stack.getCount() == 0) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    private boolean tryMoveToOutput(ItemStack stack, boolean doMove) {
        stack = stack.copy();
        for (int i = SLOT_OUTPUT_FIRST; i <= SLOT_OUTPUT_LAST; i++) {
            ItemStack outputStack = getInventory().get(i);
            if (outputStack.isEmpty()) {
                if (doMove) {
                    getInventory().set(i, stack);
                }
                return true;
            } else if (InventoryUtils.areItemsStackable(stack, outputStack) && outputStack.getCount() < outputStack.getMaxStackSize()) {
                int toUse = Math.min(stack.getCount(), outputStack.getMaxStackSize() - outputStack.getCount());
                if (doMove) {
                    outputStack.grow(toUse);
                }
                stack.shrink(toUse);
                if (stack.getCount() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void encodeFormula() {
        ItemStack formulaStack = getInventory().get(SLOT_FORMULA);
        if (!formulaStack.isEmpty() && formulaStack.getItem() instanceof ItemCraftingFormula) {
            ItemCraftingFormula item = (ItemCraftingFormula) formulaStack.getItem();
            if (item.getInventory(formulaStack) == null) {
                RecipeFormula formula = new RecipeFormula(world, getInventory(), SLOT_CRAFT_MATRIX_FIRST);
                if (formula.isValidFormula(world)) {
                    item.setInventory(formulaStack, formula.input);
                    markDirty();
                }
            }
        }
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).availableSlots;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == SLOT_ENERGY) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        }
        return slotID >= SLOT_OUTPUT_FIRST && slotID <= SLOT_OUTPUT_LAST;

    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID >= SLOT_INPUT_FIRST && slotID <= SLOT_INPUT_LAST) {
            if (formula == null) {
                return true;
            }
            List<Integer> indices = formula.getIngredientIndices(world, itemstack);
            if (indices.size() > 0) {
                if (stockControl) {
                    int filled = 0;
                    for (int i = SLOT_INPUT_FIRST; i < SLOT_INPUT_LAST; i++) {
                        ItemStack slotStack = getInventory().get(i);
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
        } else if (slotID == SLOT_ENERGY) {
            return ChargeUtils.canBeDischarged(itemstack);
        }
        return false;
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
        if (!world.isRemote) {
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

        if (world.isRemote) {
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
                    formula = new RecipeFormula(world, inv);
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
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        switch (upgrade) {
            case SPEED:
                ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage()));
                break;
            case ENERGY:
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage()));
                setMaxEnergy(MekanismUtils.getMaxEnergy(this, getBaseStorage()));
                setEnergy(Math.min(getMaxEnergy(), getEnergy()));
                break;
            default:
                break;
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