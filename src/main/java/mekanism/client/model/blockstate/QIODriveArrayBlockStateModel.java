package mekanism.client.model.blockstate;

import static mekanism.common.tile.qio.TileEntityQIODriveArray.DRIVE_SLOTS;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialFlags;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import org.joml.Vector3f;

public record QIODriveArrayBlockStateModel(BlockStateModelPart basePart, BlockStateModelPart[][] slotToBakedDrive) implements DynamicBlockStateModel {

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        parts.add(basePart);
        ModelData data = level.getModelData(pos);
        if (!data.has(TileEntityQIODriveArray.DRIVE_STATUS_PROPERTY)) {
            return;
        }
        long driveStatus = Objects.requireNonNullElse(data.get(TileEntityQIODriveArray.DRIVE_STATUS_PROPERTY), Long.valueOf(0));
        for (int slot = 0; slot < DRIVE_SLOTS; slot++) {
            int statusOrdinal = TileEntityQIODriveArray.getStatusOrdinal(slot, driveStatus);
            BlockStateModelPart modelPart = slotToBakedDrive[slot][statusOrdinal];
            if (modelPart != null) {
                parts.add(modelPart);
            }
        }
    }

    @Override
    public Baked particleMaterial() {
        return basePart.particleMaterial();
    }

    @Override
    public @MaterialFlags int materialFlags() {
        return basePart.materialFlags();
    }

    public record Unbaked(Variant baseModel) implements CustomUnbakedBlockStateModel {

        public static final MapCodec<Unbaked> MAP_CODEC = Variant.MAP_CODEC
              .xmap(Unbaked::new, Unbaked::baseModel);
        /// slot -> x,y transforms
        private static final float[][] DRIVE_PLACEMENTS = {
              {0, 6F / 16}, {-2F / 16, 6F / 16}, {-4F / 16, 6F / 16}, {-7F / 16, 6F / 16}, {-9F / 16, 6F / 16}, {-11F / 16, 6F / 16},
              {0, 0}, {-2F / 16, 0}, {-4F / 16, 0}, {-7F / 16, 0}, {-9F / 16, 0}, {-11F / 16, 0}
        };

        @Override
        public MapCodec<Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            ModelState baseTransforms = baseModel.modelState().asModelState();
            ResolvedModel model = baker.getModel(baseModel.modelLocation());
            BlockStateModelPart bakedBase = SimpleModelWrapper.bake(baker, model, baseTransforms);
            BlockStateModelPart[][] slotToBakedDrive = new BlockStateModelPart[DRIVE_SLOTS][];
            for (int slot = 0; slot < DRIVE_SLOTS; slot++) {
                BlockStateModelPart[] slotBakedParts = new BlockStateModelPart[DriveStatus.VALUES.length];
                slotToBakedDrive[slot] = slotBakedParts;
                for (int statusOrdinal = 0; statusOrdinal < DriveStatus.VALUES.length; statusOrdinal++) {
                    DriveStatus status = DriveStatus.BY_ID.apply(statusOrdinal);
                    Identifier statusModel = status.getModel();
                    if (statusModel == null) {
                        continue;
                    }
                    float[] drivePlacement = DRIVE_PLACEMENTS[slot];
                    ModelState driveModelState = UnbakedElementsHelper.composeRootTransformIntoModelState(baseTransforms, new Transformation(new Vector3f(drivePlacement[0], drivePlacement[1], 0), null, null, null));
                    slotBakedParts[statusOrdinal] = SimpleModelWrapper.bake(baker, statusModel, driveModelState);
                }
            }
            return new QIODriveArrayBlockStateModel(bakedBase, slotToBakedDrive);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(baseModel.modelLocation());
            for (DriveStatus status : DriveStatus.values()) {
                Identifier statusModel = status.getModel();
                if (statusModel != null) {
                    resolver.markDependency(statusModel);
                }
            }
        }
    }
}
