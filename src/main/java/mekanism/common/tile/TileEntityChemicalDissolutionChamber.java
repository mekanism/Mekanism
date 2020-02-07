package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackGasToGasCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import mekanism.common.util.GasUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityChemicalDissolutionChamber extends TileEntityOperationalMachine<ItemStackGasToGasRecipe> implements IGasHandler, ISustainedData, ITankManager {

    public static final int MAX_GAS = 10_000;
    public static final int BASE_INJECT_USAGE = 1;
    public static final int BASE_TICKS_REQUIRED = 100;
    public GasTank injectTank;
    public GasTank outputTank;
    public double injectUsage = BASE_INJECT_USAGE;
    public int injectUsageThisTick;
    public int gasOutput = 256;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;

    private GasInventorySlot gasInputSlot;
    private InputInventorySlot inputSlot;
    private GasInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalDissolutionChamber() {
        super(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, BASE_TICKS_REQUIRED);
        itemInputHandler = InputHelper.getInputHandler(inputSlot);
        gasInputHandler = InputHelper.getInputHandler(injectTank);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
    }

    @Override
    protected void presetVariables() {
        injectTank = new GasTank(MAX_GAS);
        outputTank = new GasTank(MAX_GAS);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(gasInputSlot = GasInventorySlot.fillOrConvert(injectTank, this::isValidGas, this::getWorld, this, 6, 65), RelativeSide.BOTTOM);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getItemInput().testType(item)), this, 26, 36),
              RelativeSide.TOP, RelativeSide.LEFT);
        builder.addSlot(outputSlot = GasInventorySlot.drain(outputTank, this, 155, 25), RelativeSide.RIGHT);
        //TODO: Make this be accessible from some side for automation??
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 155, 5));
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            gasInputSlot.fillTankOrConvert();
            outputSlot.drainTank();
            injectUsageThisTick = Math.max(BASE_INJECT_USAGE, StatUtils.inversePoisson(injectUsage));
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
            TileUtils.emitGas(this, outputTank, gasOutput, getRightSide());
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackGasToGasRecipe> getRecipeType() {
        return MekanismRecipeType.DISSOLUTION;
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackGasToGasRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ItemStackGasToGasRecipe getRecipe(int cacheIndex) {
        ItemStack stack = itemInputHandler.getInput();
        if (stack.isEmpty()) {
            return null;
        }
        GasStack gasStack = gasInputHandler.getInput();
        if (gasStack.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(stack, gasStack));
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackGasToGasRecipe> createNewCachedRecipe(@Nonnull ItemStackGasToGasRecipe recipe, int cacheIndex) {
        return new ItemStackGasToGasCachedRecipe(recipe, itemInputHandler, gasInputHandler, () -> injectUsageThisTick, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            injectTank.setStack(PacketHandler.readGasStack(dataStream));
            outputTank.setStack(PacketHandler.readGasStack(dataStream));
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(injectTank.getStack());
        data.add(outputTank.getStack());
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        injectTank.read(nbtTags.getCompound("injectTank"));
        outputTank.read(nbtTags.getCompound("outputTank"));
        GasUtils.clearIfInvalid(injectTank, this::isValidGas);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("injectTank", injectTank.write(new CompoundNBT()));
        nbtTags.put("outputTank", outputTank.write(new CompoundNBT()));
        return nbtTags;
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        if (canReceiveGas(side, stack.getType())) {
            return injectTank.fill(stack, action);
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        return GasStack.EMPTY;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return side == getLeftSide() && injectTank.canReceive(type) && isValidGas(type);
    }

    private boolean isValidGas(@Nonnull Gas gas) {
        return containsRecipe(recipe -> recipe.getGasInput().testType(gas));
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    @Override
    @Nonnull
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{injectTank, outputTank};
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
            return side != null && side != getLeftSide() && side != getRightSide();
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return side == getDirection() || side == getOppositeDirection();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!injectTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "injectTank", injectTank.getStack().write(new CompoundNBT()));
        }
        if (!outputTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "outputTank", outputTank.getStack().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        injectTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "injectTank")));
        outputTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputTank")));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put("injectTank.stored", "injectTank");
        remap.put("outputTank.stored", "outputTank");
        return remap;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.GAS || upgrade == Upgrade.SPEED) {
            injectUsage = MekanismUtils.getSecondaryEnergyPerTickMean(this, BASE_INJECT_USAGE);
        }
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{injectTank, outputTank};
    }
}