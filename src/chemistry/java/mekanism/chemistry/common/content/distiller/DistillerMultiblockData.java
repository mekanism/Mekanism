package mekanism.chemistry.common.content.distiller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.heat.HeatAPI;
import mekanism.api.recipes.DistillingRecipe;
import mekanism.api.recipes.DistillingRecipe.DistillingRecipeOutput;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.chemistry.common.config.MekanismChemistryConfig;
import mekanism.chemistry.common.tile.multiblock.TileEntityFractionatingDistillerBlock;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.FluidRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleFluid;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

public class DistillerMultiblockData extends MultiblockData implements IValveHandler, FluidRecipeLookupHandler<DistillingRecipe> {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    private static final int MAX_OUTPUT = 10_000;
    public static final int FLUID_PER_TANK = 64_000;

    private final TileEntityFractionatingDistillerBlock tile;

    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"})
    public BasicFluidTank inputTank;
    @ContainerSync
    // TODO: add computer support
    public List<BasicFluidTank> outputTanks;
    @ContainerSync
    public BasicHeatCapacitor heatCapacitor;

    private double biomeAmbientTemp;

    public float prevScale;
    @ContainerSync
    // TODO: add computer support
    public double lastGains; // TODO: this should be a list
    @ContainerSync
    @SyntheticComputerMethod(getter = "getEnvironmentalLoss")
    public double lastEnvironmentLoss;

    private final RecipeCacheLookupMonitor<DistillingRecipe> recipeCacheLookupMonitor;
    private final BooleanSupplier recheckAllRecipeErrors;
    @ContainerSync
    private final boolean[] trackedErrors = new boolean[TRACKED_ERROR_TYPES.size()];

    private final IOutputHandler<DistillingRecipeOutput> outputHandler;
    private final IInputHandler<FluidStack> inputHandler;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItemInput")
    private final FluidInventorySlot inputInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItemOutput")
    private final OutputInventorySlot outputInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputsItemInput")
    private final List<FluidInventorySlot> inputOutputSlots;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputsItemOutput")
    private final List<OutputInventorySlot> outputOutputSlots;

    public DistillerMultiblockData(TileEntityFractionatingDistillerBlock tile) {
        super(tile);

        this.tile = tile;

        recipeCacheLookupMonitor = new RecipeCacheLookupMonitor<>(this);
        recheckAllRecipeErrors = TileEntityRecipeMachine.shouldRecheckAllErrors(tile);
        //Default biome temp to the ambient temperature at the block we are at
        biomeAmbientTemp = HeatAPI.getAmbientTemp(tile.getLevel(), tile.getTilePos());
        fluidTanks.add(inputTank = MultiblockFluidTank.input(this, tile, this::getMaxFluid, this::containsRecipe, recipeCacheLookupMonitor));
        outputTanks = new ArrayList<>();
        inputHandler = InputHelper.getInputHandler(inputTank, RecipeError.NOT_ENOUGH_INPUT);
        outputHandler = new IOutputHandler<>() {
            @Override
            public void handleOutput(@Nonnull DistillingRecipeOutput toOutput, int operations) {
                for (int idx = 0; idx < toOutput.outputFluids().size(); ++idx) {
                    outputTanks.get(idx).insert(new FluidStack(toOutput.outputFluids().get(idx), toOutput.outputFluids().get(idx).getAmount() * operations), Action.EXECUTE, AutomationType.INTERNAL);
                }
            }

            @Override
            public void calculateOperationsCanSupport(@Nonnull OperationTracker tracker, @Nonnull DistillingRecipeOutput toOutput) {
                if (toOutput.outputFluids().size() > outputTanks.size()) {
                    tracker.updateOperations(0);
                    tracker.addError(RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
                }
                int operations = Integer.MAX_VALUE;
                for (int idx = 0; idx < toOutput.outputFluids().size(); ++idx) {
                    FluidStack outputFluid = toOutput.outputFluids().get(idx);
                    //Copy the stack and make it be max size
                    FluidStack maxOutput = new FluidStack(outputFluid, Integer.MAX_VALUE);
                    //Then simulate filling the fluid tank, so we can see how much actually can fit
                    FluidStack remainder = outputTanks.get(idx).insert(maxOutput, Action.SIMULATE, AutomationType.INTERNAL);
                    int amountUsed = maxOutput.getAmount() - remainder.getAmount();
                    //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
                    int thisTankOperations = amountUsed / outputFluid.getAmount();
                    operations = Math.min(operations, thisTankOperations);
                    if (thisTankOperations == 0) {
                        if (amountUsed == 0 && outputTanks.get(idx).getNeeded() > 0) {
                            tracker.addError(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT);
                        } else {
                            tracker.addError(RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
                        }
                    }
                }
                if (operations < Integer.MAX_VALUE) {
                    tracker.updateOperations(operations);
                }
            }
        };
        inventorySlots.add(inputInputSlot = FluidInventorySlot.fill(inputTank, this, 7, 7));
        inventorySlots.add(outputInputSlot = OutputInventorySlot.at(this, 7, 53));
        inputOutputSlots = new ArrayList<>();
        outputOutputSlots = new ArrayList<>();
        heatCapacitors.add(heatCapacitor = MultiblockHeatCapacitor.create(this, tile, MekanismChemistryConfig.chemistry.distillerHeatCapacity.get() * 4, () -> biomeAmbientTemp));
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);

        biomeAmbientTemp = calculateAverageAmbientTemperature(world);
        for (int idx = 0; idx < height() - 2; ++idx) {
            BasicFluidTank outputTank = MultiblockFluidTank.output(this, tile, () -> MAX_OUTPUT, BasicFluidTank.alwaysTrue);
            outputTanks.add(outputTank);
            fluidTanks.add(outputTank);
            FluidInventorySlot inputOutputSlot = FluidInventorySlot.drain(outputTank, this, 27 + 18 * idx, 7);
            inputOutputSlots.add(inputOutputSlot);
            inventorySlots.add(inputOutputSlot);
            OutputInventorySlot outputOutputSlot = OutputInventorySlot.at( this, 27 + 18 * idx, 53);
            outputOutputSlots.add(outputOutputSlot);
            inventorySlots.add(outputOutputSlot);
        }

        heatCapacitor.setHeatCapacity(MekanismChemistryConfig.chemistry.distillerHeatCapacity.get() * height(), true);
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);
        // external heat dissipation
        lastEnvironmentLoss = simulateEnvironment();
        // update temperature
        updateHeatCapacitors(null);
        for (int idx = 0; idx < inputOutputSlots.size(); ++idx) {
            inputOutputSlots.get(idx).drainTank(outputOutputSlots.get(idx));
        }
        inputInputSlot.fillTank(outputInputSlot);
        recipeCacheLookupMonitor.updateAndProcess();
        float scale = MekanismUtils.getScale(prevScale, inputTank);
        if (scale != prevScale) {
            prevScale = scale;
            needsPacket = true;
        }
        return needsPacket;
    }

    @Override
    public void readUpdateTag(CompoundTag tag) {
        super.readUpdateTag(tag);
        NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, fluid -> inputTank.setStack(fluid));
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevScale = scale);
        readValves(tag);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag) {
        super.writeUpdateTag(tag);
        tag.put(NBTConstants.FLUID_STORED, inputTank.getFluid().writeToNBT(new CompoundTag()));
        tag.putFloat(NBTConstants.SCALE, prevScale);
        writeValves(tag);
    }

    @Override
    public double simulateEnvironment() {
        double currentTemperature = getTemperature();
        double heatCapacity = heatCapacitor.getHeatCapacity();
        if (Math.abs(currentTemperature - biomeAmbientTemp) < 0.001) {
            heatCapacitor.handleHeat(biomeAmbientTemp * heatCapacity - heatCapacitor.getHeat());
        } else {
            double delta = MekanismChemistryConfig.chemistry.distillerHeatDissipation.get() * Math.sqrt(Math.abs(currentTemperature - biomeAmbientTemp));
            if (currentTemperature > biomeAmbientTemp) {
                delta = -delta;
            }
            heatCapacitor.handleHeat(heatCapacity * delta);
            if (delta < 0) {
                return -delta;
            }
        }
        return 0;
    }

    @ComputerMethod
    public double getTemperature() {
        return heatCapacitor.getTemperature();
    }

    public int getMaxFluid() {
        return height() * FLUID_PER_TANK;
    }

    @Nonnull
    @Override
    public MekanismRecipeType<DistillingRecipe, SingleFluid<DistillingRecipe>> getRecipeType() {
        return MekanismRecipeType.DISTILLING;
    }

    @Nullable
    @Override
    public DistillingRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    @Override
    public void clearRecipeErrors(int cacheIndex) {
        Arrays.fill(trackedErrors, false);
    }

    @Nonnull
    @Override
    public CachedRecipe<DistillingRecipe> createNewCachedRecipe(@Nonnull DistillingRecipe recipe, int cacheIndex) {
        return OneInputCachedRecipe.distilling(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
              .setErrorsChanged(errors -> {
                  for (int i = 0; i < trackedErrors.length; i++) {
                      trackedErrors[i] = errors.contains(TRACKED_ERROR_TYPES.get(i));
                  }
              })
              .setRequiredTicks(() -> 1)
              .setBaselineMaxOperations(() -> 1);
    }

    public boolean hasWarning(RecipeError error) {
        int errorIndex = TRACKED_ERROR_TYPES.indexOf(error);
        if (errorIndex == -1) {
            //Something went wrong
            return false;
        }
        return trackedErrors[errorIndex];
    }

    @Override
    public Level getHandlerWorld() {
        return getWorld();
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getFluidAmount(), inputTank.getCapacity());
    }
}
