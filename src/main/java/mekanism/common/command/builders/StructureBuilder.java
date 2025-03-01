package mekanism.common.command.builders;

import java.util.function.Consumer;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class StructureBuilder {

    protected final int sizeX, sizeY, sizeZ;

    protected StructureBuilder(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    protected abstract void build(Level world, BlockPos start, boolean empty);

    protected void buildFrame(Level world, BlockPos start) {
        buildPartialFrame(world, start, -1);
    }

    protected void buildPartialFrame(Level world, BlockPos start, int cutoff) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BlockState casingState = getCasing();
        for (int x = 0; x < sizeX; x++) {
            if (x > cutoff && x < sizeX - 1 - cutoff) {
                mutablePos.setWithOffset(start, x, 0, 0);
                world.setBlockAndUpdate(mutablePos, casingState);
                mutablePos.setWithOffset(start, x, sizeY - 1, 0);
                world.setBlockAndUpdate(mutablePos, casingState);
                mutablePos.setWithOffset(start, x, 0, sizeZ - 1);
                world.setBlockAndUpdate(mutablePos, casingState);
                mutablePos.setWithOffset(start, x, sizeY - 1, sizeZ - 1);
                world.setBlockAndUpdate(mutablePos, casingState);
            }
        }
        for (int y = 0; y < sizeY; y++) {
            if (y > cutoff && y < sizeY - 1 - cutoff) {
                mutablePos.setWithOffset(start, 0, y, 0);
                world.setBlockAndUpdate(mutablePos, casingState);
                mutablePos.setWithOffset(start, sizeX - 1, y, 0);
                world.setBlockAndUpdate(mutablePos, casingState);
                mutablePos.setWithOffset(start, 0, y, sizeZ - 1);
                world.setBlockAndUpdate(mutablePos, casingState);
                mutablePos.setWithOffset(start, sizeX - 1, y, sizeZ - 1);
                world.setBlockAndUpdate(mutablePos, casingState);
            }
        }
        for (int z = 0; z < sizeZ; z++) {
            if (z > cutoff && z < sizeZ - 1 - cutoff) {
                mutablePos.setWithOffset(start, 0, 0, z);
                world.setBlockAndUpdate(mutablePos, casingState);
                mutablePos.setWithOffset(start, sizeX - 1, 0, z);
                world.setBlockAndUpdate(mutablePos, casingState);
                mutablePos.setWithOffset(start, 0, sizeY - 1, z);
                world.setBlockAndUpdate(mutablePos, casingState);
                mutablePos.setWithOffset(start, sizeX - 1, sizeY - 1, z);
                world.setBlockAndUpdate(mutablePos, casingState);
            }
        }
    }

    protected void buildWalls(Level world, BlockPos start) {
        BlockPos.MutableBlockPos mutableStart = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = 1; x < sizeX - 1; x++) {
            for (int z = 1; z < sizeZ - 1; z++) {
                mutablePos.set(x, 0, z);
                mutableStart.setWithOffset(start, mutablePos);
                world.setBlockAndUpdate(mutableStart, getFloorBlock(mutablePos));
                mutablePos.set(x, sizeY - 1, z);
                mutableStart.setWithOffset(start, mutablePos);
                world.setBlockAndUpdate(mutableStart, getRoofBlock(mutablePos));
            }
        }
        for (int y = 1; y < sizeY - 1; y++) {
            for (int x = 1; x < sizeZ - 1; x++) {
                mutablePos.set(x, y, 0);
                mutableStart.setWithOffset(start, mutablePos);
                world.setBlockAndUpdate(mutableStart, getWallBlock(mutablePos));
                mutablePos.set(x, y, sizeZ - 1);
                mutableStart.setWithOffset(start, mutablePos);
                world.setBlockAndUpdate(mutableStart, getWallBlock(mutablePos));
            }
            for (int z = 1; z < sizeZ - 1; z++) {
                mutablePos.set(0, y, z);
                mutableStart.setWithOffset(start, mutablePos);
                world.setBlockAndUpdate(mutableStart, getWallBlock(mutablePos));
                mutablePos.set(sizeZ - 1, y, z);
                mutableStart.setWithOffset(start, mutablePos);
                world.setBlockAndUpdate(mutableStart, getWallBlock(mutablePos));
            }
        }
    }

    protected void buildInteriorLayers(Level world, BlockPos start, int yMin, int yMax, BlockState state) {
        for (int y = yMin; y <= yMax; y++) {
            buildInteriorLayer(world, start, y, state);
        }
    }

    protected void buildInteriorLayer(Level world, BlockPos start, int yLevel, BlockState state) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = 1; x < sizeX - 1; x++) {
            for (int z = 1; z < sizeZ - 1; z++) {
                mutablePos.setWithOffset(start, x, yLevel, z);
                world.setBlockAndUpdate(mutablePos, state);
            }
        }
    }

    protected void buildPlane(Level world, BlockPos start, int x1, int z1, int x2, int z2, int yLevel, BlockState state) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = x1; x < x2 - 1; x++) {
            for (int z = z1; z < z2 - 1; z++) {
                mutablePos.setWithOffset(start, x, yLevel, z);
                world.setBlockAndUpdate(mutablePos, state);
            }
        }
    }

    protected void buildColumn(Level world, BlockPos start, BlockPos pos, int height, BlockState state) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < height; y++) {
            mutablePos.setWithOffset(start, pos.getX(), pos.getY() + y, pos.getZ());
            world.setBlockAndUpdate(mutablePos, state);
        }
    }

    protected <T extends BlockEntity> void buildColumn(Level world, BlockPos start, BlockPos pos, int height, BlockState state, Class<T> tileClass, Consumer<T> tileConsumer) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < height; y++) {
            mutablePos.setWithOffset(start, pos.getX(), pos.getY() + y, pos.getZ());
            world.setBlockAndUpdate(mutablePos, state);
            T tile = WorldUtils.getTileEntity(tileClass, world, mutablePos);
            if (tile != null) {
                tileConsumer.accept(tile);
            }
        }
    }

    protected BlockState getWallBlock(BlockPos pos) {
        return MekanismBlocks.STRUCTURAL_GLASS.defaultState();
    }

    protected BlockState getFloorBlock(BlockPos pos) {
        return getCasing();
    }

    protected BlockState getRoofBlock(BlockPos pos) {
        return getWallBlock(pos);
    }

    protected abstract BlockState getCasing();
}
