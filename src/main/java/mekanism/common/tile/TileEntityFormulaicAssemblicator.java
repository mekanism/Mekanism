package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess;
import mekanism.api.TileNetworkList;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.PacketHandler;
import mekanism.common.SideData;
import mekanism.common.Upgrade;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.assemblicator.RecipeFormula;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.prefab.TileEntityElectricBlock;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityFormulaicAssemblicator extends TileEntityElectricBlock implements ISideConfiguration, IUpgradeTile, IRedstoneControl, IConfigCardAccess, ISecurityTile {

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

    public InventoryCrafting dummyInv = MekanismUtils.getDummyCraftingInv();

    public double BASE_ENERGY_PER_TICK = MachineType.FORMULAIC_ASSEMBLICATOR.getUsage();

    public double energyPerTick = BASE_ENERGY_PER_TICK;

    public int BASE_TICKS_REQUIRED = 40;

    public int ticksRequired = BASE_TICKS_REQUIRED;

    public int operatingTicks;

    public boolean autoMode = false;

    public boolean isRecipe = false;

    public boolean stockControl = false;
    public boolean needsOrganize = true; //organize on load

    public int pulseOperations;

    public RecipeFormula formula;
    private IRecipe cachedRecipe;
    private NonNullList<ItemStack> lastRemainingItems = EMPTY_LIST;

    public RedstoneControl controlType = RedstoneControl.DISABLED;

    public TileComponentUpgrade upgradeComponent;
    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;
    public TileComponentSecurity securityComponent;

    public ItemStack lastFormulaStack = ItemStack.EMPTY;
    public boolean needsFormulaUpdate = false;
    public ItemStack lastOutputStack = ItemStack.EMPTY;

    public TileEntityFormulaicAssemblicator() {
        super("FormulaicAssemblicator", MachineType.FORMULAIC_ASSEMBLICATOR.getStorage());
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{SLOT_INPUT_FIRST, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                                                                                                             SLOT_INPUT_LAST}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{SLOT_OUTPUT_FIRST, 22, 23, 24, 25, SLOT_OUTPUT_LAST}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{SLOT_ENERGY}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{0, 0, 0, 3, 1, 2});
        configComponent.setInputConfig(TransmissionType.ENERGY);

        inventory = NonNullList.withSize(36, ItemStack.EMPTY);

        upgradeComponent = new TileComponentUpgrade(this, SLOT_UPGRADE);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));

        securityComponent = new TileComponentSecurity(this);
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
        super.onUpdate();
        if (!world.isRemote) {
            if (formula != null && stockControl && needsOrganize) {
                needsOrganize = false;
                organizeStock();
            }
            ChargeUtils.discharge(SLOT_ENERGY, this);
            if (controlType != RedstoneControl.PULSE) {
                pulseOperations = 0;
            } else if (MekanismUtils.canFunction(this)) {
                pulseOperations++;
            }
            checkFormula();
            if (autoMode && formula == null) {
                toggleAutoMode();
            }

            if (autoMode && formula != null && ((controlType == RedstoneControl.PULSE && pulseOperations > 0) || MekanismUtils.canFunction(this))) {
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
                            ejectorComponent.outputItems();
                        }
                    } else if (getEnergy() >= energyPerTick) {
                        operatingTicks++;
                        setEnergy(getEnergy() - energyPerTick);
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
        ItemStack formulaStack = inventory.get(SLOT_FORMULA);
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
        ItemStack formulaStack = inventory.get(SLOT_FORMULA);
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
                    dummyInv.setInventorySlotContents(i, inventory.get(SLOT_CRAFT_MATRIX_FIRST + i));
                }

                lastRemainingItems = EMPTY_LIST;

                if (cachedRecipe == null || !cachedRecipe.matches(dummyInv, world)) {
                    cachedRecipe = CraftingManager.findMatchingRecipe(dummyInv, world);
                }
                if (cachedRecipe != null) {
                    lastOutputStack = cachedRecipe.getCraftingResult(dummyInv);
                    lastRemainingItems = cachedRecipe.getRemainingItems(dummyInv);
                } else {
                    lastOutputStack = MekanismUtils.findRepairRecipe(dummyInv, world);
                }
                isRecipe = !lastOutputStack.isEmpty();
            } else {
                isRecipe = formula.matches(world, inventory, SLOT_CRAFT_MATRIX_FIRST);
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
            dummyInv.setInventorySlotContents(i, inventory.get(SLOT_CRAFT_MATRIX_FIRST + i));
        }
        recalculateRecipe();

        ItemStack output = lastOutputStack;
        if (!output.isEmpty() && tryMoveToOutput(output, false) && (lastRemainingItems.isEmpty() || lastRemainingItems.stream().allMatch(it->it.isEmpty() || tryMoveToOutput(it, false)))) {
            tryMoveToOutput(output, true);
            for (ItemStack remainingItem : lastRemainingItems) {
                if (!remainingItem.isEmpty()) {
                    tryMoveToOutput(remainingItem, true);
                }
            }

            for (int i = SLOT_CRAFT_MATRIX_FIRST; i <= SLOT_CRAFT_MATRIX_LAST; i++) {
                ItemStack stack = inventory.get(i);
                if (!stack.isEmpty()) {
                    ItemStack copy = stack.copy();
                    stack.shrink(1);
                    if (stack.getCount() == 0 && copy.getItem().hasContainerItem(copy)) {
                        ItemStack container = copy.getItem().getContainerItem(copy);
                        if (!container.isEmpty() && container.isItemStackDamageable() && container.getItemDamage() > container.getMaxDamage()) {
                            container = ItemStack.EMPTY;
                        }
                        if (!container.isEmpty()) {
                            boolean move = tryMoveToOutput(container.copy(), false);
                            if (move) {
                                tryMoveToOutput(container.copy(), true);
                            }
                            inventory.set(i, move ? ItemStack.EMPTY : container.copy());
                        }
                    }
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
            if (!formula.matches(world, inventory, SLOT_CRAFT_MATRIX_FIRST)) {
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
            ItemStack recipeStack = inventory.get(i);
            if (formula.isIngredientInPos(world, recipeStack, i - SLOT_CRAFT_MATRIX_FIRST)) {
                continue;
            }
            if (!recipeStack.isEmpty()) {
                //Update recipeStack as well so we can check if it is empty without having to get it again
                inventory.set(i, recipeStack = tryMoveToInput(recipeStack));
                markDirty();
                if (!recipeStack.isEmpty()) {
                    ret = false;
                }
            } else {
                boolean found = false;
                for (int j = SLOT_INPUT_LAST; j >= SLOT_INPUT_FIRST; j--) {
                    //The stack stored in the stock inventory
                    ItemStack stockStack = inventory.get(j);
                    if (!stockStack.isEmpty() && formula.isIngredientInPos(world, stockStack, i - SLOT_CRAFT_MATRIX_FIRST)) {
                        inventory.set(i, StackUtils.size(stockStack, 1));
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
            ItemStack recipeStack = inventory.get(i);
            if (!recipeStack.isEmpty() && (forcePush || (formula != null && !formula.isIngredientInPos(world, recipeStack, i - SLOT_CRAFT_MATRIX_FIRST)))) {
                inventory.set(i, tryMoveToInput(recipeStack));
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
                ItemStack stockStack = inventory.get(i);
                if (!stockStack.isEmpty()) {
                    ItemStack compareStack = inventory.get(j);
                    if (compareStack.isEmpty()) {
                        inventory.set(j, stockStack);
                        inventory.set(i, ItemStack.EMPTY);
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
            ItemStack stockStack = inventory.get(i);
            if (stockStack.isEmpty()) {
                inventory.set(i, stack);
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
            ItemStack outputStack = inventory.get(i);
            if (outputStack.isEmpty()) {
                if (doMove) {
                    inventory.set(i, stack);
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
        ItemStack formulaStack = inventory.get(SLOT_FORMULA);
        if (!formulaStack.isEmpty() && formulaStack.getItem() instanceof ItemCraftingFormula) {
            ItemCraftingFormula item = (ItemCraftingFormula) formulaStack.getItem();
            if (item.getInventory(formulaStack) == null) {
                RecipeFormula formula = new RecipeFormula(world, inventory, SLOT_CRAFT_MATRIX_FIRST);
                if (formula.isValidFormula(world)) {
                    item.setInventory(formulaStack, formula.input);
                    markDirty();
                }
            }
        }
    }

    @Override
    public boolean canSetFacing(int side) {
        return side != 0 && side != 1;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return configComponent.getOutput(TransmissionType.ITEM, side, facing).availableSlots;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
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
                        ItemStack slotStack = inventory.get(i);
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
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        autoMode = nbtTags.getBoolean("autoMode");
        operatingTicks = nbtTags.getInteger("operatingTicks");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
        pulseOperations = nbtTags.getInteger("pulseOperations");
        stockControl = nbtTags.getBoolean("stockControl");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setBoolean("autoMode", autoMode);
        nbtTags.setInteger("operatingTicks", operatingTicks);
        nbtTags.setInteger("controlType", controlType.ordinal());
        nbtTags.setInteger("pulseOperations", pulseOperations);
        nbtTags.setBoolean("stockControl", stockControl);
        return nbtTags;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
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

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            autoMode = dataStream.readBoolean();
            operatingTicks = dataStream.readInt();
            controlType = RedstoneControl.values()[dataStream.readInt()];
            isRecipe = dataStream.readBoolean();
            stockControl = dataStream.readBoolean();
            if (dataStream.readBoolean()) {
                if (dataStream.readBoolean()) {
                    NonNullList<ItemStack> inv = NonNullList.withSize(9, ItemStack.EMPTY);
                    for (int i = 0; i < 9; i++) {
                        if (dataStream.readBoolean()) {
                            inv.set(i, PacketHandler.readStack(dataStream));
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
        data.add(controlType.ordinal());
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
    public RedstoneControl getControlType() {
        return controlType;
    }

    @Override
    public void setControlType(RedstoneControl type) {
        controlType = type;
        MekanismUtils.saveChunk(this);
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
    public EnumFacing getOrientation() {
        return facing;
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
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);
        switch (upgrade) {
            case SPEED:
                ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
                energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK);
                break;
            case ENERGY:
                energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK);
                maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
                setEnergy(Math.min(getMaxEnergy(), getEnergy()));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.CONFIG_CARD_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        }
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        return configComponent.isCapabilityDisabled(capability, side, facing) || super.isCapabilityDisabled(capability, side);
    }
}