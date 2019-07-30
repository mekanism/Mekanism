package mekanism.common.tile.prefab;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismItem;
import mekanism.common.SideData;
import mekanism.common.block.states.MachineType;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.machines.DoubleMachineRecipe;
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
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class TileEntityDoubleElectricMachine<RECIPE extends DoubleMachineRecipe<RECIPE>> extends TileEntityUpgradeableMachine<DoubleMachineInput, ItemStackOutput, RECIPE> {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};

    /**
     * Double Electric Machine -- a machine like this has a total of 4 slots. Input slot (0), secondary slot (1), output slot (2), energy slot (3), and the upgrade slot
     * (4). The machine will not run if it does not have enough electricity.
     *
     * @param soundPath     - location of the sound effect
     * @param type          - the type of this machine
     * @param ticksRequired - how many ticks it takes to smelt an item.
     */
    public TileEntityDoubleElectricMachine(String soundPath, MachineType type, int ticksRequired) {
        super(soundPath, type, 4, ticksRequired, MekanismUtils.getResource(ResourceType.GUI, "guibasicmachine.png"));
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{0}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{2}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{3}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Extra", EnumColor.PURPLE, new int[]{1}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{4, 1, 0, 3, 0, 2});
        configComponent.setInputConfig(TransmissionType.ENERGY);

        inventory = NonNullList.withSize(5, ItemStack.EMPTY);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));
    }

    @Override
    protected void upgradeInventory(TileEntityFactory factory) {
        //Double Machine
        factory.inventory.set(5, inventory.get(0));
        factory.inventory.set(4, inventory.get(1));
        factory.inventory.set(5 + 3, inventory.get(2));
        factory.inventory.set(1, inventory.get(3));
        factory.inventory.set(0, inventory.get(4));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            ChargeUtils.discharge(3, this);
            boolean inactive = false;
            RECIPE recipe = getRecipe();
            if (canOperate(recipe) && MekanismUtils.canFunction(this) && getEnergy() >= energyPerTick) {
                setActive(true);
                operatingTicks++;
                if (operatingTicks >= ticksRequired) {
                    operate(recipe);
                    operatingTicks = 0;
                }
                electricityStored -= energyPerTick;
            } else {
                inactive = true;
                setActive(false);
            }
            if (inactive && getRecipe() == null) {
                operatingTicks = 0;
            }
            prevEnergy = getEnergy();
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 2) {
            return false;
        } else if (slotID == 4) {
            return MekanismItem.SPEED_UPGRADE.itemMatches(itemstack) || MekanismItem.ENERGY_UPGRADE.itemMatches(itemstack);
        } else if (slotID == 0) {
            for (DoubleMachineInput input : getRecipes().keySet()) {
                if (ItemHandlerHelper.canItemStacksStack(input.itemStack, itemstack)) {
                    return true;
                }
            }
        } else if (slotID == 3) {
            return ChargeUtils.canBeDischarged(itemstack);
        } else if (slotID == 1) {
            for (DoubleMachineInput input : getRecipes().keySet()) {
                if (ItemHandlerHelper.canItemStacksStack(input.extraStack, itemstack)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public DoubleMachineInput getInput() {
        return new DoubleMachineInput(inventory.get(0), inventory.get(1));
    }

    @Override
    public RECIPE getRecipe() {
        DoubleMachineInput input = getInput();
        if (cachedRecipe == null || !input.testEquality(cachedRecipe.getInput())) {
            cachedRecipe = RecipeHandler.getRecipe(input, getRecipes());
        }
        return cachedRecipe;
    }

    @Override
    public void operate(RECIPE recipe) {
        recipe.operate(inventory, 0, 1, 2);
        markDirty();
    }

    @Override
    public boolean canOperate(RECIPE recipe) {
        return recipe != null && recipe.canOperate(inventory, 0, 1, 2);
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 3) {
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
                return new Object[]{maxEnergy};
            case 6:
                return new Object[]{maxEnergy - getEnergy()};
            default:
                throw new NoSuchMethodException();
        }
    }
}