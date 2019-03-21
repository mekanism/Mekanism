package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.api.TileNetworkList;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityChemicalCrystallizer extends TileEntityOperationalMachine implements IGasHandler,
      ITubeConnection, ISideConfiguration, ISustainedData, ITankManager, IConfigCardAccess {

    public static final int MAX_GAS = 10000;

    public GasTank inputTank = new GasTank(MAX_GAS);

    public CrystallizerRecipe cachedRecipe;

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    public TileEntityChemicalCrystallizer() {
        super("machine.crystallizer", "ChemicalCrystallizer",
              BlockStateMachine.MachineType.CHEMICAL_CRYSTALLIZER.baseEnergy, usage.chemicalCrystallizerUsage, 3, 200);

        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY,
              TransmissionType.GAS);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Gas", EnumColor.PURPLE, new int[]{0}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{1}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{2}));
        configComponent.setConfig(TransmissionType.ITEM, new byte[]{0, 3, 0, 0, 1, 2});

        configComponent.addOutput(TransmissionType.GAS, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.GAS, new SideData("Gas", EnumColor.YELLOW, new int[]{0}));
        configComponent.setConfig(TransmissionType.GAS, new byte[]{-1, -1, -1, -1, 1, -1});
        configComponent.setCanEject(TransmissionType.GAS, false);

        configComponent.setInputConfig(TransmissionType.ENERGY);

        inventory = NonNullList.withSize(4, ItemStack.EMPTY);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            ChargeUtils.discharge(2, this);
            TileUtils.receiveGas(inventory.get(0), inputTank);
            CrystallizerRecipe recipe = getRecipe();

            if (canOperate(recipe) && MekanismUtils.canFunction(this) && getEnergy() >= energyPerTick) {
                setActive(true);

                setEnergy(getEnergy() - energyPerTick);

                if ((operatingTicks + 1) < ticksRequired) {
                    operatingTicks++;
                } else {
                    operate(recipe);
                    operatingTicks = 0;
                }
            } else {
                if (prevEnergy >= getEnergy()) {
                    setActive(false);
                }
            }

            if (!canOperate(recipe)) {
                operatingTicks = 0;
            }

            prevEnergy = getEnergy();
        }
    }

    public GasInput getInput() {
        return new GasInput(inputTank.getGas());
    }

    public CrystallizerRecipe getRecipe() {
        GasInput input = getInput();

        if (cachedRecipe == null || !input.testEquality(cachedRecipe.getInput())) {
            cachedRecipe = RecipeHandler.getChemicalCrystallizerRecipe(getInput());
        }

        return cachedRecipe;
    }

    public boolean canOperate(CrystallizerRecipe recipe) {
        return recipe != null && recipe.canOperate(inputTank, inventory);
    }

    public void operate(CrystallizerRecipe recipe) {
        recipe.operate(inputTank, inventory);

        markDirty();
        ejectorComponent.outputItems();
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            TileUtils.readTankData(dataStream, inputTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        TileUtils.addTankData(data, inputTank);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        inputTank.read(nbtTags.getCompoundTag("rightTank"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setTag("rightTank", inputTank.write(new NBTTagCompound()));

        nbtTags.setBoolean("sideDataStored", true);

        return nbtTags;
    }

    @Override
    public boolean canSetFacing(int i) {
        return i != 0 && i != 1;
    }

    @Override
    public boolean canTubeConnect(EnumFacing side) {
        return configComponent.getOutput(TransmissionType.GAS, side, facing).hasSlot(0);
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return configComponent.getOutput(TransmissionType.GAS, side, facing).hasSlot(0) && inputTank.canReceive(type) &&
              RecipeHandler.Recipe.CHEMICAL_CRYSTALLIZER.containsRecipe(type);
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack.getGas())) {
            return inputTank.receive(stack, doTransfer);
        }

        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return false;
    }

    @Override
    @Nonnull
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{inputTank};
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.GAS_HANDLER_CAPABILITY
              || capability == Capabilities.TUBE_CONNECTION_CAPABILITY
              || capability == Capabilities.CONFIG_CARD_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.TUBE_CONNECTION_CAPABILITY
              || capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return (T) this;
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem
                  && ((IGasItem) itemstack.getItem()).getGas(itemstack) != null &&
                  RecipeHandler.Recipe.CHEMICAL_CRYSTALLIZER
                        .containsRecipe(((IGasItem) itemstack.getItem()).getGas(itemstack).getGas());
        } else if (slotID == 2) {
            return ChargeUtils.canBeDischarged(itemstack);
        }

        return false;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 0) {
            return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem
                  && ((IGasItem) itemstack.getItem()).getGas(itemstack) == null;
        } else if (slotID == 1) {
            return true;
        } else if (slotID == 2) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        }

        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return configComponent.getOutput(TransmissionType.ITEM, side, facing).availableSlots;
    }

    @Override
    public TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public EnumFacing getOrientation() {
        return facing;
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (inputTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "inputTank", inputTank.getGas().write(new NBTTagCompound()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        inputTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "inputTank")));
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{inputTank};
    }
}
