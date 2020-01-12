package mekanism.common.tile;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ElectrolysisCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ITankManager;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.GasUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;

public class TileEntityElectrolyticSeparator extends TileEntityMekanism implements IFluidHandlerWrapper, IComputerIntegration, ISustainedData, IGasHandler,
      ITankManager, IComparatorSupport, ITileCachedRecipeHolder<ElectrolysisRecipe> {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getWater", "getWaterNeeded", "getHydrogen",
                                                         "getHydrogenNeeded", "getOxygen", "getOxygenNeeded"};
    /**
     * This separator's water slot.
     */
    public FluidTank fluidTank;
    /**
     * The maximum amount of gas this block can store.
     */
    private static final int MAX_GAS = 2_400;
    /**
     * The amount of oxygen this block is storing.
     */
    public GasTank leftTank;
    /**
     * The amount of hydrogen this block is storing.
     */
    public GasTank rightTank;
    /**
     * How fast this block can output gas.
     */
    public int output = 512;
    /**
     * The type of gas this block is outputting.
     */
    public GasMode dumpLeft = GasMode.IDLE;
    /**
     * Type type of gas this block is dumping.
     */
    public GasMode dumpRight = GasMode.IDLE;
    public CachedRecipe<ElectrolysisRecipe> cachedRecipe;
    public double clientEnergyUsed;

    private int currentRedstoneLevel;

    //TODO: Remove this
    private double BASE_ENERGY_PER_TICK;

    private final IOutputHandler<@NonNull Pair<GasStack, GasStack>> outputHandler;
    private final IInputHandler<@NonNull FluidStack> inputHandler;

    private FluidInventorySlot fluidSlot;
    private GasInventorySlot leftOutputSlot;
    private GasInventorySlot rightOutputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityElectrolyticSeparator() {
        super(MekanismBlocks.ELECTROLYTIC_SEPARATOR);
        BASE_ENERGY_PER_TICK = super.getBaseUsage();

        inputHandler = InputHelper.getInputHandler(fluidTank, 0);
        outputHandler = OutputHelper.getOutputHandler(leftTank, rightTank);
    }

    @Override
    protected void presetVariables() {
        fluidTank = new FluidTank(24_000);
        leftTank = new GasTank(MAX_GAS);
        rightTank = new GasTank(MAX_GAS);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(fluidSlot = FluidInventorySlot.fill(fluidTank, fluid -> containsRecipe(recipe -> recipe.getInput().testType(fluid)), this, 26, 35),
              RelativeSide.FRONT);
        builder.addSlot(leftOutputSlot = GasInventorySlot.drain(leftTank, this, 59, 52), RelativeSide.LEFT);
        builder.addSlot(rightOutputSlot = GasInventorySlot.drain(rightTank, this, 101, 52), RelativeSide.RIGHT);
        //TODO: Make accessible for automation
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 143, 35));
        return builder.build();
    }

    @Override
    public double getBaseUsage() {
        return BASE_ENERGY_PER_TICK;
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            fluidSlot.fillTank();

            leftOutputSlot.drainTank();
            rightOutputSlot.drainTank();
            double prev = getEnergy();
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
            //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
            clientEnergyUsed = prev - getEnergy();

            int dumpAmount = 8 * (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
            handleTank(leftTank, dumpLeft, getLeftSide(), dumpAmount);
            handleTank(rightTank, dumpRight, getRightSide(), dumpAmount);
            World world = getWorld();
            if (world != null) {
                int newRedstoneLevel = getRedstoneLevel();
                if (newRedstoneLevel != currentRedstoneLevel) {
                    world.updateComparatorOutputLevel(pos, getBlockType());
                    currentRedstoneLevel = newRedstoneLevel;
                }
            }
        }
    }

    private void handleTank(GasTank tank, GasMode mode, Direction side, int dumpAmount) {
        if (!tank.isEmpty()) {
            if (mode != GasMode.DUMPING) {
                GasStack toSend = new GasStack(tank.getStack(), Math.min(tank.getStored(), output));
                tank.drain(GasUtils.emit(toSend, this, EnumSet.of(side)), Action.EXECUTE);
            } else {
                tank.drain(dumpAmount, Action.EXECUTE);
            }
            if (mode == GasMode.DUMPING_EXCESS && tank.getNeeded() < output) {
                tank.drain(output - tank.getNeeded(), Action.EXECUTE);
            }
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ElectrolysisRecipe> getRecipeType() {
        return MekanismRecipeType.SEPARATING;
    }

    @Nullable
    @Override
    public CachedRecipe<ElectrolysisRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ElectrolysisRecipe getRecipe(int cacheIndex) {
        FluidStack fluid = inputHandler.getInput();
        if (fluid.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(fluid));
    }

    @Nullable
    @Override
    public CachedRecipe<ElectrolysisRecipe> createNewCachedRecipe(@Nonnull ElectrolysisRecipe recipe, int cacheIndex) {
        //TODO: Is this fine, or do we need it somewhere that will get called in more places than ONLY when the cache is being made
        boolean update = BASE_ENERGY_PER_TICK != recipe.getEnergyUsage();
        BASE_ENERGY_PER_TICK = recipe.getEnergyUsage();
        if (update) {
            recalculateUpgrades(Upgrade.ENERGY);
        }
        return new ElectrolysisCachedRecipe(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setOnFinish(this::markDirty)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax == 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return 0;
                  }
                  return Math.min((int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), currentMax);
              });
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            byte type = dataStream.readByte();
            if (type == 0) {
                dumpLeft = dumpLeft.getNext();
            } else if (type == 1) {
                dumpRight = dumpRight.getNext();
            }
            return;
        }

        super.handlePacketData(dataStream);

        if (isRemote()) {
            TileUtils.readTankData(dataStream, fluidTank);
            TileUtils.readTankData(dataStream, leftTank);
            TileUtils.readTankData(dataStream, rightTank);
            dumpLeft = dataStream.readEnumValue(GasMode.class);
            dumpRight = dataStream.readEnumValue(GasMode.class);
            clientEnergyUsed = dataStream.readDouble();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        TileUtils.addTankData(data, fluidTank);
        TileUtils.addTankData(data, leftTank);
        TileUtils.addTankData(data, rightTank);
        data.add(dumpLeft);
        data.add(dumpRight);
        data.add(clientEnergyUsed);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains("fluidTank")) {
            fluidTank.readFromNBT(nbtTags.getCompound("fluidTank"));
        }
        leftTank.read(nbtTags.getCompound("leftTank"));
        rightTank.read(nbtTags.getCompound("rightTank"));
        dumpLeft = GasMode.byIndexStatic(nbtTags.getInt("dumpLeft"));
        dumpRight = GasMode.byIndexStatic(nbtTags.getInt("dumpRight"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (!fluidTank.isEmpty()) {
            nbtTags.put("fluidTank", fluidTank.writeToNBT(new CompoundNBT()));
        }
        nbtTags.put("leftTank", leftTank.write(new CompoundNBT()));
        nbtTags.put("rightTank", rightTank.write(new CompoundNBT()));
        nbtTags.putInt("dumpLeft", dumpLeft.ordinal());
        nbtTags.putInt("dumpRight", dumpRight.ordinal());
        return nbtTags;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{output};
            case 2:
                return new Object[]{getBaseStorage()};
            case 3:
                return new Object[]{getBaseStorage() - getEnergy()};
            case 4:
                return new Object[]{fluidTank.getFluidAmount()};
            case 5:
                return new Object[]{fluidTank.getCapacity() - fluidTank.getFluidAmount()};
            case 6:
                return new Object[]{leftTank.getStored()};
            case 7:
                return new Object[]{leftTank.getNeeded()};
            case 8:
                return new Object[]{rightTank.getStored()};
            case 9:
                return new Object[]{rightTank.getNeeded()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!fluidTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "fluidTank", fluidTank.getFluid().writeToNBT(new CompoundNBT()));
        }
        if (!leftTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "leftTank", leftTank.getStack().write(new CompoundNBT()));
        }
        if (!rightTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "rightTank", rightTank.getStack().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "fluidTank")));
        leftTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "leftTank")));
        rightTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "rightTank")));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new HashMap<>();
        remap.put("fluidTank", "fluidTank");
        remap.put("leftTank.stored", "leftTank");
        remap.put("rightTank.stored", "rightTank");
        return remap;
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        FluidStack currentFluid = fluidTank.getFluid();
        if (currentFluid.isEmpty()) {
            //If we don't have a fluid currently stored, then check if the fluid wanting to be input is valid for this machine
            return containsRecipe(recipe -> recipe.getInput().testType(fluid));
        }
        //Otherwise return true if the fluid is the same as the one we already have stored
        return currentFluid.isFluidEqual(fluid);
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return fluidTank.fill(resource, fluidAction);
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        return new IFluidTank[]{fluidTank};
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        if (side == getLeftSide()) {
            return leftTank.drain(amount, action);
        } else if (side == getRightSide()) {
            return rightTank.drain(amount, action);
        }
        return GasStack.EMPTY;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        if (side == getLeftSide()) {
            return !leftTank.isEmpty() && leftTank.getStack().isTypeEqual(type);
        } else if (side == getRightSide()) {
            return !rightTank.isEmpty() && rightTank.getStack().isTypeEqual(type);
        }
        return false;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{leftTank, rightTank};
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != null && side != getLeftSide() && side != getRightSide();
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //TODO: Make this just check the specific sides I think it is a shorter list?
            return side != null && side != getDirection() && side != getOppositeDirection() && side != getRightSide();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{fluidTank, leftTank, rightTank};
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    public boolean isFluidInputItem(ItemStack itemStack) {
        return new LazyOptionalHelper<>(FluidUtil.getFluidContained(itemStack)).matches(
              fluidStack -> !fluidStack.isEmpty() && containsRecipe(recipe -> recipe.getInput().testType(fluidStack)));
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }
}