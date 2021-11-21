package mekanism.client.model.robit;

import com.google.common.collect.Multimap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.client.model.baked.MekanismModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

public class RobitModel extends MekanismModel {

    private RobitModel(Multimap<String, BlockPartWrapper> list) {
        super(list);
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
          ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new RobitBakedModel(super.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation));
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter,
          Set<Pair<String, String>> missingTextureErrors) {
        Collection<RenderMaterial> textures = super.getTextures(owner, modelGetter, missingTextureErrors);
        //Remove any missing errors where the texture in the file was #robit
        missingTextureErrors.removeIf(p -> p.getFirst().equals("#robit"));
        return textures;
    }

    /**
     * Mekanism model loader that gets automatically wrapped into a robit baked model
     */
    public static class Loader extends MekanismModel.Loader {

        public static final Loader INSTANCE = new Loader();

        private Loader() {
        }

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
        }

        @Nonnull
        @Override
        public RobitModel read(@Nonnull JsonDeserializationContext ctx, @Nonnull JsonObject modelContents) {
            return new RobitModel(readElements(ctx, modelContents));
        }
    }
}
