package mekanism.common.tile;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IEvaporationSolar;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ITankManager;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.FluidChecker;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityThermalEvaporationController extends TileEntityThermalEvaporationBlock implements IActiveState, ITankManager {

    public static final int MAX_OUTPUT = 10000;
    public static final int MAX_SOLARS = 4;
    public static final int MAX_HEIGHT = 18;
    private static final int[] SLOTS = {0, 1, 2, 3};

    public FluidTank inputTank = new FluidTank(0);
    public FluidTank outputTank = new FluidTank(MAX_OUTPUT);

    public Set<Coord4D> tankParts = new HashSet<>();
    public IEvaporationSolar[] solars = new IEvaporationSolar[4];

    public boolean temperatureSet = false;

    public double partialInput = 0;
    public double partialOutput = 0;

    public float biomeTemp = 0;
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

    public boolean cacheStructure = false;

    public float prevScale;

    public float totalLoss = 0;

    public TileEntityThermalEvaporationController() {
        super(MekanismBlock.THERMAL_EVAPORATION_CONTROLLER);
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
            }

            manageBuckets();

            ThermalEvaporationRecipe recipe = getRecipe();
            if (canOperate(recipe)) {
                int outputNeeded = outputTank.getCapacity() - outputTank.getFluidAmount();
                int inputStored = inputTank.getFluidAmount();
                double outputRatio = (double) recipe.recipeOutput.output.getAmount() / (double) recipe.recipeInput.ingredient.getAmount();
                double tempMult = Math.max(0, getTemperature()) * MekanismConfig.general.evaporationTempMultiplier.get();
                double inputToUse = tempMult * recipe.recipeInput.ingredient.getAmount() * ((float) height / (float) MAX_HEIGHT);
                inputToUse = Math.min(inputTank.getFluidAmount(), inputToUse);
                inputToUse = Math.min(inputToUse, outputNeeded / outputRatio);

                lastGain = (float) inputToUse / (float) recipe.recipeInput.ingredient.getAmount();
                partialInput += inputToUse;

                if (partialInput >= 1) {
                    int inputInt = (int) Math.floor(partialInput);
                    inputTank.drain(inputInt, FluidAction.EXECUTE);
                    partialInput %= 1;
                    partialOutput += (double) inputInt / recipe.recipeInput.ingredient.getAmount();
                }

                if (partialOutput >= 1) {
                    int outputInt = (int) Math.floor(partialOutput);
                    outputTank.fill(new FluidStack(recipe.recipeOutput.output.getFluid(), outputInt), FluidAction.EXECUTE);
                    partialOutput %= 1;
                }
            } else {
                lastGain = 0;
            }
            if (structured) {
                if (Math.abs((float) inputTank.getFluidAmount() / inputTank.getCapacity() - prevScale) > 0.01) {
                    Mekanism.packetHandler.sendUpdatePacket(this);
                    prevScale = (float) inputTank.getFluidAmount() / inputTank.getCapacity();
                }
            }
        }
    }

    public ThermalEvaporationRecipe getRecipe() {
        return RecipeHandler.getThermalEvaporationRecipe(new FluidInput(inputTank.getFluid()));
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

    public boolean hasRecipe(Fluid fluid) {
        if (fluid == null) {
            return false;
        }
        return Recipe.THERMAL_EVAPORATION_PLANT.containsRecipe(fluid);
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
                        inputTank.getFluid().setAmount(Math.min(inputTank.getFluid().getAmount(), getMaxFluid()));
                    }
                } else {
                    clearStructure();
                }
            }
        }
    }

    public boolean canOperate(ThermalEvaporationRecipe recipe) {
        if (!structured || height < 3 || height > MAX_HEIGHT || inputTank.getFluid() == null) {
            return false;
        }
        return recipe != null && recipe.canOperate(inputTank, outputTank);

    }

    private void manageBuckets() {
        if (outputTank.getFluid() != null) {
            if (FluidContainerUtils.isFluidContainer(getInventory().get(2))) {
                FluidContainerUtils.handleContainerItemFill(this, outputTank, 2, 3);
            }
        }

        if (structured) {
            if (FluidContainerUtils.isFluidContainer(getInventory().get(0))) {
                FluidContainerUtils.handleContainerItemEmpty(this, inputTank, 0, 1, new FluidChecker() {
                    @Override
                    public boolean isValid(Fluid f) {
                        return hasRecipe(f);
                    }
                });
            }
        }
    }

    private void updateTemperature() {
        if (!temperatureSet) {
            biomeTemp = world.getBiomeBody(getPos()).getTemperature(getPos());
            temperatureSet = true;
        }
        heatToAbsorb += getActiveSolars() * MekanismConfig.general.evaporationSolarMultiplier.get();
        temperature += heatToAbsorb / (float) height;

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
        Direction right = getRightSide();
        Direction left = getLeftSide();
        height = 0;
        controllerConflict = false;
        updatedThisTick = true;

        Coord4D startPoint = Coord4D.get(this);
        while (startPoint.offset(Direction.UP).getTileEntity(world) instanceof TileEntityThermalEvaporationBlock) {
            startPoint = startPoint.offset(Direction.UP);
        }

        Coord4D test = startPoint.offset(Direction.DOWN).offset(right, 2);
        isLeftOnFace = test.getTileEntity(world) instanceof TileEntityThermalEvaporationBlock;
        startPoint = startPoint.offset(left, isLeftOnFace ? 1 : 2);
        if (!scanTopLayer(startPoint)) {
            return false;
        }

        height = 1;

        Coord4D middlePointer = startPoint.offset(Direction.DOWN);
        while (scanLowerLayer(middlePointer)) {
            middlePointer = middlePointer.offset(Direction.DOWN);
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
        Direction right = getRightSide();
        Direction back = getOppositeDirection();
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                Coord4D pointer = current.offset(right, x).offset(back, z);
                TileEntity pointerTile = pointer.getTileEntity(world);
                int corner = getCorner(x, z);
                if (corner != -1) {
                    if (!addSolarPanel(pointer.getTileEntity(world), corner)) {
                        if (pointer.offset(Direction.UP).getTileEntity(world) instanceof TileEntityThermalEvaporationBlock || !addTankPart(pointerTile)) {
                            return false;
                        }
                    }
                } else if ((x == 1 || x == 2) && (z == 1 || z == 2)) {
                    if (!pointer.isAirBlock(world)) {
                        return false;
                    }
                } else if (pointer.offset(Direction.UP).getTileEntity(world) instanceof TileEntityThermalEvaporationBlock || !addTankPart(pointerTile)) {
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
        Direction right = getRightSide();
        Direction back = getOppositeDirection();
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
        if (tile != null && !tile.isRemoved()) {
            LazyOptionalHelper<IEvaporationSolar> capabilityHelper = CapabilityUtils.getCapabilityHelper(tile, Capabilities.EVAPORATION_SOLAR_CAPABILITY, Direction.DOWN);
            capabilityHelper.ifPresent(solar -> solars[i] = solar);
            return capabilityHelper.isPresent();
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
        if (world.isRemote) {
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
                    BlockPos corner1 = getRenderLocation().getPos().offset(Direction.WEST).offset(Direction.NORTH).down();
                    BlockPos corner2 = corner1.offset(Direction.EAST, 3).offset(Direction.SOUTH, 3).up(height - 1);
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
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        inputTank.readFromNBT(nbtTags.getCompound("waterTank"));
        outputTank.readFromNBT(nbtTags.getCompound("brineTank"));

        temperature = nbtTags.getFloat("temperature");

        partialInput = nbtTags.getDouble("partialWater");
        partialOutput = nbtTags.getDouble("partialBrine");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("waterTank", inputTank.writeToNBT(new CompoundNBT()));
        nbtTags.put("brineTank", outputTank.writeToNBT(new CompoundNBT()));

        nbtTags.putFloat("temperature", temperature);

        nbtTags.putDouble("partialWater", partialInput);
        nbtTags.putDouble("partialBrine", partialOutput);
        return nbtTags;
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
    @OnlyIn(Dist.CLIENT)
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

    //TODO: Move getSlotsForFace, isItemValidForSlot, and isCapabilityDisabled to Valve
    //NOTE: For now it has to be in the controller as it uses the old multiblock structure so the valve's don't actually
    //have an inventory, which causes a crash trying to insert into them
    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return getController() == null ? InventoryUtils.EMPTY : SLOTS;
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        if (slot == 0) {
            return FluidContainerUtils.isFluidContainer(stack) && FluidUtil.getFluidContained(stack).isPresent();
        } else if (slot == 2) {
            return FluidContainerUtils.isFluidContainer(stack) && !FluidUtil.getFluidContained(stack).isPresent();
        }
        return false;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return false;
        }
        return super.isCapabilityDisabled(capability, side);
    }
}