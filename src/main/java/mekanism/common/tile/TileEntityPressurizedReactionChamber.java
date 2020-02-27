package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.BasicGasTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
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
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IChemicalTankHolder;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ITankManager;
import mekanism.common.base.handler.ChemicalTankHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
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
    private static final int MAX_GAS = 10_000;
    public FluidTank inputFluidTank;
    public BasicGasTank inputGasTank;
    public BasicGasTank outputGasTank;

    private final IOutputHandler<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>> outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;

    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityPressurizedReactionChamber() {
        super(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, 100);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.FLUID, TransmissionType.GAS);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, false, inputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(false, true, outputSlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.TOP, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);
            itemConfig.setDataType(RelativeSide.BOTTOM, DataType.ENERGY);
        }

        ConfigInfo fluidConfig = configComponent.getConfig(TransmissionType.FLUID);
        if (fluidConfig != null) {
            fluidConfig.addSlotInfo(DataType.INPUT, new FluidSlotInfo(true, false, inputFluidTank));
            //Set default config directions
            fluidConfig.setDataType(RelativeSide.BACK, DataType.INPUT);

            fluidConfig.setCanEject(false);
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if (gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT, new GasSlotInfo(true, false, inputGasTank));
            gasConfig.addSlotInfo(DataType.OUTPUT, new GasSlotInfo(false, true, outputGasTank));
            //Set default config directions
            gasConfig.setDataType(RelativeSide.LEFT, DataType.INPUT);
            gasConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT, new EnergySlotInfo(true, false));
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

    @Override
    protected void presetVariables() {
        inputFluidTank = new FluidTank(10_000);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(inputGasTank = BasicGasTank.input(MAX_GAS, gas -> containsRecipe(recipe -> recipe.getInputGas().testType(gas)), this));
        builder.addTank(outputGasTank = BasicGasTank.output(MAX_GAS, this));
        return builder.build();
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
              .setOnFinish(this::markDirty)
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        inputFluidTank.readFromNBT(nbtTags.getCompound("inputFluidTank"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("inputFluidTank", inputFluidTank.writeToNBT(new CompoundNBT()));
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
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
        if (!inputFluidTank.isEmpty()) {
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
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put("inputFluidTank", "inputFluidTank");
        remap.put("inputGasTank.stored", "inputGasTank");
        remap.put("outputGasTank.stored", "outputGasTank");
        return remap;
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{inputFluidTank, inputGasTank, outputGasTank};
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFluidStack.create(inputFluidTank));
    }
}