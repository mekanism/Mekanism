package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.common.MekanismFluids;
import mekanism.common.Upgrade;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.prefab.TileEntityMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityChemicalDissolutionChamber extends TileEntityMachine implements IGasHandler, ISustainedData,
      ITankManager {

    public static final int MAX_GAS = 10000;
    public static final int BASE_INJECT_USAGE = 1;
    public static final int BASE_TICKS_REQUIRED = 100;
    public final double BASE_ENERGY_USAGE = usage.chemicalDissolutionChamberUsage;
    public GasTank injectTank = new GasTank(MAX_GAS);
    public GasTank outputTank = new GasTank(MAX_GAS);
    public double injectUsage = BASE_INJECT_USAGE;
    public int injectUsageThisTick;
    public int gasOutput = 256;
    public int operatingTicks = 0;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    public DissolutionRecipe cachedRecipe;

    public TileEntityChemicalDissolutionChamber() {
        super("machine.dissolution", "ChemicalDissolutionChamber",
              BlockStateMachine.MachineType.CHEMICAL_DISSOLUTION_CHAMBER.baseEnergy,
              usage.chemicalDissolutionChamberUsage, 4);

        inventory = NonNullList.withSize(5, ItemStack.EMPTY);
        upgradeComponent.setSupported(Upgrade.GAS);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            ChargeUtils.discharge(3, this);
            ItemStack itemStack = inventory.get(0);

            if (!itemStack.isEmpty() && injectTank.getNeeded() > 0 && itemStack.getItem() instanceof IGasItem) {
                //TODO: Maybe make this use GasUtils.getItemGas. This only currently accepts IGasItems here though
                IGasItem item = (IGasItem) itemStack.getItem();
                GasStack gasStack = item.getGas(itemStack);
                //Check to make sure it can provide the gas it contains
                if (gasStack != null && item.canProvideGas(itemStack, gasStack.getGas())) {
                    Gas gas = gasStack.getGas();
                    if (gas != null && injectTank.canReceive(gas) && isValidGas(gas)) {
                        injectTank.receive(GasUtils.removeGas(itemStack, gas, injectTank.getNeeded()), true);
                    }
                }
            }

            TileUtils.drawGas(inventory.get(2), outputTank);

            boolean changed = false;

            DissolutionRecipe recipe = getRecipe();

            injectUsageThisTick = Math.max(BASE_INJECT_USAGE, StatUtils.inversePoisson(injectUsage));

            if (canOperate(recipe) && getEnergy() >= energyPerTick && injectTank.getStored() >= injectUsageThisTick
                  && MekanismUtils.canFunction(this)) {
                setActive(true);
                setEnergy(getEnergy() - energyPerTick);
                minorOperate();

                if ((operatingTicks + 1) < ticksRequired) {
                    operatingTicks++;
                } else {
                    operate(recipe);
                    operatingTicks = 0;
                }
            } else {
                if (prevEnergy >= getEnergy()) {
                    changed = true;
                    setActive(false);
                }
            }

            if (changed && !canOperate(recipe)) {
                operatingTicks = 0;
            }

            prevEnergy = getEnergy();

            TileUtils.emitGas(this, outputTank, gasOutput, MekanismUtils.getRight(facing));
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 1) {
            return RecipeHandler.getDissolutionRecipe(new ItemStackInput(itemstack)) != null;
        } else if (slotID == 3) {
            return ChargeUtils.canBeDischarged(itemstack);
        }

        return false;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 2) {
            return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem())
                  .canProvideGas(itemstack, null);
        }

        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == MekanismUtils.getLeft(facing) || side == EnumFacing.UP) {
            return new int[]{1};
        } else if (side == EnumFacing.DOWN) {
            return new int[]{0};
        } else if (side == MekanismUtils.getRight(facing)) {
            return new int[]{2};
        }

        return InventoryUtils.EMPTY;
    }

    public double getScaledProgress() {
        return ((double) operatingTicks) / ((double) ticksRequired);
    }

    public DissolutionRecipe getRecipe() {
        ItemStackInput input = getInput();

        if (cachedRecipe == null || !input.testEquality(cachedRecipe.getInput())) {
            cachedRecipe = RecipeHandler.getDissolutionRecipe(getInput());
        }

        return cachedRecipe;
    }

    public ItemStackInput getInput() {
        return new ItemStackInput(inventory.get(1));
    }

    public boolean canOperate(DissolutionRecipe recipe) {
        return recipe != null && recipe.canOperate(inventory, outputTank);
    }

    public void operate(DissolutionRecipe recipe) {
        recipe.operate(inventory, outputTank);

        markDirty();
    }

    public void minorOperate() {
        injectTank.draw(injectUsageThisTick, true);
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            operatingTicks = dataStream.readInt();
            TileUtils.readTankData(dataStream, injectTank);
            TileUtils.readTankData(dataStream, outputTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(operatingTicks);
        TileUtils.addTankData(data, injectTank);
        TileUtils.addTankData(data, outputTank);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        operatingTicks = nbtTags.getInteger("operatingTicks");
        injectTank.read(nbtTags.getCompoundTag("injectTank"));
        outputTank.read(nbtTags.getCompoundTag("gasTank"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setInteger("operatingTicks", operatingTicks);
        nbtTags.setTag("injectTank", injectTank.write(new NBTTagCompound()));
        nbtTags.setTag("gasTank", outputTank.write(new NBTTagCompound()));

        return nbtTags;
    }

    @Override
    public boolean canSetFacing(int i) {
        return i != 0 && i != 1;
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack.getGas())) {
            return injectTank.receive(stack, doTransfer);
        }

        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return side == MekanismUtils.getLeft(facing) && injectTank.canReceive(type) && isValidGas(type);
    }

    private boolean isValidGas(Gas gas) {
        //TODO: Replace with commented version once this becomes an AdvancedMachine
        return gas == MekanismFluids.SulfuricAcid;//Recipe.CHEMICAL_DISSOLUTION_CHAMBER.containsRecipe(gas);
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return false;
    }

    @Override
    @Nonnull
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{injectTank, outputTank};
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return (T) this;
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != MekanismUtils.getLeft(facing) && side != MekanismUtils.getRight(facing);
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (injectTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "injectTank", injectTank.getGas().write(new NBTTagCompound()));
        }

        if (outputTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "outputTank", outputTank.getGas().write(new NBTTagCompound()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        injectTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "injectTank")));
        outputTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputTank")));
    }

    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);

        switch (upgrade) {
            case ENERGY:
                energyPerTick = MekanismUtils
                      .getEnergyPerTick(this, BASE_ENERGY_PER_TICK); // incorporate speed upgrades
                break;
            case GAS:
                injectUsage = MekanismUtils.getSecondaryEnergyPerTickMean(this, BASE_INJECT_USAGE);
                break;
            case SPEED:
                ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
                energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_USAGE);
                injectUsage = MekanismUtils.getSecondaryEnergyPerTickMean(this, BASE_INJECT_USAGE);
                break;
            default:
                break;
        }
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{injectTank, outputTank};
    }
}
