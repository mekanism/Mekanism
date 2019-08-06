package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.SideData;
import mekanism.common.Upgrade;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.PressurizedInput;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.recipe.outputs.PressurizedOutput;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityBasicMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityPRC extends TileEntityBasicMachine<PressurizedInput, PressurizedOutput, PressurizedRecipe> implements IFluidHandlerWrapper, IGasHandler,
      ISustainedData, ITankManager {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded",
                                                         "getFluidStored", "getGasStored"};
    public FluidTank inputFluidTank = new FluidTank(10000);
    public GasTank inputGasTank = new GasTank(10000);
    public GasTank outputGasTank = new GasTank(10000);

    public TileEntityPRC() {
        super(MekanismBlock.PRESSURIZED_REACTION_CHAMBER, 3, 100, new ResourceLocation(Mekanism.MODID, "gui/GuiPRC.png"));
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.FLUID, TransmissionType.GAS);

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

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(3));
        ejectorComponent.setOutputData(TransmissionType.GAS, configComponent.getOutputs(TransmissionType.GAS).get(2));
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            PressurizedRecipe recipe = getRecipe();
            ChargeUtils.discharge(1, this);
            if (canOperate(recipe) && MekanismUtils.canFunction(this) &&
                getEnergy() >= MekanismUtils.getEnergyPerTick(this, getBaseUsage() + recipe.extraEnergy)) {
                boolean update = BASE_TICKS_REQUIRED != recipe.ticks;
                BASE_TICKS_REQUIRED = recipe.ticks;
                if (update) {
                    recalculateUpgrades(Upgrade.SPEED);
                }
                setActive(true);
                if ((operatingTicks + 1) < ticksRequired) {
                    operatingTicks++;
                    pullEnergy(null, MekanismUtils.getEnergyPerTick(this, getBaseUsage() + recipe.extraEnergy), false);
                } else if ((operatingTicks + 1) >= ticksRequired && getEnergy() >= MekanismUtils.getEnergyPerTick(this, getBaseUsage() + recipe.extraEnergy)) {
                    operate(recipe);
                    operatingTicks = 0;
                    pullEnergy(null, MekanismUtils.getEnergyPerTick(this, getBaseUsage() + recipe.extraEnergy), false);
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
        return new PressurizedInput(getInventory().get(0), inputFluidTank.getFluid(), inputGasTank.getGas());
    }

    @Override
    public void operate(PressurizedRecipe recipe) {
        recipe.operate(getInventory(), inputFluidTank, inputGasTank, outputGasTank);
        markDirty();
    }

    @Override
    public boolean canOperate(PressurizedRecipe recipe) {
        return recipe != null && recipe.canOperate(getInventory(), inputFluidTank, inputGasTank, outputGasTank);
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == 1) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        }
        return slotID == 2 || slotID == 4;
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
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        inputFluidTank.readFromNBT(nbtTags.getCompound("inputFluidTank"));
        inputGasTank.read(nbtTags.getCompound("inputGasTank"));
        outputGasTank.read(nbtTags.getCompound("outputGasTank"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("inputFluidTank", inputFluidTank.writeToNBT(new CompoundNBT()));
        nbtTags.put("inputGasTank", inputGasTank.write(new CompoundNBT()));
        nbtTags.put("outputGasTank", outputGasTank.write(new CompoundNBT()));
        return nbtTags;
    }

    @Nonnull
    @Override
    public String getName() {
        return LangUtils.localize(getBlockType().getTranslationKey() + ".short.name");
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
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{operatingTicks};
            case 2:
                return new Object[]{getActive()};
            case 3:
                return new Object[]{getDirection()};
            case 4:
                return new Object[]{canOperate(getRecipe())};
            case 5:
                return new Object[]{getMaxEnergy()};
            case 6:
                return new Object[]{getNeededEnergy()};
            case 7:
                return new Object[]{inputFluidTank.getFluidAmount()};
            case 8:
                return new Object[]{inputGasTank.getStored()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, boolean doFill) {
        return inputFluidTank.fill(resource, doFill);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        SideData data = configComponent.getOutput(TransmissionType.FLUID, from, getDirection());
        if (data.hasSlot(0)) {
            return FluidContainerUtils.canFill(inputFluidTank.getFluid(), fluid);
        }
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(Direction from) {
        SideData data = configComponent.getOutput(TransmissionType.FLUID, from, getDirection());
        return data.getFluidTankInfo(this);
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return new FluidTankInfo[]{inputFluidTank.getInfo()};
    }

    @Override
    public int receiveGas(Direction side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack.getGas())) {
            return inputGasTank.receive(stack, doTransfer);
        }
        return 0;
    }

    @Override
    public GasStack drawGas(Direction side, int amount, boolean doTransfer) {
        if (canDrawGas(side, null)) {
            return outputGasTank.draw(amount, doTransfer);
        }
        return null;
    }

    @Override
    public boolean canReceiveGas(Direction side, Gas type) {
        return configComponent.getOutput(TransmissionType.GAS, side, getDirection()).hasSlot(1) && inputGasTank.canReceive(type);
    }

    @Override
    public boolean canDrawGas(Direction side, Gas type) {
        return configComponent.getOutput(TransmissionType.GAS, side, getDirection()).hasSlot(2) && outputGasTank.canDraw(type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{inputGasTank, outputGasTank};
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandlerWrapper(this, side));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        return configComponent.isCapabilityDisabled(capability, side, getDirection()) || super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (inputFluidTank.getFluid() != null) {
            ItemDataUtils.setCompound(itemStack, "inputFluidTank", inputFluidTank.getFluid().writeToNBT(new CompoundNBT()));
        }
        if (inputGasTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "inputGasTank", inputGasTank.getGas().write(new CompoundNBT()));
        }
        if (outputGasTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "outputGasTank", outputGasTank.getGas().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        inputFluidTank.setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "inputFluidTank")));
        inputGasTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "inputGasTank")));
        outputGasTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputGasTank")));
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{inputFluidTank, inputGasTank, outputGasTank};
    }
}