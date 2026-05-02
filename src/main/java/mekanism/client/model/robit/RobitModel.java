package mekanism.client.model.robit;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.client.RobitSpriteUploader;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jspecify.annotations.NonNull;

public class RobitModel implements DynamicBlockStateModel {

    private final BlockStateModelPart basePart;

    public RobitModel(BlockStateModelPart basePart) {
        this.basePart = basePart;
    }

    @Override
    public void collectParts(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random, List<BlockStateModelPart> parts) {
        parts.add(basePart);
    }

    @Override
    public Material.@NonNull Baked particleMaterial() {
        return basePart.particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return basePart.materialFlags();
    }

    public record Unbaked() implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public @NonNull BlockStateModel bake(@NonNull ModelBaker baker) {
            ModelBaker atlasRedirector = new ModelBaker() {
                @Override
                public @NonNull ResolvedModel getModel(@NonNull Identifier id) {
                    return baker.getModel(id);
                }

                @Override
                public @NonNull BlockStateModelPart missingBlockModelPart() {
                    return baker.missingBlockModelPart();
                }

                @Override
                public @NonNull MaterialBaker materials() {
                    MaterialBaker original = baker.materials();
                    return new MaterialBaker() {
                        @Override
                        public Material.@NonNull Baked get(@NonNull Material material, @NonNull ModelDebugName modelDebugName) {
                            if (material.sprite().getNamespace().equals(Mekanism.MODID) && material.sprite().getPath().contains("robit")) {
                                return original.get(new Material(RobitSpriteUploader.ATLAS_LOCATION), modelDebugName);
                            }
                            return original.get(material, modelDebugName);
                        }

                        @Override
                        public Material.@NonNull Baked reportMissingReference(@NonNull String reference, @NonNull ModelDebugName debugName) {
                            return original.reportMissingReference(reference, debugName);
                        }
                    };
                }

                @Override
                public @NonNull Interner interner() {
                    return baker.interner();
                }

                @Override
                public <T> T compute(@NonNull SharedOperationKey<T> sharedOperationKey) {
                    return baker.compute(sharedOperationKey);
                }
            };

            BlockStateModelPart part = SimpleModelWrapper.bake(atlasRedirector, Mekanism.rl("item/robit_base"), (ModelState) BlockModelRotation.NO_TRANSFORM);
            return new RobitModel(part);
        }

        @Override
        public @NonNull MapCodec<Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.@NonNull Resolver resolver) {
        }
    }
}
