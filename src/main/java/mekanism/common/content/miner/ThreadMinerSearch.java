package mekanism.common.content.miner;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import java.util.BitSet;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class ThreadMinerSearch extends Thread {

    private final TileEntityDigitalMiner tile;
    private final Long2ObjectMap<BitSet> oresToMine = new Long2ObjectOpenHashMap<>();
    @Nullable
    private MinerRegionCache chunkCache;
    public State state = State.IDLE;
    public int found = 0;

    public ThreadMinerSearch(TileEntityDigitalMiner tile) {
        super("Digital Miner Search thread " + tile.getBlockPos());
        this.tile = tile;
        setDaemon(true);
    }

    public void setChunkCache(MinerRegionCache cache) {
        this.chunkCache = cache;
    }

    @Override
    public void run() {
        state = State.SEARCHING;
        if (!tile.getInverse() && !tile.getFilterManager().hasEnabledFilters()) {
            state = State.FINISHED;
            return;
        }
        Reference2BooleanMap<Block> acceptedItems = new Reference2BooleanOpenHashMap<>();
        BlockPos pos = tile.getStartingPos();
        int diameter = tile.getDiameter();
        int size = tile.getTotalSize();
        BlockPos minerPos = tile.getBlockPos();
        for (int i = 0; i < size; i++) {
            if (tile.isRemoved() || isInterrupted()) {
                //Make sure the miner is still valid and something hasn't gone wrong
                return;
            }
            BlockPos testPos = TileEntityDigitalMiner.getOffsetForIndex(pos, diameter, i);
            if (minerPos.equals(testPos)) {
                //Skip the miner itself
                continue;
            }
            BlockState state = chunkCache.getBlockState(testPos);
            if (!TileEntityDigitalMiner.canBreakBlock(chunkCache, testPos, state)) {
                //Skip air, blacklisted blocks, bounding blocks, special cased block states, unbreakable blocks, and liquids
                continue;
            }
            Block block = state.getBlock();
            boolean accepted;
            if (acceptedItems.containsKey(block)) {
                accepted = acceptedItems.getBoolean(block);
            } else {
                if (tile.isReplaceTarget(ItemResource.of(block))) {
                    //If it is a replace target just mark it as never being accepted
                    accepted = false;
                } else {
                    //Ensure that the inverse mode is the opposite of the filter match
                    accepted = tile.getInverse() != tile.getFilterManager().anyEnabledMatch(state, MinerFilter::canFilter);
                }
                acceptedItems.put(block, accepted);
            }
            if (accepted) {
                long chunk = ChunkPos.pack(testPos);
                oresToMine.computeIfAbsent(chunk, k -> new BitSet()).set(i);
                found++;
            }
        }

        state = State.FINISHED;
        chunkCache = null;
        if (interrupted()) {
            return;//no point checking as we got cancelled
        }
        if (tile.searcher == this) {
            //Only update search if we are still valid and didn't get replaced due to a reset call
            tile.updateFromSearch(oresToMine, found);
        }
    }

    public enum State implements IHasEnumNameTextComponent, StringRepresentable {
        IDLE(MekanismLang.MINER_IDLE),
        SEARCHING(MekanismLang.MINER_SEARCHING),
        PAUSED(MekanismLang.MINER_PAUSED),
        FINISHED(MekanismLang.MINER_READY);

        public static final Codec<State> CODEC = StringRepresentable.fromEnum(State::values);
        public static final IntFunction<State> BY_ID = ByIdMap.continuous(State::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, State> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, State::ordinal);

        private final String serializedName;
        private final ILangEntry langEntry;

        State(ILangEntry langEntry) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translate();
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}