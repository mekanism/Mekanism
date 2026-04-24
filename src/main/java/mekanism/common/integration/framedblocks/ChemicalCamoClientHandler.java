package mekanism.common.integration.framedblocks;

import io.github.xfacthd.framedblocks.api.camo.CamoContentClientHandler;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.api.annotations.MethodsAreNotNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.Chemical;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
@MethodsAreNotNullByDefault
final class ChemicalCamoClientHandler extends CamoContentClientHandler<ChemicalCamoContent> {

    static final CamoContentClientHandler<ChemicalCamoContent> INSTANCE = new ChemicalCamoClientHandler();
    private static final Map<Chemical, BlockStateModel> CHEMICAL_MODEL_CACHE = new ConcurrentHashMap<>();

    private ChemicalCamoClientHandler() { }

    //@Override
    //public ChunkRenderTypeSet getRenderTypes(ChemicalCamoContent camo, RandomSource random, ModelData data) {
    //    return ModelUtils.TRANSLUCENT;
    //}

    @Override//TODO - 26.1 models
    public BlockStateModel getOrCreateModel(ChemicalCamoContent camo) {
        throw new UnsupportedOperationException("TODO");
        //return CHEMICAL_MODEL_CACHE.computeIfAbsent(camo.getChemicalHolder().value(), ChemicalModel::create);
    }

    @Override
    public Particle makeHitDestroyParticle(ClientLevel level, double x, double y, double z, double sx, double sy, double sz, ChemicalCamoContent camo, BlockPos pos) {
        return new ChemicalSpriteParticle(level, x, y, z, sx, sy, sz, camo.getChemicalHolder());
    }

    @Override
    public int getTintCount(ChemicalCamoContent camo) {
        return 1;
    }

    @Override
    public void collectTintValues(ChemicalCamoContent camo, BlockAndTintGetter level, BlockPos pos, IntList tintList) {
        tintList.add(camo.getChemicalHolder().value().getTint());
    }

    @Override
    public void collectTintValues(ChemicalCamoContent camo, ItemStack stack, IntList tintList) {
        tintList.add(camo.getChemicalHolder().value().getTint());
    }

    @Override
    public int getParticleTintValue(ChemicalCamoContent camo) {
        return camo.getChemicalHolder().value().getTint();
    }

    static void clearModelCache() {
        CHEMICAL_MODEL_CACHE.clear();
    }
}
