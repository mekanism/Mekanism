package mekanism.client.model.blockstate;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

public class QIORedstoneAdapterModel implements DynamicBlockStateModel {

    private final BlockStateModelPart litPart;
    private final BlockStateModelPart unlitPart;

    public QIORedstoneAdapterModel(BlockStateModelPart litPart, BlockStateModelPart unlitPart) {
        this.litPart = litPart;
        this.unlitPart = unlitPart;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelData modelData = level.getModelData(pos);
        boolean emitting = Boolean.TRUE.equals(modelData.get(TileEntityQIORedstoneAdapter.EMITTING));
        if (emitting) {
            parts.add(litPart);
        } else {
            parts.add(unlitPart);
        }
    }

    @Override
    public Material.Baked particleMaterial() {
        return unlitPart.particleMaterial();
    }

    @BakedQuad.MaterialFlags
    @Override
    public int materialFlags() {
        return unlitPart.materialFlags() | litPart.materialFlags();
    }

    public record Unbaked(Identifier unlit, Identifier lit, Variant.SimpleModelState state) implements CustomUnbakedBlockStateModel {

        public static final Identifier ID = Mekanism.rl("special/qio_redstone_adapter");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
              Identifier.CODEC.fieldOf("unlit").forGetter(Unbaked::unlit),
              Identifier.CODEC.fieldOf("lit").forGetter(Unbaked::lit),
              Variant.SimpleModelState.MAP_CODEC.forGetter(Unbaked::state)
        ).apply(in, Unbaked::new));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            ModelState modelState = state.asModelState();
            return new QIORedstoneAdapterModel(bakePart(baker, modelState, lit), bakePart(baker, modelState, unlit));
        }

        private static BlockStateModelPart bakePart(ModelBaker baker, ModelState modelState, Identifier identifier) {
            ResolvedModel model = baker.getModel(identifier);
            return SimpleModelWrapper.bake(baker, model, modelState);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(unlit);
            resolver.markDependency(lit);
        }
    }
}
