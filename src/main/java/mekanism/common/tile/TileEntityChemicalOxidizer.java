package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackToGasCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableGasStack;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityChemicalOxidizer extends TileEntityOperationalMachine<ItemStackToGasRecipe> implements ISustainedData, ITankManager, IGasHandler {

    public static final int MAX_GAS = 10_000;
    public GasTank gasTank;
    public int gasOutput = 256;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> inputHandler;

    private InputInventorySlot inputSlot;
    private GasInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalOxidizer() {
        super(MekanismBlocks.CHEMICAL_OXIDIZER, 100);
        inputHandler = InputHelper.getInputHandler(inputSlot);
        outputHandler = OutputHelper.getOutputHandler(gasTank);
    }

    @Override
    protected void presetVariables() {
        gasTank = new GasTank(MAX_GAS);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getInput().testType(item)), this, 26, 36), RelativeSide.LEFT);
        builder.addSlot(outputSlot = GasInventorySlot.drain(gasTank, this, 155, 25), RelativeSide.RIGHT);
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 155, 5), RelativeSide.BOTTOM, RelativeSide.TOP);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            outputSlot.drainTank();
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
            TileUtils.emitGas(this, gasTank, gasOutput, getRightSide());
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackToGasRecipe> getRecipeType() {
        return MekanismRecipeType.OXIDIZING;
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackToGasRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ItemStackToGasRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandler.getInput();
        if (stack.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(stack));
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackToGasRecipe> createNewCachedRecipe(@Nonnull ItemStackToGasRecipe recipe, int cacheIndex) {
        return new ItemStackToGasCachedRecipe(recipe, inputHandler, outputHandler)
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
        gasTank.read(nbtTags.getCompound("gasTank"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("gasTank", gasTank.write(new CompoundNBT()));
        return nbtTags;
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
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != null && side != getRightSide();
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return side == getDirection() || side == getOppositeDirection();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!gasTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "gasTank", gasTank.getStack().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        gasTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "gasTank")));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put("gasTank.stored", "gasTank");
        return remap;
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{gasTank};
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return false;
    }


    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        if (canDrawGas(side, MekanismAPI.EMPTY_GAS)) {
            return gasTank.drain(amount, action);
        }
        return GasStack.EMPTY;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return side == getRightSide() && gasTank.canDraw(type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{gasTank};
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableGasStack.create(gasTank));
    }
}