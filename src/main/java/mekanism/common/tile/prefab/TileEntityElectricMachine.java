package mekanism.common.tile.prefab;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismItems;
import mekanism.common.SideData;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;

public abstract class TileEntityElectricMachine<RECIPE extends BasicMachineRecipe<RECIPE>> extends TileEntityUpgradeableMachine<ItemStackInput, ItemStackOutput, RECIPE> {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};

    /**
     * A simple electrical machine. This has 3 slots - the input slot (0), the energy slot (1), output slot (2), and the upgrade slot (3). It will not run if it does not
     * have enough energy.
     *
     * @param soundPath     - location of the sound effect
     * @param type          - type of this machine
     * @param ticksRequired - ticks required to operate -- or smelt an item.
     */
    public TileEntityElectricMachine(String soundPath, MachineType type, int ticksRequired) {
        super(soundPath, type, 3, ticksRequired, MekanismUtils.getResource(ResourceType.GUI, "GuiBasicMachine.png"));
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{0}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{2}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{1}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{3, 1, 0, 0, 0, 2});
        configComponent.setInputConfig(TransmissionType.ENERGY);

        inventory = NonNullList.withSize(4, ItemStack.EMPTY);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));
    }

    @Override
    protected void upgradeInventory(TileEntityFactory factory) {
        factory.inventory.set(5, inventory.get(0));
        factory.inventory.set(1, inventory.get(1));
        factory.inventory.set(5 + 3, inventory.get(2));
        factory.inventory.set(0, inventory.get(3));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            ChargeUtils.discharge(1, this);
            RECIPE recipe = getRecipe();
            if (canOperate(recipe) && MekanismUtils.canFunction(this) && getEnergy() >= energyPerTick) {
                setActive(true);
                electricityStored -= energyPerTick;
                if ((operatingTicks + 1) < ticksRequired) {
                    operatingTicks++;
                } else if ((operatingTicks + 1) >= ticksRequired) {
                    operate(recipe);
                    operatingTicks = 0;
                }
            } else if (prevEnergy >= getEnergy()) {
                setActive(false);
            }
            if (!canOperate(recipe)) {
                operatingTicks = 0;
            }
            prevEnergy = getEnergy();
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 2) {
            return false;
        } else if (slotID == 3) {
            return itemstack.getItem() == MekanismItems.SpeedUpgrade || itemstack.getItem() == MekanismItems.EnergyUpgrade;
        } else if (slotID == 0) {
            return RecipeHandler.isInRecipe(itemstack, getRecipes());
        } else if (slotID == 1) {
            return ChargeUtils.canBeDischarged(itemstack);
        }
        return false;
    }

    @Override
    public ItemStackInput getInput() {
        return new ItemStackInput(inventory.get(0));
    }

    @Override
    public RECIPE getRecipe() {
        ItemStackInput input = getInput();
        if (cachedRecipe == null || !input.testEquality(cachedRecipe.getInput())) {
            cachedRecipe = RecipeHandler.getRecipe(input, getRecipes());
        }
        return cachedRecipe;
    }

    @Override
    public void operate(RECIPE recipe) {
        recipe.operate(inventory, 0, 2);
        markDirty();
        ejectorComponent.outputItems();
    }

    @Override
    public boolean canOperate(RECIPE recipe) {
        return recipe != null && recipe.canOperate(inventory, 0, 2);
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 1) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        }
        return slotID == 2;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{operatingTicks};
            case 2:
                return new Object[]{isActive};
            case 3:
                return new Object[]{facing};
            case 4:
                return new Object[]{canOperate(getRecipe())};
            case 5:
                return new Object[]{getMaxEnergy()};
            case 6:
                return new Object[]{getMaxEnergy() - getEnergy()};
            default:
                throw new NoSuchMethodException();
        }
    }
}