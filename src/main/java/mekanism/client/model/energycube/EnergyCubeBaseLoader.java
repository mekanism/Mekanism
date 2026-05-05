package mekanism.client.model.energycube;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mekanism.common.Mekanism;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public class EnergyCubeBaseLoader implements UnbakedModelLoader<EnergyCubeBaseUnbakedModel>, ResourceManagerReloadListener {
    public static final EnergyCubeBaseLoader INSTANCE = new EnergyCubeBaseLoader();
    public static final Identifier ID = Mekanism.rl("energy_cube");

    private EnergyCubeBaseLoader() {}

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        // Handle any cache clearing logic
    }

    @Override
    public EnergyCubeBaseUnbakedModel read(JsonObject obj, JsonDeserializationContext context) throws JsonParseException {
        // Use the given JsonObject and, if needed, the JsonDeserializationContext to get properties from the model JSON.
        // The MyUnbakedModel constructor may have constructor parameters (see below).

        // Read the data used to create the quads
        EnergyCubeBaseGeometry geometry = EnergyCubeBaseGeometry.parse(obj, context);

        // For the basic parameters provided by vanilla and NeoForge, you can use the StandardModelParameters
        StandardModelParameters params = StandardModelParameters.parse(obj, context);

        return new EnergyCubeBaseUnbakedModel(params, geometry);
    }
}