package mekanism.common.integration.framedblocks;

import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

final class ChemicalModel implements BakedModel {

    private static final ModelState SIMPLE_STATE = new SimpleModelState(Transformation.identity());
    static final ModelResourceLocation BARE_MODEL = ModelResourceLocation.standalone(FramedBlocksIntegration.Constants.CHEMICAL_DUMMY_MODEL);

    private final RenderType renderType;
    private final ChunkRenderTypeSet renderTypeSet;
    private final Map<Direction, List<BakedQuad>> quads;
    private final TextureAtlasSprite particles;

    private ChemicalModel(RenderType renderType, Map<Direction, List<BakedQuad>> quads, TextureAtlasSprite particles) {
        this.renderType = renderType;
        this.renderTypeSet = ChunkRenderTypeSet.of(renderType);
        this.quads = quads;
        this.particles = particles;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return getQuads(state, side, random, ModelData.EMPTY, RenderType.translucent());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, RenderType layer) {
        return (side == null || layer != renderType) ? Collections.emptyList() : quads.get(side);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return renderTypeSet;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particles;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    static ChemicalModel create(Chemical chemical) {
        ModelBakery modelBakery = Minecraft.getInstance().getModelManager().getModelBakery();
        UnbakedModel bareModel = modelBakery.getModel(BARE_MODEL.id());
        Preconditions.checkNotNull(bareModel, "Bare chemical model not loaded!");

        ModelResourceLocation modelName = new ModelResourceLocation(
                Mekanism.rl("chemical/" + chemical.toString().replace(":", "_")),
                "mekanism_framedblocks_dynamic_chemical"
        );
        TextureAtlasSprite sprite = MekanismRenderer.getSprite(chemical.getIcon());
        BakedModel model = bareModel.bake(
                modelBakery.new ModelBakerImpl(
                        (modelLoc, material) -> sprite,
                        modelName
                ),
                loc -> sprite,
                SIMPLE_STATE
        );
        Preconditions.checkNotNull(model, "Failed to bake chemical model for chemical %s", chemical);

        Map<Direction, List<BakedQuad>> quads = new EnumMap<>(Direction.class);
        RandomSource random = RandomSource.create();
        RenderType layer = RenderType.translucent();

        for (Direction side : Direction.values())
        {
            quads.put(side, model.getQuads(Blocks.AIR.defaultBlockState(), side, random, ModelData.EMPTY, layer));
        }

        return new ChemicalModel(layer, quads, sprite);
    }
}
