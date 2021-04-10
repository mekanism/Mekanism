package mekanism.common.tile.machine;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.GasToGasCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.ChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleChemical;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.RainType;

public class TileEntitySolarNeutronActivator extends TileEntityMekanism implements IBoundingBlock, ChemicalRecipeLookupHandler<Gas, GasStack, GasToGasRecipe> {

    public static final long MAX_GAS = 10_000;

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"})
    public IGasTank inputTank;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"})
    public IGasTank outputTank;

    private RecipeCacheLookupMonitor<GasToGasRecipe> recipeCacheLookupMonitor;

    @SyntheticComputerMethod(getter = "getPeakProductionRate")
    private float peakProductionRate;
    @SyntheticComputerMethod(getter = "getProductionRate")
    private float productionRate;
    private boolean settingsChecked;
    private boolean needsRainCheck;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull GasStack> inputHandler;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem")
    private GasInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private GasInventorySlot outputSlot;

    public TileEntitySolarNeutronActivator() {
        super(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
        inputHandler = InputHelper.getInputHandler(inputTank);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        recipeCacheLookupMonitor = new RecipeCacheLookupMonitor<>(this);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSide(this::getDirection);
        builder.addTank(inputTank = ChemicalTankBuilder.GAS.create(MAX_GAS, ChemicalTankBuilder.GAS.notExternal, ChemicalTankBuilder.GAS.alwaysTrueBi,
              this::containsRecipe, ChemicalAttributeValidator.ALWAYS_ALLOW, recipeCacheLookupMonitor), RelativeSide.BOTTOM);
        builder.addTank(outputTank = ChemicalTankBuilder.GAS.output(MAX_GAS, this), RelativeSide.FRONT);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(inputSlot = GasInventorySlot.fill(inputTank, this, 5, 56), RelativeSide.BOTTOM, RelativeSide.TOP, RelativeSide.RIGHT,
              RelativeSide.LEFT, RelativeSide.BACK);
        builder.addSlot(outputSlot = GasInventorySlot.drain(outputTank, this, 155, 56), RelativeSide.FRONT);
        inputSlot.setSlotType(ContainerSlotType.INPUT);
        inputSlot.setSlotOverlay(SlotOverlay.MINUS);
        outputSlot.setSlotType(ContainerSlotType.OUTPUT);
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    private void recheckSettings() {
        World world = getLevel();
        if (world == null) {
            return;
        }
        Biome b = world.getBiomeManager().getBiome(getBlockPos());
        needsRainCheck = b.getPrecipitation() != RainType.NONE;
        // Consider the best temperature to be 0.8; biomes that are higher than that
        // will suffer an efficiency loss (semiconductors don't like heat); biomes that are cooler
        // get a boost. We scale the efficiency to around 30% so that it doesn't totally dominate
        float tempEff = 0.3F * (0.8F - b.getTemperature(getBlockPos()));

        // Treat rainfall as a proxy for humidity; any humidity works as a drag on overall efficiency.
        // As with temperature, we scale it so that it doesn't overwhelm production. Note the signedness
        // on the scaling factor. Also note that we only use rainfall as a proxy if it CAN rain; some dimensions
        // (like the End) have rainfall set, but can't actually support rain.
        float humidityEff = -0.3F * (needsRainCheck ? b.getDownfall() : 0.0F);
        peakProductionRate = MekanismConfig.general.maxSolarNeutronActivatorRate.get() * (1.0F + tempEff + humidityEff);
        settingsChecked = true;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (!settingsChecked) {
            recheckSettings();
        }
        inputSlot.fillTank();
        outputSlot.drainTank();
        productionRate = recalculateProductionRate();
        recipeCacheLookupMonitor.updateAndProcess();
        ChemicalUtil.emit(EnumSet.of(getDirection()), outputTank, this, MekanismConfig.general.chemicalAutoEjectRate.get());
    }

    @Nonnull
    @Override
    public MekanismRecipeType<GasToGasRecipe, SingleChemical<Gas, GasStack, GasToGasRecipe>> getRecipeType() {
        return MekanismRecipeType.ACTIVATING;
    }

    @Nullable
    @Override
    public GasToGasRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    private boolean canFunction() {
        // Sort out if the solar neutron activator can see the sun; we no longer check if it's raining here,
        // since under the new rules, we can still function when it's raining, albeit at a significant penalty.
        return MekanismUtils.canFunction(this) && WorldUtils.canSeeSun(level, worldPosition.above());
    }

    private float recalculateProductionRate() {
        World world = getLevel();
        if (world == null || !canFunction()) {
            return 0;
        }
        //Get the brightness of the sun; note that there are some implementations that depend on the base
        // brightness function which doesn't take into account the fact that rain can't occur in some biomes.
        float brightness = WorldUtils.getSunBrightness(world, 1.0F);
        //Production is a function of the peak possible output in this biome and sun's current brightness
        float production = peakProductionRate * brightness;
        //If the solar neutron activator is in a biome where it can rain and it's raining penalize production by 80%
        if (needsRainCheck && (world.isRaining() || world.isThundering())) {
            production *= 0.2F;
        }
        return production;
    }

    @Nonnull
    @Override
    public CachedRecipe<GasToGasRecipe> createNewCachedRecipe(@Nonnull GasToGasRecipe recipe, int cacheIndex) {
        return new GasToGasCachedRecipe(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(this::canFunction)
              .setActive(this::setActive)
              .setOnFinish(() -> markDirty(false))
              //Edge case handling, this should almost always end up being 1
              .setRequiredTicks(() -> productionRate > 0 && productionRate < 1 ? (int) Math.ceil(1 / productionRate) : 1)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax <= 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return currentMax;
                  }
                  return Math.min(currentMax, productionRate > 0 && productionRate < 1 ? 1 : (int) productionRate);
              });
    }

    @Override
    public void onPlace() {
        WorldUtils.makeBoundingBlock(getLevel(), getBlockPos().above(), getBlockPos());
    }

    @Override
    public void onBreak(BlockState oldState) {
        World world = getLevel();
        if (world != null) {
            world.removeBlock(getBlockPos().above(), false);
            world.removeBlock(getBlockPos(), false);
        }
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(worldPosition, worldPosition.offset(1, 2, 1));
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getStored(), inputTank.getCapacity());
    }
}