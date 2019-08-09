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
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismFluids;
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.gas_tank.TileEntityGasTank.GasMode;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityElectrolyticSeparator extends TileEntityMachine implements IFluidHandlerWrapper, IComputerIntegration, ISustainedData, IGasHandler,
      IUpgradeInfoHandler, ITankManager, IComparatorSupport {

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
    public SeparatorRecipe cachedRecipe;
    public double clientEnergyUsed;
    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);
    /**
     * This machine's current RedstoneControl type.
     */
    public RedstoneControl controlType = RedstoneControl.DISABLED;

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
            if (!getInventory().get(0).isEmpty()) {
                if (Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(getInventory().get(0))) {
                    if (FluidContainerUtils.isFluidContainer(getInventory().get(0))) {
                        fluidTank.fill(FluidContainerUtils.extractFluid(fluidTank, this, 0), true);
                    }
                }
            }

            if (!getInventory().get(1).isEmpty() && leftTank.getStored() > 0) {
                leftTank.draw(GasUtils.addGas(getInventory().get(1), leftTank.getGas()), true);
                MekanismUtils.saveChunk(this);
            }
            if (!getInventory().get(2).isEmpty() && rightTank.getStored() > 0) {
                rightTank.draw(GasUtils.addGas(getInventory().get(2), rightTank.getGas()), true);
                MekanismUtils.saveChunk(this);
            }
            SeparatorRecipe recipe = getRecipe();

            if (canOperate(recipe) && getEnergy() >= getEnergyPerTick() && MekanismUtils.canFunction(this)) {
                setActive(true);
                boolean update = getBaseUsage() != recipe.energyUsage;
                BASE_ENERGY_PER_TICK = recipe.energyUsage;
                if (update) {
                    recalculateUpgrades(Upgrade.ENERGY);
                }
                int operations = operate(recipe);
                double prev = getEnergy();
                setEnergy(getEnergy() - getEnergyPerTick() * operations);
                clientEnergyUsed = prev - getEnergy();
            } else if (prevEnergy >= getEnergy()) {
                setActive(false);
            }
            int dumpAmount = 8 * (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
            handleTank(leftTank, dumpLeft, getLeftSide(), dumpAmount);
            handleTank(rightTank, dumpRight, getRightSide(), dumpAmount);
            prevEnergy = getEnergy();

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

    public int getUpgradedUsage(SeparatorRecipe recipe) {
        int possibleProcess;
        if (leftTank.getGasType() == recipe.recipeOutput.leftGas.getGas()) {
            possibleProcess = leftTank.getNeeded() / recipe.recipeOutput.leftGas.amount;
            possibleProcess = Math.min(rightTank.getNeeded() / recipe.recipeOutput.rightGas.amount, possibleProcess);
        } else {
            possibleProcess = leftTank.getNeeded() / recipe.recipeOutput.rightGas.amount;
            possibleProcess = Math.min(rightTank.getNeeded() / recipe.recipeOutput.leftGas.amount, possibleProcess);
        }
        possibleProcess = Math.min((int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), possibleProcess);
        possibleProcess = Math.min((int) (getEnergy() / getEnergyPerTick()), possibleProcess);
        return Math.min(fluidTank.getFluidAmount() / recipe.recipeInput.ingredient.amount, possibleProcess);
    }

    public SeparatorRecipe getRecipe() {
        FluidInput input = getInput();
        if (cachedRecipe == null || !input.testEquality(cachedRecipe.getInput())) {
            cachedRecipe = RecipeHandler.getElectrolyticSeparatorRecipe(getInput());
        }
        return cachedRecipe;
    }

    public FluidInput getInput() {
        return new FluidInput(fluidTank.getFluid());
    }

    public boolean canOperate(SeparatorRecipe recipe) {
        return recipe != null && recipe.canOperate(fluidTank, leftTank, rightTank);
    }

    public int operate(SeparatorRecipe recipe) {
        int operations = getUpgradedUsage(recipe);
        recipe.operate(fluidTank, leftTank, rightTank, operations);
        return operations;
    }

    public boolean canFill(ChemicalPairOutput gases) {
        return leftTank.canReceive(gases.leftGas.getGas()) && leftTank.getNeeded() >= gases.leftGas.amount
               && rightTank.canReceive(gases.rightGas.getGas()) && rightTank.getNeeded() >= gases.rightGas.amount;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == 3) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        } else if (slotID == 0) {
            return FluidUtil.getFluidContained(itemstack) == null;
        } else if (slotID == 1 || slotID == 2) {
            return itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem()).getGas(itemstack) != null
                   && ((IGasItem) itemstack.getItem()).getGas(itemstack).amount == ((IGasItem) itemstack.getItem()).getMaxGas(itemstack);
        }
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(itemstack);
        } else if (slotID == 1) {
            return itemstack.getItem() instanceof IGasItem &&
                   (((IGasItem) itemstack.getItem()).getGas(itemstack) == null || ((IGasItem) itemstack.getItem()).getGas(itemstack).getGas() == MekanismFluids.Hydrogen);
        } else if (slotID == 2) {
            return itemstack.getItem() instanceof IGasItem &&
                   (((IGasItem) itemstack.getItem()).getGas(itemstack) == null || ((IGasItem) itemstack.getItem()).getGas(itemstack).getGas() == MekanismFluids.Oxygen);
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
    public boolean canSetFacing(@Nonnull Direction facing) {
        return facing != Direction.DOWN && facing != Direction.UP;
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
        if (fluidTank.getFluid() != null) {
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
                return new Object[]{fluidTank.getFluid() != null ? fluidTank.getFluid().amount : 0};
            case 5:
                return new Object[]{fluidTank.getFluid() != null ? (fluidTank.getCapacity() - fluidTank.getFluid().amount) : 0};
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
        if (fluidTank.getFluid() != null) {
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
        return Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(fluid.getFluid());
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, boolean doFill) {
        return fluidTank.fill(resource, doFill);
    }

    @Override
    public FluidTankInfo[] getTankInfo(Direction from) {
        return new FluidTankInfo[]{fluidTank.getInfo()};
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public int receiveGas(Direction side, GasStack stack, boolean doTransfer) {
        return 0;
    }

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
    public boolean canReceiveGas(Direction side, Gas type) {
        return false;
    }

    @Override
    public boolean canDrawGas(Direction side, Gas type) {
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
    public List<String> getInfo(Upgrade upgrade) {
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
}