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
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.RotaryCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.Mekanism;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TileEntityRotaryCondensentrator extends TileEntityMekanism implements ISustainedData, IFluidHandlerWrapper, IGasHandler, ITankManager,
      ITileCachedRecipeHolder<RotaryRecipe> {

    public GasTank gasTank;
    public FluidTank fluidTank;
    /**
     * True: fluid -> gas
     *
     * False: gas -> fluid
     */
    public boolean mode;

    public int gasOutput = 256;

    private final IOutputHandler<@NonNull GasStack> gasOutputHandler;
    private final IOutputHandler<@NonNull FluidStack> fluidOutputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;

    private CachedRecipe<RotaryRecipe> cachedRecipe;

    public double clientEnergyUsed;

    private GasInventorySlot gasInputSlot;
    private OutputInventorySlot gasOutputSlot;
    private FluidInventorySlot fluidInputSlot;
    private OutputInventorySlot fluidOutputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityRotaryCondensentrator() {
        super(MekanismBlocks.ROTARY_CONDENSENTRATOR);
        gasInputHandler = InputHelper.getInputHandler(gasTank);
        fluidInputHandler = InputHelper.getInputHandler(fluidTank, 0);
        gasOutputHandler = OutputHelper.getOutputHandler(gasTank);
        fluidOutputHandler = OutputHelper.getOutputHandler(fluidTank);
    }

    @Override
    protected void presetVariables() {
        gasTank = new GasTank(10_000);
        fluidTank = new FluidTank(10_000);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        //TODO: Fix these, not only is the naming bad, but there is a dedicated drain and empty slot for gas unlike how the fluid is
        builder.addSlot(gasInputSlot = GasInventorySlot.rotary(gasTank, this::isValidGas, () -> mode, this, 5, 25), RelativeSide.LEFT);
        builder.addSlot(gasOutputSlot = OutputInventorySlot.at(this, 5, 56), RelativeSide.LEFT);
        builder.addSlot(fluidInputSlot = FluidInventorySlot.rotary(fluidTank, this::isValidFluid, () -> mode, this, 155, 25), RelativeSide.RIGHT);
        builder.addSlot(fluidOutputSlot = OutputInventorySlot.at(this, 155, 56), RelativeSide.RIGHT);
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 155, 5), RelativeSide.FRONT, RelativeSide.BACK, RelativeSide.BOTTOM, RelativeSide.TOP);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            if (mode) {//Fluid to Gas
                fluidInputSlot.fillTank(fluidOutputSlot);
                gasInputSlot.drainTank();
                TileUtils.emitGas(this, gasTank, gasOutput, getLeftSide());
            } else {//Gas to Fluid
                //TODO: FIXME make this use logic via GasInventorySlot
                TileUtils.receiveGas(gasOutputSlot.getStack(), gasTank);
                fluidInputSlot.drainTank(fluidOutputSlot);
                //TODO: Auto eject fluid?
            }
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
        }
    }

    public boolean isValidGas(@Nonnull Gas gas) {
        return !gas.isEmptyType() && containsRecipe(recipe -> recipe.hasGasToFluid() && recipe.getGasInput().testType(gas));
    }

    public boolean isValidFluid(@Nonnull FluidStack fluidStack) {
        return !fluidStack.isEmpty() && containsRecipe(recipe -> recipe.hasFluidToGas() && recipe.getFluidInput().testType(fluidStack));
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                mode = !mode;
            }
            for (PlayerEntity player : playersUsing) {
                Mekanism.packetHandler.sendTo(new PacketTileEntity(this), (ServerPlayerEntity) player);
            }
            return;
        }

        super.handlePacketData(dataStream);
        if (isRemote()) {
            mode = dataStream.readBoolean();
            clientEnergyUsed = dataStream.readDouble();
            TileUtils.readTankData(dataStream, fluidTank);
            TileUtils.readTankData(dataStream, gasTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(mode);
        data.add(clientEnergyUsed);
        TileUtils.addTankData(data, fluidTank);
        TileUtils.addTankData(data, gasTank);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        mode = nbtTags.getBoolean("mode");
        gasTank.read(nbtTags.getCompound("gasTank"));
        if (nbtTags.contains("fluidTank")) {
            fluidTank.readFromNBT(nbtTags.getCompound("fluidTank"));
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean("mode", mode);
        nbtTags.put("gasTank", gasTank.write(new CompoundNBT()));
        if (!fluidTank.isEmpty()) {
            nbtTags.put("fluidTank", fluidTank.writeToNBT(new CompoundNBT()));
        }
        return nbtTags;
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        if (canReceiveGas(side, stack.getType())) {
            return gasTank.fill(stack, action);
        }
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
        return mode && side == getLeftSide() && gasTank.canDraw(type);
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return !mode && side == getLeftSide() && gasTank.canReceive(type) && isValidGas(type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{gasTank};
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
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != null && side != getLeftSide();
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return side != null && side != getRightSide();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!fluidTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "fluidTank", fluidTank.getFluid().writeToNBT(new CompoundNBT()));
        }
        if (!gasTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "gasTank", gasTank.getStack().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "fluidTank")));
        gasTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "gasTank")));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new HashMap<>();
        remap.put("fluidTank", "fluidTank");
        remap.put("gasTank.stored", "gasTank");
        return remap;
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        if (canFill(from, resource)) {
            return fluidTank.fill(resource, fluidAction);
        }
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        if (canDrain(from, FluidStack.EMPTY)) {
            return fluidTank.drain(maxDrain, fluidAction);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return mode && from == getRightSide() && FluidContainerUtils.canFill(fluidTank.getFluid(), fluid) && isValidFluid(fluid);
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        return !mode && from == getRightSide() && FluidContainerUtils.canDrain(fluidTank.getFluid(), fluid);
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        if (from == getRightSide()) {
            return getAllTanks();
        }
        return PipeUtils.EMPTY;
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return new IFluidTank[]{fluidTank};
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{gasTank, fluidTank};
    }

    @Override
    public int getRedstoneLevel() {
        if (mode) {
            return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
        }
        return MekanismUtils.redstoneLevelFromContents(gasTank.getStored(), gasTank.getCapacity());
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Nonnull
    @Override
    public MekanismRecipeType<RotaryRecipe> getRecipeType() {
        return MekanismRecipeType.ROTARY;
    }

    @Nullable
    @Override
    public CachedRecipe<RotaryRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public RotaryRecipe getRecipe(int cacheIndex) {
        if (mode) {//Fluid to Gas
            FluidStack fluid = fluidInputHandler.getInput();
            if (fluid.isEmpty()) {
                return null;
            }
            return findFirstRecipe(recipe -> recipe.test(fluid));
        }
        //Gas to Fluid
        GasStack gas = gasInputHandler.getInput();
        if (gas.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(gas));
    }

    @Nullable
    @Override
    public CachedRecipe<RotaryRecipe> createNewCachedRecipe(@Nonnull RotaryRecipe recipe, int cacheIndex) {
        return new RotaryCachedRecipe(recipe, fluidInputHandler, gasInputHandler, gasOutputHandler, fluidOutputHandler, () -> mode)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setOnFinish(this::markDirty)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax == 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return 0;
                  }
                  int possibleProcess = (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
                  if (mode) {
                      //Fluid to gas
                      return Math.min(Math.min(fluidTank.getFluidAmount(), gasTank.getNeeded()), possibleProcess);
                  }
                  //Gas to fluid
                  return Math.min(Math.min(gasTank.getStored(), fluidTank.getSpace()), possibleProcess);
              });
    }
}