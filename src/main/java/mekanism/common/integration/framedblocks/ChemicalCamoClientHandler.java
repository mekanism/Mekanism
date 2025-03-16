package mekanism.common.integration.framedblocks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.api.chemical.Chemical;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import xfacthd.framedblocks.api.camo.CamoClientHandler;
import xfacthd.framedblocks.api.model.util.ModelUtils;

final class ChemicalCamoClientHandler extends CamoClientHandler<ChemicalCamoContent> {

    static final CamoClientHandler<ChemicalCamoContent> INSTANCE = new ChemicalCamoClientHandler();
    private static final Map<Chemical, BakedModel> CHEMICAL_MODEL_CACHE = new ConcurrentHashMap<>();

    private ChemicalCamoClientHandler() { }

    @Override
    public ChunkRenderTypeSet getRenderTypes(ChemicalCamoContent camo, RandomSource random, ModelData data) {
        return ModelUtils.TRANSLUCENT;
    }

    @Override
    public BakedModel getOrCreateModel(ChemicalCamoContent camo) {
        return CHEMICAL_MODEL_CACHE.computeIfAbsent(camo.getChemicalHolder().value(), ChemicalModel::create);
    }

    @Override
    public Particle makeHitDestroyParticle(ClientLevel level, double x, double y, double z, double sx, double sy, double sz, ChemicalCamoContent camo, BlockPos pos) {
        return new ChemicalSpriteParticle(level, x, y, z, sx, sy, sz, camo.getChemicalHolder());
    }

    static void clearModelCache() {
        CHEMICAL_MODEL_CACHE.clear();
    }
}
