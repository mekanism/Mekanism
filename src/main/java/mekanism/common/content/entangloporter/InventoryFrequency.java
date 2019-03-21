package mekanism.common.content.entangloporter;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.PacketHandler;
import mekanism.common.Tier;
import mekanism.api.TileNetworkList;
import mekanism.common.frequency.Frequency;
import mekanism.common.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class InventoryFrequency extends Frequency {

    public static final String ENTANGLOPORTER = "Entangloporter";

    public static final int FLUID_TANK_SIZE = Tier.FluidTankTier.ULTIMATE.output;
    public static final int GAS_TANK_SIZE = Tier.GasTankTier.ULTIMATE.output;

    public double storedEnergy;
    public FluidTank storedFluid;
    public GasTank storedGas;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    public double temperature;

    public InventoryFrequency(String n, UUID uuid) {
        super(n, uuid);

        storedFluid = new FluidTank(FLUID_TANK_SIZE);
        storedGas = new GasTank(GAS_TANK_SIZE);
    }

    public InventoryFrequency(NBTTagCompound nbtTags) {
        super(nbtTags);
    }

    public InventoryFrequency(ByteBuf dataStream) {
        super(dataStream);
    }

    @Override
    public void write(NBTTagCompound nbtTags) {
        super.write(nbtTags);

        nbtTags.setDouble("storedEnergy", storedEnergy);

        if (storedFluid.getFluid() != null) {
            nbtTags.setTag("storedFluid", storedFluid.writeToNBT(new NBTTagCompound()));
        }

        if (storedGas.getGas() != null) {
            nbtTags.setTag("storedGas", storedGas.write(new NBTTagCompound()));
        }

        NBTTagList tagList = new NBTTagList();

        for (int slotCount = 0; slotCount < 1; slotCount++) {
            if (!inventory.get(slotCount).isEmpty()) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte) slotCount);
                inventory.get(slotCount).writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        nbtTags.setTag("Items", tagList);

        nbtTags.setDouble("temperature", temperature);
    }

    @Override
    protected void read(NBTTagCompound nbtTags) {
        super.read(nbtTags);

        storedFluid = new FluidTank(FLUID_TANK_SIZE);
        storedGas = new GasTank(GAS_TANK_SIZE);

        storedEnergy = nbtTags.getDouble("storedEnergy");

        if (nbtTags.hasKey("storedFluid")) {
            storedFluid.readFromNBT(nbtTags.getCompoundTag("storedFluid"));
        }

        if (nbtTags.hasKey("storedGas")) {
            storedGas.read(nbtTags.getCompoundTag("storedGas"));
        }

        NBTTagList tagList = nbtTags.getTagList("Items", NBT.TAG_COMPOUND);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);

        for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(tagCount);
            byte slotID = tagCompound.getByte("Slot");

            if (slotID >= 0 && slotID < 1) {
                inventory.set(slotID, InventoryUtils.loadFromNBT(tagCompound));
            }
        }

        temperature = nbtTags.getDouble("temperature");
    }

    @Override
    public void write(TileNetworkList data) {
        super.write(data);

        data.add(storedEnergy);

        if (storedFluid.getFluid() != null) {
            data.add(true);
            data.add(FluidRegistry.getFluidName(storedFluid.getFluid()));
            data.add(storedFluid.getFluidAmount());
        } else {
            data.add(false);
        }

        if (storedGas.getGas() != null) {
            data.add(true);
            data.add(storedGas.getGasType().getID());
            data.add(storedGas.getStored());
        } else {
            data.add(false);
        }

        data.add(temperature);
    }

    @Override
    protected void read(ByteBuf dataStream) {
        super.read(dataStream);

        storedFluid = new FluidTank(FLUID_TANK_SIZE);
        storedGas = new GasTank(GAS_TANK_SIZE);

        storedEnergy = dataStream.readDouble();

        if (dataStream.readBoolean()) {
            storedFluid.setFluid(
                  new FluidStack(FluidRegistry.getFluid(PacketHandler.readString(dataStream)), dataStream.readInt()));
        } else {
            storedFluid.setFluid(null);
        }

        if (dataStream.readBoolean()) {
            storedGas.setGas(new GasStack(dataStream.readInt(), dataStream.readInt()));
        } else {
            storedGas.setGas(null);
        }

        temperature = dataStream.readDouble();
    }
}
