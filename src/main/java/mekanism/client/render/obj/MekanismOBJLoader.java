package mekanism.client.render.obj;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;

public class MekanismOBJLoader implements ICustomModelLoader {

    public static final MekanismOBJLoader INSTANCE = new MekanismOBJLoader();
    private final Map<ResourceLocation, MekanismOBJModel> modelCache = new HashMap<>();

    @Override
    public boolean accepts(@Nonnull ResourceLocation modelLocation) {
        return modelLocation.getPath().endsWith(".obj.mek");
    }

    @Nonnull
    @Override
    public IModel loadModel(@Nonnull ResourceLocation loc) throws Exception {
        ResourceLocation file = new ResourceLocation(loc.getNamespace(), loc.getPath());
        if (!modelCache.containsKey(file)) {
            IModel model = OBJLoader.INSTANCE.loadModel(file);
            if (model instanceof OBJModel) {
                if (file.getPath().contains("transmitter")) {
                    MekanismOBJModel mekModel = new MekanismOBJModel(((OBJModel) model).getMatLib(), file);
                    modelCache.put(file, mekModel);
                }
            }
        }

        MekanismOBJModel mekModel = modelCache.get(file);
        if (mekModel == null) {
            return ModelLoaderRegistry.getMissingModel();
        }
        return mekModel;
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
        modelCache.clear();
    }
}