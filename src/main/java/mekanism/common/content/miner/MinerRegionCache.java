package mekanism.common.content.miner;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

/**
 * Copy of PathNavigationRegion, but will force chunks to load as PathNavigationRegion won't do it (if anchor upgrade installed
 */
@NothingNullByDefault
public class MinerRegionCache implements CollisionGetter {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final int centerX;
    protected final int centerZ;
    protected final ChunkAccess[][] chunks;
    protected boolean allEmpty;
    protected final Level level;
    private final Supplier<Holder<Biome>> plains;

    public MinerRegionCache(ServerLevel level, BlockPos centerPos, BlockPos offsetPos, boolean hasAnchor) {
        this.level = level;
        this.plains = Suppliers.memoize(() -> level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS));
        this.centerX = SectionPos.blockToSectionCoord(centerPos.getX());
        this.centerZ = SectionPos.blockToSectionCoord(centerPos.getZ());
        int i = SectionPos.blockToSectionCoord(offsetPos.getX());
        int j = SectionPos.blockToSectionCoord(offsetPos.getZ());
        this.chunks = new ChunkAccess[i - this.centerX + 1][j - this.centerZ + 1];
        ServerChunkCache chunksource = level.getChunkSource();
        this.allEmpty = true;

        for (int x = this.centerX; x <= i; x++) {
            for (int z = this.centerZ; z <= j; z++) {
                ChunkAccess chunkAccess;
                if (hasAnchor) {
                    try {
                        chunkAccess = chunksource.getChunkFuture(x, z, ChunkStatus.FULL, true).get().orElse(null);
                    }catch (InterruptedException | ExecutionException ignored){
                        chunkAccess = null;
                    }
                } else {
                    chunkAccess = chunksource.getChunkNow(x, z);// returns null if not loaded
                }
                this.chunks[x - this.centerX][z - this.centerZ] = chunkAccess;
                if (chunkAccess == null) {
                    LOGGER.error("Failed to load chunk for searcher cache: {}, {}", x, z);
                }
            }
        }

        for (int x = SectionPos.blockToSectionCoord(centerPos.getX()); x <= SectionPos.blockToSectionCoord(offsetPos.getX()); x++) {
            for (int z = SectionPos.blockToSectionCoord(centerPos.getZ()); z <= SectionPos.blockToSectionCoord(offsetPos.getZ()); z++) {
                ChunkAccess chunkaccess = this.chunks[x - this.centerX][z - this.centerZ];
                if (chunkaccess != null && !chunkaccess.isYSpaceEmpty(centerPos.getY(), offsetPos.getY())) {
                    this.allEmpty = false;
                    return;
                }
            }
        }
    }

    private ChunkAccess getChunk(BlockPos pos) {
        return this.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    private ChunkAccess getChunk(int x, int z) {
        int i = x - this.centerX;
        int j = z - this.centerZ;
        if (i >= 0 && i < this.chunks.length && j >= 0 && j < this.chunks[i].length) {
            ChunkAccess chunkaccess = this.chunks[i][j];
            return chunkaccess != null ? chunkaccess : new EmptyLevelChunk(this.level, new ChunkPos(x, z), this.plains.get());
        } else {
            return new EmptyLevelChunk(this.level, new ChunkPos(x, z), this.plains.get());
        }
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.level.getWorldBorder();
    }

    @Override
    public BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ);
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB collisionBox) {
        return List.of();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        ChunkAccess chunkaccess = this.getChunk(pos);
        return chunkaccess.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (this.isOutsideBuildHeight(pos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            ChunkAccess chunkaccess = this.getChunk(pos);
            return chunkaccess.getBlockState(pos);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (this.isOutsideBuildHeight(pos)) {
            return Fluids.EMPTY.defaultFluidState();
        } else {
            ChunkAccess chunkaccess = this.getChunk(pos);
            return chunkaccess.getFluidState(pos);
        }
    }

    @Override
    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    public ProfilerFiller getProfiler() {
        return this.level.getProfiler();
    }
}
