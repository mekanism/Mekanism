package mekanism.client.model.robit;

import com.google.common.collect.Multimap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import mekanism.client.model.baked.MekanismModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;

public class RobitModel extends MekanismModel {

    private RobitModel(Multimap<String, MekanismModelPart> list) {
        super(list);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
          ItemOverrides overrides, ResourceLocation modelLocation) {
        return new RobitBakedModel(super.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation));
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter,
          Set<Pair<String, String>> missingTextureErrors) {
        Collection<Material> textures = super.getMaterials(owner, modelGetter, missingTextureErrors);
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

        @NotNull
        @Override
        public RobitModel read(@NotNull JsonObject modelContents, @NotNull JsonDeserializationContext ctx) {
            return new RobitModel(readElements(ctx, modelContents));
        }
    }
}
