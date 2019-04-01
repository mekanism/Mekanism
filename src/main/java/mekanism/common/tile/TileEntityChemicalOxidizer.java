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
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityChemicalOxidizer extends TileEntityOperationalMachine implements ISustainedData, ITankManager,
      IGasHandler {

    public static final int MAX_GAS = 10000;
    public GasTank gasTank = new GasTank(MAX_GAS);
    public int gasOutput = 256;

    public OxidationRecipe cachedRecipe;

    public TileEntityChemicalOxidizer() {
        super("machine.oxidizer", "ChemicalOxidizer", BlockStateMachine.MachineType.CHEMICAL_OXIDIZER.baseEnergy,
              usage.rotaryCondensentratorUsage, 3, 100);

        inventory = NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            ChargeUtils.discharge(1, this);
            TileUtils.drawGas(inventory.get(2), gasTank);

            OxidationRecipe recipe = getRecipe();

            if (canOperate(recipe) && getEnergy() >= energyPerTick && MekanismUtils.canFunction(this)) {
                setActive(true);
                setEnergy(getEnergy() - energyPerTick);

                if (operatingTicks < ticksRequired) {
                    operatingTicks++;
                } else {
                    operate(recipe);

                    operatingTicks = 0;
                    markDirty();
                }
            } else {
                if (prevEnergy >= getEnergy()) {
                    setActive(false);
                }
            }

            prevEnergy = getEnergy();

            TileUtils.emitGas(this, gasTank, gasOutput, MekanismUtils.getRight(facing));
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return RecipeHandler.getOxidizerRecipe(new ItemStackInput(itemstack)) != null;
        } else if (slotID == 1) {
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
        if (side == MekanismUtils.getLeft(facing)) {
            return new int[]{0};
        } else if (side.getAxis() == Axis.Y) {
            return new int[]{1};
        } else if (side == MekanismUtils.getRight(facing)) {
            return new int[]{2};
        }

        return InventoryUtils.EMPTY;
    }

    public OxidationRecipe getRecipe() {
        ItemStackInput input = getInput();

        if (cachedRecipe == null || !input.testEquality(cachedRecipe.getInput())) {
            cachedRecipe = RecipeHandler.getOxidizerRecipe(getInput());
        }

        return cachedRecipe;
    }

    public ItemStackInput getInput() {
        return new ItemStackInput(inventory.get(0));
    }

    public boolean canOperate(OxidationRecipe recipe) {
        return recipe != null && recipe.canOperate(inventory, gasTank);
    }

    public void operate(OxidationRecipe recipe) {
        recipe.operate(inventory, gasTank);

        markDirty();
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            TileUtils.readTankData(dataStream, gasTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        TileUtils.addTankData(data, gasTank);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        gasTank.read(nbtTags.getCompoundTag("gasTank"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setTag("gasTank", gasTank.write(new NBTTagCompound()));

        return nbtTags;
    }

    @Override
    public boolean canSetFacing(int i) {
        return i != 0 && i != 1;
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
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != MekanismUtils.getRight(facing);
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (gasTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "gasTank", gasTank.getGas().write(new NBTTagCompound()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        gasTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "gasTank")));
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{gasTank};
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return false;
    }


    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        if (canDrawGas(side, null)) {
            return gasTank.draw(amount, doTransfer);
        }
        return null;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return side == MekanismUtils.getRight(facing) && gasTank.canDraw(type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{gasTank};
    }
}
