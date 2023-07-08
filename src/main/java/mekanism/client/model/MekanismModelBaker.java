package mekanism.client.model;

import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

//Vanilla Copy of ModelBakery.ModelBakerImpl but uses the material's sprite lookup for getting textures from materials
@NothingNullByDefault
public class MekanismModelBaker implements ModelBaker {

    private final Function<Material, TextureAtlasSprite> modelTextureGetter;
    private final ModelBakery bakery;

    public MekanismModelBaker() {
        this.bakery = Minecraft.getInstance().getModelManager().getModelBakery();
        this.modelTextureGetter = Material::sprite;
    }

    @Override
    public UnbakedModel getModel(ResourceLocation name) {
        return bakery.getModel(name);
    }

    @Nullable
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public BakedModel bake(ResourceLocation name, ModelState state) {
        return bake(name, state, getModelTextureGetter());
    }

    @Nullable
    @Override
    public BakedModel bake(ResourceLocation name, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
        ModelBakery.BakedCacheKey cacheKey = new ModelBakery.BakedCacheKey(name, state.getRotation(), state.isUvLocked());
        BakedModel bakedModel = bakery.bakedCache.get(cacheKey);
        if (bakedModel == null) {
            UnbakedModel unbakedModel = getModel(name);
            if (unbakedModel instanceof BlockModel blockModel && blockModel.getRootModel() == ModelBakery.GENERATION_MARKER) {
                return ModelBakery.ITEM_MODEL_GENERATOR.generateBlockModel(sprites, blockModel).bake(this, blockModel, sprites, state, name, false);
            }
            bakedModel = unbakedModel.bake(this, sprites, state, name);
            bakery.bakedCache.put(cacheKey, bakedModel);
        }
        return bakedModel;
    }

    @Override
    public Function<Material, TextureAtlasSprite> getModelTextureGetter() {
        return modelTextureGetter;
    }
}