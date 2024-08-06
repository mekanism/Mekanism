package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe.PressurizedReactionRecipeOutput;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.PressurizedReactionCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.recipes.vanilla_input.ReactionRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.capabilities.energy.PRCEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.computer.computercraft.ComputerConstants;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ITripleRecipeLookupHandler.ItemFluidChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemFluidChemical;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityPressurizedReactionChamber extends TileEntityProgressMachine<PressurizedReactionRecipe> implements
      ItemFluidChemicalRecipeLookupHandler<PressurizedReactionRecipe> {

    public static final RecipeError NOT_ENOUGH_ITEM_INPUT_ERROR = RecipeError.create();
    public static final RecipeError NOT_ENOUGH_FLUID_INPUT_ERROR = RecipeError.create();
    public static final RecipeError NOT_ENOUGH_GAS_INPUT_ERROR = RecipeError.create();
    public static final RecipeError NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR = RecipeError.create();
    public static final RecipeError NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR = RecipeError.create();
    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          NOT_ENOUGH_ITEM_INPUT_ERROR,
          NOT_ENOUGH_FLUID_INPUT_ERROR,
          NOT_ENOUGH_GAS_INPUT_ERROR,
          NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR,
          NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    private static final int BASE_DURATION = 5 * SharedConstants.TICKS_PER_SECOND;
    public static final int MAX_FLUID = 10 * FluidType.BUCKET_VOLUME;
    public static final long MAX_GAS = 10 * FluidType.BUCKET_VOLUME;

    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getInputFluid", "getInputFluidCapacity", "getInputFluidNeeded",
                                                                                     "getInputFluidFilledPercentage"}, docPlaceholder = "fluid input")
    public BasicFluidTank inputFluidTank;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getInputGas", "getInputGasCapacity", "getInputGasNeeded",
                                                                                        "getInputGasFilledPercentage"}, docPlaceholder = "gas input")
    public IChemicalTank inputGasTank;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getOutputGas", "getOutputGasCapacity", "getOutputGasNeeded",
                                                                                        "getOutputGasFilledPercentage"}, docPlaceholder = "gas output")
    public IChemicalTank outputGasTank;

    private long recipeEnergyRequired = 0;
    private final IOutputHandler<@NotNull PressurizedReactionRecipeOutput> outputHandler;
    private final IInputHandler<@NotNull ItemStack> itemInputHandler;
    private final IInputHandler<@NotNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NotNull ChemicalStack> gasInputHandler;

    private PRCEnergyContainer energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "item input slot")
    InputInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "item output slot")
    OutputInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityPressurizedReactionChamber(BlockPos pos, BlockState state) {
        super(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, pos, state, TRACKED_ERROR_TYPES, BASE_DURATION);
        configComponent.setupItemIOConfig(inputSlot, outputSlot, energySlot);
        configComponent.setupInputConfig(TransmissionType.FLUID, inputFluidTank);
        configComponent.setupIOConfig(TransmissionType.CHEMICAL, inputGasTank, outputGasTank, RelativeSide.RIGHT, false, true);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.CHEMICAL)
              .setCanTankEject(tank -> tank != inputGasTank);

        itemInputHandler = InputHelper.getInputHandler(inputSlot, NOT_ENOUGH_ITEM_INPUT_ERROR);
        fluidInputHandler = InputHelper.getInputHandler(inputFluidTank, NOT_ENOUGH_FLUID_INPUT_ERROR);
        gasInputHandler = InputHelper.getInputHandler(inputGasTank, NOT_ENOUGH_GAS_INPUT_ERROR);
        outputHandler = OutputHelper.getOutputHandler(outputSlot, NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR, outputGasTank, NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR);
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
        //Allow extracting out of the input gas tank if it isn't external OR the output tank is empty AND the input is radioactive
        builder.addTank(inputGasTank = ChemicalTankBuilder.CHEMICAL.create(MAX_GAS, ChemicalTankHelper.radioactiveInputTankPredicate(() -> outputGasTank),
              (gas, automationType) -> containsRecipeCAB(inputSlot.getStack(), inputFluidTank.getFluid(), gas), this::containsRecipeC,
              ChemicalAttributeValidator.ALWAYS_ALLOW, recipeCacheListener));
        builder.addTank(outputGasTank = ChemicalTankBuilder.CHEMICAL.output(MAX_GAS, recipeCacheUnpauseListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addTank(inputFluidTank = BasicFluidTank.input(MAX_FLUID, fluid -> containsRecipeBAC(inputSlot.getStack(), fluid, inputGasTank.getStack()),
              this::containsRecipeB, recipeCacheListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = PRCEnergyContainer.input(this, recipeCacheUnpauseListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipeABC(item, inputFluidTank.getFluid(), inputGasTank.getStack()), this::containsRecipeA,
                    recipeCacheListener, 54, 35))
              .tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(NOT_ENOUGH_ITEM_INPUT_ERROR)));
        builder.addSlot(outputSlot = OutputInventorySlot.at(recipeCacheUnpauseListener, 116, 35))
              .tracksWarnings(slot -> slot.warning(WarningType.NO_SPACE_IN_OUTPUT, getWarningCheck(NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR)));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 141, 17));
        return builder.build();
    }

    @Override
    public void onCachedRecipeChanged(@Nullable CachedRecipe<PressurizedReactionRecipe> cachedRecipe, int cacheIndex) {
        super.onCachedRecipeChanged(cachedRecipe, cacheIndex);
        int recipeDuration;
        if (cachedRecipe == null) {
            recipeDuration = BASE_DURATION;
            recipeEnergyRequired = 0L;
        } else {
            PressurizedReactionRecipe recipe = cachedRecipe.getRecipe();
            recipeDuration = recipe.getDuration();
            recipeEnergyRequired = recipe.getEnergyRequired();
        }
        boolean update = baseTicksRequired != recipeDuration;
        baseTicksRequired = recipeDuration;
        if (update) {
            recalculateUpgrades(Upgrade.SPEED);
        }
        //Ensure we take our recipe's energy per tick into account
        energyContainer.updateEnergyPerTick();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        recipeCacheLookupMonitor.updateAndProcess();
        return sendUpdatePacket;
    }

    public long getRecipeEnergyRequired() {
        return recipeEnergyRequired;
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<ReactionRecipeInput, PressurizedReactionRecipe, ItemFluidChemical<PressurizedReactionRecipe>> getRecipeType() {
        return MekanismRecipeType.REACTION;
    }

    @Override
    public IRecipeViewerRecipeType<PressurizedReactionRecipe> recipeViewerType() {
        return RecipeViewerRecipeType.REACTION;
    }

    @Nullable
    @Override
    public PressurizedReactionRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(itemInputHandler, fluidInputHandler, gasInputHandler);
    }

    @NotNull
    @Override
    public CachedRecipe<PressurizedReactionRecipe> createNewCachedRecipe(@NotNull PressurizedReactionRecipe recipe, int cacheIndex) {
        return new PressurizedReactionCachedRecipe(recipe, recheckAllRecipeErrors, itemInputHandler, fluidInputHandler, gasInputHandler, outputHandler)
              .setErrorsChanged(this::onErrorsChanged)
              .setCanHolderFunction(this::canFunction)
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    public PRCEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = ComputerConstants.DESCRIPTION_GET_ENERGY_USAGE)
    long getEnergyUsage() {
        return getActive() ? energyContainer.getEnergyPerTick() : 0;
    }
    //End methods IComputerTile
}
