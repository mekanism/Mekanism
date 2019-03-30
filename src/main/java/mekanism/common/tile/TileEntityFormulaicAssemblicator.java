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
import mekanism.common.config.MekanismConfig.usage;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityFormulaicAssemblicator extends TileEntityElectricBlock implements ISideConfiguration,
      IUpgradeTile, IRedstoneControl, IConfigCardAccess, ISecurityTile {

    public InventoryCrafting dummyInv = MekanismUtils.getDummyCraftingInv();

    public double BASE_ENERGY_PER_TICK = usage.metallurgicInfuserUsage;

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

    public RedstoneControl controlType = RedstoneControl.DISABLED;

    public TileComponentUpgrade upgradeComponent;
    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;
    public TileComponentSecurity securityComponent;

    public ItemStack lastFormulaStack = ItemStack.EMPTY;
    public boolean needsFormulaUpdate = false;
    public ItemStack lastOutputStack = ItemStack.EMPTY;

    public TileEntityFormulaicAssemblicator() {
        super("FormulaicAssemblicator", MachineType.FORMULAIC_ASSEMBLICATOR.baseEnergy);

        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED,
              new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}));
        configComponent.addOutput(TransmissionType.ITEM,
              new SideData("Output", EnumColor.DARK_BLUE, new int[]{21, 22, 23, 24, 25, 26}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{1}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{0, 0, 0, 3, 1, 2});
        configComponent.setInputConfig(TransmissionType.ENERGY);

        inventory = NonNullList.withSize(36, ItemStack.EMPTY);

        upgradeComponent = new TileComponentUpgrade(this, 0);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));

        securityComponent = new TileComponentSecurity(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            if (formula != null && stockControl && needsOrganize) {
                needsOrganize = false;
                organizeStock();
            }

            ChargeUtils.discharge(1, this);

            if (controlType != RedstoneControl.PULSE) {
                pulseOperations = 0;
            } else if (MekanismUtils.canFunction(this)) {
                pulseOperations++;
            }

            RecipeFormula prev = formula;

            if (!inventory.get(2).isEmpty() && inventory.get(2).getItem() instanceof ItemCraftingFormula) {
                ItemCraftingFormula item = (ItemCraftingFormula) inventory.get(2).getItem();

                if (formula == null || lastFormulaStack != inventory.get(2)) {
                    loadFormula();
                }
            } else {
                formula = null;
            }

            if (prev != formula) {
                needsFormulaUpdate = true;
            }

            lastFormulaStack = inventory.get(2);

            if (autoMode && formula == null) {
                toggleAutoMode();
            }

            if (autoMode && formula != null && ((controlType == RedstoneControl.PULSE && pulseOperations > 0)
                  || MekanismUtils.canFunction(this))) {
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
                    } else {
                        if (getEnergy() >= energyPerTick) {
                            operatingTicks++;
                            setEnergy(getEnergy() - energyPerTick);
                        }
                    }
                } else {
                    operatingTicks = 0;
                }
            } else {
                operatingTicks = 0;
            }
        }
    }

    public void loadFormula() {
        ItemCraftingFormula item = (ItemCraftingFormula) inventory.get(2).getItem();

        if (item.getInventory(inventory.get(2)) != null && !item.isInvalid(inventory.get(2))) {
            RecipeFormula itemFormula = new RecipeFormula(world, item.getInventory(inventory.get(2)));

            if (itemFormula.isValidFormula(world)) {
                if (formula != null && !formula.isFormulaEqual(world, itemFormula)) {
                    formula = itemFormula;
                    operatingTicks = 0;
                } else if (formula == null) {
                    formula = itemFormula;
                }
            } else {
                formula = null;
                item.setInvalid(inventory.get(2), true);
            }
        } else {
            formula = null;
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (world != null && !world.isRemote) {
            if (formula == null) {
                for (int i = 0; i < 9; i++) {
                    dummyInv.setInventorySlotContents(i, inventory.get(27 + i));
                }

                lastOutputStack = MekanismUtils.findMatchingRecipe(dummyInv, world);
                isRecipe = !lastOutputStack.isEmpty();
            } else {
                isRecipe = formula.matches(world, inventory, 27);
                lastOutputStack = isRecipe ? formula.recipe.getRecipeOutput() : ItemStack.EMPTY;
            }

            needsOrganize = true;
        }
    }

    private boolean doSingleCraft() {
        for (int i = 0; i < 9; i++) {
            dummyInv.setInventorySlotContents(i, inventory.get(27 + i));
        }

        ItemStack output = lastOutputStack;

        if (!output.isEmpty() && tryMoveToOutput(output, false)) {
            tryMoveToOutput(output, true);

            for (int i = 27; i <= 35; i++) {
                if (!inventory.get(i).isEmpty()) {
                    ItemStack stack = inventory.get(i).copy();

                    inventory.get(i).shrink(1);

                    if (inventory.get(i).getCount() == 0 && stack.getItem().hasContainerItem(stack)) {
                        ItemStack container = stack.getItem().getContainerItem(stack);

                        if (!container.isEmpty() && container.isItemStackDamageable()
                              && container.getItemDamage() > container.getMaxDamage()) {
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

            if (!formula.matches(world, inventory, 27)) {
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

        for (int i = 27; i <= 35; i++) {
            if (formula.isIngredientInPos(world, inventory.get(i), i - 27)) {
                continue;
            }

            if (!inventory.get(i).isEmpty()) {
                inventory.set(i, tryMoveToInput(inventory.get(i)));
                markDirty();

                if (!inventory.get(i).isEmpty()) {
                    ret = false;
                }
            } else {
                boolean found = false;

                for (int j = 20; j >= 3; j--) {
                    if (!inventory.get(j).isEmpty() && formula.isIngredientInPos(world, inventory.get(j), i - 27)) {
                        inventory.set(i, StackUtils.size(inventory.get(j), 1));
                        inventory.get(j).shrink(1);

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
        for (int i = 27; i <= 35; i++) {
            if (!inventory.get(i).isEmpty() && (forcePush || (formula != null && !formula
                  .isIngredientInPos(world, inventory.get(i), i - 27)))) {
                inventory.set(i, tryMoveToInput(inventory.get(i)));
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
        for (int j = 3; j <= 20; j++) {
            for (int i = 20; i > j; i--) {
                if (!inventory.get(i).isEmpty()) {
                    if (inventory.get(j).isEmpty()) {
                        inventory.set(j, inventory.get(i));
                        inventory.set(i, ItemStack.EMPTY);
                        markDirty();
                        return;
                    } else if (inventory.get(j).getCount() < inventory.get(j).getMaxStackSize()) {
                        if (InventoryUtils.areItemsStackable(inventory.get(i), inventory.get(j))) {
                            int newCount = inventory.get(j).getCount() + inventory.get(i).getCount();
                            inventory.get(j).setCount(Math.min(inventory.get(j).getMaxStackSize(), newCount));
                            inventory.get(i).setCount(Math.max(0, newCount - inventory.get(j).getMaxStackSize()));
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

        for (int i = 3; i <= 20; i++) {
            if (inventory.get(i).isEmpty()) {
                inventory.set(i, stack);

                return ItemStack.EMPTY;
            } else if (InventoryUtils.areItemsStackable(stack, inventory.get(i))
                  && inventory.get(i).getCount() < inventory.get(i).getMaxStackSize()) {
                int toUse = Math
                      .min(stack.getCount(), inventory.get(i).getMaxStackSize() - inventory.get(i).getCount());

                inventory.get(i).grow(toUse);
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

        for (int i = 21; i <= 26; i++) {
            if (inventory.get(i).isEmpty()) {
                if (doMove) {
                    inventory.set(i, stack);
                }

                return true;
            } else if (InventoryUtils.areItemsStackable(stack, inventory.get(i))
                  && inventory.get(i).getCount() < inventory.get(i).getMaxStackSize()) {
                int toUse = Math
                      .min(stack.getCount(), inventory.get(i).getMaxStackSize() - inventory.get(i).getCount());

                if (doMove) {
                    inventory.get(i).grow(toUse);
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
        if (!inventory.get(2).isEmpty() && inventory.get(2).getItem() instanceof ItemCraftingFormula) {
            ItemCraftingFormula item = (ItemCraftingFormula) inventory.get(2).getItem();

            if (item.getInventory(inventory.get(2)) == null) {
                RecipeFormula formula = new RecipeFormula(world, inventory, 27);

                if (formula.isValidFormula(world)) {
                    item.setInventory(inventory.get(2), formula.input);
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
        if (slotID == 1) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        } else {
            return slotID >= 21 && slotID <= 26;
        }

    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID >= 3 && slotID <= 20) {
            if (formula == null) {
                return true;
            } else {
                List<Integer> indices = formula.getIngredientIndices(world, itemstack);

                if (indices.size() > 0) {
                    if (stockControl) {
                        int filled = 0;

                        for (int i = 3; i < 20; i++) {
                            if (!inventory.get(i).isEmpty()) {
                                if (formula.isIngredientInPos(world, inventory.get(i), indices.get(0))) {
                                    filled++;
                                }
                            }
                        }

                        return filled < indices.size() * 2;
                    } else {
                        return true;
                    }
                }
            }
        } else if (slotID == 1) {
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
            return (T) this;
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        return configComponent.isCapabilityDisabled(capability, side, facing) || super
              .isCapabilityDisabled(capability, side);
    }
}
