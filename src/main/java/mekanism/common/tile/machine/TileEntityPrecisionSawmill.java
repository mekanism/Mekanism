package mekanism.common.tile.machine;

import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.SawmillCachedRecipe;
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
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.ItemRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.upgrade.SawmillUpgradeData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;

public class TileEntityPrecisionSawmill extends TileEntityProgressMachine<SawmillRecipe> implements ItemRecipeLookupHandler<SawmillRecipe> {

    private final IOutputHandler<@NonNull ChanceOutput> outputHandler;
    private final IInputHandler<@NonNull ItemStack> inputHandler;

    private MachineEnergyContainer<TileEntityPrecisionSawmill> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInput")
    private InputInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutput")
    private OutputInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getSecondaryOutput")
    private OutputInventorySlot secondaryOutputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityPrecisionSawmill() {
        super(MekanismBlocks.PRECISION_SAWMILL, 200);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
        configComponent.setupItemIOConfig(Collections.singletonList(inputSlot), Arrays.asList(outputSlot, secondaryOutputSlot), energySlot, false);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        inputHandler = InputHelper.getInputHandler(inputSlot);
        outputHandler = OutputHelper.getOutputHandler(outputSlot, secondaryOutputSlot);
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
        builder.addSlot(inputSlot = InputInventorySlot.at(this::containsRecipe, recipeCacheLookupMonitor, 56, 17));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 116, 35));
        builder.addSlot(secondaryOutputSlot = OutputInventorySlot.at(this, 132, 35));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 56, 53));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        recipeCacheLookupMonitor.updateAndProcess();
    }

    @Override
    @Nonnull
    public MekanismRecipeType<SawmillRecipe, SingleItem<SawmillRecipe>> getRecipeType() {
        return MekanismRecipeType.SAWING;
    }

    @Nullable
    @Override
    public SawmillRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    @Nonnull
    @Override
    public CachedRecipe<SawmillRecipe> createNewCachedRecipe(@Nonnull SawmillRecipe recipe, int cacheIndex) {
        return new SawmillCachedRecipe(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(() -> markDirty(false))
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    @Nonnull
    @Override
    public SawmillUpgradeData getUpgradeData() {
        return new SawmillUpgradeData(redstone, getControlType(), getEnergyContainer(), getOperatingTicks(), energySlot, inputSlot, outputSlot, secondaryOutputSlot,
              getComponents());
    }

    public MachineEnergyContainer<TileEntityPrecisionSawmill> getEnergyContainer() {
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