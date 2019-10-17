package mekanism.common.content.entangloporter;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.GasTank;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.inventory.slot.InternalInventorySlot;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.GasTankTier;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class InventoryFrequency extends Frequency {

    public static final String ENTANGLOPORTER = "Entangloporter";
    private static final Supplier<FluidTank> FLUID_TANK_SUPPLIER = () -> new FluidTank(MekanismConfig.general.quantumEntangloporterFluidBuffer.get());
    private static final Supplier<GasTank> GAS_TANK_SUPPLIER = () -> new GasTank(MekanismConfig.general.quantumEntangloporterGasBuffer.get());

    public double storedEnergy;
    public FluidTank storedFluid;
    public GasTank storedGas;
    public double temperature;

    //TODO: input: configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).ioState == IOState.INPUT
    //TODO: output: configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).ioState == IOState.OUTPUT
    //TODO: Should this be an input and output slot then? Also should this even be two slots big?
    public final List<IInventorySlot> inventorySlots = Arrays.asList(InternalInventorySlot.create(), InternalInventorySlot.create());

    public InventoryFrequency(String n, UUID uuid) {
        super(n, uuid);
        storedFluid = FLUID_TANK_SUPPLIER.get();
        storedGas = GAS_TANK_SUPPLIER.get();
    }

    public InventoryFrequency(CompoundNBT nbtTags) {
        super(nbtTags);
    }

    public InventoryFrequency(PacketBuffer dataStream) {
        super(dataStream);
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble("storedEnergy", storedEnergy);
        if (!storedFluid.getFluid().isEmpty()) {
            nbtTags.put("storedFluid", storedFluid.writeToNBT(new CompoundNBT()));
        }
        if (!storedGas.isEmpty()) {
            nbtTags.put("storedGas", storedGas.write(new CompoundNBT()));
        }
        ListNBT tagList = new ListNBT();
        for (int slotCount = 0; slotCount < 1; slotCount++) {
            IInventorySlot slot = inventorySlots.get(slotCount);
            if (!slot.isEmpty()) {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.putByte("Slot", (byte) slotCount);
                slot.getStack().write(tagCompound);
                tagList.add(tagCompound);
            }
        }
        nbtTags.put("Items", tagList);
        nbtTags.putDouble("temperature", temperature);
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        storedFluid = FLUID_TANK_SUPPLIER.get();
        storedGas = GAS_TANK_SUPPLIER.get();
        storedEnergy = nbtTags.getDouble("storedEnergy");

        if (nbtTags.contains("storedFluid")) {
            storedFluid.readFromNBT(nbtTags.getCompound("storedFluid"));
        }
        if (nbtTags.contains("storedGas")) {
            storedGas.read(nbtTags.getCompound("storedGas"));
            storedGas.setCapacity(MekanismConfig.general.quantumEntangloporterGasBuffer.get());
        }

        ListNBT tagList = nbtTags.getList("Items", NBT.TAG_COMPOUND);
        for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundNBT tagCompound = tagList.getCompound(tagCount);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID == 0) {
                inventorySlots.get(slotID).setStack(ItemStack.read(tagCompound));
            }
        }
        temperature = nbtTags.getDouble("temperature");
    }

    @Override
    public void write(TileNetworkList data) {
        super.write(data);
        data.add(storedEnergy);
        TileUtils.addTankData(data, storedFluid);
        TileUtils.addTankData(data, storedGas);
        data.add(temperature);
    }

    @Override
    protected void read(PacketBuffer dataStream) {
        super.read(dataStream);
        storedFluid = new FluidTank(FluidTankTier.ULTIMATE.getOutput());
        storedGas = new GasTank(GasTankTier.ULTIMATE.getOutput());
        storedEnergy = dataStream.readDouble();
        TileUtils.readTankData(dataStream, storedFluid);
        TileUtils.readTankData(dataStream, storedGas);
        temperature = dataStream.readDouble();
    }
}