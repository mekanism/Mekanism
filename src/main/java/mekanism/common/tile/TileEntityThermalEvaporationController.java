package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IEvaporationSolar;
import mekanism.api.TileNetworkList;
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
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityThermalEvaporationController extends TileEntityThermalEvaporationBlock implements IActiveState, ITankManager,
      ITileCachedRecipeHolder<FluidToFluidRecipe> {

    public static final int MAX_OUTPUT = 10_000;
    public static final int MAX_SOLARS = 4;
    public static final int MAX_HEIGHT = 18;

    public FluidTank inputTank;
    public FluidTank outputTank;

    public Set<Coord4D> tankParts = new ObjectOpenHashSet<>();
    public IEvaporationSolar[] solars = new IEvaporationSolar[4];

    public boolean temperatureSet = false;

    public double partialInput = 0;
    public double partialOutput = 0;

    public float biomeTemp = 0;
    //TODO: 1.14 potentially convert temperature to a double given we are using a DoubleSupplier anyways
    // Will make it so we don't have cast issues from the configs. Doing so in 1.12 may be slightly annoying
    // due to the fact the variables are stored in NBT as floats. Even though it should be able to load the float as a double
    public float temperature = 0;
    public double heatToAbsorb = 0;

    public float lastGain = 0;

    public int height = 0;

    public boolean structured = false;
    public boolean controllerConflict = false;
    public boolean isLeftOnFace;
    public int renderY;

    public boolean updatedThisTick = false;

    public int clientSolarAmount;
    public boolean clientStructured;

    public float prevScale;

    public float totalLoss = 0;

    private CachedRecipe<FluidToFluidRecipe> cachedRecipe;

    private final IOutputHandler<@NonNull FluidStack> outputHandler;
    private final IInputHandler<@NonNull FluidStack> inputHandler;

    //TODO: Better names?
    private FluidInventorySlot inputInputSlot;
    private OutputInventorySlot outputInputSlot;
    private FluidInventorySlot inputOutputSlot;
    private OutputInventorySlot outputOutputSlot;

    public TileEntityThermalEvaporationController() {
        super(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER);
        inputHandler = InputHelper.getInputHandler(inputTank, 0);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
    }

    @Override
    protected void presetVariables() {
        inputTank = new FluidTank(0);
        outputTank = new FluidTank(MAX_OUTPUT);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        //TODO: Make the inventory be accessible via the valves instead
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(inputInputSlot = FluidInventorySlot.fill(inputTank, fluid -> containsRecipe(recipe -> recipe.getInput().testType(fluid)), this, 28, 20));
        builder.addSlot(outputInputSlot = OutputInventorySlot.at(this, 28, 51));
        builder.addSlot(inputOutputSlot = FluidInventorySlot.drain(outputTank, this, 132, 20));
        builder.addSlot(outputOutputSlot = OutputInventorySlot.at(this, 132, 51));
        return builder.build();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            updatedThisTick = false;
            if (ticker == 5) {
                refresh();
            }
            if (structured) {
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
            if (structured) {
                if (Math.abs((float) inputTank.getFluidAmount() / inputTank.getCapacity() - prevScale) > 0.01) {
                    Mekanism.packetHandler.sendUpdatePacket(this);
                    prevScale = (float) inputTank.getFluidAmount() / inputTank.getCapacity();
                }
            }
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
        if (!isRemote()) {
            if (!updatedThisTick) {
                clearStructure();
                structured = buildStructure();
                if (structured != clientStructured) {
                    Mekanism.packetHandler.sendUpdatePacket(this);
                    clientStructured = structured;
                }

                if (structured) {
                    inputTank.setCapacity(getMaxFluid());

                    if (!inputTank.isEmpty()) {
                        inputTank.getFluid().setAmount(Math.min(inputTank.getFluidAmount(), getMaxFluid()));
                    }
                } else {
                    clearStructure();
                }
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
        //TODO: Have lastGain be set properly, and our setActive -> false set lastGain to zero
        //TODO: HANDLE ALL THIS STUFF, A good chunk of it can probably go in the getOutputHandler (or in a custom one we pass from here)
        // But none of it should remain outside of what gets passed in one way or another to the cached recipe
        return new FluidToFluidCachedRecipe(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(() -> structured && height > 2 && height <= MAX_HEIGHT && MekanismUtils.canFunction(this))
              .setOnFinish(this::markDirty)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax == 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return 0;
                  }

                  double tempMult = Math.max(0, getTemperature()) * MekanismConfig.general.evaporationTempMultiplier.get();
                  double multiplier = tempMult * height / (float) MAX_HEIGHT;
                  //TODO: See how close all these checks are to properly calculating usage
                  //Also set values like lastGain
                  return Math.min(MekanismUtils.clampToInt(currentMax * multiplier), currentMax);
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
        temperature = (float) Math.min(MekanismConfig.general.evaporationMaxTemp.get(), temperature + incr / (float) height);

        if (incr < 0) {
            totalLoss = prev - temperature;
        } else {
            totalLoss = 0;
        }
        heatToAbsorb = 0;
        MekanismUtils.saveChunk(this);
    }

    public float getTemperature() {
        return temperature;
    }

    public int getActiveSolars() {
        if (isRemote()) {
            return clientSolarAmount;
        }
        int ret = 0;
        for (IEvaporationSolar solar : solars) {
            if (solar != null && solar.canSeeSun()) {
                ret++;
            }
        }
        return ret;
    }

    public boolean buildStructure() {
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
        structured = true;
        markDirty();
        return true;
    }

    public boolean scanTopLayer(BlockPos currentPos) {
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

    public int getMaxFluid() {
        return height * 4 * TankUpdateProtocol.FLUID_PER_TANK;
    }

    public int getCorner(int x, int z) {
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

    public boolean scanLowerLayer(BlockPos currentPos) {
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

    public boolean addTankPart(TileEntity tile) {
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

    public boolean addSolarPanel(TileEntity tile, int i) {
        if (tile != null && !tile.isRemoved()) {
            Optional<IEvaporationSolar> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.EVAPORATION_SOLAR_CAPABILITY, Direction.DOWN));
            if (capability.isPresent()) {
                solars[i] = capability.get();
                return true;
            }
        }
        return false;
    }

    public int getScaledTempLevel(int i) {
        return (int) (i * Math.min(1, getTemperature() / MekanismConfig.general.evaporationMaxTemp.get()));
    }

    public Coord4D getRenderLocation() {
        if (!structured) {
            return null;
        }
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
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            inputTank.setFluid(dataStream.readFluidStack());
            outputTank.setFluid(dataStream.readFluidStack());

            structured = dataStream.readBoolean();
            controllerConflict = dataStream.readBoolean();
            clientSolarAmount = dataStream.readInt();
            height = dataStream.readInt();
            temperature = dataStream.readFloat();
            biomeTemp = dataStream.readFloat();
            isLeftOnFace = dataStream.readBoolean();
            lastGain = dataStream.readFloat();
            totalLoss = dataStream.readFloat();
            renderY = dataStream.readInt();

            if (structured != clientStructured) {
                inputTank.setCapacity(getMaxFluid());
                MekanismUtils.updateBlock(getWorld(), getPos());
                if (structured) {
                    // Calculate the two corners of the evap tower using the render location as basis (which is the
                    // lowest rightmost corner inside the tower, relative to the controller).
                    BlockPos corner1 = getRenderLocation().getPos().west().north().down();
                    BlockPos corner2 = corner1.east(3).south(3).up(height - 1);
                    // Use the corners to spin up the sparkle
                    Mekanism.proxy.doMultiblockSparkle(this, corner1, corner2, tile -> tile instanceof TileEntityThermalEvaporationBlock);
                }
                clientStructured = structured;
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(inputTank.getFluid());
        data.add(outputTank.getFluid());
        data.add(structured);
        data.add(controllerConflict);
        data.add(getActiveSolars());
        data.add(height);
        data.add(temperature);
        data.add(biomeTemp);
        data.add(isLeftOnFace);
        data.add(lastGain);
        data.add(totalLoss);
        data.add(renderY);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        inputTank.readFromNBT(nbtTags.getCompound("waterTank"));
        outputTank.readFromNBT(nbtTags.getCompound("brineTank"));

        temperature = nbtTags.getFloat("temperature");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("waterTank", inputTank.writeToNBT(new CompoundNBT()));
        nbtTags.put("brineTank", outputTank.writeToNBT(new CompoundNBT()));

        nbtTags.putFloat("temperature", temperature);
        return nbtTags;
    }

    @Override
    public TileEntityThermalEvaporationController getController() {
        return structured ? this : null;
    }

    public void clearStructure() {
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
    public boolean getActive() {
        return structured;
    }

    @Override
    public void setActive(boolean active) {
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{inputTank, outputTank};
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        //TODO: Should this be disabled via the inventory slots instead. (Then we can't access the items when opening the controller)
        if (!structured && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }
}