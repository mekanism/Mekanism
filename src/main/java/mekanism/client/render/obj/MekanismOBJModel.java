package mekanism.client.render.obj;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;

//TODO: Should this be renamed to be more transmitter specific
public class MekanismOBJModel extends OBJModel {

    private ResourceLocation location;

    public MekanismOBJModel(MaterialLibrary matLib, ResourceLocation modelLocation) {
        super(matLib, modelLocation);
        location = modelLocation;
    }

    @Nonnull
    @Override
    public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IBakedModel preBaked = super.bake(state, format, bakedTextureGetter);
        return new TransmitterModel(preBaked, this, state, format, TransmitterModel.getTexturesForOBJModel(preBaked), null);
    }

    @Nonnull
    @Override
    public IModel process(@Nonnull ImmutableMap<String, String> customData) {
        return new MekanismOBJModel(getMatLib(), location);
    }

    @Nonnull
    @Override
    public IModel retexture(@Nonnull ImmutableMap<String, String> textures) {
        return new MekanismOBJModel(getMatLib().makeLibWithReplacements(textures), location);
    }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getTextures() {
        return super.getTextures().stream().filter(r -> !r.getPath().startsWith("#")).collect(Collectors.toList());
    }
}