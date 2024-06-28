package mekanism.common.lib.multiblock;

import java.util.Collection;
import mekanism.api.SerializationConstants;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

public interface IValveHandler {

    default void writeValves(CompoundTag updateTag) {
        ListTag valves = new ListTag();
        for (ValveData valveData : getValveData()) {
            if (valveData.activeTicks > 0) {
                CompoundTag valveNBT = new CompoundTag();
                valveNBT.put(SerializationConstants.POSITION, NbtUtils.writeBlockPos(valveData.location));
                NBTUtils.writeEnum(valveNBT, SerializationConstants.SIDE, valveData.side);
                valves.add(valveNBT);
            }
        }
        updateTag.put(SerializationConstants.VALVE, valves);
    }

    default void readValves(CompoundTag updateTag) {
        getValveData().clear();
        if (updateTag.contains(SerializationConstants.VALVE, Tag.TAG_LIST)) {
            ListTag valves = updateTag.getList(SerializationConstants.VALVE, Tag.TAG_COMPOUND);
            for (int i = 0; i < valves.size(); i++) {
                CompoundTag valveNBT = valves.getCompound(i);
                NBTUtils.setBlockPosIfPresent(valveNBT, SerializationConstants.POSITION, pos -> {
                    Direction side = Direction.from3DDataValue(valveNBT.getInt(SerializationConstants.SIDE));
                    getValveData().add(new ValveData(pos, side));
                });
            }
        }
    }

    default void triggerValveTransfer(IMultiblock<?> multiblock) {
        if (multiblock.getMultiblock().isFormed()) {
            BlockPos pos = multiblock.getBlockPos();
            for (ValveData data : getValveData()) {
                if (pos.equals(data.location)) {
                    data.onTransfer();
                    break;
                }
            }
        }
    }

    Collection<ValveData> getValveData();

    class ValveData {

        public final BlockPos location;
        public final Direction side;

        public boolean prevActive;
        public int activeTicks;

        public ValveData(BlockPos location, Direction side) {
            this.location = location;
            this.side = side;
        }

        public void onTransfer() {
            activeTicks = SharedConstants.TICKS_PER_SECOND + MekanismUtils.TICKS_PER_HALF_SECOND;
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
            return obj instanceof ValveData other && other.side == side && other.location.equals(location);
        }
    }
}
