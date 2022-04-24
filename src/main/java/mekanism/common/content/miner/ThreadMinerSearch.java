package mekanism.common.content.miner;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.IFluidBlock;

public class ThreadMinerSearch extends Thread {

    private final TileEntityDigitalMiner tile;
    private final Long2ObjectMap<BitSet> oresToMine = new Long2ObjectOpenHashMap<>();
    private PathNavigationRegion chunkCache;
    public State state = State.IDLE;
    public int found = 0;

    public ThreadMinerSearch(TileEntityDigitalMiner tile) {
        this.tile = tile;
    }

    public void setChunkCache(PathNavigationRegion cache) {
        this.chunkCache = cache;
    }

    @Override
    public void run() {
        state = State.SEARCHING;
        List<MinerFilter<?>> filters = tile.getFilters();
        if (!tile.getInverse() && filters.isEmpty()) {
            state = State.FINISHED;
            return;
        }
        Map<Block, MinerFilter<?>> acceptedItems = new Object2ObjectOpenHashMap<>();
        BlockPos pos = tile.getStartingPos();
        int diameter = tile.getDiameter();
        int size = tile.getTotalSize();
        Block info;
        BlockPos minerPos = tile.getBlockPos();
        for (int i = 0; i < size; i++) {
            if (tile.isRemoved()) {
                //Make sure the miner is still valid and something hasn't gone wrong
                return;
            }
            BlockPos testPos = pos.offset(i % diameter, i / diameter / diameter, (i / diameter) % diameter);
            if (minerPos.equals(testPos) || WorldUtils.getTileEntity(TileEntityBoundingBlock.class, chunkCache, testPos) != null) {
                //Skip the miner itself, and also skip any bounding blocks
                continue;
            }
            BlockState state = chunkCache.getBlockState(testPos);
            if (state.isAir() || state.is(MekanismTags.Blocks.MINER_BLACKLIST) || state.getDestroySpeed(chunkCache, testPos) < 0) {
                //Skip air, blacklisted blocks, and unbreakable blocks
                continue;
            }
            info = state.getBlock();
            if (info instanceof LiquidBlock || info instanceof IFluidBlock) {
                //Skip liquids
                continue;
            }
            MinerFilter<?> filterFound = null;
            if (acceptedItems.containsKey(info)) {
                filterFound = acceptedItems.get(info);
            } else {
                if (tile.isReplaceTarget(info.asItem())) {
                    continue;
                }
                for (MinerFilter<?> filter : filters) {
                    if (filter.canFilter(state)) {
                        filterFound = filter;
                        break;
                    }
                }
                acceptedItems.put(info, filterFound);
            }
            if (tile.getInverse() == (filterFound == null)) {
                long chunk = WorldUtils.getChunkPosAsLong(testPos);
                oresToMine.computeIfAbsent(chunk, k -> new BitSet()).set(i);
                found++;
            }
        }

        state = State.FINISHED;
        chunkCache = null;
        if (tile.searcher == this) {
            //Only update search if we are still valid and didn't get replaced due to a reset call
            tile.updateFromSearch(oresToMine, found);
        }
    }

    public enum State implements IHasTextComponent {
        IDLE(MekanismLang.MINER_IDLE),
        SEARCHING(MekanismLang.MINER_SEARCHING),
        PAUSED(MekanismLang.MINER_PAUSED),
        FINISHED(MekanismLang.MINER_READY);

        private static final State[] MODES = values();

        private final ILangEntry langEntry;

        State(ILangEntry langEntry) {
            this.langEntry = langEntry;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translate();
        }

        public static State byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}