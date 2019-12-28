package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CombinerCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.prefab.TileEntityUpgradeableMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.item.ItemStack;

public class TileEntityCombiner extends TileEntityUpgradeableMachine<CombinerRecipe> {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> inputHandler;
    private final IInputHandler<@NonNull ItemStack> extraInputHandler;

    private InputInventorySlot mainInputSlot;
    private InputInventorySlot extraInputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    /**
     * Double Electric Machine -- a machine like this has a total of 4 slots. Input slot (0), secondary slot (1), output slot (2), energy slot (3), and the upgrade slot
     * (4). The machine will not run if it does not have enough electricity.
     */
    public TileEntityCombiner() {
        super(MekanismBlocks.COMBINER, 200, MekanismUtils.getResource(ResourceType.GUI, "basic_machine.png"));
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(mainInputSlot));
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(extraInputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(outputSlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(energySlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.TOP, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.BOTTOM, DataType.EXTRA);
            itemConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);
            itemConfig.setDataType(RelativeSide.BACK, DataType.ENERGY);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT, new EnergySlotInfo());
            energyConfig.fill(DataType.INPUT);
            energyConfig.setCanEject(false);
        }

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, itemConfig);

        inputHandler = InputHelper.getInputHandler(mainInputSlot);
        extraInputHandler = InputHelper.getInputHandler(extraInputSlot);
        outputHandler = OutputHelper.getOutputHandler(outputSlot);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        //TODO: Should we limit ACTUAL insertion to be based on the other slot's contents?
        builder.addSlot(mainInputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getMainInput().testType(item)), this, 56, 17));
        builder.addSlot(extraInputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getExtraInput().testType(item)), this, 56, 53));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 116, 35));
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 31, 35));
        return builder.build();
    }

    @Override
    protected void upgradeInventory(TileEntityFactory<?> factory) {
        //TODO: Upgrade
        /*NonNullList<ItemStack> factoryInventory = factory.getInventory();
        NonNullList<ItemStack> inventory = getInventory();
        //Double Machine
        factoryInventory.set(5, inventory.get(0));
        factoryInventory.set(4, inventory.get(1));
        factoryInventory.set(5 + 3, inventory.get(2));
        factoryInventory.set(1, inventory.get(3));
        factoryInventory.set(0, inventory.get(4));*/
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<CombinerRecipe> getRecipeType() {
        return MekanismRecipeType.COMBINING;
    }

    @Nullable
    @Override
    public CachedRecipe<CombinerRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public CombinerRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandler.getInput();
        if (stack.isEmpty()) {
            return null;
        }
        ItemStack extraStack = extraInputHandler.getInput();
        if (extraStack.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(stack, extraStack));
    }

    @Nullable
    @Override
    public CachedRecipe<CombinerRecipe> createNewCachedRecipe(@Nonnull CombinerRecipe recipe, int cacheIndex) {
        return new CombinerCachedRecipe(recipe, inputHandler, extraInputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty);
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
                return new Object[]{getOperatingTicks()};
            case 2:
                return new Object[]{getActive()};
            case 3:
                return new Object[]{getDirection()};
            case 4:
                //TODO: Decide if we should try to get the cached recipe if it is null
                return new Object[]{cachedRecipe != null && cachedRecipe.canFunction()};
            case 5:
                return new Object[]{getMaxEnergy()};
            case 6:
                return new Object[]{getNeededEnergy()};
            default:
                throw new NoSuchMethodException();
        }
    }
}