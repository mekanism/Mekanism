package mekanism.common.content.miner;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import java.util.BitSet;
import java.util.function.IntFunction;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class ThreadMinerSearch extends Thread {

    private final TileEntityDigitalMiner tile;
    private final Long2ObjectMap<BitSet> oresToMine = new Long2ObjectOpenHashMap<>();
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
        Block info;
        BlockPos minerPos = tile.getBlockPos();
        for (int i = 0; i < size; i++) {
            if (tile.isRemoved() || isInterrupted()) {
                //Make sure the miner is still valid and something hasn't gone wrong
                return;
            }
            BlockPos testPos = TileEntityDigitalMiner.getOffsetForIndex(pos, diameter, i);
            if (minerPos.equals(testPos) || WorldUtils.getTileEntity(TileEntityBoundingBlock.class, chunkCache, testPos) != null) {
                //Skip the miner itself, and also skip any bounding blocks
                continue;
            }
            BlockState state = chunkCache.getBlockState(testPos);
            if (state.isAir() || state.is(MekanismTags.Blocks.MINER_BLACKLIST) || shouldSkipState(state) || state.getDestroySpeed(chunkCache, testPos) < 0) {
                //Skip air, blacklisted blocks, special cased block states, and unbreakable blocks
                continue;
            }
            info = state.getBlock();
            if (MekanismUtils.isLiquidBlock(info)) {//Skip liquids
                continue;
            }
            boolean accepted;
            if (acceptedItems.containsKey(info)) {
                accepted = acceptedItems.getBoolean(info);
            } else {
                if (tile.isReplaceTarget(info.asItem())) {
                    //If it is a replace target just mark it as never being accepted
                    accepted = false;
                } else {
                    //Ensure that the inverse mode is the opposite of the filter match
                    accepted = tile.getInverse() != tile.getFilterManager().anyEnabledMatch(state, MinerFilter::canFilter);
                }
                acceptedItems.put(info, accepted);
            }
            if (accepted) {
                long chunk = ChunkPos.asLong(testPos);
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

    /**
     * Special cased vanilla blocks that only have one state actually have the drop and the other one just causes it to break. This includes things like two tall flowers,
     * beds, and doors. If a data pack modifies the loot table so that the "secondary" block also provides drops those will then be handled by the fallback we have for
     * collecting any drops that happen from breaking the block.
     */
    private boolean shouldSkipState(BlockState state) {
        if (state.getBlock() instanceof BedBlock) {
            return state.getValue(BlockStateProperties.BED_PART) == BedPart.FOOT;
        } else if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof DoublePlantBlock) {
            return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
        }
        return false;
    }

    @NothingNullByDefault
    public enum State implements IHasEnumNameTextComponent {
        IDLE(MekanismLang.MINER_IDLE),
        SEARCHING(MekanismLang.MINER_SEARCHING),
        PAUSED(MekanismLang.MINER_PAUSED),
        FINISHED(MekanismLang.MINER_READY);

        public static final IntFunction<State> BY_ID = ByIdMap.continuous(State::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, State> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, State::ordinal);

        private final ILangEntry langEntry;

        State(ILangEntry langEntry) {
            this.langEntry = langEntry;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translate();
        }
    }
}