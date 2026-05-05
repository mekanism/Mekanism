package mekanism.client.model.energycube;

import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;

// The unbaked model contains all the information read from the JSON.
// It provides the basic settings and geometry.
// Using AbstractUnbakedModel sets the Vanilla and NeoForge properties methods
public class EnergyCubeBaseUnbakedModel extends AbstractUnbakedModel {

    private final EnergyCubeBaseGeometry geometry;

    public EnergyCubeBaseUnbakedModel(StandardModelParameters params, EnergyCubeBaseGeometry geometry) {
        super(params);
        this.geometry = geometry;
    }

    @Override
    public UnbakedGeometry geometry() {
        // The geometry to used to construct the baked quads
        return this.geometry;
    }

    @Override
    public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
        super.fillAdditionalProperties(propertiesBuilder);
        // Add additional properties below by calling withParameter(ContextKey<T>, T)
        // They can then be accessed in the ContextMap provided in UnbakedGeometry#bake
        //TODO add side data??
    }
}