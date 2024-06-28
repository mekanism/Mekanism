package mekanism.common.tile.prefab;

import java.util.List;
import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.ItemStackConstantChemicalToItemStackCachedRecipe;
import mekanism.api.recipes.cache.ItemStackConstantChemicalToItemStackCachedRecipe.ChemicalUsageMultiplier;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.computer.computercraft.ComputerConstants;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler.ItemChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.ConstantUsageRecipeLookupHandler;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.upgrade.AdvancedMachineUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityAdvancedElectricMachine extends TileEntityProgressMachine<ItemStackGasToItemStackRecipe> implements
      ItemChemicalRecipeLookupHandler<Gas, GasStack, ItemStackGasToItemStackRecipe>, ConstantUsageRecipeLookupHandler {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    public static final int BASE_TICKS_REQUIRED = 10 * SharedConstants.TICKS_PER_SECOND;
    public static final long MAX_GAS = 210;

    private final ChemicalUsageMultiplier gasUsageMultiplier;
    private double gasPerTickMeanMultiplier = 1;
    private long baseTotalUsage;
    private long usedSoFar;

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getChemical", "getChemicalCapacity", "getChemicalNeeded",
                                                                                        "getChemicalFilledPercentage"}, docPlaceholder = "gas tank")
    public IGasTank gasTank;

    protected final IOutputHandler<@NotNull ItemStack> outputHandler;
    protected final IInputHandler<@NotNull ItemStack> itemInputHandler;
    protected final ILongInputHandler<@NotNull GasStack> gasInputHandler;

    private MachineEnergyContainer<TileEntityAdvancedElectricMachine> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInput", docPlaceholder = "input slot")
    InputInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutput", docPlaceholder = "output slot")
    OutputInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getChemicalItem", docPlaceholder = "secondary input slot")
    GasInventorySlot secondarySlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityAdvancedElectricMachine(IBlockProvider blockProvider, BlockPos pos, BlockState state, int ticksRequired) {
        super(blockProvider, pos, state, TRACKED_ERROR_TYPES, ticksRequired);
        configComponent.setupItemIOExtraConfig(inputSlot, outputSlot, secondarySlot, energySlot);
        if (allowExtractingChemical()) {
            configComponent.setupIOConfig(TransmissionType.GAS, gasTank, RelativeSide.RIGHT).setCanEject(false);
        } else {
            configComponent.setupInputConfig(TransmissionType.GAS, gasTank);
        }
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        itemInputHandler = InputHelper.getInputHandler(inputSlot, RecipeError.NOT_ENOUGH_INPUT);
        gasInputHandler = InputHelper.getConstantInputHandler(gasTank);
        outputHandler = OutputHelper.getOutputHandler(outputSlot, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
        baseTotalUsage = baseTicksRequired;
        if (useStatisticalMechanics()) {
            //Note: Statistical mechanics works best by just using the mean gas usage we want to target
            // rather than adjusting the mean each time to try and reach a given target
            gasUsageMultiplier = (usedSoFar, operatingTicks) -> StatUtils.inversePoisson(gasPerTickMeanMultiplier);
        } else {
            gasUsageMultiplier = (usedSoFar, operatingTicks) -> {
                long baseRemaining = baseTotalUsage - usedSoFar;
                int remainingTicks = getTicksRequired() - operatingTicks;
                if (baseRemaining < remainingTicks) {
                    //If we already used more than we would need to use (due to removing speed upgrades or adding gas upgrades)
                    // then just don't use any gas this tick
                    return 0;
                } else if (baseRemaining == remainingTicks) {
                    return 1;
                }
                return Math.max(MathUtils.clampToLong(baseRemaining / (double) remainingTicks), 0);
            };
        }
    }

    @NotNull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        BiPredicate<@NotNull Gas, @NotNull AutomationType> canExtract = allowExtractingChemical() ? ChemicalTankBuilder.GAS.alwaysTrueBi : ChemicalTankBuilder.GAS.notExternal;
        builder.addTank(gasTank = ChemicalTankBuilder.GAS.create(MAX_GAS, canExtract, (gas, automationType) -> containsRecipeBA(inputSlot.getStack(), gas),
              this::containsRecipeB, recipeCacheListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, recipeCacheUnpauseListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipeAB(item, gasTank.getStack()), this::containsRecipeA, recipeCacheListener, 64, 17))
              .tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));
        builder.addSlot(secondarySlot = GasInventorySlot.fillOrConvert(gasTank, this::getLevel, listener, 64, 53));
        builder.addSlot(outputSlot = OutputInventorySlot.at(recipeCacheUnpauseListener, 116, 35))
              .tracksWarnings(slot -> slot.warning(WarningType.NO_SPACE_IN_OUTPUT, getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE)));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 39, 35));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        secondarySlot.fillTankOrConvert();
        recipeCacheLookupMonitor.updateAndProcess();
        return sendUpdatePacket;
    }

    protected boolean allowExtractingChemical() {
        return !useStatisticalMechanics();
    }

    protected boolean useStatisticalMechanics() {
        return false;
    }

    @Nullable
    @Override
    public ItemStackGasToItemStackRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(itemInputHandler, gasInputHandler);
    }

    @NotNull
    @Override
    public CachedRecipe<ItemStackGasToItemStackRecipe> createNewCachedRecipe(@NotNull ItemStackGasToItemStackRecipe recipe, int cacheIndex) {
        return new ItemStackConstantChemicalToItemStackCachedRecipe<>(recipe, recheckAllRecipeErrors, itemInputHandler, gasInputHandler, gasUsageMultiplier,
              used -> usedSoFar = used, outputHandler)
              .setErrorsChanged(this::onErrorsChanged)
              .setCanHolderFunction(this::canFunction)
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED || (upgrade == Upgrade.GAS && supportsUpgrade(Upgrade.GAS))) {
            if (useStatisticalMechanics()) {
                gasPerTickMeanMultiplier = MekanismUtils.getGasPerTickMeanMultiplier(this);
            } else {
                baseTotalUsage = MekanismUtils.getBaseUsage(this, baseTicksRequired);
            }
        }
    }

    @NotNull
    @Override
    public AdvancedMachineUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new AdvancedMachineUpgradeData(provider, redstone, getControlType(), getEnergyContainer(), getOperatingTicks(), usedSoFar, gasTank, secondarySlot,
              energySlot, inputSlot, outputSlot, getComponents());
    }

    public MachineEnergyContainer<TileEntityAdvancedElectricMachine> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public boolean isConfigurationDataCompatible(Block blockType) {
        //Allow exact match or factories of the same type (as we will just ignore the extra data)
        return super.isConfigurationDataCompatible(blockType) || MekanismUtils.isSameTypeFactory(getBlockType(), blockType);
    }

    @Override
    public long getSavedUsedSoFar(int cacheIndex) {
        return usedSoFar;
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        usedSoFar = nbt.getLong(SerializationConstants.USED_SO_FAR);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putLong(SerializationConstants.USED_SO_FAR, usedSoFar);
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = ComputerConstants.DESCRIPTION_GET_ENERGY_USAGE)
    FloatingLong getEnergyUsage() {
        return getActive() ? energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Empty the contents of the gas tank into the environment")
    void dumpChemical() throws ComputerException {
        validateSecurityIsPublic();
        gasTank.setEmpty();
    }
    //End methods IComputerTile
}
