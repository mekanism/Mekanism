package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.PressurizedReactionCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.SideData;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.IInventorySlotHolder;
import mekanism.common.inventory.InventorySlotHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityBasicMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.tuple.Pair;

public class TileEntityPressurizedReactionChamber extends TileEntityBasicMachine<PressurizedReactionRecipe> implements IFluidHandlerWrapper, IGasHandler, ISustainedData,
      ITankManager {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded",
                                                         "getFluidStored", "getGasStored"};
    public FluidTank inputFluidTank = new FluidTank(10000);
    public GasTank inputGasTank = new GasTank(10000);
    public GasTank outputGasTank = new GasTank(10000);

    private final IOutputHandler<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>> outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;

    public TileEntityPressurizedReactionChamber() {
        super(MekanismBlock.PRESSURIZED_REACTION_CHAMBER, 100, new ResourceLocation(Mekanism.MODID, "gui/gui_pressurized_reaction_chamber.png"));
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.FLUID, TransmissionType.GAS);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GRAY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{0}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{1}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{2}));
        configComponent.setConfig(TransmissionType.ITEM, new byte[]{2, 1, 0, 0, 0, 3});

        configComponent.addOutput(TransmissionType.FLUID, new SideData("None", EnumColor.GRAY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.FLUID, new SideData("Fluid", EnumColor.YELLOW, new int[]{0}));
        configComponent.setConfig(TransmissionType.FLUID, new byte[]{0, 0, 0, 1, 0, 0});
        configComponent.setCanEject(TransmissionType.FLUID, false);

        configComponent.addOutput(TransmissionType.GAS, new SideData("None", EnumColor.GRAY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.GAS, new SideData("Gas", EnumColor.DARK_RED, new int[]{1}));
        configComponent.addOutput(TransmissionType.GAS, new SideData("Output", EnumColor.DARK_BLUE, new int[]{2}));
        configComponent.setConfig(TransmissionType.GAS, new byte[]{0, 0, 0, 0, 1, 2});

        configComponent.setInputConfig(TransmissionType.ENERGY);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(3));
        ejectorComponent.setOutputData(TransmissionType.GAS, configComponent.getOutputs(TransmissionType.GAS).get(2));

        itemInputHandler = InputHelper.getInputHandler(this, 0);
        fluidInputHandler = InputHelper.getInputHandler(inputFluidTank, 0);
        gasInputHandler = InputHelper.getInputHandler(inputGasTank);
        outputHandler = OutputHelper.getOutputHandler(outputGasTank, this, 2);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        //return configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).availableSlots;
        //TODO: Some way to tie slots to a config component? So that we can filter by the config component?
        // This can probably be done by letting the configurations know the relative side information?
        InventorySlotHelper.Builder builder = InventorySlotHelper.Builder.forSide(this::getDirection);
        builder.addSlot(InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getInputSolid().testType(item)), 54, 35));
        builder.addSlot(EnergyInventorySlot.discharge(141, 19));
        builder.addSlot(OutputInventorySlot.at(116, 35));
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            ChargeUtils.discharge(1, this);
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<PressurizedReactionRecipe> getRecipeType() {
        return MekanismRecipeType.REACTION;
    }

    @Nullable
    @Override
    public CachedRecipe<PressurizedReactionRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public PressurizedReactionRecipe getRecipe(int cacheIndex) {
        ItemStack stack = itemInputHandler.getInput();
        if (stack.isEmpty()) {
            return null;
        }
        FluidStack fluid = fluidInputHandler.getInput();
        if (fluid.isEmpty()) {
            return null;
        }
        GasStack gas = gasInputHandler.getInput();
        if (gas.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(stack, fluid, gas));
    }

    @Nullable
    @Override
    public CachedRecipe<PressurizedReactionRecipe> createNewCachedRecipe(@Nonnull PressurizedReactionRecipe recipe, int cacheIndex) {
        //TODO: Is this fine, or do we need it somewhere that will get called in more places than ONLY when the cache is being made
        boolean update = BASE_TICKS_REQUIRED != recipe.getDuration();
        BASE_TICKS_REQUIRED = recipe.getDuration();
        if (update) {
            recalculateUpgrades(Upgrade.SPEED);
        }
        return new PressurizedReactionCachedRecipe(recipe, itemInputHandler, fluidInputHandler, gasInputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(() -> MekanismUtils.getEnergyPerTick(this, getBaseUsage() + recipe.getEnergyRequired()), this::getEnergy,
                    energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty);
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
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
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
                return new Object[]{getOperatingTicks()};
            case 2:
                return new Object[]{getActive()};
            case 3:
                return new Object[]{getDirection()};
            case 4:
                //TODO: Decide if we should try to get the cached recipe if it is null
                return new Object[]{cachedRecipe != null && cachedRecipe.canFunction()};
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
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return inputFluidTank.fill(resource, fluidAction);
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
    public IFluidTank[] getTankInfo(Direction from) {
        SideData data = configComponent.getOutput(TransmissionType.FLUID, from, getDirection());
        return data.getFluidTankInfo(this);
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return new IFluidTank[]{inputFluidTank};
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        if (canReceiveGas(side, stack.getType())) {
            return inputGasTank.fill(stack, action);
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        if (canDrawGas(side, MekanismAPI.EMPTY_GAS)) {
            return outputGasTank.drain(amount, action);
        }
        return GasStack.EMPTY;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return configComponent.getOutput(TransmissionType.GAS, side, getDirection()).hasSlot(1) && inputGasTank.canReceive(type);
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return configComponent.getOutput(TransmissionType.GAS, side, getDirection()).hasSlot(2) && outputGasTank.canDraw(type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{inputGasTank, outputGasTank};
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
        return configComponent.isCapabilityDisabled(capability, side, getDirection()) || super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!inputFluidTank.getFluid().isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "inputFluidTank", inputFluidTank.getFluid().writeToNBT(new CompoundNBT()));
        }
        if (!inputGasTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "inputGasTank", inputGasTank.getStack().write(new CompoundNBT()));
        }
        if (!outputGasTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "outputGasTank", outputGasTank.getStack().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        inputFluidTank.setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "inputFluidTank")));
        inputGasTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "inputGasTank")));
        outputGasTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputGasTank")));
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{inputFluidTank, inputGasTank, outputGasTank};
    }
}