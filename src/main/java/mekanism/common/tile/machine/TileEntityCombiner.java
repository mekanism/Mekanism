package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CombinerCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler.DoubleItemRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.DoubleItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.upgrade.CombinerUpgradeData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;

public class TileEntityCombiner extends TileEntityProgressMachine<CombinerRecipe> implements DoubleItemRecipeLookupHandler<CombinerRecipe> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> inputHandler;
    private final IInputHandler<@NonNull ItemStack> extraInputHandler;

    private MachineEnergyContainer<TileEntityCombiner> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getMainInput")
    private InputInventorySlot mainInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getSecondaryInput")
    private InputInventorySlot extraInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutput")
    private OutputInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityCombiner() {
        super(MekanismBlocks.COMBINER, 200);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
        configComponent.setupItemIOExtraConfig(mainInputSlot, outputSlot, extraInputSlot, energySlot);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        inputHandler = InputHelper.getInputHandler(mainInputSlot);
        extraInputHandler = InputHelper.getInputHandler(extraInputSlot);
        outputHandler = OutputHelper.getOutputHandler(outputSlot);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(mainInputSlot = InputInventorySlot.at(item -> containsRecipeAB(item, extraInputSlot.getStack()), this::containsRecipeA, recipeCacheLookupMonitor,
              64, 17));
        builder.addSlot(extraInputSlot = InputInventorySlot.at(item -> containsRecipeBA(mainInputSlot.getStack(), item), this::containsRecipeB, recipeCacheLookupMonitor,
              64, 53));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 116, 35));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 39, 35));
        extraInputSlot.setSlotType(ContainerSlotType.EXTRA);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        recipeCacheLookupMonitor.updateAndProcess();
    }

    @Nonnull
    @Override
    public MekanismRecipeType<CombinerRecipe, DoubleItem<CombinerRecipe>> getRecipeType() {
        return MekanismRecipeType.COMBINING;
    }

    @Nullable
    @Override
    public CombinerRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler, extraInputHandler);
    }

    @Nonnull
    @Override
    public CachedRecipe<CombinerRecipe> createNewCachedRecipe(@Nonnull CombinerRecipe recipe, int cacheIndex) {
        return new CombinerCachedRecipe(recipe, inputHandler, extraInputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(() -> markDirty(false))
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    @Nonnull
    @Override
    public CombinerUpgradeData getUpgradeData() {
        return new CombinerUpgradeData(redstone, getControlType(), getEnergyContainer(), getOperatingTicks(), energySlot, extraInputSlot, mainInputSlot, outputSlot, getComponents());
    }

    public MachineEnergyContainer<TileEntityCombiner> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public boolean isConfigurationDataCompatible(TileEntityType<?> tileType) {
        //Allow exact match or factories of the same type (as we will just ignore the extra data)
        return super.isConfigurationDataCompatible(tileType) || MekanismUtils.isSameTypeFactory(getBlockType(), tileType);
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private FloatingLong getEnergyUsage() {
        return getActive() ? energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
    }
    //End methods IComputerTile
}