package mekanism.common.multiblock;

import java.util.Collection;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public interface IValveHandler {

    public default void writeValves(CompoundNBT updateTag) {
        ListNBT valves = new ListNBT();
        for (ValveData valveData : getValveData()) {
            if (valveData.activeTicks > 0) {
                CompoundNBT valveNBT = new CompoundNBT();
                valveData.location.write(valveNBT);
                valveNBT.putInt(NBTConstants.SIDE, valveData.side.ordinal());
                valves.add(valveNBT);
            }
        }
        updateTag.put(NBTConstants.VALVE, valves);
    }

    public default void readValves(CompoundNBT updateTag) {
        getValveData().clear();
        if (updateTag.contains(NBTConstants.VALVE, NBT.TAG_LIST)) {
            ListNBT valves = updateTag.getList(NBTConstants.VALVE, NBT.TAG_COMPOUND);
            for (int i = 0; i < valves.size(); i++) {
                CompoundNBT valveNBT = valves.getCompound(i);
                ValveData data = new ValveData();
                data.location = Coord4D.read(valveNBT);
                data.side = Direction.byIndex(valveNBT.getInt(NBTConstants.SIDE));
                getValveData().add(data);
            }
        }
    }

    public default boolean needsValveUpdate() {
        for (ValveData data : getValveData()) {
            if (data.activeTicks > 0) {
                data.activeTicks--;
            }
            if (data.activeTicks > 0 != data.prevActive) {
                return true;
            }
            data.prevActive = data.activeTicks > 0;
        }
        return false;
    }

    public Collection<ValveData> getValveData();

    public static class ValveData {

        public Direction side;
        public Coord4D location;

        public boolean prevActive;
        public int activeTicks;

        public void onTransfer() {
            activeTicks = 30;
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + side.ordinal();
            code = 31 * code + location.hashCode();
            return code;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ValveData && ((ValveData) obj).side == side && ((ValveData) obj).location.equals(location);
        }
    }
}
