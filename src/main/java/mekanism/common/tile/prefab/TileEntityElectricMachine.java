package mekanism.common.tile.prefab;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.inventory.IInventorySlotHolder;
import mekanism.common.inventory.InventorySlotHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.item.ItemStack;

public abstract class TileEntityElectricMachine extends TileEntityUpgradeableMachine<ItemStackToItemStackRecipe> {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};

    protected final IInputHandler<@NonNull ItemStack> inputHandler;
    protected final IOutputHandler<@NonNull ItemStack> outputHandler;

    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;

    /**
     * A simple electrical machine. This has 3 slots - the input slot (0), the energy slot (1), output slot (2), and the upgrade slot (3). It will not run if it does not
     * have enough energy.
     *
     * @param ticksRequired - ticks required to operate -- or smelt an item.
     */
    public TileEntityElectricMachine(IBlockProvider blockProvider, int ticksRequired) {
        super(blockProvider, ticksRequired, MekanismUtils.getResource(ResourceType.GUI, "basic_machine.png"));
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GRAY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{0}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{2}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{1}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{3, 1, 0, 0, 0, 2});
        configComponent.setInputConfig(TransmissionType.ENERGY);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));

        inputHandler = InputHelper.getInputHandler(inputSlot);
        outputHandler = OutputHelper.getOutputHandler(outputSlot);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        //return configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).availableSlots;
        //TODO: Some way to tie slots to a config component? So that we can filter by the config component?
        // This can probably be done by letting the configurations know the relative side information?
        InventorySlotHelper.Builder builder = InventorySlotHelper.Builder.forSide(this::getDirection);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getInput().testType(item)), this, 56, 17));
        builder.addSlot(EnergyInventorySlot.discharge(this, 56, 53));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 116, 35));
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
            ChargeUtils.discharge(1, this);
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