package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IEvaporationSolar;
import mekanism.api.TileNetworkList;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.FluidToFluidCachedRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.FluidChecker;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityThermalEvaporationController extends TileEntityThermalEvaporationBlock implements IActiveState, ITankManager, ICachedRecipeHolder<FluidToFluidRecipe> {

    public static final int MAX_OUTPUT = 10000;
    public static final int MAX_SOLARS = 4;
    public static final int MAX_HEIGHT = 18;
    private static final int[] SLOTS = {0, 1, 2, 3};

    public FluidTank inputTank = new FluidTank(0);
    public FluidTank outputTank = new FluidTank(MAX_OUTPUT);

    public Set<Coord4D> tankParts = new HashSet<>();
    public IEvaporationSolar[] solars = new IEvaporationSolar[4];

    public boolean temperatureSet = false;

    public float biomeTemp = 0;
    //TODO: 1.14 potentially convert temperature to a double given we are using a DoubleSupplier anyways
    // Will make it so we don't have cast issues from the configs. Doing so in 1.12 may be slightly annoying
    // due to the fact the variables are stored in NBT as floats. Even though it should be able to load the float as a double
    public float temperature = 0;
    public float heatToAbsorb = 0;

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

    public CachedRecipe<FluidToFluidRecipe> cachedRecipe;

    public TileEntityThermalEvaporationController() {
        super("ThermalEvaporationController");
        inventory = NonNullList.withSize(SLOTS.length, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            updatedThisTick = false;
            if (ticker == 5) {
                refresh();
            }
            if (structured) {
                updateTemperature();
                manageBuckets();
            }
            cachedRecipe = getUpdatedCache(cachedRecipe, 0);
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
    public void onChunkUnload() {
        super.onChunkUnload();
        refresh();
    }

    @Override
    public void onNeighborChange(Block block) {
        super.onNeighborChange(block);
        refresh();
    }

    public boolean hasRecipe(FluidStack fluid) {
        return Recipe.THERMAL_EVAPORATION_PLANT.contains(recipe -> recipe.getInput().testType(fluid));
    }

    protected void refresh() {
        if (!world.isRemote) {
            if (!updatedThisTick) {
                clearStructure();
                structured = buildStructure();
                if (structured != clientStructured) {
                    Mekanism.packetHandler.sendUpdatePacket(this);
                    clientStructured = structured;
                }

                if (structured) {
                    inputTank.setCapacity(getMaxFluid());

                    if (inputTank.getFluid() != null) {
                        inputTank.getFluid().amount = Math.min(inputTank.getFluid().amount, getMaxFluid());
                    }
                } else {
                    clearStructure();
                }
            }
        }
    }

    @Nonnull
    @Override
    public Recipe<FluidToFluidRecipe> getRecipes() {
        return Recipe.THERMAL_EVAPORATION_PLANT;
    }

    @Nullable
    @Override
    public FluidToFluidRecipe getRecipe(int cacheIndex) {
        FluidStack fluid = inputTank.getFluid();
        return fluid == null ? null : getRecipes().findFirst(recipe -> recipe.test(fluid));
    }

    @Nullable
    @Override
    public CachedRecipe<FluidToFluidRecipe> createNewCachedRecipe(@Nonnull FluidToFluidRecipe recipe, int cacheIndex) {
        //TODO: Have lastGain be set properly, and our setActive -> false set lastGain to zero
        //TODO: HANDLE ALL THIS STUFF, A good chunk of it can probably go in the getOutputHandler (or in a custom one we pass from here)
        // But none of it should remain outside of what gets passed in one way or another to the cached reipe
        return new FluidToFluidCachedRecipe(recipe, () -> inputTank, OutputHelper.getOutputHandler(outputTank))
              .setCanHolderFunction(() -> structured && height > 2 && height <= MAX_HEIGHT && MekanismUtils.canFunction(this))
              .setOnFinish(this::markDirty)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax == 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return 0;
                  }

                  double tempMult = Math.max(0, getTemperature()) * MekanismConfig.current().general.evaporationTempMultiplier.val();
                  double multiplier = tempMult * height / (float) MAX_HEIGHT;
                  //TODO: See how close all these checks are to properly calculating usage
                  //Also set values like lastGain
                  return Math.min(MekanismUtils.clampToInt(currentMax * multiplier), currentMax);
              });
    }

    private void manageBuckets() {
        if (outputTank.getFluid() != null) {
            if (FluidContainerUtils.isFluidContainer(inventory.get(2))) {
                FluidContainerUtils.handleContainerItemFill(this, outputTank, 2, 3);
            }
        }

        if (structured) {
            if (FluidContainerUtils.isFluidContainer(inventory.get(0))) {
                FluidContainerUtils.handleContainerItemEmpty(this, inputTank, 0, 1, new FluidChecker() {
                    @Override
                    public boolean isValid(Fluid f) {
                        return hasRecipe(new FluidStack(f, 1));
                    }
                });
            }
        }
    }

    private void updateTemperature() {
        if (!temperatureSet) {
            biomeTemp = world.getBiomeForCoordsBody(getPos()).getTemperature(getPos());
            temperatureSet = true;
        }
        heatToAbsorb += getActiveSolars() * MekanismConfig.current().general.evaporationSolarMultiplier.val();
        temperature += heatToAbsorb / (float) height;

        float biome = biomeTemp - 0.5F;
        float base = biome > 0 ? biome * 20 : biomeTemp * 40;

        if (Math.abs(temperature - base) < 0.001) {
            temperature = base;
        }
        float incr = (float) Math.sqrt(Math.abs(temperature - base)) * (float) MekanismConfig.current().general.evaporationHeatDissipation.val();

        if (temperature > base) {
            incr = -incr;
        }

        float prev = temperature;
        temperature = (float) Math.min(MekanismConfig.current().general.evaporationMaxTemp.val(), temperature + incr / (float) height);

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
        if (world.isRemote) {
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
        EnumFacing right = MekanismUtils.getRight(facing);
        EnumFacing left = MekanismUtils.getLeft(facing);
        height = 0;
        controllerConflict = false;
        updatedThisTick = true;

        Coord4D startPoint = Coord4D.get(this);
        while (startPoint.offset(EnumFacing.UP).getTileEntity(world) instanceof TileEntityThermalEvaporationBlock) {
            startPoint = startPoint.offset(EnumFacing.UP);
        }

        Coord4D test = startPoint.offset(EnumFacing.DOWN).offset(right, 2);
        isLeftOnFace = test.getTileEntity(world) instanceof TileEntityThermalEvaporationBlock;
        startPoint = startPoint.offset(left, isLeftOnFace ? 1 : 2);
        if (!scanTopLayer(startPoint)) {
            return false;
        }

        height = 1;

        Coord4D middlePointer = startPoint.offset(EnumFacing.DOWN);
        while (scanLowerLayer(middlePointer)) {
            middlePointer = middlePointer.offset(EnumFacing.DOWN);
        }
        renderY = middlePointer.y + 1;
        if (height < 3 || height > MAX_HEIGHT) {
            height = 0;
            return false;
        }
        structured = true;
        markDirty();
        return true;
    }

    public boolean scanTopLayer(Coord4D current) {
        EnumFacing right = MekanismUtils.getRight(facing);
        EnumFacing back = MekanismUtils.getBack(facing);
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                Coord4D pointer = current.offset(right, x).offset(back, z);
                TileEntity pointerTile = pointer.getTileEntity(world);
                int corner = getCorner(x, z);
                if (corner != -1) {
                    if (!addSolarPanel(pointer.getTileEntity(world), corner)) {
                        if (pointer.offset(EnumFacing.UP).getTileEntity(world) instanceof TileEntityThermalEvaporationBlock || !addTankPart(pointerTile)) {
                            return false;
                        }
                    }
                } else if ((x == 1 || x == 2) && (z == 1 || z == 2)) {
                    if (!pointer.isAirBlock(world)) {
                        return false;
                    }
                } else if (pointer.offset(EnumFacing.UP).getTileEntity(world) instanceof TileEntityThermalEvaporationBlock || !addTankPart(pointerTile)) {
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

    public boolean scanLowerLayer(Coord4D current) {
        EnumFacing right = MekanismUtils.getRight(facing);
        EnumFacing back = MekanismUtils.getBack(facing);
        boolean foundCenter = false;
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                Coord4D pointer = current.offset(right, x).offset(back, z);
                TileEntity pointerTile = pointer.getTileEntity(world);
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
                    } else if (foundCenter || !pointer.isAirBlock(world)) {
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
        if (tile != null && !tile.isInvalid() && CapabilityUtils.hasCapability(tile, Capabilities.EVAPORATION_SOLAR_CAPABILITY, EnumFacing.DOWN)) {
            solars[i] = CapabilityUtils.getCapability(tile, Capabilities.EVAPORATION_SOLAR_CAPABILITY, EnumFacing.DOWN);
            return true;
        }
        return false;
    }

    public int getScaledTempLevel(int i) {
        return (int) (i * Math.min(1, getTemperature() / MekanismConfig.current().general.evaporationMaxTemp.val()));
    }

    public Coord4D getRenderLocation() {
        if (!structured) {
            return null;
        }
        EnumFacing right = MekanismUtils.getRight(facing);
        Coord4D renderLocation = Coord4D.get(this).offset(right);
        renderLocation = isLeftOnFace ? renderLocation.offset(right) : renderLocation;
        renderLocation = renderLocation.offset(right.getOpposite()).offset(MekanismUtils.getBack(facing));
        renderLocation.y = renderY;
        switch (facing) {
            case SOUTH:
                renderLocation = renderLocation.offset(EnumFacing.NORTH).offset(EnumFacing.WEST);
                break;
            case WEST:
                renderLocation = renderLocation.offset(EnumFacing.NORTH);
                break;
            case EAST:
                renderLocation = renderLocation.offset(EnumFacing.WEST);
                break;
        }
        return renderLocation;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            TileUtils.readTankData(dataStream, inputTank);
            TileUtils.readTankData(dataStream, outputTank);

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
                MekanismUtils.updateBlock(world, getPos());
                if (structured) {
                    // Calculate the two corners of the evap tower using the render location as basis (which is the
                    // lowest rightmost corner inside the tower, relative to the controller).
                    BlockPos corner1 = getRenderLocation().getPos().offset(EnumFacing.WEST).offset(EnumFacing.NORTH).down();
                    BlockPos corner2 = corner1.offset(EnumFacing.EAST, 3).offset(EnumFacing.SOUTH, 3).up(height - 1);
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
        TileUtils.addTankData(data, inputTank);
        TileUtils.addTankData(data, outputTank);
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
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        inputTank.readFromNBT(nbtTags.getCompoundTag("waterTank"));
        outputTank.readFromNBT(nbtTags.getCompoundTag("brineTank"));

        temperature = nbtTags.getFloat("temperature");

        //TODO: These are no longer used, or do they need to be used
        //partialInput = nbtTags.getDouble("partialWater");
        //partialOutput = nbtTags.getDouble("partialBrine");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setTag("waterTank", inputTank.writeToNBT(new NBTTagCompound()));
        nbtTags.setTag("brineTank", outputTank.writeToNBT(new NBTTagCompound()));

        nbtTags.setFloat("temperature", temperature);

        //TODO: These are no longer used, or do they need to be used
        //nbtTags.setDouble("partialWater", partialInput);
        //nbtTags.setDouble("partialBrine", partialOutput);
        return nbtTags;
    }

    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP;
    }

    @Override
    public TileEntityThermalEvaporationController getController() {
        return structured ? this : null;
    }

    public void clearStructure() {
        for (Coord4D tankPart : tankParts) {
            TileEntity tile = tankPart.getTileEntity(world);
            if (tile instanceof TileEntityThermalEvaporationBlock) {
                ((TileEntityThermalEvaporationBlock) tile).controllerGone();
            }
        }
        tankParts.clear();
        solars = new IEvaporationSolar[]{null, null, null, null};
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
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
    public boolean lightUpdate() {
        return false;
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{inputTank, outputTank};
    }

    //TODO: Move getSlotsForFace, isItemValidForSlot, and isCapabilityDisabled to Valve
    //NOTE: For now it has to be in the controller as it uses the old multiblock structure so the valve's don't actually
    //have an inventory, which causes a crash trying to insert into them
    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return getController() == null ? InventoryUtils.EMPTY : SLOTS;
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        if (slot == 0) {
            return FluidContainerUtils.isFluidContainer(stack) && FluidUtil.getFluidContained(stack) != null;
        } else if (slot == 2) {
            return FluidContainerUtils.isFluidContainer(stack) && FluidUtil.getFluidContained(stack) == null;
        }
        return false;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return false;
        }
        return super.isCapabilityDisabled(capability, side);
    }
}