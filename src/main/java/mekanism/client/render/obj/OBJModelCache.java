package mekanism.client.render.obj;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.ModelSettings;

public class OBJModelCache {

    private static final Map<ResourceLocation, OBJModelData> modelMap = new Object2ObjectOpenHashMap<>();
    private static final Set<Runnable> callbacks = new HashSet<>();

    public static OBJModelData MEKASUIT = register(Mekanism.rl("models/entity/mekasuit.obj"));
    public static OBJModelData MEKASUIT_MODULES = register(Mekanism.rl("models/entity/mekasuit_modules.obj"));

    public static void onBake(ModelBakeEvent evt) {
        modelMap.values().forEach(OBJModelData::reload);
        callbacks.forEach(Runnable::run);
    }

    public static void reloadCallback(Runnable callback) {
        callbacks.add(callback);
    }

    private static OBJModelData register(ResourceLocation rl) {
        OBJModelData data = new OBJModelData(rl);
        modelMap.put(rl, data);
        return data;
    }

    public static class OBJModelData {

        private final ResourceLocation rl;
        private OBJModel model;
        private final Map<IModelConfiguration, IBakedModel> bakedMap = new Object2ObjectOpenHashMap<>();

        private OBJModelData(ResourceLocation rl) {
            this.rl = rl;
        }

        private void reload() {
            model = OBJLoader.INSTANCE.loadModel(new ModelSettings(rl, true, false, true, true, null));
            bakedMap.clear();
        }

        public IBakedModel getBakedModel(IModelConfiguration config) {
            return bakedMap.computeIfAbsent(config, c -> model.bake(c, ModelLoader.instance(), ModelLoader.defaultTextureGetter(), SimpleModelTransform.IDENTITY, ItemOverrideList.EMPTY, rl));
        }

        public OBJModel getModel() {
            return model;
        }
    }
}
