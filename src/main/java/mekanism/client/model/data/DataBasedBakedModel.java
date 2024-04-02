package mekanism.client.model.data;

import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DataBasedBakedModel extends BakedModelWrapper<BakedModel> {

    private final Map<ModelProperty<Void>, BakedModel> propertyBased;

    DataBasedBakedModel(BakedModel baseModel, Map<ModelProperty<Void>, BakedModel> propertyBased) {
        super(baseModel);
        this.propertyBased = propertyBased;
    }

    private BakedModel getModelForData(ModelData data) {
        if (data.getProperties().isEmpty()) {
            return originalModel;
        }
        //TODO: Allow supporting multiple properties at once to combine a result?
        for (Map.Entry<ModelProperty<Void>, BakedModel> entry : propertyBased.entrySet()) {
            if (data.has(entry.getKey())) {
                return entry.getValue();
            }
        }
        return originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        return getModelForData(extraData).getQuads(state, side, rand, extraData, renderType);
    }

    @Override
    public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        return getModelForData(data).useAmbientOcclusion(state, data, renderType);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        return getModelForData(modelData).getModelData(level, pos, state, modelData);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return getModelForData(data).getParticleIcon(data);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return getModelForData(data).getRenderTypes(state, rand, data);
    }
}