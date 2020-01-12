package mekanism.generators.common.tile;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.FluidSlot;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FuelInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntityBioGenerator extends TileEntityGenerator implements IFluidHandlerWrapper, ISustainedData, IComparatorSupport {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getBioFuel", "getBioFuelNeeded"};
    private static IFluidTank[] ALL_TANKS = new IFluidTank[0];
    /**
     * The FluidSlot biofuel instance for this generator.
     */
    public FluidSlot bioFuelSlot = new FluidSlot(24000, -1);

    private int currentRedstoneLevel;

    private FuelInventorySlot fuelSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityBioGenerator() {
        super(GeneratorsBlocks.BIO_GENERATOR, MekanismGeneratorsConfig.generators.bioGeneration.get() * 2);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(fuelSlot = FuelInventorySlot.forFuel(this::getFuel, fluidStack -> fluidStack.getFluid().isIn(GeneratorTags.BIO_ETHANOL), this, 17, 35),
              RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.charge(this, 143, 35), RelativeSide.RIGHT);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        ItemStack fuelStack = fuelSlot.getStack();
        if (!fuelStack.isEmpty()) {
            energySlot.charge(this);
            FluidStack fluidStack = FluidUtil.getFluidContained(fuelStack).orElse(FluidStack.EMPTY);
            if (fluidStack.isEmpty()) {
                int fuel = getFuel(fuelStack);
                if (fuel > 0) {
                    int fuelNeeded = bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored;
                    if (fuel <= fuelNeeded) {
                        bioFuelSlot.fluidStored += fuel;
                        ItemStack containerItem = fuelStack.getItem().getContainerItem(fuelStack);
                        if (!containerItem.isEmpty()) {
                            fuelSlot.setStack(containerItem);
                        } else {
                            if (fuelSlot.shrinkStack(1, Action.EXECUTE) != 1) {
                                //TODO: Print error that something went wrong
                            }
                        }
                    }
                }
            } else if (fluidStack.getFluid().isIn(GeneratorTags.BIO_ETHANOL)) {
                FluidUtil.getFluidHandler(fuelStack).ifPresent(handler -> {
                    FluidStack drained = handler.drain(bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored, FluidAction.EXECUTE);
                    if (!drained.isEmpty()) {
                        bioFuelSlot.fluidStored += drained.getAmount();
                    }
                });
            }
        }
        if (canOperate()) {
            if (!isRemote()) {
                setActive(true);
            }
            bioFuelSlot.setFluid(bioFuelSlot.fluidStored - 1);
            setEnergy(getEnergy() + MekanismGeneratorsConfig.generators.bioGeneration.get());
        } else if (!isRemote()) {
            setActive(false);
        }
        if (!isRemote()) {
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

    @Override
    public boolean canOperate() {
        return getEnergy() < getBaseStorage() && bioFuelSlot.fluidStored > 0 && MekanismUtils.canFunction(this);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        bioFuelSlot.fluidStored = nbtTags.getInt("bioFuelStored");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("bioFuelStored", bioFuelSlot.fluidStored);
        return nbtTags;
    }

    public int getFuel(ItemStack stack) {
        return MekanismItems.BIO_FUEL.itemMatches(stack) ? 200 : 0;
    }

    /**
     * Gets the scaled fuel level for the GUI.
     *
     * @param i - multiplier
     *
     * @return Scaled fuel level
     */
    public int getScaledFuelLevel(int i) {
        return bioFuelSlot.fluidStored * i / bioFuelSlot.MAX_FLUID;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            bioFuelSlot.fluidStored = dataStream.readInt();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(bioFuelSlot.fluidStored);
        return data;
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
                return new Object[]{output};
            case 2:
                return new Object[]{getBaseStorage()};
            case 3:
                return new Object[]{getBaseStorage() - getEnergy()};
            case 4:
                return new Object[]{bioFuelSlot.fluidStored};
            case 5:
                return new Object[]{bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        int fuelNeeded = bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored;
        int fuelTransfer = Math.min(resource.getAmount(), fuelNeeded);
        if (fluidAction.execute()) {
            bioFuelSlot.setFluid(bioFuelSlot.fluidStored + fuelTransfer);
        }
        return fuelTransfer;
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return from != getDirection() && fluid.getFluid().isIn(GeneratorTags.BIO_ETHANOL);
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        return PipeUtils.EMPTY;
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        ItemDataUtils.setInt(itemStack, "fluidStored", bioFuelSlot.fluidStored);
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        bioFuelSlot.setFluid(ItemDataUtils.getInt(itemStack, "fluidStored"));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new HashMap<>();
        remap.put("bioFuelStored", "fluidStored");
        return remap;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (side != getDirection() && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return ALL_TANKS;
    }

    @Override
    public int getRedstoneLevel() {
        return ItemHandlerHelper.calcRedstoneFromInventory(this);
    }
}