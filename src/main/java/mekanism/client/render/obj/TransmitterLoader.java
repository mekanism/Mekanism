package mekanism.client.render.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;

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
        return new TransmitterModel(OBJLoader.INSTANCE.read(deserializationContext, modelContents));
    }
}