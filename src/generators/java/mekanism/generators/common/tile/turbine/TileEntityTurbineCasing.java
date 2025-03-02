package mekanism.generators.common.tile.turbine;

import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityTurbineCasing extends TileEntityMultiblock<TurbineMultiblockData> implements IHasGasMode {

    public TileEntityTurbineCasing(BlockPos pos, BlockState state) {
        this(GeneratorsBlocks.TURBINE_CASING, pos, state);
    }

    public TileEntityTurbineCasing(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public void nextMode(int tank) {
        if (tank == 0) {
            TurbineMultiblockData multiblock = getMultiblock();
            multiblock.setDumpMode(multiblock.dumpMode.getNext());
        }
    }

    @NotNull
    @Override
    public TurbineMultiblockData createMultiblock() {
        return new TurbineMultiblockData(this);
    }

    @Override
    public MultiblockManager<TurbineMultiblockData> getManager() {
        return MekanismGenerators.turbineManager;
    }
}