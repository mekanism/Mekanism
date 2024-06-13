package mekanism.generators.client.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.Quad;
import mekanism.common.lib.Color;
import mekanism.generators.common.tile.fission.TileEntityFissionAssembly;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

//TODO: Eventually make use of this if we figure out a way to not have coolant rendering make this invisible
@NothingNullByDefault
public class FuelAssemblyBakedModel extends BakedModelWrapper<BakedModel> {

    private static final Color GLOW_ARGB = Color.rgbad(0.466, 0.882, 0.929, 0.6);
    private static final Vector3f NORTH_EAST = new Vector3f(0.95F, 0.125F, 0.05F);
    private static final Vector3f SOUTH_WEST = new Vector3f(0.05F, 0.125F, 0.95F);

    private final Map<Direction, List<BakedQuad>> cachedGlows = new EnumMap<>(Direction.class);
    @Nullable
    private ChunkRenderTypeSet renderTypes;
    private final float height;

    public FuelAssemblyBakedModel(BakedModel original, float height) {
        super(original);
        this.height = height;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        List<BakedQuad> quads = super.getQuads(state, side, rand, data, renderType);
        //Note: Technically we would want the null side rather than a cullable side, but as this only would end up being used in fission reactors where
        // it should be in a checkerboard pattern we may as well allow it to be culled
        //TODO: Re-evaluate the above thought if we ever end up being able to make use of this model
        if (side != null && side.getAxis().isHorizontal() && renderType == RenderType.translucent()) {
            if (data.has(TileEntityFissionAssembly.GLOWING)) {
                //TODO: Eventually we may want to make the glow component be part of the json so resource packs can customize it more
                List<BakedQuad> allQuads = cachedGlows.get(side);
                if (allQuads == null) {
                    Vector3f startPos = switch (side) {
                        case NORTH, EAST -> NORTH_EAST;
                        case SOUTH, WEST -> SOUTH_WEST;
                        default -> throw new IllegalStateException("Unexpected face");
                    };
                    Quad.Builder quadBuilder = new Quad.Builder(MekanismRenderer.whiteIcon, side)
                          .light(LightTexture.FULL_BLOCK, LightTexture.FULL_SKY)
                          .uv(0, 0, 16, 16)
                          .color(GLOW_ARGB)
                          .rect(startPos, 0.9F, height, 1);
                    allQuads = List.of(quadBuilder.build().bake());
                    cachedGlows.put(side, allQuads);
                }
                if (quads.isEmpty()) {
                    return allQuads;
                }
                //Combine the quads if the base model has any translucent ones
                List<BakedQuad> mergedQuads = new ArrayList<>(quads);
                mergedQuads.addAll(allQuads);
                return mergedQuads;
            }
        }
        //Fallback to our "default" model arrangement. The item variant uses this
        return quads;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        if (data.has(TileEntityFissionAssembly.GLOWING)) {
            if (renderTypes == null) {
                renderTypes = ChunkRenderTypeSet.union(super.getRenderTypes(state, rand, data), ChunkRenderTypeSet.of(RenderType.translucent()));
            }
            return renderTypes;
        }
        return super.getRenderTypes(state, rand, data);
    }
}