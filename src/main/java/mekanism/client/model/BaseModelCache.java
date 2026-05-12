package mekanism.client.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

public class BaseModelCache {

    private final Map<Identifier, MekanismModelData> modelMap = new Object2ObjectOpenHashMap<>();

    private final String modid;

    protected BaseModelCache(String modid) {
        this.modid = modid;
    }

    private Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(modid, path);
    }

    public void onBake(ModelEvent.BakingCompleted evt) {
        for (MekanismModelData m : modelMap.values()) {
            m.reload(evt);
        }
    }

    public void setup(ModelEvent.RegisterStandalone event) {
        for (MekanismModelData mekanismModelData : modelMap.values()) {
            mekanismModelData.setup(event);
        }
    }

    protected OBJModelData registerOBJ(String path) {
        return registerOBJ(rl(path));
    }

    protected OBJModelData registerOBJ(Identifier rl) {
        return register(rl, OBJModelData::new);
    }

    protected JSONModelData registerJSON(String path) {
        return registerJSON(rl(path));
    }

    protected JSONModelData registerJSON(Identifier rl) {
        return register(rl, JSONModelData::new);
    }

    /*protected JSONModelData registerJSONAndBake(Identifier rl) {
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        ModelBakery modelBakery = modelManager.getModelBakery();
        StandaloneModelKey<?> mrl = ModelResourceLocation.standalone(rl);
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
    }*/

    protected <DATA extends MekanismModelData> DATA register(Identifier rl, Function<Identifier, DATA> creator) {
        DATA data = creator.apply(rl);
        modelMap.put(rl, data);
        return data;
    }

    /*private static UnbakedModel getUnbakedModel(ModelBakery modelBakery, ModelBaker baker, ModelResourceLocation rl) {
        UnbakedModel unbakedModel = baker.getTopLevelModel(rl);
        if (unbakedModel == null) {
            return modelBakery.getModel(rl.id());
        }
        return unbakedModel;
    }*/

    /*public static <MODEL> MODEL getBakedModel(ModelEvent.BakingCompleted evt, StandaloneModelKey<MODEL> rl) {
        MODEL model = evt.getBakingResult().standaloneModels().get(rl);
        if (model == null) {
            Mekanism.logger.error("Baked model doesn't exist: {}", rl);
            return evt.getModelManager().getMissingBlockStateModel();
        }
        return model;
    }*/

    public static class MekanismModelData implements ModelDebugName {

        //protected IUnbakedGeometry<?> model;

        protected final Identifier rl;
        //protected final StandaloneModelKey<T> mrl;
        //private final Map<IGeometryBakingContext, BakedModel> bakedMap = new Object2ObjectOpenHashMap<>();

        protected MekanismModelData(Identifier rl) {
            this.rl = rl;
            //this.mrl = new StandaloneModelKey<>(this);//ModelResourceLocation.standalone(rl);
        }

        @Override
        public String debugName() {
            return rl.toString();
        }

        protected void reload(ModelEvent.BakingCompleted evt) {
            //bakedMap.clear();
        }

        protected void setup(ModelEvent.RegisterStandalone event) {
        }

        /*public BakedModel bake(IGeometryBakingContext config) {
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
        }*/
    }

    public static class OBJModelData extends MekanismModelData {

        protected OBJModelData(Identifier rl) {
            super(rl);
        }

        @Override
        protected void reload(ModelEvent.BakingCompleted evt) {
            super.reload(evt);
            //model = ObjLoader.INSTANCE.loadModel(new ModelSettings(rl, true, useDiffuseLighting(), true, true, null));
        }

        /*@Override
        public ObjModel getModel() {
            return (ObjModel) super.getModel();
        }*/

        protected boolean useDiffuseLighting() {
            return true;
        }
    }

    public static class JSONModelData extends MekanismModelData {

        //this is a list due to SubmitNodeCollector wanting a list
        private List<BlockStateModelPart> bakedModel;
        private final StandaloneModelKey<BlockStateModelPart> key;

        private JSONModelData(Identifier rl) {
            super(rl);
            key = new StandaloneModelKey<>(this);
        }

        @Override
        protected void reload(ModelEvent.BakingCompleted evt) {
            super.reload(evt);
            BlockStateModelPart modelPart = evt.getBakingResult().standaloneModels().get(key);
            bakedModel = modelPart == null ? Collections.emptyList() : List.of(modelPart);
            /*bakedModel = BaseModelCache.getBakedModel(evt, mrl);
            ModelBaker baker = evt.getModelBakery().new ModelBakerImpl(
                  (modelLoc, material) -> material.sprite(),
                  mrl
            );
            if (getUnbakedModel(evt.getModelBakery(), baker, mrl) instanceof BlockModel blockModel) {
                model = blockModel.customData.getCustomGeometry();
            }*/
        }

        @Override
        protected void setup(ModelEvent.RegisterStandalone event) {
            event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(rl));
        }

        public List<BlockStateModelPart> getBakedModel() {
            return bakedModel;
        }
    }
}
