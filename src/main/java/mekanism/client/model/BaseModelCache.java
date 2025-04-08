package mekanism.client.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import net.neoforged.neoforge.client.model.obj.ObjModel;
import net.neoforged.neoforge.client.model.obj.ObjModel.ModelSettings;
import org.jetbrains.annotations.Nullable;

public class BaseModelCache {

    private final Map<ResourceLocation, MekanismModelData> modelMap = new Object2ObjectOpenHashMap<>();

    private final String modid;

    protected BaseModelCache(String modid) {
        this.modid = modid;
    }

    private ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(modid, path);
    }

    public void onBake(BakingCompleted evt) {
        for (MekanismModelData m : modelMap.values()) {
            m.reload(evt);
        }
    }

    public void setup(RegisterAdditional event) {
        for (MekanismModelData mekanismModelData : modelMap.values()) {
            mekanismModelData.setup(event);
        }
    }

    protected OBJModelData registerOBJ(String path) {
        return registerOBJ(rl(path));
    }

    protected OBJModelData registerOBJ(ResourceLocation rl) {
        return register(rl, OBJModelData::new);
    }

    protected JSONModelData registerJSON(String path) {
        return registerJSON(rl(path));
    }

    protected JSONModelData registerJSON(ResourceLocation rl) {
        return register(rl, JSONModelData::new);
    }

    protected JSONModelData registerJSONAndBake(ResourceLocation rl) {
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        ModelBakery modelBakery = modelManager.getModelBakery();
        ModelResourceLocation mrl = ModelResourceLocation.standalone(rl);
        ModelBaker baker = modelBakery.new ModelBakerImpl(
              (modelLoc, material) -> material.sprite(),
              mrl
        );
        //Register the model
        JSONModelData data = registerJSON(rl);
        //Manually run the JsonModelData#reload logic
        data.bakedModel = baker.bake(rl, BlockModelRotation.X0_Y0, Material::sprite);
        if (getUnbakedModel(modelBakery, baker, mrl) instanceof BlockModel blockModel) {
            data.model = blockModel.customData.getCustomGeometry();
        }
        return data;
    }

    protected <DATA extends MekanismModelData> DATA register(ResourceLocation rl, Function<ResourceLocation, DATA> creator) {
        DATA data = creator.apply(rl);
        modelMap.put(rl, data);
        return data;
    }

    private static UnbakedModel getUnbakedModel(ModelBakery modelBakery, ModelBaker baker, ModelResourceLocation rl) {
        UnbakedModel unbakedModel = baker.getTopLevelModel(rl);
        if (unbakedModel == null) {
            return modelBakery.getModel(rl.id());
        }
        return unbakedModel;
    }

    public static BakedModel getBakedModel(BakingCompleted evt, ModelResourceLocation rl) {
        BakedModel bakedModel = evt.getModels().get(rl);
        if (bakedModel == null) {
            Mekanism.logger.error("Baked model doesn't exist: {}", rl);
            return evt.getModelManager().getMissingModel();
        }
        return bakedModel;
    }

    public static class MekanismModelData {

        protected IUnbakedGeometry<?> model;

        protected final ResourceLocation rl;
        protected final ModelResourceLocation mrl;
        private final Map<IGeometryBakingContext, BakedModel> bakedMap = new Object2ObjectOpenHashMap<>();

        protected MekanismModelData(ResourceLocation rl) {
            this.rl = rl;
            this.mrl = ModelResourceLocation.standalone(rl);
        }

        protected void reload(BakingCompleted evt) {
            bakedMap.clear();
        }

        protected void setup(RegisterAdditional event) {
        }

        public BakedModel bake(IGeometryBakingContext config) {
            BakedModel bakedModel = bakedMap.get(config);
            if (bakedModel == null) {
                ModelBaker baker = Minecraft.getInstance().getModelManager().getModelBakery().new ModelBakerImpl(
                      (modelLoc, material) -> material.sprite(),
                      mrl
                );
                bakedModel = model.bake(config, baker, Material::sprite, BlockModelRotation.X0_Y0, ItemOverrides.EMPTY);
                bakedMap.put(config, bakedModel);
            }
            return bakedModel;
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
            bakedModel = BaseModelCache.getBakedModel(evt, mrl);
            ModelBaker baker = evt.getModelBakery().new ModelBakerImpl(
                  (modelLoc, material) -> material.sprite(),
                  mrl
            );
            if (getUnbakedModel(evt.getModelBakery(), baker, mrl) instanceof BlockModel blockModel) {
                model = blockModel.customData.getCustomGeometry();
            }
        }

        @Override
        protected void setup(RegisterAdditional event) {
            event.register(mrl);
        }

        public List<BakedQuad> getQuads(RandomSource random) {
            //TODO: Decide if this should just redirect to the other get quads method (some impls might be different depending on if it gets data and render type vs not)
            return getBakedModel().getQuads(null, null, random);
        }

        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
            return getBakedModel().getQuads(state, side, rand, data, renderType);
        }

        public BakedModel getBakedModel() {
            return bakedModel;
        }
    }
}
