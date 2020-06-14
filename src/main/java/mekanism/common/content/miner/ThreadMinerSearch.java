package mekanism.common.content.miner;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Region;
import net.minecraftforge.fluids.IFluidBlock;

public class ThreadMinerSearch extends Thread {

    private final TileEntityDigitalMiner tile;

    public State state = State.IDLE;

    private final Map<ChunkPos, BitSet> oresToMine = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectMap<MinerFilter<?>> replaceMap = new Int2ObjectOpenHashMap<>();
    private final Map<Block, MinerFilter<?>> acceptedItems = new Object2ObjectOpenHashMap<>();
    private Region chunkCache;

    public int found = 0;

    public ThreadMinerSearch(TileEntityDigitalMiner tile) {
        this.tile = tile;
    }

    public void setChunkCache(Region cache) {
        this.chunkCache = cache;
    }

    @Override
    public void run() {
        state = State.SEARCHING;
        List<MinerFilter<?>> filters = tile.getFilters();
        if (!tile.inverse && filters.isEmpty()) {
            state = State.FINISHED;
            return;
        }
        BlockPos pos = tile.getStartingPos();
        int diameter = tile.getDiameter();
        int size = tile.getTotalSize();
        Block info;
        BlockPos minerPos = tile.getPos();
        for (int i = 0; i < size; i++) {
            if (tile.isRemoved()) {
                //Make sure the miner is still valid and something hasn't gone wrong
                return;
            }
            BlockPos testPos = pos.add(i % diameter, i / diameter / diameter, (i / diameter) % diameter);
            if (minerPos.equals(testPos) || MekanismUtils.getTileEntity(TileEntityBoundingBlock.class, chunkCache, testPos) != null) {
                //Skip the miner itself, and also skip any bounding blocks
                continue;
            }
            BlockState state = chunkCache.getBlockState(testPos);
            if (state.isAir(chunkCache, testPos) || state.getBlockHardness(chunkCache, testPos) < 0) {
                //Skip air and unbreakable blocks
                continue;
            }
            info = state.getBlock();
            if (info instanceof FlowingFluidBlock || info instanceof IFluidBlock) {
                //Skip liquids
                continue;
            }
            MinerFilter<?> filterFound = null;
            if (acceptedItems.containsKey(info)) {
                filterFound = acceptedItems.get(info);
            } else {
                ItemStack stack = new ItemStack(info);
                if (tile.isReplaceStack(stack)) {
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
            if (tile.inverse == (filterFound == null)) {
                set(i, testPos);
                replaceMap.put(i, filterFound);
                found++;
            }
        }

        state = State.FINISHED;
        tile.oresToMine = oresToMine;
        tile.replaceMap = replaceMap;
        chunkCache = null;
        tile.markDirty(false);
        tile.cachedToMine = found;
    }

    public void set(int i, BlockPos pos) {
        ChunkPos chunk = new ChunkPos(pos);
        oresToMine.computeIfAbsent(chunk, k -> new BitSet());
        oresToMine.get(chunk).set(i);
    }

    public void reset() {
        state = State.IDLE;
        chunkCache = null;
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
        public ITextComponent getTextComponent() {
            return langEntry.translate();
        }

        public static State byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}