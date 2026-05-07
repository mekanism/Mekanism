package mekanism.common.lib.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import mekanism.api.SerializationConstants;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import org.jetbrains.annotations.NotNull;

public interface IValveHandler {

    default void writeValves(@NotNull ValueOutput output) {
        TypedOutputList<ValveData> valveList = output.list(SerializationConstants.VALVE, ValveData.CODEC);
        for (ValveData valveData : getValveData()) {
            if (valveData.activeTicks > 0) {
                valveList.add(valveData);
            }
        }
        if (valveList.isEmpty()) {
            output.discard(SerializationConstants.VALVE);
        }
    }

    default void readValves(@NotNull ValueInput input) {
        Collection<ValveData> valveData = getValveData();
        valveData.clear();
        for (ValveData data : input.listOrEmpty(SerializationConstants.VALVE, ValveData.CODEC)) {
            valveData.add(data);
        }
    }

    //TODO - 26.1: Hook valve transferring back up
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

        //TODO - 26.1: Re-evaluate how we get the side, do we want to just store the side itself?
        public static final Codec<ValveData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              BlockPos.CODEC.fieldOf(SerializationConstants.POSITION).forGetter(data -> data.location),
              Direction.LEGACY_ID_CODEC.fieldOf(SerializationConstants.AMOUNT).forGetter(data -> data.side)
        ).apply(instance, ValveData::new));


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
