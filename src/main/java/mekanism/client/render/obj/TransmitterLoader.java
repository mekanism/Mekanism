package mekanism.client.render.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
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
        //TODO: Check if we need to clear the cache, given each is cached in their own model,
        // and when it reloads it should theoretically allow GC to then clear those instances
    }

    @Nonnull
    @Override
    public TransmitterModel read(@Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents) {
        //Wrap the Obj loader to read our file
        OBJModel model = OBJLoader.INSTANCE.read(deserializationContext, modelContents);
        OBJModel glass = null;
        if (modelContents.has(JsonConstants.GLASS)) {
            glass = (OBJModel) ModelLoaderRegistry.deserializeGeometry(deserializationContext, modelContents.get(JsonConstants.GLASS).getAsJsonObject());
        }
        return new TransmitterModel(model, glass);
    }
}