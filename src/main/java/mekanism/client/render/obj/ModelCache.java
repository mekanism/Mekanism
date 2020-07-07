package mekanism.client.render.obj;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.Mekanism;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
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

public class ModelCache {

    private static final Map<ResourceLocation, ModelData> modelMap = new Object2ObjectOpenHashMap<>();
    private static final Set<Runnable> callbacks = new HashSet<>();

    public static final OBJModelData MEKASUIT = registerOBJ(Mekanism.rl("models/entity/mekasuit.obj"));
    public static final OBJModelData MEKASUIT_MODULES = registerOBJ(Mekanism.rl("models/entity/mekasuit_modules.obj"));
    public static final OBJModelData MEKATOOL = registerOBJ(Mekanism.rl("models/entity/mekatool.obj"));

    public static final JSONModelData[] QIO_DRIVES = new JSONModelData[DriveStatus.values().length];
    static {
        for (DriveStatus status : DriveStatus.values()) {
            if (status == DriveStatus.NONE) {
                continue;
            }
            QIO_DRIVES[status.ordinal()] = registerJSON(status.getModel());
        }
    }

    public static void onBake(ModelBakeEvent evt) {
        modelMap.values().forEach(m -> m.reload(evt));
        callbacks.forEach(Runnable::run);
    }

    public static void reloadCallback(Runnable callback) {
        callbacks.add(callback);
    }

    public static void setup() {
        modelMap.values().forEach(m -> m.setup());
    }

    private static OBJModelData registerOBJ(ResourceLocation rl) {
        OBJModelData data = new OBJModelData(rl);
        modelMap.put(rl, data);
        return data;
    }

    private static JSONModelData registerJSON(ResourceLocation rl) {
        JSONModelData data = new JSONModelData(rl);
        modelMap.put(rl, data);
        return data;
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

        protected void setup() {}

        public IBakedModel bake(IModelConfiguration config) {
            return bakedMap.computeIfAbsent(config, c -> model.bake(c, ModelLoader.instance(), ModelLoader.defaultTextureGetter(), SimpleModelTransform.IDENTITY, ItemOverrideList.EMPTY, rl));
        }

        public IModelGeometry<?> getModel() {
            return model;
        }
    }

    public static class OBJModelData extends ModelData {

        private OBJModelData(ResourceLocation rl) {
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
            bakedModel = evt.getModelRegistry().get(rl);
            IUnbakedModel unbaked = evt.getModelLoader().getUnbakedModel(rl);
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
