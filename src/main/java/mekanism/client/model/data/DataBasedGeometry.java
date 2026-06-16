package mekanism.client.model.data;

//TODO - 26.2 models
/*import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jspecify.annotations.Nullable;

public class DataBasedGeometry implements IUnbakedGeometry<DataBasedGeometry> {

    private final Map<ModelProperty<Void>, UnbakedModel> propertyBasedUnbakedModels = new HashMap<>();
    private final Map<ModelProperty<Void>, Identifier> propertyBasedModels;
    private final Identifier noData;
    @Nullable
    private UnbakedModel unbakedModel;

    DataBasedGeometry(Identifier noData, Map<ModelProperty<Void>, Identifier> propertyBasedModels) {
        this.noData = noData;
        this.propertyBasedModels = propertyBasedModels;
    }

    //TODO - 1.20.4: Should/can we somehow override UnbakedModel#getDependencies??
    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        Objects.requireNonNull(unbakedModel, "Unbaked model should not be null");
        BakedModel bakedModel;
        if (unbakedModel instanceof BlockModel unbakedBlock) {
            bakedModel = unbakedBlock.bake(baker, unbakedBlock, spriteGetter, modelState, context.useBlockLight());
        } else {
            bakedModel = unbakedModel.bake(baker, spriteGetter, modelState);
        }
        Objects.requireNonNull(bakedModel, "Baked model should not be null");
        Map<ModelProperty<Void>, BakedModel> propertyBasedBakedModels = new HashMap<>(propertyBasedUnbakedModels.size());
        for (Map.Entry<ModelProperty<Void>, UnbakedModel> entry : propertyBasedUnbakedModels.entrySet()) {
            BakedModel baked;
            if (entry.getValue() instanceof BlockModel unbakedBlock) {
                baked = unbakedBlock.bake(baker, unbakedBlock, spriteGetter, modelState, context.useBlockLight());
            } else {
                baked = entry.getValue().bake(baker, spriteGetter, modelState);
            }
            Objects.requireNonNull(baked, "Baked model should not be null");
            propertyBasedBakedModels.put(entry.getKey(), baked);
        }
        return new DataBasedBakedModel(bakedModel, Collections.unmodifiableMap(propertyBasedBakedModels));
    }

    @Override
    public void resolveParents(Function<Identifier, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        unbakedModel = resolve(modelGetter, context, noData);
        for (Map.Entry<ModelProperty<Void>, Identifier> entry : propertyBasedModels.entrySet()) {
            propertyBasedUnbakedModels.put(entry.getKey(), resolve(modelGetter, context, entry.getValue()));
        }
    }

    private UnbakedModel resolve(Function<Identifier, UnbakedModel> modelGetter, IGeometryBakingContext context, Identifier modelName) {
        UnbakedModel model = modelGetter.apply(modelName);
        if (model == null) {
            Mekanism.logger.warn("Could not find '{}' while loading model '{}'", modelName, context.getModelName());
            model = modelGetter.apply(ModelBakery.MISSING_MODEL_LOCATION);
        } else {
            model.resolveParents(modelGetter);
        }
        return model;
    }
}*/