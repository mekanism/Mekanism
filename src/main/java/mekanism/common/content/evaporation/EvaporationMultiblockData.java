package mekanism.common.content.evaporation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IEvaporationSolar;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NonNull;
import mekanism.api.heat.HeatAPI;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.FluidToFluidCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
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
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.FluidRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleFluid;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationBlock;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class EvaporationMultiblockData extends MultiblockData implements IValveHandler, FluidRecipeLookupHandler<FluidToFluidRecipe> {

    private static final int MAX_OUTPUT = 10_000;
    public static final int MAX_HEIGHT = 18;
    public static final double MAX_MULTIPLIER_TEMP = 3_000;
    public static final int FLUID_PER_TANK = 64_000;

    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"})
    public BasicFluidTank inputTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"})
    public BasicFluidTank outputTank;
    @ContainerSync
    public BasicHeatCapacitor heatCapacitor;

    private boolean temperatureSet;

    private double biomeAmbientTemp;
    private double tempMultiplier;

    public float prevScale;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getProductionAmount")
    public double lastGain;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getEnvironmentalLoss")
    public double totalLoss;

    private final RecipeCacheLookupMonitor<FluidToFluidRecipe> recipeCacheLookupMonitor;

    private IEvaporationSolar[] solars = new IEvaporationSolar[4];

    private final IOutputHandler<@NonNull FluidStack> outputHandler;
    private final IInputHandler<@NonNull FluidStack> inputHandler;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItemInput")
    private final FluidInventorySlot inputInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItemOutput")
    private final OutputInventorySlot outputInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItemInput")
    private final FluidInventorySlot inputOutputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItemOutput")
    private final OutputInventorySlot outputOutputSlot;

    public EvaporationMultiblockData(TileEntityThermalEvaporationBlock tile) {
        super(tile);
        recipeCacheLookupMonitor = new RecipeCacheLookupMonitor<>(this);
        fluidTanks.add(inputTank = MultiblockFluidTank.input(this, tile, this::getMaxFluid, this::containsRecipe, recipeCacheLookupMonitor));
        fluidTanks.add(outputTank = MultiblockFluidTank.output(this, tile, () -> MAX_OUTPUT, BasicFluidTank.alwaysTrue));
        inputHandler = InputHelper.getInputHandler(inputTank);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
        inventorySlots.add(inputInputSlot = FluidInventorySlot.fill(inputTank, this, 28, 20));
        inventorySlots.add(outputInputSlot = OutputInventorySlot.at(this, 28, 51));
        inventorySlots.add(inputOutputSlot = FluidInventorySlot.drain(outputTank, this, 132, 20));
        inventorySlots.add(outputOutputSlot = OutputInventorySlot.at(this, 132, 51));
        inputInputSlot.setSlotType(ContainerSlotType.INPUT);
        inputOutputSlot.setSlotType(ContainerSlotType.INPUT);
        heatCapacitors.add(heatCapacitor = MultiblockHeatCapacitor.create(this, tile, MekanismConfig.general.evaporationHeatCapacity.get() * 3));
    }

    @Override
    public void onCreated(World world) {
        super.onCreated(world);
        // update the heat capacity now that we've read
        heatCapacitor.setHeatCapacity(MekanismConfig.general.evaporationHeatCapacity.get() * height(), true);
        updateSolars(world);
    }

    @Override
    public boolean tick(World world) {
        boolean needsPacket = super.tick(world);
        updateTemperature(world);
        inputOutputSlot.drainTank(outputOutputSlot);
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
    public void readUpdateTag(CompoundNBT tag) {
        super.readUpdateTag(tag);
        NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, fluid -> inputTank.setStack(fluid));
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevScale = scale);
        readValves(tag);
    }

    @Override
    public void writeUpdateTag(CompoundNBT tag) {
        super.writeUpdateTag(tag);
        tag.put(NBTConstants.FLUID_STORED, inputTank.getFluid().writeToNBT(new CompoundNBT()));
        tag.putFloat(NBTConstants.SCALE, prevScale);
        writeValves(tag);
    }

    private void updateTemperature(World world) {
        if (!temperatureSet) {
            //Take a rough average of the biome temperature between the min and max positions of the multiblock
            double biomeTemp = (world.getBiome(getMinPos()).getTemperature(getMinPos()) + world.getBiome(getMaxPos()).getTemperature(getMaxPos())) / 2;
            biomeAmbientTemp = HeatAPI.getAmbientTemp(biomeTemp);
            temperatureSet = true;
        }
        int activeSolars = getActiveSolars();
        if (activeSolars > 0) {
            heatCapacitor.handleHeat(MekanismConfig.general.evaporationSolarMultiplier.get() * activeSolars * heatCapacitor.getHeatCapacity());
        }
        if (Math.abs(heatCapacitor.getTemperature() - biomeAmbientTemp) < 0.001) {
            heatCapacitor.handleHeat((biomeAmbientTemp * heatCapacitor.getHeatCapacity()) - heatCapacitor.getHeat());
            totalLoss = 0;
        } else {
            double incr = MekanismConfig.general.evaporationHeatDissipation.get() * Math.sqrt(Math.abs(heatCapacitor.getTemperature() - biomeAmbientTemp));
            if (heatCapacitor.getTemperature() > biomeAmbientTemp) {
                incr = -incr;
            }
            heatCapacitor.handleHeat(heatCapacitor.getHeatCapacity() * incr);
            totalLoss = incr < 0 ? -incr / heatCapacitor.getHeatCapacity() : 0;
        }

        tempMultiplier = (Math.min(MAX_MULTIPLIER_TEMP, heatCapacitor.getTemperature()) - HeatAPI.AMBIENT_TEMP) * MekanismConfig.general.evaporationTempMultiplier.get() * ((double) height() / MAX_HEIGHT);
        updateHeatCapacitors(null);
    }

    @ComputerMethod(nameOverride = "getTemperature")
    public double getTemp() {
        return heatCapacitor.getTemperature();
    }

    public int getMaxFluid() {
        return height() * 4 * FLUID_PER_TANK;
    }

    @Nonnull
    @Override
    public MekanismRecipeType<FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> getRecipeType() {
        return MekanismRecipeType.EVAPORATING;
    }

    @Nullable
    @Override
    public FluidToFluidRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    @Nonnull
    @Override
    public CachedRecipe<FluidToFluidRecipe> createNewCachedRecipe(@Nonnull FluidToFluidRecipe recipe, int cacheIndex) {
        return new FluidToFluidCachedRecipe(recipe, inputHandler, outputHandler)
              .setActive(active -> {
                  //TODO: Make the numbers for lastGain be based on how much the recipe provides as an output rather than "assuming" it is 1 mB
                  // Also fix that the numbers don't quite accurately reflect the values as we modify number of operations, and not have a fractional
                  // amount
                  if (active) {
                      if (tempMultiplier > 0 && tempMultiplier < 1) {
                          lastGain = 1F / (int) Math.ceil(1 / tempMultiplier);
                      } else {
                          lastGain = tempMultiplier;
                      }
                  } else {
                      lastGain = 0;
                  }
              })
              .setRequiredTicks(() -> tempMultiplier > 0 && tempMultiplier < 1 ? (int) Math.ceil(1 / tempMultiplier) : 1)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax <= 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return currentMax;
                  }
                  return Math.min(currentMax, tempMultiplier > 0 && tempMultiplier < 1 ? 1 : (int) tempMultiplier);
              });
    }

    @Override
    public World getHandlerWorld() {
        return getWorld();
    }

    @ComputerMethod
    private int getActiveSolars() {
        int ret = 0;
        for (IEvaporationSolar solar : solars) {
            if (solar != null && solar.canSeeSun()) {
                ret++;
            }
        }
        return ret;
    }

    private void addSolarPanel(TileEntity tile, int i) {
        if (tile != null && !tile.isRemoved()) {
            CapabilityUtils.getCapability(tile, Capabilities.EVAPORATION_SOLAR_CAPABILITY, Direction.DOWN).ifPresent(solar -> solars[i] = solar);
        }
    }

    public boolean isSolarSpot(BlockPos pos) {
        return pos.getY() == getMaxPos().getY() && getBounds().isOnCorner(pos);
    }

    public void updateSolars(World world) {
        solars = new IEvaporationSolar[4];
        addSolarPanel(WorldUtils.getTileEntity(world, getMaxPos()), 0);
        addSolarPanel(WorldUtils.getTileEntity(world, getMaxPos().offset(-3, 0, 0)), 1);
        addSolarPanel(WorldUtils.getTileEntity(world, getMaxPos().offset(0, 0, -3)), 2);
        addSolarPanel(WorldUtils.getTileEntity(world, getMaxPos().offset(-3, 0, -3)), 3);
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getFluidAmount(), inputTank.getCapacity());
    }
}