package mekanism.client.model.baked;

import java.util.List;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadTransformation.TextureFilteredTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.client.model.data.IModelData;

public class QIORedstoneAdapterBakedModel extends ExtensionBakedModel<Boolean> {

    private final QuadTransformation TORCH_TRANSFORM = QuadTransformation.list(QuadTransformation.fullbright, QuadTransformation.texture(MekanismRenderer.redstoneTorch));

    public QIORedstoneAdapterBakedModel(IBakedModel original) {
        super(original);
    }

    @Override
    public List<BakedQuad> createQuads(QuadsKey<Boolean> key) {
        List<BakedQuad> quads = key.getQuads();
        if (key.getData()) {
            quads = QuadUtils.transformBakedQuads(quads, TextureFilteredTransformation.of(TORCH_TRANSFORM, s -> s.getPath().contains("redstone")));
        }
        return quads;
    }

    @Override
    public QuadsKey<Boolean> createKey(QuadsKey<Boolean> key, IModelData data) {
        Boolean powering = data.getData(TileEntityQIORedstoneAdapter.POWERING_PROPERTY);
        if (powering == null) {
            return null;
        }
        return key.data(powering, Boolean.hashCode(powering), Boolean::equals);
    }

    @Override
    protected QIORedstoneAdapterBakedModel wrapModel(IBakedModel model) {
        return new QIORedstoneAdapterBakedModel(model);
    }
}
