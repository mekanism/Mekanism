package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.infuse.InfusionTank;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.MetallurgicInfuserCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInfusionStack;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InfusionInventorySlot;
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
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import mekanism.common.upgrade.MetallurgicInfuserUpgradeData;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityMetallurgicInfuser extends TileEntityOperationalMachine<MetallurgicInfuserRecipe> implements IComputerIntegration, ISideConfiguration,
      IConfigCardAccess, ISustainedData {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded", "getInfuse",
                                                         "getInfuseNeeded"};

    public static final int MAX_INFUSE = 1_000;
    public InfusionTank infusionTank;
    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final IInputHandler<@NonNull InfusionStack> infusionInputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;

    private InfusionInventorySlot infusionSlot;
    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityMetallurgicInfuser() {
        super(MekanismBlocks.METALLURGIC_INFUSER, 200);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, false, inputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(false, true, outputSlot));
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, infusionSlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.LEFT, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);
            itemConfig.setDataType(RelativeSide.BOTTOM, DataType.EXTRA);
            itemConfig.setDataType(RelativeSide.BACK, DataType.ENERGY);
        }

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, itemConfig);

        infusionInputHandler = InputHelper.getInputHandler(infusionTank);
        itemInputHandler = InputHelper.getInputHandler(inputSlot);
        outputHandler = OutputHelper.getOutputHandler(outputSlot);
    }

    @Override
    protected void presetVariables() {
        infusionTank = new InfusionTank(MAX_INFUSE);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(infusionSlot = InfusionInventorySlot.input(infusionTank, type -> containsRecipe(recipe -> recipe.getInfusionInput().testType(type)), this::getWorld, this, 17, 35));
        //TODO: Verify that it is properly querying the infusion tank's type if it changes
        builder.addSlot(inputSlot = InputInventorySlot.at(stack -> {
            if (!infusionTank.isEmpty()) {
                return containsRecipe(recipe -> recipe.getInfusionInput().testType(infusionTank.getType()) && recipe.getItemInput().testType(stack));
            }
            //Otherwise just look for items that can be used
            return containsRecipe(recipe -> recipe.getItemInput().testType(stack));
        }, this, 51, 43));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 109, 43));
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 143, 35));
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            infusionSlot.fillTank();
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<MetallurgicInfuserRecipe> getRecipeType() {
        return MekanismRecipeType.METALLURGIC_INFUSING;
    }

    @Nullable
    @Override
    public CachedRecipe<MetallurgicInfuserRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public MetallurgicInfuserRecipe getRecipe(int cacheIndex) {
        ItemStack stack = itemInputHandler.getInput();
        if (stack.isEmpty()) {
            return null;
        }
        InfusionStack infusionStack = infusionInputHandler.getInput();
        if (infusionStack.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(infusionStack, stack));
    }

    @Nullable
    @Override
    public CachedRecipe<MetallurgicInfuserRecipe> createNewCachedRecipe(@Nonnull MetallurgicInfuserRecipe recipe, int cacheIndex) {
        return new MetallurgicInfuserCachedRecipe(recipe, infusionInputHandler, itemInputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty)
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        infusionTank.read(nbtTags.getCompound("infuseStored"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (!infusionTank.isEmpty()) {
            nbtTags.put("infuseStored", infusionTank.write(new CompoundNBT()));
        }
        return nbtTags;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int amount = dataStream.readInt();
            //TODO: Make this use a specialized "dump" method
            if (amount == 0) {
                infusionTank.setEmpty();
            }
            return;
        }
        super.handlePacketData(dataStream);
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
                return new Object[]{getDirection()};
            case 3:
                //TODO: Decide if we should try to get the cached recipe if it is null
                return new Object[]{cachedRecipe != null && cachedRecipe.canFunction()};
            case 4:
                return new Object[]{getMaxEnergy()};
            case 5:
                return new Object[]{getNeededEnergy()};
            case 6:
                return new Object[]{infusionTank};
            case 7:
                return new Object[]{infusionTank.getNeeded()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public Direction getOrientation() {
        return getDirection();
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        return configComponent.isCapabilityDisabled(capability, side) || super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!infusionTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "infusionStored", infusionTank.getStack().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        infusionTank.setStack(InfusionStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "infusionStored")));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put("infuseStored.stored", "infusionStored");
        return remap;
    }

    @Nonnull
    @Override
    public MetallurgicInfuserUpgradeData getUpgradeData() {
        return new MetallurgicInfuserUpgradeData(redstone, getControlType(), getEnergy(), getOperatingTicks(), infusionTank.getStack(), infusionSlot, energySlot,
              inputSlot, outputSlot, getComponents());
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInfusionStack.create(infusionTank));
    }
}