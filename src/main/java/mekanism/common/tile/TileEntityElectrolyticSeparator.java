package mekanism.common.tile;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ElectrolysisCachedRecipe;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.MekanismBlock;
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ITankManager;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.tile.prefab.TileEntityMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityElectrolyticSeparator extends TileEntityMachine implements IFluidHandlerWrapper, IComputerIntegration, ISustainedData, IGasHandler,
      IUpgradeInfoHandler, ITankManager, IComparatorSupport, ITileCachedRecipeHolder<ElectrolysisRecipe> {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getWater", "getWaterNeeded", "getHydrogen",
                                                         "getHydrogenNeeded", "getOxygen", "getOxygenNeeded"};
    /**
     * This separator's water slot.
     */
    public FluidTank fluidTank = new FluidTank(24000);
    /**
     * The maximum amount of gas this block can store.
     */
    public int MAX_GAS = 2400;
    /**
     * The amount of oxygen this block is storing.
     */
    public GasTank leftTank = new GasTank(MAX_GAS);
    /**
     * The amount of hydrogen this block is storing.
     */
    public GasTank rightTank = new GasTank(MAX_GAS);
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

    public TileEntityElectrolyticSeparator() {
        super(MekanismBlock.ELECTROLYTIC_SEPARATOR, 4);
        BASE_ENERGY_PER_TICK = super.getBaseUsage();
    }

    @Override
    public double getBaseUsage() {
        return BASE_ENERGY_PER_TICK;
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            ChargeUtils.discharge(3, this);
            ItemStack fluidInputStack = inventory.get(0);
            if (!fluidInputStack.isEmpty() && isFluidInputItem(fluidInputStack)) {
                fluidTank.fill(FluidContainerUtils.extractFluid(fluidTank, this, 0), FluidAction.EXECUTE);
            }

            //TODO: look at other places that call drawGas and the likes and see if they need saving
            boolean needsSaving = TileUtils.drawGas(inventory.get(1), leftTank);
            needsSaving |= TileUtils.drawGas(inventory.get(2), rightTank);
            if (needsSaving) {
                MekanismUtils.saveChunk(this);
            }
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
            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                currentRedstoneLevel = newRedstoneLevel;
            }
        }
    }

    private void handleTank(GasTank tank, GasMode mode, Direction side, int dumpAmount) {
        if (tank.getGas() != null) {
            if (mode != GasMode.DUMPING) {
                GasStack toSend = new GasStack(tank.getGas().getGas(), Math.min(tank.getStored(), output));
                tank.draw(GasUtils.emit(toSend, this, EnumSet.of(side)), true);
            } else {
                tank.draw(dumpAmount, true);
            }
            if (mode == GasMode.DUMPING_EXCESS && tank.getNeeded() < output) {
                tank.draw(output - tank.getNeeded(), true);
            }
        }
    }

    @Nonnull
    @Override
    public Recipe<ElectrolysisRecipe> getRecipes() {
        return Recipe.ELECTROLYTIC_SEPARATOR;
    }

    @Nullable
    @Override
    public CachedRecipe<ElectrolysisRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ElectrolysisRecipe getRecipe(int cacheIndex) {
        FluidStack fluid = fluidTank.getFluid();
        return fluid.isEmpty() ? null : getRecipes().findFirst(recipe -> recipe.test(fluid));
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
        return new ElectrolysisCachedRecipe(recipe, InputHelper.getInputHandler(fluidTank), OutputHelper.getOutputHandler(leftTank, rightTank))
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
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == 3) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        } else if (slotID == 0) {
            return !FluidUtil.getFluidContained(itemstack).isPresent();
        } else if (slotID == 1 || slotID == 2) {
            if (itemstack.getItem() instanceof IGasItem) {
                IGasItem gasItem = (IGasItem) itemstack.getItem();
                GasStack gasInItem = gasItem.getGas(itemstack);
                return !gasInItem.isEmpty() && gasInItem.getAmount() == gasItem.getMaxGas(itemstack);
            }
        }
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return isFluidInputItem(itemstack);
        } else if (slotID == 1) {
            if (itemstack.getItem() instanceof IGasItem) {
                GasStack gasStack = ((IGasItem) itemstack.getItem()).getGas(itemstack);
                return gasStack == null || gasStack.getGas().isIn(MekanismTags.HYDROGEN);
            }
            return false;
        } else if (slotID == 2) {
            if (itemstack.getItem() instanceof IGasItem) {
                GasStack gasStack = ((IGasItem) itemstack.getItem()).getGas(itemstack);
                return gasStack == null || gasStack.getGas().isIn(MekanismTags.OXYGEN);
            }
        } else if (slotID == 3) {
            return ChargeUtils.canBeDischarged(itemstack);
        }
        return true;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        if (side == getRightSide()) {
            return new int[]{3};
        } else if (side == getDirection() || side == getOppositeDirection()) {
            return new int[]{1, 2};
        }
        return InventoryUtils.EMPTY;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!world.isRemote) {
            byte type = dataStream.readByte();
            if (type == 0) {
                dumpLeft = GasMode.values()[dumpLeft.ordinal() == GasMode.values().length - 1 ? 0 : dumpLeft.ordinal() + 1];
            } else if (type == 1) {
                dumpRight = GasMode.values()[dumpRight.ordinal() == GasMode.values().length - 1 ? 0 : dumpRight.ordinal() + 1];
            }
            return;
        }

        super.handlePacketData(dataStream);

        if (world.isRemote) {
            TileUtils.readTankData(dataStream, fluidTank);
            TileUtils.readTankData(dataStream, leftTank);
            TileUtils.readTankData(dataStream, rightTank);
            dumpLeft = GasMode.values()[dataStream.readInt()];
            dumpRight = GasMode.values()[dataStream.readInt()];
            clientEnergyUsed = dataStream.readDouble();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        TileUtils.addTankData(data, fluidTank);
        TileUtils.addTankData(data, leftTank);
        TileUtils.addTankData(data, rightTank);
        data.add(dumpLeft.ordinal());
        data.add(dumpRight.ordinal());
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
        dumpLeft = GasMode.values()[nbtTags.getInt("dumpLeft")];
        dumpRight = GasMode.values()[nbtTags.getInt("dumpRight")];
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (!fluidTank.getFluid().isEmpty()) {
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
                return new Object[]{fluidTank.getFluid().getAmount()};
            case 5:
                return new Object[]{fluidTank.getCapacity() - fluidTank.getFluid().getAmount()};
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
        if (!fluidTank.getFluid().isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "fluidTank", fluidTank.getFluid().writeToNBT(new CompoundNBT()));
        }
        if (leftTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "leftTank", leftTank.getGas().write(new CompoundNBT()));
        }
        if (rightTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "rightTank", rightTank.getGas().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "fluidTank")));
        leftTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "leftTank")));
        rightTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "rightTank")));
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        FluidStack currentFluid = fluidTank.getFluid();
        if (currentFluid.isEmpty()) {
            //If we don't have a fluid currently stored, then check if the fluid wanting to be input is valid for this machine
            return getRecipes().contains(recipe -> recipe.getInput().testType(fluid));
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
    public int receiveGas(Direction side, @Nonnull GasStack stack, boolean doTransfer) {
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, boolean doTransfer) {
        if (side == getLeftSide()) {
            return leftTank.draw(amount, doTransfer);
        } else if (side == getRightSide()) {
            return rightTank.draw(amount, doTransfer);
        }
        return null;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        if (side == getLeftSide()) {
            return leftTank.getGas() != null && leftTank.getGas().getGas() == type;
        } else if (side == getRightSide()) {
            return rightTank.getGas() != null && rightTank.getGas().getGas() == type;
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
    public List<ITextComponent> getInfo(Upgrade upgrade) {
        return upgrade == Upgrade.SPEED ? upgrade.getExpScaledInfo(this) : upgrade.getMultScaledInfo(this);
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{fluidTank, leftTank, rightTank};
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.ENERGY) {
            setMaxEnergy(MekanismUtils.getMaxEnergy(this, getBaseStorage()));
            setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage()));
            setEnergy(Math.min(getMaxEnergy(), getEnergy()));
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    public boolean isFluidInputItem(ItemStack itemStack) {
        return new LazyOptionalHelper<>(FluidUtil.getFluidContained(itemStack)).matches(
              fluidStack -> !fluidStack.isEmpty() && getRecipes().contains(recipe -> recipe.getInput().testType(fluidStack)));
    }
}