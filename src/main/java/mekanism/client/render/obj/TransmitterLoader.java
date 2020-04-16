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
        //I believe given each cache is in the individual models when the manager reloads it allows GC to clear
        // the individual caches due to the models themselves no longer being referenced.
        // If this ends up not being the case then we will need to clear the caches from here
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