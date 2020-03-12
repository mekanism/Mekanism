package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IEvaporationSolar;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.FluidToFluidCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFloat;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityThermalEvaporationController extends TileEntityThermalEvaporationBlock implements IActiveState, ITankManager,
      ITileCachedRecipeHolder<FluidToFluidRecipe> {

    private static final int MAX_OUTPUT = 10_000;
    private static final int MAX_HEIGHT = 18;

    public BasicFluidTank inputTank;
    public BasicFluidTank outputTank;

    private Set<Coord4D> tankParts = new ObjectOpenHashSet<>();
    private IEvaporationSolar[] solars = new IEvaporationSolar[4];

    private boolean temperatureSet;

    private float biomeTemp;
    //TODO: Evaluate converting this temperature to a double as all other places temperature is stored it is a double
    private float temperature;
    public double heatToAbsorb;
    private double tempMultiplier;

    public float lastGain;

    private int inputCapacity;
    public int height;

    private boolean clientStructured;
    public boolean controllerConflict;
    private boolean isLeftOnFace;
    private int renderY;

    private boolean updatedThisTick;

    private float prevScale;

    public float totalLoss;

    private CachedRecipe<FluidToFluidRecipe> cachedRecipe;

    private final IOutputHandler<@NonNull FluidStack> outputHandler;
    private final IInputHandler<@NonNull FluidStack> inputHandler;

    private FluidInventorySlot inputInputSlot;
    private OutputInventorySlot outputInputSlot;
    private FluidInventorySlot inputOutputSlot;
    private OutputInventorySlot outputOutputSlot;

    public TileEntityThermalEvaporationController() {
        super(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER);
        inputHandler = InputHelper.getInputHandler(inputTank);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(inputTank = VariableCapacityFluidTank.input(this::getMaxFluid, fluid -> containsRecipe(recipe -> recipe.getInput().testType(fluid)), this));
        builder.addTank(outputTank = BasicFluidTank.output(MAX_OUTPUT, this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        //TODO: Make the inventory be accessible via the valves instead
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(inputInputSlot = FluidInventorySlot.fill(inputTank, this, 28, 20));
        builder.addSlot(outputInputSlot = OutputInventorySlot.at(this, 28, 51));
        builder.addSlot(inputOutputSlot = FluidInventorySlot.drain(outputTank, this, 132, 20));
        builder.addSlot(outputOutputSlot = OutputInventorySlot.at(this, 132, 51));
        inputInputSlot.setSlotType(ContainerSlotType.INPUT);
        inputOutputSlot.setSlotType(ContainerSlotType.INPUT);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        updatedThisTick = false;
        if (ticker == 5) {
            refresh();
        }
        boolean active = getActive();
        if (active && height == 0) {
            //If we are active but we can't possibly be valid and our data will get corrupted
            // due to not actually having a valid height, then force a refresh
            //TODO: Find a better way to do this, maybe once the evap tower has multiblock data
            // in general, if that is how we end up rewriting the
            refresh();
            active = getActive();
        }
        if (active) {
            updateTemperature();
            inputOutputSlot.drainTank(outputOutputSlot);
            inputInputSlot.fillTank(outputInputSlot);
        }
        //Note: This is not in a structured check as we want to make sure it stops if we do not have a structure
        //TODO: Think through the logic, given we are calling the process so technically if it is not structured, then we
        // don't actually have it processing so we don't need this outside of the structured? Verify
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
        if (active && Math.abs((float) inputTank.getFluidAmount() / inputTank.getCapacity() - prevScale) > 0.01) {
            prevScale = (float) inputTank.getFluidAmount() / inputTank.getCapacity();
            sendUpdatePacket();
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        refresh();
    }

    @Override
    public void onNeighborChange(Block block) {
        super.onNeighborChange(block);
        refresh();
    }

    public boolean hasRecipe(FluidStack fluid) {
        return containsRecipe(recipe -> recipe.getInput().testType(fluid));
    }

    protected void refresh() {
        if (!isRemote() && !updatedThisTick) {
            clearStructure();
            boolean active = buildStructure();
            setActive(active);
            if (active) {
                updateMaxFluid();
                if (!inputTank.isEmpty()) {
                    inputTank.setStackSize(Math.min(inputTank.getFluidAmount(), getMaxFluid()), Action.EXECUTE);
                }
            } else {
                clearStructure();
            }
        }
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
              .setCanHolderFunction(() -> getActive() && height > 2 && height <= MAX_HEIGHT && MekanismUtils.canFunction(this))
              .setOnFinish(this::markDirty)
              .setActive(active -> {
                  //TODO: Make the numbers for lastGain be based on how much the recipe provides as an output rather than "assuming" it is 1 mB
                  // Also fix that the numbers don't quite accurately reflect the values as we modify number of operations, and not have a fractional
                  // amount
                  if (active) {
                      if (tempMultiplier > 0 && tempMultiplier < 1) {
                          lastGain = 1F / (int) Math.ceil(1 / tempMultiplier);
                      } else {
                          lastGain = (float) tempMultiplier;
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

    private void updateTemperature() {
        if (!temperatureSet) {
            biomeTemp = world.getBiomeManager().getBiome(getPos()).getTemperature(getPos());
            temperatureSet = true;
        }
        heatToAbsorb += getActiveSolars() * MekanismConfig.general.evaporationSolarMultiplier.get();
        temperature += (float) (heatToAbsorb / height);

        float biome = biomeTemp - 0.5F;
        float base = biome > 0 ? biome * 20 : biomeTemp * 40;

        if (Math.abs(temperature - base) < 0.001) {
            temperature = base;
        }
        float incr = (float) Math.sqrt(Math.abs(temperature - base)) * MekanismConfig.general.evaporationHeatDissipation.get();

        if (temperature > base) {
            incr = -incr;
        }

        float prev = temperature;
        temperature = (float) Math.min(MekanismConfig.general.evaporationMaxTemp.get(), temperature + incr / height);

        if (incr < 0) {
            totalLoss = prev - temperature;
        } else {
            totalLoss = 0;
        }
        heatToAbsorb = 0;
        tempMultiplier = Math.max(0, temperature) * MekanismConfig.general.evaporationTempMultiplier.get() * height / MAX_HEIGHT;
        markDirty();
    }

    public float getTemperature() {
        return temperature;
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

    private boolean buildStructure() {
        Direction right = getRightSide();
        Direction left = getLeftSide();
        height = 0;
        controllerConflict = false;
        updatedThisTick = true;

        BlockPos startPoint = getPos();
        while (MekanismUtils.getTileEntity(TileEntityThermalEvaporationBlock.class, world, startPoint.up()) != null) {
            startPoint = startPoint.up();
        }

        BlockPos test = startPoint.down().offset(right, 2);
        isLeftOnFace = MekanismUtils.getTileEntity(TileEntityThermalEvaporationBlock.class, world, test) != null;
        startPoint = startPoint.offset(left, isLeftOnFace ? 1 : 2);
        if (!scanTopLayer(startPoint)) {
            return false;
        }

        height = 1;

        BlockPos middlePointer = startPoint.down();
        while (scanLowerLayer(middlePointer)) {
            middlePointer = middlePointer.down();
        }
        renderY = middlePointer.getY() + 1;
        if (height < 3 || height > MAX_HEIGHT) {
            height = 0;
            return false;
        }
        markDirty();
        return true;
    }

    private boolean scanTopLayer(BlockPos currentPos) {
        Direction right = getRightSide();
        Direction back = getOppositeDirection();
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                BlockPos pointerPos = currentPos.offset(right, x).offset(back, z);
                TileEntity pointerTile = MekanismUtils.getTileEntity(world, pointerPos);
                int corner = getCorner(x, z);
                if (corner != -1) {
                    if (!addSolarPanel(pointerTile, corner)) {
                        if (MekanismUtils.getTileEntity(TileEntityThermalEvaporationBlock.class, world, pointerPos.up()) != null || !addTankPart(pointerTile)) {
                            return false;
                        }
                    }
                } else if ((x == 1 || x == 2) && (z == 1 || z == 2)) {
                    if (!world.isAirBlock(pointerPos)) {
                        return false;
                    }
                } else if (MekanismUtils.getTileEntity(TileEntityThermalEvaporationBlock.class, world, pointerPos.up()) != null || !addTankPart(pointerTile)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateMaxFluid() {
        inputCapacity = height * 4 * TankUpdateProtocol.FLUID_PER_TANK;
    }

    public int getMaxFluid() {
        return inputCapacity;
    }

    private int getCorner(int x, int z) {
        if (x == 0 && z == 0) {
            return 0;
        } else if (x == 0 && z == 3) {
            return 1;
        } else if (x == 3 && z == 0) {
            return 2;
        } else if (x == 3 && z == 3) {
            return 3;
        }
        return -1;
    }

    private boolean scanLowerLayer(BlockPos currentPos) {
        Direction right = getRightSide();
        Direction back = getOppositeDirection();
        boolean foundCenter = false;
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                BlockPos pointerPos = currentPos.offset(right, x).offset(back, z);
                TileEntity pointerTile = MekanismUtils.getTileEntity(world, pointerPos);
                if ((x == 1 || x == 2) && (z == 1 || z == 2)) {
                    if (pointerTile instanceof TileEntityThermalEvaporationBlock) {
                        if (!foundCenter) {
                            if (x == 1 && z == 1) {
                                foundCenter = true;
                            } else {
                                height = -1;
                                return false;
                            }
                        }
                    } else if (foundCenter || !world.isAirBlock(pointerPos)) {
                        height = -1;
                        return false;
                    }
                } else if (!addTankPart(pointerTile)) {
                    height = -1;
                    return false;
                }
            }
        }
        height++;
        return !foundCenter;
    }

    private boolean addTankPart(TileEntity tile) {
        if (tile instanceof TileEntityThermalEvaporationBlock && (tile == this || !(tile instanceof TileEntityThermalEvaporationController))) {
            if (tile != this) {
                ((TileEntityThermalEvaporationBlock) tile).addToStructure(Coord4D.get(this));
                tankParts.add(Coord4D.get(tile));
            }
            return true;
        } else if (tile != this && tile instanceof TileEntityThermalEvaporationController) {
            controllerConflict = true;
        }
        return false;
    }

    private boolean addSolarPanel(TileEntity tile, int i) {
        if (tile != null && !tile.isRemoved()) {
            Optional<IEvaporationSolar> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.EVAPORATION_SOLAR_CAPABILITY, Direction.DOWN));
            if (capability.isPresent()) {
                solars[i] = capability.get();
                return true;
            }
        }
        return false;
    }

    public Coord4D getRenderLocation() {
        Direction right = getRightSide();
        Coord4D renderLocation = Coord4D.get(this).offset(right);
        renderLocation = isLeftOnFace ? renderLocation.offset(right) : renderLocation;
        renderLocation = renderLocation.offset(getLeftSide()).offset(getOppositeDirection());
        renderLocation.y = renderY;
        switch (getDirection()) {
            case SOUTH:
                renderLocation = renderLocation.offset(Direction.NORTH).offset(Direction.WEST);
                break;
            case WEST:
                renderLocation = renderLocation.offset(Direction.NORTH);
                break;
            case EAST:
                renderLocation = renderLocation.offset(Direction.WEST);
                break;
        }
        return renderLocation;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        temperature = nbtTags.getFloat(NBTConstants.TEMPERATURE);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putFloat(NBTConstants.TEMPERATURE, temperature);
        return nbtTags;
    }

    @Override
    public TileEntityThermalEvaporationController getController() {
        return getActive() ? this : null;
    }

    private void clearStructure() {
        for (Coord4D tankPart : tankParts) {
            TileEntityThermalEvaporationBlock tile = MekanismUtils.getTileEntity(TileEntityThermalEvaporationBlock.class, world, tankPart.getPos());
            if (tile != null) {
                tile.controllerGone();
            }
        }
        tankParts.clear();
        solars = new IEvaporationSolar[]{null, null, null, null};
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (active != clientStructured) {
            clientStructured = active;
            sendUpdatePacket();
        }
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public Object[] getManagedTanks() {
        return new Object[]{inputTank, outputTank};
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        //TODO: Should this be disabled via the inventory slots instead. (Then we can't access the items when opening the controller)
        if (!getActive() && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            //Never allow the fluid handler cap to be enabled here even though internally we handle fluid
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> height, value -> height = value));
        container.track(SyncableBoolean.create(() -> controllerConflict, value -> controllerConflict = value));
        container.track(SyncableFloat.create(this::getTemperature, value -> temperature = value));
        container.track(SyncableFloat.create(() -> lastGain, value -> lastGain = value));
        container.track(SyncableFloat.create(() -> totalLoss, value -> totalLoss = value));
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        updateTag.put(NBTConstants.FLUID_STORED, inputTank.getFluid().writeToNBT(new CompoundNBT()));
        updateTag.putInt(NBTConstants.HEIGHT, height);
        updateTag.putBoolean(NBTConstants.LEFT_ON_FACE, isLeftOnFace);
        updateTag.putInt(NBTConstants.RENDER_Y, renderY);
        updateTag.putBoolean(NBTConstants.ACTIVE, getActive());
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, fluid -> inputTank.setStack(fluid));
        NBTUtils.setIntIfPresent(tag, NBTConstants.HEIGHT, value -> {
            height = value;
            updateMaxFluid();
        });
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.LEFT_ON_FACE, value -> isLeftOnFace = value);
        NBTUtils.setIntIfPresent(tag, NBTConstants.RENDER_Y, value -> renderY = value);
        //Note: we send the active state over the network as when we are force syncing it we may not have the accurate block state
        // on the client yet
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.LEFT_ON_FACE, active -> {
            if (clientStructured != active) {
                clientStructured = active;
                if (active) {
                    // Calculate the two corners of the evap tower using the render location as basis (which is the
                    // lowest rightmost corner inside the tower, relative to the controller).
                    BlockPos corner1 = getRenderLocation().getPos().west().north().down();
                    BlockPos corner2 = corner1.east(3).south(3).up(height - 1);
                    // Use the corners to spin up the sparkle
                    Mekanism.proxy.doMultiblockSparkle(this, corner1, corner2, tile -> tile instanceof TileEntityThermalEvaporationBlock);
                }
            }
        });
    }
}