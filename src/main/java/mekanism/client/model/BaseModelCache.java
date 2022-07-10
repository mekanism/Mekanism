package mekanism.client.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Function;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjModel;
import net.minecraftforge.client.model.obj.ObjModel.ModelSettings;

public class BaseModelCache {

    private final Map<ResourceLocation, MekanismModelData> modelMap = new Object2ObjectOpenHashMap<>();

    public void onBake(BakingCompleted evt) {
        modelMap.values().forEach(m -> m.reload(evt));
    }

    public void setup(RegisterAdditional event) {
        modelMap.values().forEach(mekanismModelData -> mekanismModelData.setup(event));
    }

    protected OBJModelData registerOBJ(ResourceLocation rl) {
        return register(rl, OBJModelData::new);
    }

    protected JSONModelData registerJSON(ResourceLocation rl) {
        return register(rl, JSONModelData::new);
    }

    protected <DATA extends MekanismModelData> DATA register(ResourceLocation rl, Function<ResourceLocation, DATA> creator) {
        DATA data = creator.apply(rl);
        modelMap.put(rl, data);
        return data;
    }

    public static BakedModel getBakedModel(BakingCompleted evt, ResourceLocation rl) {
        BakedModel bakedModel = evt.getModels().get(rl);
        if (bakedModel == null) {
            Mekanism.logger.error("Baked model doesn't exist: {}", rl.toString());
            return evt.getModelManager().getMissingModel();
        }
        return bakedModel;
    }

    public static class MekanismModelData {

        protected IUnbakedGeometry<?> model;

        protected final ResourceLocation rl;
        private final Map<IGeometryBakingContext, BakedModel> bakedMap = new Object2ObjectOpenHashMap<>();

        protected MekanismModelData(ResourceLocation rl) {
            this.rl = rl;
        }

        protected void reload(BakingCompleted evt) {
            bakedMap.clear();
        }

        protected void setup(RegisterAdditional event) {
        }

        public BakedModel bake(IGeometryBakingContext config) {
            return bakedMap.computeIfAbsent(config, c -> model.bake(c, Minecraft.getInstance().getModelManager().getModelBakery(), Material::sprite,
                  BlockModelRotation.X0_Y0, ItemOverrides.EMPTY, rl));
        }

        public IUnbakedGeometry<?> getModel() {
            return model;
        }
    }

    public static class OBJModelData extends MekanismModelData {

        protected OBJModelData(ResourceLocation rl) {
            super(rl);
        }

        @Override
        protected void reload(BakingCompleted evt) {
            super.reload(evt);
            model = ObjLoader.INSTANCE.loadModel(new ModelSettings(rl, true, useDiffuseLighting(), true, true, null));
        }

        @Override
        public ObjModel getModel() {
            return (ObjModel) super.getModel();
        }

        protected boolean useDiffuseLighting() {
            return true;
        }
    }

    public static class JSONModelData extends MekanismModelData {

        private BakedModel bakedModel;

        private JSONModelData(ResourceLocation rl) {
            super(rl);
        }

        @Override
        protected void reload(BakingCompleted evt) {
            super.reload(evt);
            bakedModel = BaseModelCache.getBakedModel(evt, rl);
            UnbakedModel unbaked = evt.getModelBakery().getModel(rl);
            if (unbaked instanceof BlockModel blockModel) {
                model = blockModel.customData.getCustomGeometry();
            }
        }

        @Override
        protected void setup(RegisterAdditional event) {
            event.register(rl);
        }

        public BakedModel getBakedModel() {
            return bakedModel;
        }
    }
}
