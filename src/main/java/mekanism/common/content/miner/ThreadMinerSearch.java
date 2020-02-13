package mekanism.common.content.miner;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.BitSet;
import java.util.Map;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.HashList;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Region;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fluids.IFluidBlock;

public class ThreadMinerSearch extends Thread {

    private TileEntityDigitalMiner tile;

    public State state = State.IDLE;

    private Map<Chunk3D, BitSet> oresToMine = new Object2ObjectOpenHashMap<>();
    private Int2ObjectMap<MinerFilter<?>> replaceMap = new Int2ObjectOpenHashMap<>();
    private Map<Block, MinerFilter<?>> acceptedItems = new Object2ObjectOpenHashMap<>();
    private DimensionType dimensionType;
    private Region chunkCache;

    public int found = 0;

    public ThreadMinerSearch(TileEntityDigitalMiner tile) {
        this.tile = tile;
    }

    public void setChunkCache(Region cache, DimensionType dimensionType) {
        this.chunkCache = cache;
        this.dimensionType = dimensionType;
    }

    @Override
    public void run() {
        state = State.SEARCHING;
        HashList<MinerFilter<?>> filters = tile.getFilters();
        if (!tile.inverse && filters.isEmpty()) {
            state = State.FINISHED;
            return;
        }
        Coord4D coord = tile.getStartingCoord();
        int diameter = tile.getDiameter();
        int size = tile.getTotalSize();
        Block info;
        BlockPos minerPos = tile.getPos();

        for (int i = 0; i < size; i++) {
            if (tile.isRemoved()) {
                //Make sure the miner is still valid and something hasn't gone wrong
                return;
            }
            int x = coord.x + i % diameter;
            int z = coord.z + (i / diameter) % diameter;
            int y = coord.y + (i / diameter / diameter);
            if (minerPos.getX() == x && minerPos.getY() == y && minerPos.getZ() == z) {
                //Skip the miner itself
                continue;
            }

            BlockPos testPos = new BlockPos(x, y, z);
            //TODO: verify that we don't need to check if the block is loaded. Probably got removed given a region is a cache
            if (/*!chunkCache.isBlockLoaded(testPos) || */MekanismUtils.getTileEntity(TileEntityBoundingBlock.class, chunkCache, testPos) != null) {
                //If it is not loaded or it is a bounding block skip it
                continue;
            }

            BlockState state = chunkCache.getBlockState(testPos);
            info = state.getBlock();

            if (info instanceof FlowingFluidBlock || info instanceof IFluidBlock || info.isAir(state, chunkCache, testPos)) {
                //Skip air and liquids
                continue;
            }

            if (state.getBlockHardness(chunkCache, testPos) >= 0) {
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
                    set(i, new Coord4D(x, y, z, dimensionType));
                    replaceMap.put(i, filterFound);
                    found++;
                }
            }
        }

        state = State.FINISHED;
        tile.oresToMine = oresToMine;
        tile.replaceMap = replaceMap;
        chunkCache = null;
        dimensionType = null;
        MekanismUtils.saveChunk(tile);
    }

    public void set(int i, Coord4D location) {
        Chunk3D chunk = new Chunk3D(location);
        oresToMine.computeIfAbsent(chunk, k -> new BitSet());
        oresToMine.get(chunk).set(i);
    }

    public void reset() {
        state = State.IDLE;
        chunkCache = null;
        dimensionType = null;
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
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return MODES[Math.floorMod(index, MODES.length)];
        }
    }
}