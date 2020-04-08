package mekanism.generators.common.content.fission;

import java.util.List;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class FissionReactorUpdateProtocol extends UpdateProtocol<SynchronizedFissionReactorData> {

    public FissionReactorUpdateProtocol(TileEntityFissionReactorCasing tile) {
        super(tile);
    }

    @Override
    protected boolean isValidFrame(int x, int y, int z) {
        return BlockTypeTile.is(pointer.getWorld().getBlockState(new BlockPos(x, y, z)).getBlock(), GeneratorsBlockTypes.FISSION_REACTOR_CASING);
    }

    @Override
    protected MultiblockCache<SynchronizedFissionReactorData> getNewCache() {
        return new FissionReactorCache();
    }

    @Override
    protected SynchronizedFissionReactorData getNewStructure() {
        return new SynchronizedFissionReactorData((TileEntityFissionReactorCasing) pointer);
    }

    @Override
    protected MultiblockManager<SynchronizedFissionReactorData> getManager() {
        return MekanismGenerators.fissionReactorManager;
    }

    @Override
    protected void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<SynchronizedFissionReactorData> cache, MultiblockCache<SynchronizedFissionReactorData> merge) {

    }
}
