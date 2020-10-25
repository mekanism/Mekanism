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
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
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

public class EvaporationMultiblockData extends MultiblockData implements ITileCachedRecipeHolder<FluidToFluidRecipe>, IValveHandler {

    private static final int MAX_OUTPUT = 10_000;
    private static final int MAX_HEIGHT = 18;
    public static final double MAX_MULTIPLIER_TEMP = 3_000;

    @ContainerSync
    public BasicFluidTank inputTank;
    @ContainerSync
    public BasicFluidTank outputTank;
    @ContainerSync
    public BasicHeatCapacitor heatCapacitor;

    private boolean temperatureSet;

    private double biomeTemp;
    private double tempMultiplier;

    public float prevScale;
    @ContainerSync
    public double lastGain;
    @ContainerSync
    public double totalLoss;

    private CachedRecipe<FluidToFluidRecipe> cachedRecipe;

    private IEvaporationSolar[] solars = new IEvaporationSolar[4];

    private final IOutputHandler<@NonNull FluidStack> outputHandler;
    private final IInputHandler<@NonNull FluidStack> inputHandler;

    private final FluidInventorySlot inputInputSlot;
    private final OutputInventorySlot outputInputSlot;
    private final FluidInventorySlot inputOutputSlot;
    private final OutputInventorySlot outputOutputSlot;

    public EvaporationMultiblockData(TileEntityThermalEvaporationBlock tile) {
        super(tile);
        fluidTanks.add(inputTank = MultiblockFluidTank.input(this, tile, this::getMaxFluid, fluid -> containsRecipe(recipe -> recipe.getInput().testType(fluid))));
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
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
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
            biomeTemp = world.getBiomeManager().getBiome(getMinPos()).getTemperature(getMinPos());
            temperatureSet = true;
        }
        heatCapacitor.handleHeat(MekanismConfig.general.evaporationSolarMultiplier.get() * getActiveSolars() * heatCapacitor.getHeatCapacity());
        double biome = biomeTemp - 0.5;
        double base = biome > 0 ? biome * 20 : biomeTemp * 40;
        base += HeatAPI.AMBIENT_TEMP;
        if (Math.abs(getTemp() - base) < 0.001) {
            heatCapacitor.handleHeat((base * heatCapacitor.getHeatCapacity()) - heatCapacitor.getHeat());
            totalLoss = 0;
        } else {
            double incr = MekanismConfig.general.evaporationHeatDissipation.get() * Math.sqrt(Math.abs(heatCapacitor.getTemperature() - base));
            if (heatCapacitor.getTemperature() > base) {
                incr = -incr;
            }
            heatCapacitor.handleHeat(heatCapacitor.getHeatCapacity() * incr);
            totalLoss = incr < 0 ? -incr / heatCapacitor.getHeatCapacity() : 0;
        }

        tempMultiplier = (Math.min(MAX_MULTIPLIER_TEMP, heatCapacitor.getTemperature()) - HeatAPI.AMBIENT_TEMP) * MekanismConfig.general.evaporationTempMultiplier.get() * ((double) height() / MAX_HEIGHT);
        updateHeatCapacitors(null);
    }

    public double getTemp() {
        return heatCapacitor.getTemperature();
    }

    public int getMaxFluid() {
        return height() * 4 * TankMultiblockData.FLUID_PER_TANK;
    }

    @Nonnull
    @Override
    public MekanismRecipeType<FluidToFluidRecipe> getRecipeType() {
        return MekanismRecipeType.EVAPORATING;
    }

    @Nullable
    @Override
    public CachedRecipe<FluidToFluidRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public FluidToFluidRecipe getRecipe(int cacheIndex) {
        FluidStack fluid = inputHandler.getInput();
        if (fluid.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(fluid));
    }

    @Nullable
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
    public World getTileWorld() {
        return getWorld();
    }

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
        addSolarPanel(WorldUtils.getTileEntity(world, getMaxPos().add(-3, 0, 0)), 1);
        addSolarPanel(WorldUtils.getTileEntity(world, getMaxPos().add(0, 0, -3)), 2);
        addSolarPanel(WorldUtils.getTileEntity(world, getMaxPos().add(-3, 0, -3)), 3);
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getFluidAmount(), inputTank.getCapacity());
    }
}
