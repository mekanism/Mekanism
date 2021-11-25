package mekanism.client.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Function;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel.ModelSettings;

public class BaseModelCache {

    private final Map<ResourceLocation, ModelData> modelMap = new Object2ObjectOpenHashMap<>();

    public void onBake(ModelBakeEvent evt) {
        modelMap.values().forEach(m -> m.reload(evt));
    }

    public void setup() {
        modelMap.values().forEach(ModelData::setup);
    }

    protected OBJModelData registerOBJ(ResourceLocation rl) {
        return register(rl, OBJModelData::new);
    }

    protected JSONModelData registerJSON(ResourceLocation rl) {
        return register(rl, JSONModelData::new);
    }

    protected <DATA extends ModelData> DATA register(ResourceLocation rl, Function<ResourceLocation, DATA> creator) {
        DATA data = creator.apply(rl);
        modelMap.put(rl, data);
        return data;
    }

    public static IBakedModel getBakedModel(ModelBakeEvent evt, ResourceLocation rl) {
        IBakedModel bakedModel = evt.getModelRegistry().get(rl);
        if (bakedModel == null) {
            Mekanism.logger.error("Baked model doesn't exist: {}", rl.toString());
            return evt.getModelManager().getMissingModel();
        }
        return bakedModel;
    }

    public static class ModelData {

        protected IModelGeometry<?> model;

        protected final ResourceLocation rl;
        private final Map<IModelConfiguration, IBakedModel> bakedMap = new Object2ObjectOpenHashMap<>();

        protected ModelData(ResourceLocation rl) {
            this.rl = rl;
        }

        protected void reload(ModelBakeEvent evt) {
            bakedMap.clear();
        }

        protected void setup() {
        }

        public IBakedModel bake(IModelConfiguration config) {
            return bakedMap.computeIfAbsent(config, c -> model.bake(c, ModelLoader.instance(), ModelLoader.defaultTextureGetter(), SimpleModelTransform.IDENTITY, ItemOverrideList.EMPTY, rl));
        }

        public IModelGeometry<?> getModel() {
            return model;
        }
    }

    public static class OBJModelData extends ModelData {

        protected OBJModelData(ResourceLocation rl) {
            super(rl);
        }

        @Override
        protected void reload(ModelBakeEvent evt) {
            super.reload(evt);
            model = OBJLoader.INSTANCE.loadModel(new ModelSettings(rl, true, true, true, true, null));
        }
    }

    public static class JSONModelData extends ModelData {

        private IBakedModel bakedModel;

        private JSONModelData(ResourceLocation rl) {
            super(rl);
        }

        @Override
        protected void reload(ModelBakeEvent evt) {
            super.reload(evt);
            bakedModel = BaseModelCache.getBakedModel(evt, rl);
            IUnbakedModel unbaked = evt.getModelLoader().getModel(rl);
            if (unbaked instanceof BlockModel) {
                model = ((BlockModel) unbaked).customData.getCustomGeometry();
            }
        }

        @Override
        protected void setup() {
            ModelLoader.addSpecialModel(rl);
        }

        public IBakedModel getBakedModel() {
            return bakedModel;
        }
    }
}
