package mekanism.generators.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TileEntityBioGenerator extends TileEntityGenerator implements IFluidHandlerWrapper, ISustainedData {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getBioFuel", "getBioFuelNeeded"};
    private static IFluidTank[] ALL_TANKS = new IFluidTank[0];
    private static final int MAX_FLUID = 24_000;

    //TODO: At some point we probably want to cleanup the implementation for how we keep track of how much bio fuel is stored
    private int bioFuelStored;

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
                    int fuelNeeded = MAX_FLUID - bioFuelStored;
                    if (fuel <= fuelNeeded) {
                        bioFuelStored += fuel;
                        ItemStack containerItem = fuelStack.getItem().getContainerItem(fuelStack);
                        if (!containerItem.isEmpty()) {
                            fuelSlot.setStack(containerItem);
                        } else if (fuelSlot.shrinkStack(1, Action.EXECUTE) != 1) {
                            //TODO: Print error that something went wrong
                        }
                    }
                }
            } else if (fluidStack.getFluid().isIn(GeneratorTags.BIO_ETHANOL)) {
                FluidUtil.getFluidHandler(fuelStack).ifPresent(handler -> {
                    FluidStack drained = handler.drain(MAX_FLUID - bioFuelStored, FluidAction.EXECUTE);
                    if (!drained.isEmpty()) {
                        setBioFuelStored(bioFuelStored + drained.getAmount());
                    }
                });
            }
        }
        if (canOperate()) {
            if (!isRemote()) {
                setActive(true);
            }
            setBioFuelStored(bioFuelStored - 1);
            setEnergy(getEnergy() + MekanismGeneratorsConfig.generators.bioGeneration.get());
        } else if (!isRemote()) {
            setActive(false);
        }
    }

    @Override
    public boolean canOperate() {
        return getEnergy() < getBaseStorage() && bioFuelStored > 0 && MekanismUtils.canFunction(this);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        setBioFuelStored(nbtTags.getInt("bioFuelStored"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("bioFuelStored", bioFuelStored);
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
        return bioFuelStored * i / MAX_FLUID;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            setBioFuelStored(dataStream.readInt());
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        //Note: We still have to sync bio fuel stored as it is used in rendering the tile
        data.add(bioFuelStored);
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
                return new Object[]{bioFuelStored};
            case 5:
                return new Object[]{MAX_FLUID - bioFuelStored};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        int fuelNeeded = MAX_FLUID - bioFuelStored;
        int fuelTransfer = Math.min(resource.getAmount(), fuelNeeded);
        if (fluidAction.execute()) {
            setBioFuelStored(bioFuelStored + fuelTransfer);
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
        ItemDataUtils.setInt(itemStack, "fluidStored", bioFuelStored);
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        setBioFuelStored(ItemDataUtils.getInt(itemStack, "fluidStored"));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
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

    public int getBioFuelStored() {
        return bioFuelStored;
    }

    private void setBioFuelStored(int amount) {
        bioFuelStored = Math.max(Math.min(amount, MAX_FLUID), 0);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getBioFuelStored, this::setBioFuelStored));
    }
}