package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.Upgrade;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.api.TileNetworkList;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.PressurizedInput;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.recipe.outputs.PressurizedOutput;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityBasicMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityPRC extends
      TileEntityBasicMachine<PressurizedInput, PressurizedOutput, PressurizedRecipe> implements IFluidHandlerWrapper,
      IGasHandler, ITubeConnection, ISustainedData, ITankManager {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "isActive", "facing", "canOperate",
          "getMaxEnergy", "getEnergyNeeded", "getFluidStored", "getGasStored"};
    public FluidTank inputFluidTank = new FluidTank(10000);
    public GasTank inputGasTank = new GasTank(10000);
    public GasTank outputGasTank = new GasTank(10000);

    public TileEntityPRC() {
        super("prc", BlockStateMachine.MachineType.PRESSURIZED_REACTION_CHAMBER.blockName,
              BlockStateMachine.MachineType.PRESSURIZED_REACTION_CHAMBER.baseEnergy, usage.pressurizedReactionBaseUsage,
              3, 100, new ResourceLocation("mekanism", "gui/GuiPRC.png"));

        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY,
              TransmissionType.FLUID, TransmissionType.GAS);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{0}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{1}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{2}));
        configComponent.setConfig(TransmissionType.ITEM, new byte[]{2, 1, 0, 0, 0, 3});

        configComponent.addOutput(TransmissionType.FLUID, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.FLUID, new SideData("Fluid", EnumColor.YELLOW, new int[]{0}));
        configComponent.setConfig(TransmissionType.FLUID, new byte[]{0, 0, 0, 1, 0, 0});
        configComponent.setCanEject(TransmissionType.FLUID, false);

        configComponent.addOutput(TransmissionType.GAS, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.GAS, new SideData("Gas", EnumColor.DARK_RED, new int[]{1}));
        configComponent.addOutput(TransmissionType.GAS, new SideData("Output", EnumColor.DARK_BLUE, new int[]{2}));
        configComponent.setConfig(TransmissionType.GAS, new byte[]{0, 0, 0, 0, 1, 2});

        configComponent.setInputConfig(TransmissionType.ENERGY);

        inventory = NonNullList.withSize(4, ItemStack.EMPTY);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(3));
        ejectorComponent.setOutputData(TransmissionType.GAS, configComponent.getOutputs(TransmissionType.GAS).get(2));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            PressurizedRecipe recipe = getRecipe();

            ChargeUtils.discharge(1, this);

            if (canOperate(recipe) && MekanismUtils.canFunction(this) && getEnergy() >= MekanismUtils
                  .getEnergyPerTick(this, BASE_ENERGY_PER_TICK + recipe.extraEnergy)) {
                boolean update = BASE_TICKS_REQUIRED != recipe.ticks;

                BASE_TICKS_REQUIRED = recipe.ticks;

                if (update) {
                    recalculateUpgradables(Upgrade.SPEED);
                }

                setActive(true);

                if ((operatingTicks + 1) < ticksRequired) {
                    operatingTicks++;
                    electricityStored -= MekanismUtils
                          .getEnergyPerTick(this, BASE_ENERGY_PER_TICK + recipe.extraEnergy);
                } else if ((operatingTicks + 1) >= ticksRequired && getEnergy() >= MekanismUtils
                      .getEnergyPerTick(this, BASE_ENERGY_PER_TICK + recipe.extraEnergy)) {
                    operate(recipe);

                    operatingTicks = 0;
                    electricityStored -= MekanismUtils
                          .getEnergyPerTick(this, BASE_ENERGY_PER_TICK + recipe.extraEnergy);
                }
            } else {
                BASE_TICKS_REQUIRED = 100;

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

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return RecipeHandler.isInPressurizedRecipe(itemstack);
        } else if (slotID == 1) {
            return ChargeUtils.canBeDischarged(itemstack);
        } else if (slotID == 3) {
            return itemstack.getItem() instanceof ItemUpgrade;
        }

        return false;
    }

    @Override
    public PressurizedRecipe getRecipe() {
        PressurizedInput input = getInput();

        if (cachedRecipe == null || !input.testEquality(cachedRecipe.getInput())) {
            cachedRecipe = RecipeHandler.getPRCRecipe(input);
        }

        return cachedRecipe;
    }

    @Override
    public PressurizedInput getInput() {
        return new PressurizedInput(inventory.get(0), inputFluidTank.getFluid(), inputGasTank.getGas());
    }

    @Override
    public void operate(PressurizedRecipe recipe) {
        recipe.operate(inventory, inputFluidTank, inputGasTank, outputGasTank);

        markDirty();
        ejectorComponent.outputItems();
    }

    @Override
    public boolean canOperate(PressurizedRecipe recipe) {
        return recipe != null && recipe.canOperate(inventory, inputFluidTank, inputGasTank, outputGasTank);
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 1) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        } else {
            return slotID == 2 || slotID == 4;
        }

    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        TileUtils.addTankData(data, inputFluidTank);
        TileUtils.addTankData(data, inputGasTank);
        TileUtils.addTankData(data, outputGasTank);
        return data;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            TileUtils.readTankData(dataStream, inputFluidTank);
            TileUtils.readTankData(dataStream, inputGasTank);
            TileUtils.readTankData(dataStream, outputGasTank);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        inputFluidTank.readFromNBT(nbtTags.getCompoundTag("inputFluidTank"));
        inputGasTank.read(nbtTags.getCompoundTag("inputGasTank"));
        outputGasTank.read(nbtTags.getCompoundTag("outputGasTank"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setTag("inputFluidTank", inputFluidTank.writeToNBT(new NBTTagCompound()));
        nbtTags.setTag("inputGasTank", inputGasTank.write(new NBTTagCompound()));
        nbtTags.setTag("outputGasTank", outputGasTank.write(new NBTTagCompound()));

        return nbtTags;
    }

    @Nonnull
    @Override
    public String getName() {
        return LangUtils.localize(getBlockType().getTranslationKey() + "." + fullName + ".short.name");
    }

    @Override
    public Map<PressurizedInput, PressurizedRecipe> getRecipes() {
        return null;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws Exception {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{operatingTicks};
            case 2:
                return new Object[]{isActive};
            case 3:
                return new Object[]{facing};
            case 4:
                return new Object[]{canOperate(getRecipe())};
            case 5:
                return new Object[]{getMaxEnergy()};
            case 6:
                return new Object[]{getMaxEnergy() - getEnergy()};
            case 7:
                return new Object[]{inputFluidTank.getFluidAmount()};
            case 8:
                return new Object[]{inputGasTank.getStored()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public int fill(EnumFacing from, @Nullable FluidStack resource, boolean doFill) {
        if (canFill(from, resource)) {
            return inputFluidTank.fill(resource, doFill);
        }

        return 0;
    }

    @Override
    public FluidStack drain(EnumFacing from, @Nullable FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(EnumFacing from, @Nullable FluidStack fluid) {
        SideData data = configComponent.getOutput(TransmissionType.FLUID, from, facing);

        if (data.hasSlot(0)) {
            return inputFluidTank.getFluid() == null || inputFluidTank.getFluid().isFluidEqual(fluid);
        }

        return false;
    }

    @Override
    public boolean canDrain(EnumFacing from, @Nullable FluidStack fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        SideData data = configComponent.getOutput(TransmissionType.FLUID, from, facing);

        return data.getFluidTankInfo(this);
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return new FluidTankInfo[]{inputFluidTank.getInfo()};
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack.getGas())) {
            return inputGasTank.receive(stack, doTransfer);
        }

        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        if (canDrawGas(side, null)) {
            return outputGasTank.draw(amount, doTransfer);
        }

        return null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return configComponent.getOutput(TransmissionType.GAS, side, facing).hasSlot(1) && inputGasTank
              .canReceive(type);
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return configComponent.getOutput(TransmissionType.GAS, side, facing).hasSlot(2) && outputGasTank.canDraw(type);
    }

    @Override
    public boolean canTubeConnect(EnumFacing side) {
        return configComponent.getOutput(TransmissionType.GAS, side, facing).hasSlot(1, 2);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{inputGasTank, outputGasTank};
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.GAS_HANDLER_CAPABILITY
              || capability == Capabilities.TUBE_CONNECTION_CAPABILITY
              || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY
              || capability == Capabilities.TUBE_CONNECTION_CAPABILITY) {
            return (T) this;
        }

        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) new FluidHandlerWrapper(this, side);
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (inputFluidTank.getFluid() != null) {
            ItemDataUtils
                  .setCompound(itemStack, "inputFluidTank", inputFluidTank.getFluid().writeToNBT(new NBTTagCompound()));
        }

        if (inputGasTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "inputGasTank", inputGasTank.getGas().write(new NBTTagCompound()));
        }

        if (outputGasTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "outputGasTank", outputGasTank.getGas().write(new NBTTagCompound()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        inputFluidTank
              .setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "inputFluidTank")));
        inputGasTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "inputGasTank")));
        outputGasTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputGasTank")));
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{inputFluidTank, inputGasTank, outputGasTank};
    }
}
