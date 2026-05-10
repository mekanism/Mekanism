package mekanism.client.model.blockstate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.client.model.data.TransmitterModelData;
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
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.Nullable;

public class TransmitterBlockStateModel implements DynamicBlockStateModel {

    private static final List<String> ALL_PART_GROUPS = Direction.stream()
          .flatMap(direction -> Arrays.stream(ConnectionType.values())
                .map(connectionType -> getPartName(direction, connectionType))
          )
          .toList();

    private final Table<Direction, ConnectionType, BlockStateModelPart> baseParts;
    @Nullable
    private final Table<Direction, ConnectionType, BlockStateModelPart> glassParts;
    private final Material.Baked particleMaterial;
    private final int materialFlags;

    public TransmitterBlockStateModel(Table<Direction, ConnectionType, BlockStateModelPart> baseParts, @Nullable Table<Direction, ConnectionType, BlockStateModelPart> glassParts, Material.Baked particleMaterial, int materialFlags) {
        this.baseParts = baseParts;
        this.glassParts = glassParts;
        this.particleMaterial = particleMaterial;
        this.materialFlags = materialFlags;
    }

    private static void addPart(Table<Direction, ConnectionType, BlockStateModelPart> partTable, List<BlockStateModelPart> partsList, Direction side, ConnectionType connectionType) {
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
                addPart(baseParts, parts, direction, ConnectionType.NONE);
                if (glassParts != null) {
                    addPart(glassParts, parts, direction, ConnectionType.NONE);
                }
            }
            return;
        }

        for (Map.Entry<Direction, ConnectionType> entry : transmitterModelData.getConnectionsMap().entrySet()) {
            addPart(baseParts, parts, entry.getKey(), entry.getValue());
            if (glassParts != null) {
                addPart(glassParts, parts, entry.getKey(), entry.getValue());
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

    @Override
    @Nullable//TODO
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return DynamicBlockStateModel.super.createGeometryKey(level, pos, state, random);
    }

    private static String getPartName(Direction side, ConnectionType connectionType) {
        return side.getSerializedName() + connectionType.name();
    }

    public static class Unbaked implements CustomUnbakedBlockStateModel {

        public static final Identifier ID = Mekanism.rl("special/transmitter");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
              Variant.MAP_CODEC.forGetter(Unbaked::base),
              Identifier.CODEC.optionalFieldOf("glass").forGetter(Unbaked::glass)
        ).apply(inst, Unbaked::new));
        public static final int NUM_DIRECTIONS = Direction.values().length;
        public static final int NUM_CONNECTIONS = ConnectionType.values().length;

        private final Variant base;
        @Nullable
        private final Identifier glass;

        public Unbaked(Variant base, Optional<Identifier> glass) {
            this.base = base;
            this.glass = glass.orElse(null);
        }

        public Variant base() {
            return base;
        }

        @Nullable
        public Optional<Identifier> glass() {
            return Optional.ofNullable(glass);
        }

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker modelBakery) {
            ResolvedModel baseModel = modelBakery.getModel(base.modelLocation());
            ResolvedModel glassModel = glass != null ? modelBakery.getModel(glass) : null;
            Table<Direction, ConnectionType, BlockStateModelPart> baseParts = HashBasedTable.create(NUM_DIRECTIONS, NUM_CONNECTIONS);
            Table<Direction, ConnectionType, BlockStateModelPart> glassParts = glassModel != null ? HashBasedTable.create(NUM_DIRECTIONS, NUM_CONNECTIONS) : null;
            int materialFlags = 0;
            ModelState modelState = base.modelState().asModelState();
            Map<String, Boolean> partsVisibility = new HashMap<>(ALL_PART_GROUPS.size());//nb: shared with the delegate

            //start with setting everything invisible (default is true if not in the map)
            for (String group : ALL_PART_GROUPS) {
                partsVisibility.put(group, false);
            }

            //create the delegate to help baking
            DelegateResolvedModel delegate = new DelegateResolvedModel(partsVisibility);

            //now bake all the parts
            for (ConnectionType connectionType : ConnectionType.values()) {
                for (Direction direction : EnumUtils.DIRECTIONS) {
                    //setup visibility
                    String partName = getPartName(direction, connectionType);
                    partsVisibility.put(partName, true);

                    //bake core
                    BlockStateModelPart baked = SimpleModelWrapper.bake(modelBakery, delegate.as(baseModel), modelState);
                    materialFlags |= baked.materialFlags();
                    baseParts.put(direction, connectionType, baked);

                    //if we have glass, bake that
                    if (glassModel != null) {
                        baked = SimpleModelWrapper.bake(modelBakery, delegate.as(glassModel), modelState);
                        glassParts.put(direction, connectionType, baked);
                        materialFlags |= baked.materialFlags();
                    }

                    //reset the visibilities for the next round
                    partsVisibility.put(partName, false);
                }
            }

            return new TransmitterBlockStateModel(baseParts, glassParts, baseModel.resolveParticleMaterial(baseModel.getTopTextureSlots(), modelBakery), materialFlags);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(base.modelLocation());
            if (glass != null) {
                resolver.markDependency(glass);
            }
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
            var builder = new ContextMap.Builder();
            fillAdditionalProperties(this, builder);
            builder.withParameter(NeoForgeModelProperties.PART_VISIBILITY, visibilityMap);
            return builder.create(ContextKeySet.EMPTY);
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

        //copied from Neo as it's private
        private static void fillAdditionalProperties(@Nullable ResolvedModel model, ContextMap.Builder propertiesBuilder) {
            if (model != null) {
                fillAdditionalProperties(model.parent(), propertiesBuilder);
                //noinspection OverrideOnly
                model.wrapped().fillAdditionalProperties(propertiesBuilder);
            }
        }
    }
}
