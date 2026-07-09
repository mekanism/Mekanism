package mekanism.client.model.blockstate;

import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import mekanism.client.ModelUtil;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.client.model.data.TransmitterModelData.VisualConnectionStatus;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.ComposedModelState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class TransmitterBlockStateModel implements DynamicBlockStateModel {

    public static final Set<String> ALL_PART_GROUPS = Direction.stream()
          .flatMap(direction -> Arrays.stream(VisualConnectionStatus.values())
                .map(connectionType -> getPartName(direction, connectionType))
          )
          .collect(Collectors.toSet());

    private final PartStorage baseParts;
    @Nullable
    private final PartStorage glassParts;
    private final Material.Baked particleMaterial;
    private final int materialFlags;

    private TransmitterBlockStateModel(PartStorage baseParts, @Nullable PartStorage glassParts, Material.Baked particleMaterial, int materialFlags) {
        this.baseParts = baseParts;
        this.glassParts = glassParts;
        this.particleMaterial = particleMaterial;
        this.materialFlags = materialFlags;
    }

    private static void addPart(PartStorage partTable, List<BlockStateModelPart> partsList, Direction side, VisualConnectionStatus connectionType) {
        BlockStateModelPart modelPart = partTable.get(side, connectionType);
        if (modelPart != null) {
            partsList.add(modelPart);
        }
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelData modelData = level.getModelData(pos);
        TransmitterModelData transmitterModelData = modelData.get(TileEntityTransmitter.TRANSMITTER_PROPERTY);

        //Fallback to all none if no data
        if (transmitterModelData == null) {
            for (Direction direction : EnumUtils.DIRECTIONS) {
                addPart(baseParts, parts, direction, VisualConnectionStatus.NONE);
            }
            return;
        }

        boolean needsGlass = glassParts != null && transmitterModelData.getHasColor();

        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            Direction direction = EnumUtils.DIRECTIONS[i];
            VisualConnectionStatus connectionType = transmitterModelData.getConnectionType(i);
            addPart(baseParts, parts, direction, connectionType);
            //Skip rendering the glass if we don't actually have any glass, or we don't have a color for it:
            if (needsGlass) {
                addPart(Objects.requireNonNull(glassParts), parts, direction, connectionType);
            }
        }
    }

    @Override
    public Material.Baked particleMaterial() {
        return particleMaterial;
    }

    @BakedQuad.MaterialFlags
    @Override
    public int materialFlags() {
        return materialFlags;
    }

    private static String getPartName(Direction side, VisualConnectionStatus connectionType) {
        return side.getSerializedName() + connectionType.partName();
    }

    public static class Unbaked implements CustomUnbakedBlockStateModel {

        public static final Identifier ID = Mekanism.rl("special/transmitter");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
              Variant.MAP_CODEC.forGetter(Unbaked::base),
              Identifier.CODEC.optionalFieldOf("glass").forGetter(Unbaked::glass),
              Codec.BOOL.optionalFieldOf("hideContiguousJoin", true).forGetter(Unbaked::hideContiguousJoin)
        ).apply(inst, Unbaked::new));

        private final Variant base;
        @Nullable
        private final Identifier glass;
        private final boolean hideContiguousJoin;

        public Unbaked(Variant base, Optional<Identifier> glass, boolean hideContiguousJoin) {
            this.base = base;
            this.glass = glass.orElse(null);
            this.hideContiguousJoin = hideContiguousJoin;
        }

        public Variant base() {
            return base;
        }

        @Nullable
        public Optional<Identifier> glass() {
            return Optional.ofNullable(glass);
        }

        public boolean hideContiguousJoin() {
            return hideContiguousJoin;
        }

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker modelBakery) {
            ResolvedModel baseModel = modelBakery.getModel(base.modelLocation());
            ResolvedModel glassModel = glass == null ? null : modelBakery.getModel(glass);
            PartStorage baseParts = new PartStorage(hideContiguousJoin);
            PartStorage glassParts = glassModel == null ? null : new PartStorage(hideContiguousJoin);
            int materialFlags = 0;
            ModelState modelState = base.modelState().asModelState();
            Map<String, Boolean> partsVisibility = new HashMap<>(ALL_PART_GROUPS.size());//nb: shared with the delegate

            //start with setting everything invisible (default is true if not in the map)
            for (String group : ALL_PART_GROUPS) {
                partsVisibility.put(group, false);
            }

            //create the delegates to help baking
            DelegateResolvedModel partVisibilityDelegate = new DelegateResolvedModel(partsVisibility);
            BakerOverrider noneSegmentOverrider = new BakerOverrider(modelBakery, new NoneSegmentRemap(modelBakery.materials()));

            //now bake all the parts
            for (VisualConnectionStatus connectionType : VisualConnectionStatus.values()) {
                if (!hideContiguousJoin && (connectionType == VisualConnectionStatus.NONE_CONTIGUOUS || connectionType == VisualConnectionStatus.NONE_CONTIGUOUS_ROTATED)) {
                    continue; //skip as this model doesn't want it and its model data shouldn't contain it
                }
                ModelBaker bakerToUse = (connectionType == VisualConnectionStatus.NONE_CONTIGUOUS || connectionType == VisualConnectionStatus.NONE_CONTIGUOUS_ROTATED) ?
                                        noneSegmentOverrider : modelBakery;
                for (Direction direction : EnumUtils.DIRECTIONS) {
                    ModelState modelStateToUse = connectionType == VisualConnectionStatus.NONE_CONTIGUOUS_ROTATED ? new ComposedModelState(modelState, makeIconStatusTransform(direction)) : modelState;

                    //setup visibility
                    String partName = getPartName(direction, connectionType);
                    partsVisibility.put(partName, true);

                    //bake core
                    BlockStateModelPart baked = SimpleModelWrapper.bake(bakerToUse, partVisibilityDelegate.as(baseModel), modelStateToUse);
                    materialFlags |= baked.materialFlags();
                    baseParts.put(direction, connectionType, baked);

                    //if we have glass, bake that
                    if (glassModel != null) {
                        baked = SimpleModelWrapper.bake(bakerToUse, partVisibilityDelegate.as(glassModel), modelStateToUse);
                        glassParts.put(direction, connectionType, baked);
                        materialFlags |= baked.materialFlags();
                    }

                    //reset the visibilities for the next round
                    partsVisibility.put(partName, false);
                }
            }

            return new TransmitterBlockStateModel(baseParts, glassParts, baseModel.resolveParticleMaterial(baseModel.getTopTextureSlots(), modelBakery), materialFlags);
        }

        private static Transformation makeIconStatusTransform(Direction direction) {
            Vector3f vecForDirection = direction.getUnitVec3f().mul(-1, new Vector3f());
            Quaternionf quaternion = new Quaternionf().setAngleAxis(VisualConnectionStatus.CONTIGUOUS_ROTATION, vecForDirection.x, vecForDirection.y, vecForDirection.z);
            return new Transformation(null, quaternion, null, null);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(base.modelLocation());
            if (glass != null) {
                resolver.markDependency(glass);
            }
        }
    }

    private static class PartStorage {

        private final BlockStateModelPart[][] parts;

        private PartStorage(boolean hideContiguousJoin) {
            int connections = hideContiguousJoin ? VisualConnectionStatus.values().length : ConnectionType.values().length;
            parts = new BlockStateModelPart[EnumUtils.DIRECTIONS.length][connections];
        }

        public void put(Direction direction, VisualConnectionStatus status, BlockStateModelPart part) {
            parts[direction.ordinal()][status.ordinal()] = part;
        }

        @Nullable//technically, but shouldn't be in practice
        public BlockStateModelPart get(Direction direction, VisualConnectionStatus status) {
            return parts[direction.ordinal()][status.ordinal()];
        }
    }

    /// Delegate for the actual resolved model, with a mutable delegate
    ///
    /// Exists so that we can influence the visible groups without copying lots of vanilla code.
    ///
    /// Does not support merging part visibility elements from the actual model
    private static class DelegateResolvedModel implements ResolvedModel {

        @SuppressWarnings("NotNullFieldNotInitialized")
        private ResolvedModel delegate;
        private final Map<String, Boolean> visibilityMap;

        private DelegateResolvedModel(Map<String, Boolean> visibilityMap) {
            this.visibilityMap = visibilityMap;
        }

        @This
        public DelegateResolvedModel as(ResolvedModel other) {
            this.delegate = other;
            return this;
        }

        @Override
        public ContextMap getTopAdditionalProperties() {
            return ModelUtil.partVisibility(this, visibilityMap);
        }

        @Override
        public UnbakedModel wrapped() {
            return delegate.wrapped();
        }

        @Override
        @Nullable
        public ResolvedModel parent() {
            return delegate.parent();
        }

        @Override
        public String debugName() {
            return "delegate:" + delegate.debugName();
        }
    }

    private abstract static class DelegateMaterialBaker extends MaterialBaker {

        private final MaterialBaker upstream;

        private DelegateMaterialBaker(MaterialBaker upstream) {
            super(upstream.missingSprite.sprite());
            this.upstream = upstream;
        }

        @Override
        public Material.@Nullable Baked bake(Material material) {
            return upstream.bake(material);
        }

        @Override
        public Material.Baked resolveSlot(TextureSlots slots, String id, ModelDebugName name) {
            return super.resolveSlot(slots, remapReference(id), name);
        }

        protected abstract String remapReference(String id);
    }

    /// Remaps the texture used in the contiguous NONE sections
    private static class NoneSegmentRemap extends DelegateMaterialBaker {

        private NoneSegmentRemap(MaterialBaker upstream) {
            super(upstream);
        }

        @Override
        protected String remapReference(String id) {
            if (isDirectional(id)) {
                id = id.contains("glass") ? "#side_glass" : "#side";
            }
            return id;
        }

        private static boolean isDirectional(String piece) {
            return piece.endsWith("down") ||
                   piece.endsWith("up") ||
                   piece.endsWith("north") ||
                   piece.endsWith("south") ||
                   piece.endsWith("east") ||
                   piece.endsWith("west");
        }
    }

    private record BakerOverrider(ModelBaker delegateBaker, DelegateMaterialBaker materials) implements ModelBaker {

        @Override
        public ResolvedModel getModel(Identifier location) {
            return delegateBaker.getModel(location);
        }

        @Override
        public BlockStateModelPart missingBlockModelPart() {
            return delegateBaker.missingBlockModelPart();
        }

        @Override
        public Interner interner() {
            return delegateBaker.interner();
        }

        @Override
        public <T> T compute(SharedOperationKey<T> key) {
            return delegateBaker.compute(key);
        }
    }

}
