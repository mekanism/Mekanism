package mekanism.client.model.energycube;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mekanism.api.RelativeSide;
import mekanism.common.tile.TileEntityEnergyCube.CubeSideState;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import org.joml.Matrix4fc;

// Holds the unbaked quads to render
// Other information that is stored in the unbaked model should be passed to the context map
public class EnergyCubeBaseGeometry implements ExtendedUnbakedGeometry {

    private final UnbakedCuboidGeometry frame;
    private final Map<RelativeSide, UnbakedCuboidGeometry> leds;
    private final Map<RelativeSide, UnbakedCuboidGeometry> ledsLit;
    private final Map<RelativeSide, UnbakedCuboidGeometry> ports;

    public EnergyCubeBaseGeometry(UnbakedCuboidGeometry frame, Map<RelativeSide, UnbakedCuboidGeometry> leds, Map<RelativeSide, UnbakedCuboidGeometry> ledsLit, Map<RelativeSide, UnbakedCuboidGeometry> ports) {
        // Store the unbaked quads to bake
        this.frame = frame;
        this.leds = leds;
        this.ledsLit = ledsLit;
        this.ports = ports;
    }

    // Method responsible for model baking, returning the quad collection. Parameters in this method are:
    // - The map of texture names to their associated materials.
    // - The model baker. Can be used for getting sub-models to bake and getting sprites from the texture slots.
    // - The model state. This holds the transformations from the blockstate file, typically from rotations and the uvlock.
    // - The name of the model.
    // - A ContextMap of settings provided by NeoForge and your unbaked model. See the 'NeoForgeModelProperties' class for all available properties.
    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties) {
        if (!(state instanceof CubeSideModelState cubeState)) {
            return frame.bake(textureSlots, baker, state, debugName, additionalProperties);
        }

        // The builder to create the collection
        var builder = new QuadCollection.Builder();
        // Build the quads for baking

        CubeSideState sideState = cubeState.state;
        if (sideState == CubeSideState.ACTIVE_LIT) {
            builder.addAll(ledsLit.get(cubeState.side).bake(textureSlots, baker, state, debugName, additionalProperties));
        } else {
            builder.addAll(leds.get(cubeState.side).bake(textureSlots, baker, state, debugName, additionalProperties));
        }
        if (sideState != CubeSideState.INACTIVE) {
            builder.addAll(ports.get(cubeState.side).bake(textureSlots, baker, state, debugName, additionalProperties));
        }

        // Create the quad collection
        return builder.build();
    }

    public static EnergyCubeBaseGeometry parse(JsonObject object, JsonDeserializationContext context) {
        UnbakedCuboidGeometry frame = getElements(context, object, "frame");
        Map<RelativeSide, UnbakedCuboidGeometry> leds = getSidedMap(context, object, "leds");
        JsonArray ledUVShiftArr = GsonHelper.getAsJsonArray(GsonHelper.getAsJsonObject(object, "leds"), "lit_uv_shift");
        float ledUShift = ledUVShiftArr.get(0).getAsFloat();
        float ledVShift = ledUVShiftArr.get(1).getAsFloat();
        Map<RelativeSide, UnbakedCuboidGeometry> ledsLit = new EnumMap<>(RelativeSide.class);
        for (Map.Entry<RelativeSide, UnbakedCuboidGeometry> entry : leds.entrySet()) {
            List<CuboidModelElement> litElements = entry.getValue().elements().stream()
                  .map(unlit -> new CuboidModelElement(
                        unlit.from(),
                        unlit.to(),
                        remapLedUVs(unlit, ledUShift, ledVShift),
                        unlit.rotation(),
                        false,
                        15,
                        unlit.faceData()
                  ))
                  .toList();
            ledsLit.put(entry.getKey(), new UnbakedCuboidGeometry(litElements));
        }
        Map<RelativeSide, UnbakedCuboidGeometry> ports = getSidedMap(context, object, "ports");
        return new EnergyCubeBaseGeometry(frame, leds, ledsLit, ports);
    }

    private static Map<Direction, CuboidFace> remapLedUVs(CuboidModelElement unlit, float ledUShift, float ledVShift) {
        return unlit.faces().entrySet().stream()
              .map(faceEntry -> {
                  CuboidFace origFace = faceEntry.getValue();
                  CuboidFace.UVs origUvs = origFace.uvs();
                  if (origUvs == null) {
                      return faceEntry;//no remap needed
                  }
                  //create new uvs
                  CuboidFace.UVs newUVs = new CuboidFace.UVs(
                        origUvs.minU() + ledUShift,
                        origUvs.minV() + ledVShift,
                        origUvs.maxU() + ledUShift,
                        origUvs.maxV() + ledVShift
                  );
                  //attach them to a new face instance
                  CuboidFace newFace = new CuboidFace(
                        origFace.cullForDirection(),
                        origFace.tintIndex(),
                        origFace.texture(),
                        newUVs,
                        origFace.rotation(),
                        origFace.faceData(),
                        origFace.parent()
                  );
                  //zip it back up
                  return Map.entry(faceEntry.getKey(), newFace);
              })
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<RelativeSide, UnbakedCuboidGeometry> getSidedMap(JsonDeserializationContext context, JsonObject object, String key) {
        Map<RelativeSide, UnbakedCuboidGeometry> parts = new EnumMap<>(RelativeSide.class);
        JsonObject group = GsonHelper.getAsJsonObject(object, key);
        for (RelativeSide side : RelativeSide.values()) {
            parts.put(side, getElements(context, group, side.getSerializedName()));
        }
        return parts;
    }

    protected static UnbakedCuboidGeometry getElements(JsonDeserializationContext context, JsonObject object, String key) {
        if (!object.has(key)) {
            throw new JsonSyntaxException(key + " is required");
        } else {
            List<CuboidModelElement> elements = new ArrayList<>();

            for (JsonElement element : GsonHelper.getAsJsonArray(object, key)) {
                elements.add(context.deserialize(element, CuboidModelElement.class));
            }

            return new UnbakedCuboidGeometry(elements);
        }
    }

    public record CubeSideModelState(ModelState parent, RelativeSide side, CubeSideState state) implements ModelState {

        @Override
        public Transformation transformation() {
            return parent.transformation();
        }

        @Override
        public Matrix4fc faceTransformation(Direction face) {
            return parent.faceTransformation(face);
        }

        @Override
        public Matrix4fc inverseFaceTransformation(Direction face) {
            return parent.inverseFaceTransformation(face);
        }

        @Override
        public boolean mayApplyArbitraryRotation() {
            return parent.mayApplyArbitraryRotation();
        }
    }
}