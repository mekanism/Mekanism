package mekanism.client.model.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.jetbrains.annotations.NotNull;

/**
 * Mekanism model loader that properly loads models and switches between them based on the active model data
 */
public class DataBasedModelLoader implements IGeometryLoader<DataBasedGeometry> {

    public static final ModelProperty<Void> EMITTING = new ModelProperty<>();

    public static final DataBasedModelLoader INSTANCE = new DataBasedModelLoader();

    private static final Map<String, ModelProperty<Void>> SUPPORTED_PROPERTIES = Map.of(
          "emitting", EMITTING
    );

    private DataBasedModelLoader() {
    }

    @NotNull
    @Override
    public DataBasedGeometry read(@NotNull JsonObject jsonObject, @NotNull JsonDeserializationContext ctx) {
        ResourceLocation noData = readModelPath(jsonObject, "no_data");
        Map<ModelProperty<Void>, ResourceLocation> propertyBasedModels = new HashMap<>();
        for (Map.Entry<String, ModelProperty<Void>> entry : SUPPORTED_PROPERTIES.entrySet()) {
            if (jsonObject.has(entry.getKey())) {
                propertyBasedModels.put(entry.getValue(), readModelPath(jsonObject, entry.getKey()));
            }
        }
        if (propertyBasedModels.isEmpty()) {
            throw new JsonParseException("Model data based models require at least one property based model.");
        }
        return new DataBasedGeometry(noData, propertyBasedModels);
    }

    private ResourceLocation readModelPath(JsonObject jsonObject, String modelName) {
        String model = GsonHelper.getAsString(jsonObject, modelName);
        ResourceLocation modelRl = ResourceLocation.tryParse(model);
        if (modelRl == null) {
            throw new JsonParseException("Expected '" + modelName + "' to be a valid resource location.");
        }
        return modelRl;
    }
}