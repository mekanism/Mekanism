package mekanism.common.tile;

import java.util.HashMap;
import java.util.Map;
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
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.GasToGasCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.RainType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntitySolarNeutronActivator extends TileEntityMekanism implements IBoundingBlock, IGasHandler, IActiveState, ISustainedData, ITankManager,
      IComparatorSupport, ITileCachedRecipeHolder<GasToGasRecipe> {

    public static final int MAX_GAS = 10_000;

    public GasTank inputTank;
    public GasTank outputTank;

    public int gasOutput = 256;

    private CachedRecipe<GasToGasRecipe> cachedRecipe;

    private int currentRedstoneLevel;
    private boolean settingsChecked;
    private boolean needsRainCheck;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull GasStack> inputHandler;

    private GasInventorySlot inputSlot;
    private GasInventorySlot outputSlot;

    public TileEntitySolarNeutronActivator() {
        super(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
        inputHandler = InputHelper.getInputHandler(inputTank);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
    }

    @Override
    protected void presetVariables() {
        inputTank = new GasTank(MAX_GAS);
        outputTank = new GasTank(MAX_GAS);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(inputSlot = GasInventorySlot.fill(inputTank, this::isValidGas, this, 5, 56),
              RelativeSide.BOTTOM, RelativeSide.TOP, RelativeSide.RIGHT, RelativeSide.LEFT, RelativeSide.BACK);
        builder.addSlot(outputSlot = GasInventorySlot.drain(outputTank, this, 155, 56), RelativeSide.FRONT);
        return builder.build();
    }

    protected void recheckSettings() {
        Biome b = world.getBiomeManager().getBiome(getPos());
        needsRainCheck = b.getPrecipitation() != RainType.NONE;
        settingsChecked = true;
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            if (!settingsChecked) {
                recheckSettings();
            }
            inputSlot.fillTank();
            outputSlot.drainTank();
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }

            TileUtils.emitGas(this, outputTank, gasOutput, getDirection());
            // Every 20 ticks (once a second), send update to client. Note that this is a 50% reduction in network
            // traffic from previous implementation that send the update every 10 ticks.
            if (world.getDayTime() % 20 == 0) {
                //TODO: Why do we have to be sending updates to the client anyways?
                // I believe we send when state changes, and otherwise we only should have to be sending if recipe actually processes
                Mekanism.packetHandler.sendUpdatePacket(this);
            }

            World world = getWorld();
            if (world != null) {
                int newRedstoneLevel = getRedstoneLevel();
                if (newRedstoneLevel != currentRedstoneLevel) {
                    world.updateComparatorOutputLevel(pos, getBlockType());
                    currentRedstoneLevel = newRedstoneLevel;
                }
            }
        }
    }

    private boolean isValidGas(@Nonnull Gas gas) {
        return containsRecipe(recipe -> recipe.getInput().testType(gas));
    }

    @Nonnull
    @Override
    public MekanismRecipeType<GasToGasRecipe> getRecipeType() {
        return MekanismRecipeType.ACTIVATING;
    }

    @Nullable
    @Override
    public CachedRecipe<GasToGasRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public GasToGasRecipe getRecipe(int cacheIndex) {
        GasStack gas = inputHandler.getInput();
        if (gas.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(gas));
    }

    private boolean canFunction(BlockPos positionAbove) {
        // TODO: Ideally the neutron activator should use the sky brightness to determine throughput; but
        // changing this would dramatically affect a lot of setups with Fusion reactors which can take
        // a long time to relight. I don't want to be chased by a mob right now, so just doing basic
        // rain checks.
        boolean seesSun = world.isDaytime() && world.canBlockSeeSky(positionAbove) && !world.getDimension().isNether();
        if (needsRainCheck) {
            seesSun &= !(world.isRaining() || world.isThundering());
        }
        return seesSun && MekanismUtils.canFunction(this);
    }

    @Nullable
    @Override
    public CachedRecipe<GasToGasRecipe> createNewCachedRecipe(@Nonnull GasToGasRecipe recipe, int cacheIndex) {
        BlockPos positionAbove = getPos().up();
        return new GasToGasCachedRecipe(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(() -> canFunction(positionAbove))
              .setActive(this::setActive)
              .setOnFinish(this::markDirty)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax == 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return 0;
                  }
                  return Math.min((int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), currentMax);
              });
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            TileUtils.readTankData(dataStream, inputTank);
            TileUtils.readTankData(dataStream, outputTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        TileUtils.addTankData(data, inputTank);
        TileUtils.addTankData(data, outputTank);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        inputTank.read(nbtTags.getCompound("inputTank"));
        outputTank.read(nbtTags.getCompound("outputTank"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("inputTank", inputTank.write(new CompoundNBT()));
        nbtTags.put("outputTank", outputTank.write(new CompoundNBT()));
        return nbtTags;
    }

    @Override
    public void onPlace() {
        MekanismUtils.makeBoundingBlock(getWorld(), getPos().up(), getPos());
    }

    @Override
    public void onBreak() {
        World world = getWorld();
        if (world != null) {
            world.removeBlock(getPos().up(), false);
            world.removeBlock(getPos(), false);
        }
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        if (canReceiveGas(side, stack.getType())) {
            return inputTank.fill(stack, action);
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        if (canDrawGas(side, MekanismAPI.EMPTY_GAS)) {
            return outputTank.drain(amount, action);
        }
        return GasStack.EMPTY;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return side == Direction.DOWN && inputTank.canReceive(type) && isValidGas(type);
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return side == getDirection() && outputTank.canDraw(type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{inputTank, outputTank};
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
            return side != null && side != getDirection() && side != Direction.DOWN;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!inputTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "inputTank", inputTank.getStack().write(new CompoundNBT()));
        }
        if (!outputTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "outputTank", outputTank.getStack().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        inputTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "inputTank")));
        outputTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputTank")));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new HashMap<>();
        remap.put("inputTank.stored", "inputTank");
        remap.put("outputTank.stored", "outputTank");
        return remap;
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{inputTank, outputTank};
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    public double getProgress() {
        if (getActive()) {
            return .16 * (1 + (world.getDayTime() % 6));
        }
        return 0;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getStored(), inputTank.getCapacity());
    }
}