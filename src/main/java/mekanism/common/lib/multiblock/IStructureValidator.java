package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Set;
import mekanism.common.lib.math.voxel.IShape;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;

public interface IStructureValidator<T extends MultiblockData> {

    void init(World world, MultiblockManager<T> manager, Structure structure);

    boolean precheck();

    FormationResult validate(FormationProtocol<T> ctx, Long2ObjectMap<IChunk> chunkMap);

    FormationResult postcheck(T structure, Set<BlockPos> innerNodes, Long2ObjectMap<IChunk> chunkMap);

    IShape getShape();
}
