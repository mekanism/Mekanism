package mekanism.client.render.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;

public class TransmitterLoader implements IModelLoader<TransmitterModel> {

    public static final TransmitterLoader INSTANCE = new TransmitterLoader();

    private TransmitterLoader() {
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
        //TODO: 1.15 We want to make sure to clear the cache
    }

    @Nonnull
    @Override
    public TransmitterModel read(@Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents) {
        //Wrap the Obj loader to read our file
        OBJModel model = OBJLoader.INSTANCE.read(deserializationContext, modelContents);
        OBJModel glass = null;
        if (modelContents.has("glass")) {
            //TODO: Look into if there is a way to make the obj.mek file be the same and just change the mtl
            glass = (OBJModel) ModelLoaderRegistry.deserializeGeometry(deserializationContext, modelContents.get("glass").getAsJsonObject());
        }
        return new TransmitterModel(model, glass);
    }
}