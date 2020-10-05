package mekanism.common.lib.multiblock;

import java.util.Collection;
import mekanism.api.NBTConstants;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public interface IValveHandler {

    default void writeValves(CompoundNBT updateTag) {
        ListNBT valves = new ListNBT();
        for (ValveData valveData : getValveData()) {
            if (valveData.activeTicks > 0) {
                CompoundNBT valveNBT = new CompoundNBT();
                valveNBT.put(NBTConstants.POSITION, NBTUtil.writeBlockPos(valveData.location));
                valveNBT.putInt(NBTConstants.SIDE, valveData.side.ordinal());
                valves.add(valveNBT);
            }
        }
        updateTag.put(NBTConstants.VALVE, valves);
    }

    default void readValves(CompoundNBT updateTag) {
        getValveData().clear();
        if (updateTag.contains(NBTConstants.VALVE, NBT.TAG_LIST)) {
            ListNBT valves = updateTag.getList(NBTConstants.VALVE, NBT.TAG_COMPOUND);
            for (int i = 0; i < valves.size(); i++) {
                CompoundNBT valveNBT = valves.getCompound(i);
                ValveData data = new ValveData();
                NBTUtils.setBlockPosIfPresent(valveNBT, NBTConstants.POSITION, pos -> data.location = pos);
                data.side = Direction.byIndex(valveNBT.getInt(NBTConstants.SIDE));
                getValveData().add(data);
            }
        }
    }

    default void triggerValveTransfer(IMultiblock<?> multiblock) {
        if (multiblock.getMultiblock().isFormed()) {
            for (ValveData data : getValveData()) {
                if (multiblock.getTilePos().equals(data.location)) {
                    data.onTransfer();
                    break;
                }
            }
        }
    }

    Collection<ValveData> getValveData();

    class ValveData {

        public Direction side;
        public BlockPos location;

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
