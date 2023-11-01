package mekanism.client.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ModelDataBakedModel extends BakedModelWrapper<BakedModel> {

    private final ModelData modelData;
    private final List<BakedModel> renderPasses;

    public ModelDataBakedModel(BakedModel original, ModelData data) {
        super(original);
        this.modelData = data;
        this.renderPasses = Collections.singletonList(this);
    }

    @Override
    @Deprecated
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, modelData, null);
    }

    @Override
    @Deprecated
    public TextureAtlasSprite getParticleIcon() {
        return getParticleIcon(modelData);
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext displayContext, PoseStack mat, boolean applyLeftHandTransform) {
        // have the original model apply any perspective transforms onto the MatrixStack
        super.applyTransform(displayContext, mat, applyLeftHandTransform);
        // return this model, as we want to draw the item variant quads ourselves
        return this;
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        //Make sure our model is the one that gets rendered rather than the internal one
        return renderPasses;
    }
}