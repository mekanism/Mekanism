package mekanism.generators.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.fission.TileEntityFissionAssembly;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

/**
 * 26.1 DynamicBlockStateModel for Fission Fuel/Control Rod Assembly.
 * <p>
 * When the assembly is part of an active fission multiblock ({@link TileEntityFissionAssembly#GLOWING} is present
 * in {@link ModelData}), an emissive translucent glow overlay is appended to the base model parts.
 * <p>
 * This replaces the legacy {@code FuelAssemblyBakedModel} which relied on {@code ModifyBakingResult}.
 */
@NothingNullByDefault
public class FuelAssemblyModel implements DynamicBlockStateModel {

    private final BlockStateModelPart basePart;
    private final BlockStateModelPart glowPart;

    public FuelAssemblyModel(BlockStateModelPart basePart, BlockStateModelPart glowPart) {
        this.basePart = basePart;
        this.glowPart = glowPart;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        parts.add(basePart);
        ModelData modelData = level.getModelData(pos);
        if (modelData.has(TileEntityFissionAssembly.GLOWING)) {
            parts.add(glowPart);
        }
    }

    @Override
    public net.minecraft.client.resources.model.sprite.Material.Baked particleMaterial() {
        return basePart.particleMaterial();
    }

    @net.minecraft.client.resources.model.geometry.BakedQuad.MaterialFlags
    @Override
    public int materialFlags() {
        return basePart.materialFlags() | glowPart.materialFlags();
    }

    public record Unbaked(Identifier baseModel, Identifier glowModel, Variant.SimpleModelState state) implements CustomUnbakedBlockStateModel {

        public static final Identifier ID = MekanismGenerators.rl("fuel_assembly");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
              Identifier.CODEC.fieldOf("base_model").forGetter(Unbaked::baseModel),
              Identifier.CODEC.fieldOf("glow_model").forGetter(Unbaked::glowModel),
              Variant.SimpleModelState.MAP_CODEC.forGetter(Unbaked::state)
        ).apply(in, Unbaked::new));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }

        @Override
        public FuelAssemblyModel bake(ModelBaker baker) {
            ModelState modelState = state.asModelState();
            return new FuelAssemblyModel(
                  bakePart(baker, modelState, baseModel),
                  bakePart(baker, modelState, glowModel)
            );
        }

        private static BlockStateModelPart bakePart(ModelBaker baker, ModelState modelState, Identifier identifier) {
            ResolvedModel model = baker.getModel(identifier);
            return SimpleModelWrapper.bake(baker, model, modelState);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(baseModel);
            resolver.markDependency(glowModel);
        }
    }
}
