package mekanism.api.heat;

import mekanism.api.NBTConstants;
import mekanism.api.math.FloatingLong;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IHeatCapacitor extends INBTSerializable<CompoundNBT> {

    FloatingLong getTemperature();

    double getInverseConductionCoefficient();

    void handleHeatChange(HeatPacket transfer);

    @Override
    default CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(NBTConstants.STORED, getTemperature().toString());
        return nbt;
    }
}
