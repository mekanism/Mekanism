package mekanism.common.content.evaporation;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.api.IEvaporationSolar;
import mekanism.api.SerializationConstants;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.heat.HeatAPI;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.FluidRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleFluid;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvaporationMultiblockData extends MultiblockData implements IValveHandler, FluidRecipeLookupHandler<FluidToFluidRecipe> {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    public static final int MAX_HEIGHT = 18;
    public static final double MAX_MULTIPLIER_TEMP = 3_000;

    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getInput", "getInputCapacity", "getInputNeeded",
                                                                                     "getInputFilledPercentage"}, docPlaceholder = "input tank")
    public BasicFluidTank inputTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded",
                                                                                     "getOutputFilledPercentage"}, docPlaceholder = "output tank")
    public BasicFluidTank outputTank;
    @ContainerSync
    public VariableHeatCapacitor heatCapacitor;

    private double biomeAmbientTemp;
    private double tempMultiplier;

    private int inputTankCapacity;
    public float prevScale;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getProductionAmount")
    public double lastGain;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getEnvironmentalLoss")
    public double lastEnvironmentLoss;

    private final RecipeCacheLookupMonitor<FluidToFluidRecipe> recipeCacheLookupMonitor;
    private final BooleanSupplier recheckAllRecipeErrors;
    @ContainerSync
    private final boolean[] trackedErrors = new boolean[TRACKED_ERROR_TYPES.size()];

    private final Int2ObjectMap<BlockCapabilityCache<IEvaporationSolar, Void>> cachedSolar = new Int2ObjectArrayMap<>(4);

    private final IOutputHandler<@NotNull FluidStack> outputHandler;
    private final IInputHandler<@NotNull FluidStack> inputHandler;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItemInput", docPlaceholder = "input side's input slot")
    final FluidInventorySlot inputInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItemOutput", docPlaceholder = "input side's output slot")
    final OutputInventorySlot outputInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItemInput", docPlaceholder = "output side's input slot")
    final FluidInventorySlot inputOutputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItemOutput", docPlaceholder = "output side's output slot")
    final OutputInventorySlot outputOutputSlot;

    public EvaporationMultiblockData(TileEntityThermalEvaporationBlock tile) {
        super(tile);
        recipeCacheLookupMonitor = new RecipeCacheLookupMonitor<>(this);
        recheckAllRecipeErrors = TileEntityRecipeMachine.shouldRecheckAllErrors(tile);
        //Default biome temp to the ambient temperature at the block we are at
        biomeAmbientTemp = HeatAPI.getAmbientTemp(tile.getLevel(), tile.getBlockPos());
        fluidTanks.add(inputTank = VariableCapacityFluidTank.input(this, this::getMaxFluid, this::containsRecipe, createSaveAndComparator(recipeCacheLookupMonitor)));
        fluidTanks.add(outputTank = VariableCapacityFluidTank.output(this, MekanismConfig.general.evaporationOutputTankCapacity, ConstantPredicates.alwaysTrue(), this));
        inputHandler = InputHelper.getInputHandler(inputTank, RecipeError.NOT_ENOUGH_INPUT);
        outputHandler = OutputHelper.getOutputHandler(outputTank, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
        inventorySlots.add(inputInputSlot = FluidInventorySlot.fill(inputTank, this, 28, 20));
        inventorySlots.add(outputInputSlot = OutputInventorySlot.at(this, 28, 51));
        inventorySlots.add(inputOutputSlot = FluidInventorySlot.drain(outputTank, this, 152, 20));
        inventorySlots.add(outputOutputSlot = OutputInventorySlot.at(this, 152, 51));
        inputInputSlot.setSlotType(ContainerSlotType.INPUT);
        inputOutputSlot.setSlotType(ContainerSlotType.INPUT);
        heatCapacitors.add(heatCapacitor = VariableHeatCapacitor.create(MekanismConfig.general.evaporationHeatCapacity.get() * 3, () -> biomeAmbientTemp, this));
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        biomeAmbientTemp = calculateAverageAmbientTemperature(world);
        // update the heat capacity now that we've read
        heatCapacitor.setHeatCapacity(MekanismConfig.general.evaporationHeatCapacity.get() * height(), true);
        updateSolars(world);
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (getTemperature() > HeatAPI.AMBIENT_TEMP) {
            recipeCacheLookupMonitor.unpause();
        }
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);
        // external heat dissipation
        lastEnvironmentLoss = simulateEnvironment();
        // update temperature
        updateHeatCapacitors(null);
        //After we update the heat capacitors, update our temperature multiplier
        // Note: We use the ambient temperature without taking our biome into account as we want to have a consistent multiplier
        tempMultiplier = (Math.min(MAX_MULTIPLIER_TEMP, getTemperature()) - HeatAPI.AMBIENT_TEMP) * MekanismConfig.general.evaporationTempMultiplier.get() *
                         ((double) height() / MAX_HEIGHT);
        inputOutputSlot.drainTank(outputOutputSlot);
        inputInputSlot.fillTank(outputInputSlot);
        recipeCacheLookupMonitor.updateAndProcess();
        float scale = MekanismUtils.getScale(prevScale, inputTank);
        if (!Mth.equal(scale, prevScale)) {
            prevScale = scale;
            needsPacket = true;
        }
        return needsPacket;
    }

    @Override
    public boolean allowsStructuralGuiAccess(TileEntityStructuralMultiblock multiblock) {
        return false;
    }

    @Override
    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.readUpdateTag(tag, provider);
        NBTUtils.setFluidStackIfPresent(provider, tag, SerializationConstants.FLUID, fluid -> inputTank.setStack(fluid));
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE, scale -> prevScale = scale);
        readValves(tag);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeUpdateTag(tag, provider);
        tag.put(SerializationConstants.FLUID, inputTank.getFluid().saveOptional(provider));
        tag.putFloat(SerializationConstants.SCALE, prevScale);
        writeValves(tag);
    }

    @Override
    public double simulateEnvironment() {
        double currentTemperature = getTemperature();
        double heatCapacity = heatCapacitor.getHeatCapacity();
        heatCapacitor.handleHeat(getActiveSolars() * MekanismConfig.general.evaporationSolarMultiplier.get() * heatCapacity);
        if (Math.abs(currentTemperature - biomeAmbientTemp) < 0.001) {
            heatCapacitor.handleHeat(biomeAmbientTemp * heatCapacity - heatCapacitor.getHeat());
        } else {
            double incr = MekanismConfig.general.evaporationHeatDissipation.get() * Math.sqrt(Math.abs(currentTemperature - biomeAmbientTemp));
            if (currentTemperature > biomeAmbientTemp) {
                incr = -incr;
            }
            heatCapacitor.handleHeat(heatCapacity * incr);
            if (incr < 0) {
                return -incr;
            }
        }
        return 0;
    }

    @ComputerMethod
    public double getTemperature() {
        return heatCapacitor.getTemperature();
    }

    @Override
    public void setVolume(int volume) {
        if (getVolume() != volume) {
            super.setVolume(volume);
            //Note: We only count the inner volume for the tank capacity for the evap tower
            inputTankCapacity = (volume / 4) * MekanismConfig.general.evaporationFluidPerTank.get();
        }
    }

    public int getMaxFluid() {
        return inputTankCapacity;
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleFluidRecipeInput, FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> getRecipeType() {
        return MekanismRecipeType.EVAPORATING;
    }

    @Override
    public IRecipeViewerRecipeType<FluidToFluidRecipe> recipeViewerType() {
        return RecipeViewerRecipeType.EVAPORATING;
    }

    @Nullable
    @Override
    public FluidToFluidRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    @Override
    public void clearRecipeErrors(int cacheIndex) {
        Arrays.fill(trackedErrors, false);
    }

    @NotNull
    @Override
    public CachedRecipe<FluidToFluidRecipe> createNewCachedRecipe(@NotNull FluidToFluidRecipe recipe, int cacheIndex) {
        return OneInputCachedRecipe.fluidToFluid(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
              .setErrorsChanged(errors -> {
                  for (int i = 0; i < trackedErrors.length; i++) {
                      trackedErrors[i] = errors.contains(TRACKED_ERROR_TYPES.get(i));
                  }
              })
              .setActive(active -> {
                  //TODO: Make the numbers for lastGain be based on how much the recipe provides as an output rather than "assuming" it is 1 mB
                  // Also fix that the numbers don't quite accurately reflect the values as we modify number of operations, and not have a fractional
                  // amount
                  if (active) {
                      if (tempMultiplier > 0 && tempMultiplier < 1) {
                          lastGain = 1F / Mth.ceil(1 / tempMultiplier);
                      } else {
                          lastGain = tempMultiplier;
                      }
                  } else {
                      lastGain = 0;
                  }
              })
              .setRequiredTicks(() -> tempMultiplier > 0 && tempMultiplier < 1 ? Mth.ceil(1 / tempMultiplier) : 1)
              .setBaselineMaxOperations(() -> tempMultiplier > 0 && tempMultiplier < 1 ? 1 : (int) tempMultiplier);
    }

    public boolean hasWarning(RecipeError error) {
        int errorIndex = TRACKED_ERROR_TYPES.indexOf(error);
        if (errorIndex == -1) {
            //Something went wrong
            return false;
        }
        return trackedErrors[errorIndex];
    }

    @ComputerMethod
    int getActiveSolars() {
        int ret = 0;
        for (BlockCapabilityCache<IEvaporationSolar, Void> capability : cachedSolar.values()) {
            IEvaporationSolar solar = capability.getCapability();
            if (solar != null && solar.canSeeSun()) {
                ret++;
            }
        }
        return ret;
    }

    private void updateSolarSpot(Level world, BlockPos pos, int corner) {
        //Create a capability cache for the given corner. When we are unformed we will clear references to our caches
        // which allow them to be garbage collected
        cachedSolar.put(corner, BlockCapabilityCache.create(Capabilities.EVAPORATION_SOLAR, (ServerLevel) world, pos, null));
    }

    private void updateSolars(Level world) {
        BlockPos maxPos = getMaxPos();
        updateSolarSpot(world, maxPos, 0);
        updateSolarSpot(world, maxPos.west(3), 1);
        updateSolarSpot(world, maxPos.north(3), 2);
        updateSolarSpot(world, maxPos.offset(-3, 0, -3), 3);
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getFluidAmount(), inputTank.getCapacity());
    }

    @Override
    public void remove(Level world, Structure oldStructure) {
        //Clear the cached solar panels so that we don't hold references to them and prevent them from being able to be garbage collected
        cachedSolar.clear();
        super.remove(world, oldStructure);
    }
}
