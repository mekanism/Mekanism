package mekanism.common.tile.prefab;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NonNull;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackToItemStackCachedRecipe;
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
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.item.ItemStack;

public abstract class TileEntityElectricMachine extends TileEntityUpgradeableMachine<ItemStackToItemStackRecipe> {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};

    protected final IInputHandler<@NonNull ItemStack> inputHandler;
    protected final IOutputHandler<@NonNull ItemStack> outputHandler;

    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    /**
     * A simple electrical machine. This has 3 slots - the input slot (0), the energy slot (1), output slot (2), and the upgrade slot (3). It will not run if it does not
     * have enough energy.
     *
     * @param ticksRequired - ticks required to operate -- or smelt an item.
     */
    public TileEntityElectricMachine(IBlockProvider blockProvider, int ticksRequired) {
        super(blockProvider, ticksRequired, MekanismUtils.getResource(ResourceType.GUI, "basic_machine.png"));
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(inputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(outputSlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(energySlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.TOP, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);
            itemConfig.setDataType(RelativeSide.BOTTOM, DataType.ENERGY);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT, new EnergySlotInfo());
            energyConfig.fill(DataType.INPUT);
            energyConfig.setCanEject(false);
        }

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, itemConfig);

        inputHandler = InputHelper.getInputHandler(inputSlot);
        outputHandler = OutputHelper.getOutputHandler(outputSlot);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getInput().testType(item)), this, 56, 17));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 116, 35));
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 56, 53));
        return builder.build();
    }

    @Override
    protected void upgradeInventory(TileEntityFactory factory) {
        //TODO: Upgrade
        /*NonNullList<ItemStack> factoryInventory = factory.getInventory();
        NonNullList<ItemStack> inventory = getInventory();
        factoryInventory.set(5, inventory.get(0));
        factoryInventory.set(1, inventory.get(1));
        factoryInventory.set(5 + 3, inventory.get(2));
        factoryInventory.set(0, inventory.get(3));*/
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

    @Nullable
    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ItemStackToItemStackRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandler.getInput();
        return stack.isEmpty() ? null : findFirstRecipe(recipe -> recipe.test(stack));
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@Nonnull ItemStackToItemStackRecipe recipe, int cacheIndex) {
        return new ItemStackToItemStackCachedRecipe(recipe, inputHandler, outputHandler)
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