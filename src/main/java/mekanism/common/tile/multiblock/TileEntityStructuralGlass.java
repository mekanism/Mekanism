package mekanism.common.tile.multiblock;

import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Locale;
import mekanism.common.lib.multiblock.MultiblockType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityStructuralGlass extends TileEntityStructuralMultiblock {

    private static final Object2BooleanMap<Identifier> reactorTypeCache = new Object2BooleanOpenHashMap<>();
    private static final Object2BooleanFunction<Identifier> isReactor = key -> ((Identifier) key).getPath().toLowerCase(Locale.ROOT).contains("reactor");

    private static boolean isReactor(MultiblockType<?> multiblockType) {
        return reactorTypeCache.computeIfAbsent(multiblockType.id(), isReactor);
    }

    public TileEntityStructuralGlass(BlockPos pos, BlockState state) {
        super(MekanismBlocks.STRUCTURAL_GLASS, pos, state);
    }

    @Override
    public boolean canInterface(MultiblockType<?> multiblockType) {
        return !isReactor(multiblockType);
    }
}
