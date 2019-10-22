package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.RelativeSide;
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
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.GasSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.prefab.TileEntityBasicMachine;
import mekanism.common.util.FluidContainerUtils;
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

    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityPressurizedReactionChamber() {
        super(MekanismBlock.PRESSURIZED_REACTION_CHAMBER, 100, new ResourceLocation(Mekanism.MODID, "gui/gui_pressurized_reaction_chamber.png"));
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.FLUID, TransmissionType.GAS);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(inputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(outputSlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(energySlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.TOP, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);
            itemConfig.setDataType(RelativeSide.BOTTOM, DataType.ENERGY);
        }

        ConfigInfo fluidConfig = configComponent.getConfig(TransmissionType.FLUID);
        if (fluidConfig != null) {
            fluidConfig.addSlotInfo(DataType.INPUT, new FluidSlotInfo(inputFluidTank));
            //Set default config directions
            fluidConfig.setDataType(RelativeSide.BACK, DataType.INPUT);

            fluidConfig.setCanEject(false);
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if (gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT, new GasSlotInfo(inputGasTank));
            gasConfig.addSlotInfo(DataType.OUTPUT, new GasSlotInfo(outputGasTank));
            //Set default config directions
            gasConfig.setDataType(RelativeSide.LEFT, DataType.INPUT);
            gasConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT, new EnergySlotInfo());
            energyConfig.fill(DataType.INPUT);
            energyConfig.setCanEject(false);
        }

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, itemConfig);
        ejectorComponent.setOutputData(TransmissionType.GAS, gasConfig);

        itemInputHandler = InputHelper.getInputHandler(inputSlot);
        fluidInputHandler = InputHelper.getInputHandler(inputFluidTank, 0);
        gasInputHandler = InputHelper.getInputHandler(inputGasTank);
        outputHandler = OutputHelper.getOutputHandler(outputGasTank, outputSlot);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getInputSolid().testType(item)), this, 54, 35));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 116, 35));
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 141, 19));
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
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
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.FLUID, from);
        if (slotInfo instanceof FluidSlotInfo) {
            FluidSlotInfo fluidSlotInfo = (FluidSlotInfo) slotInfo;
            return fluidSlotInfo.canInput() && fluidSlotInfo.hasTank(inputFluidTank) && FluidContainerUtils.canFill(inputFluidTank.getFluid(), fluid);
        }
        return false;
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.FLUID, from);
        if (slotInfo instanceof FluidSlotInfo) {
            return ((FluidSlotInfo) slotInfo).getTankInfo();
        }
        return new IFluidTank[0];
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
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.GAS, side);
        if (slotInfo instanceof GasSlotInfo) {
            GasSlotInfo gasSlotInfo = (GasSlotInfo) slotInfo;
            return gasSlotInfo.canInput() && gasSlotInfo.hasTank(inputGasTank) && inputGasTank.canReceive(type);
        }
        return false;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.GAS, side);
        if (slotInfo instanceof GasSlotInfo) {
            GasSlotInfo gasSlotInfo = (GasSlotInfo) slotInfo;
            return gasSlotInfo.canOutput() && gasSlotInfo.hasTank(outputGasTank) && outputGasTank.canDraw(type);
        }
        return false;
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
        return configComponent.isCapabilityDisabled(capability, side) || super.isCapabilityDisabled(capability, side);
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