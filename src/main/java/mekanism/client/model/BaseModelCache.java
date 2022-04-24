package mekanism.client.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Function;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
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

    public static BakedModel getBakedModel(ModelBakeEvent evt, ResourceLocation rl) {
        BakedModel bakedModel = evt.getModelRegistry().get(rl);
        if (bakedModel == null) {
            Mekanism.logger.error("Baked model doesn't exist: {}", rl.toString());
            return evt.getModelManager().getMissingModel();
        }
        return bakedModel;
    }

    public static class ModelData {

        protected IModelGeometry<?> model;

        protected final ResourceLocation rl;
        private final Map<IModelConfiguration, BakedModel> bakedMap = new Object2ObjectOpenHashMap<>();

        protected ModelData(ResourceLocation rl) {
            this.rl = rl;
        }

        protected void reload(ModelBakeEvent evt) {
            bakedMap.clear();
        }

        protected void setup() {
        }

        public BakedModel bake(IModelConfiguration config) {
            return bakedMap.computeIfAbsent(config, c -> model.bake(c, ForgeModelBakery.instance(), ForgeModelBakery.defaultTextureGetter(), BlockModelRotation.X0_Y0, ItemOverrides.EMPTY, rl));
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
            model = OBJLoader.INSTANCE.loadModel(new ModelSettings(rl, true, useDiffuseLighting(), true, true, null));
        }

        @Override
        public OBJModel getModel() {
            return (OBJModel) super.getModel();
        }

        protected boolean useDiffuseLighting() {
            return true;
        }
    }

    public static class JSONModelData extends ModelData {

        private BakedModel bakedModel;

        private JSONModelData(ResourceLocation rl) {
            super(rl);
        }

        @Override
        protected void reload(ModelBakeEvent evt) {
            super.reload(evt);
            bakedModel = BaseModelCache.getBakedModel(evt, rl);
            UnbakedModel unbaked = evt.getModelLoader().getModel(rl);
            if (unbaked instanceof BlockModel blockModel) {
                model = blockModel.customData.getCustomGeometry();
            }
        }

        @Override
        protected void setup() {
            ForgeModelBakery.addSpecialModel(rl);
        }

        public BakedModel getBakedModel() {
            return bakedModel;
        }
    }
}
