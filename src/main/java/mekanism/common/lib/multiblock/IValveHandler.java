package mekanism.common.lib.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import mekanism.api.SerializationConstants;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.capabilities.fluid.ValveFluidTankWrapper;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public interface IValveHandler {

    default void writeValves(@NotNull ValueOutput output) {
        TypedOutputList<PositionedValve> valveList = output.list(SerializationConstants.VALVE, PositionedValve.CODEC);
        for (Map.Entry<BlockPos, ValveData> entry : getValveData().entrySet()) {
            ValveData valveData = entry.getValue();
            if (valveData.activeTicks > 0) {
                valveList.add(new PositionedValve(entry.getKey(), valveData));
            }
        }
        if (valveList.isEmpty()) {
            output.discard(SerializationConstants.VALVE);
        }
    }

    default void readValves(@NotNull ValueInput input) {
        Map<BlockPos, ValveData> valveData = getValveData();
        valveData.clear();
        for (PositionedValve valve : input.listOrEmpty(SerializationConstants.VALVE, PositionedValve.CODEC)) {
            valveData.put(valve.location(), valve.valve());
        }
    }

    default void triggerValveTransfer(IMultiblock<?> multiblock, TransactionContext transaction) {
        if (multiblock.getMultiblock().isFormed()) {
            ValveData data = getValveData().get(multiblock.getBlockPos());
            if (data != null) {
                data.onTransfer(transaction);
            }
        }
    }

    Map<BlockPos, ValveData> getValveData();

    record PositionedValve(BlockPos location, ValveData valve) {

        public static final Codec<PositionedValve> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              BlockPos.CODEC.fieldOf(SerializationConstants.POSITION).forGetter(PositionedValve::location),
              ValveData.CODEC.fieldOf(SerializationConstants.VALVE).forGetter(PositionedValve::valve)
        ).apply(instance, PositionedValve::new));
    }

    class ValveData extends SnapshotJournal<Integer> {

        public static final Codec<ValveData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              Direction.CODEC.fieldOf(SerializationConstants.SIDE).forGetter(data -> data.side)
        ).apply(instance, ValveData::new));

        public final Direction side;

        @Nullable
        private List<IFluidTank> valveTanks;
        private boolean prevActive;
        private int activeTicks;

        public ValveData(Direction side) {
            this.side = side;
        }

        //TODO - 26.1: Validate that this only gets called once per valve per multiblock
        public void addTank(IFluidTank tank, boolean wrap) {
            if (this.valveTanks == null) {
                this.valveTanks = new ArrayList<>();
            }
            this.valveTanks.add(wrap ? new ValveFluidTankWrapper(tank, this) : tank);
        }

        public List<IFluidTank> getValveTanks() {
            return valveTanks == null ? Collections.emptyList() : valveTanks;
        }

        public void onTransfer(TransactionContext transaction) {
            updateSnapshots(transaction);
            activeTicks = SharedConstants.TICKS_PER_SECOND + MekanismUtils.TICKS_PER_HALF_SECOND;
        }

        public boolean tick() {
            if (activeTicks > 0) {
                activeTicks--;
            }
            if (activeTicks > 0 == prevActive) {
                return false;
            }
            prevActive = !prevActive;
            return true;
        }

        @Override
        public int hashCode() {
            return side.ordinal();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ValveData other && other.side == side;
        }

        @Override
        protected Integer createSnapshot() {
            return activeTicks;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            activeTicks = snapshot;
        }
    }
}
