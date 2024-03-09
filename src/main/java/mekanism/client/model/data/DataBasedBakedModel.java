package mekanism.client.model.data;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DataBasedBakedModel implements IDynamicBakedModel {

    private final BakedModel baseModel;
    private final Map<ModelProperty<Void>, BakedModel> propertyBased;

    DataBasedBakedModel(BakedModel baseModel, Map<ModelProperty<Void>, BakedModel> propertyBased) {
        this.baseModel = baseModel;
        this.propertyBased = propertyBased;
    }

    private BakedModel getModelForNoData() {
        return baseModel;
    }

    private BakedModel getModelForData(ModelData data) {
        if (data.getProperties().isEmpty()) {
            return getModelForNoData();
        }
        //TODO: Allow supporting multiple properties at once to combine a result?
        for (Map.Entry<ModelProperty<Void>, BakedModel> entry : propertyBased.entrySet()) {
            if (data.has(entry.getKey())) {
                return entry.getValue();
            }
        }
        return getModelForNoData();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        return getModelForData(extraData).getQuads(state, side, rand, extraData, renderType);
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state) {
        return getModelForNoData().useAmbientOcclusion(state);
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state, RenderType renderType) {
        return getModelForNoData().useAmbientOcclusion(state, renderType);
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return getModelForNoData().applyTransform(transformType, poseStack, applyLeftHandTransform);
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

    @Override
    public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
        return getModelForNoData().getRenderTypes(stack, fabulous);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return getModelForNoData().getRenderPasses(stack, fabulous);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return getModelForNoData().useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return getModelForNoData().isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return getModelForNoData().usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return getModelForNoData().isCustomRenderer();
    }

    @Override
    @Deprecated
    public TextureAtlasSprite getParticleIcon() {
        return getModelForNoData().getParticleIcon();
    }

    @Override
    @Deprecated
    public ItemTransforms getTransforms() {
        return getModelForNoData().getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return getModelForNoData().getOverrides();
    }
}