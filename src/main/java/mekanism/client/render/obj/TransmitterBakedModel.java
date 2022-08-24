package mekanism.client.render.obj;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.client.render.obj.TransmitterModelConfiguration.IconStatus;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.obj.ObjModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class TransmitterBakedModel extends BakedModelWrapper<BakedModel> {

    private static final ChunkRenderTypeSet CUTOUT = ChunkRenderTypeSet.of(RenderType.cutout());
    private static final ChunkRenderTypeSet FULL = ChunkRenderTypeSet.of(RenderType.cutout(), RenderType.translucent());

    private final ObjModel internal;
    @Nullable
    private final ObjModel glass;
    private final IGeometryBakingContext owner;
    private final ModelBakery bakery;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final ModelState modelTransform;
    private final ItemOverrides overrides;
    private final ResourceLocation modelLocation;

    private final Map<QuickHash, List<BakedQuad>> modelCache;

    public TransmitterBakedModel(ObjModel internal, @Nullable ObjModel glass, IGeometryBakingContext owner, ModelBakery bakery,
          Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        //We define our baked variant to be how the item is. As we should always have model data when we have a state
        super(internal.bake(new VisibleModelConfiguration(owner, Arrays.stream(EnumUtils.DIRECTIONS).map(side ->
              side.getName() + (side.getAxis() == Axis.Y ? "NORMAL" : "NONE")).toList()), bakery, spriteGetter, modelTransform, overrides, modelLocation));
        //4^6 number of states, if we have a glass texture (support coloring), multiply by 2
        this.modelCache = new Object2ObjectOpenHashMap<>(glass == null ? 4_096 : 8_192);
        this.internal = internal;
        this.glass = glass;
        this.owner = owner;
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        this.overrides = overrides;
        this.modelLocation = modelLocation;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData,
          @Nullable RenderType renderType) {
        if (side != null) {
            return Collections.emptyList();
        }
        TransmitterModelData data = extraData.get(TileEntityTransmitter.TRANSMITTER_PROPERTY);
        if (data != null) {
            boolean hasColor = data.getHasColor() && renderType == RenderType.translucent();
            QuickHash quickHash = new QuickHash(data.getConnectionsMap(), hasColor);
            return modelCache.computeIfAbsent(quickHash, hash -> {
                List<String> visible = new ArrayList<>();
                for (Direction dir : EnumUtils.DIRECTIONS) {
                    visible.add(dir.getSerializedName() + data.getConnectionType(dir).getSerializedName().toUpperCase(Locale.ROOT));
                }
                return bake(rand, new TransmitterModelConfiguration(owner, visible, extraData), hasColor, extraData, renderType)
                      .getQuads(state, null, rand, extraData, renderType);
            });
        }
        //Fallback to our "default" model arrangement. The item variant uses this
        return super.getQuads(state, null, rand, extraData, renderType);
    }

    /**
     * Rotates the pieces that need rotating.
     */
    private BakedModel bake(RandomSource rand, TransmitterModelConfiguration configuration, boolean hasColor, @NotNull ModelData extraData,
          @Nullable RenderType renderType) {
        TextureAtlasSprite particle = spriteGetter.apply(configuration.getMaterial("particle"));
        ResourceLocation renderTypeHint = configuration.getRenderTypeHint();
        RenderTypeGroup renderTypes = renderTypeHint == null ? RenderTypeGroup.EMPTY : configuration.getRenderType(renderTypeHint);
        IModelBuilder<?> builder = IModelBuilder.of(configuration.useAmbientOcclusion(), configuration.useBlockLight(), configuration.isGui3d(),
              configuration.getTransforms(), overrides, particle, renderTypes);
        addPartQuads(rand, configuration, builder, internal, extraData, renderType);
        if (glass != null && hasColor) {
            addPartQuads(rand, configuration, builder, glass, extraData, renderType);
        }
        return builder.build();
    }

    private void addPartQuads(RandomSource rand, TransmitterModelConfiguration configuration, IModelBuilder<?> builder, ObjModel objModel, @NotNull ModelData extraData,
          @Nullable RenderType renderType) {
        record TransformKey(@Nullable Direction dir, float angle) {}
        TransformKey fallback = new TransformKey(null, 0);
        Map<TransformKey, Set<String>> sortedPieces = new HashMap<>();
        for (String component : objModel.getRootComponentNames()) {
            if (configuration.isComponentVisible(component, true)) {
                if (component.endsWith("NONE")) {
                    Direction dir = directionForPiece(component);
                    //We should not have been able to get here if dir was null but check just in case
                    IconStatus status = configuration.getIconStatus(dir);
                    if (dir != null && status.getAngle() > 0) {
                        //If the part should be rotated, then we need to use a custom IModelTransform
                        sortedPieces.computeIfAbsent(new TransformKey(dir, status.getAngle()), key -> new HashSet<>()).add(component);
                        continue;
                    }
                }
                sortedPieces.computeIfAbsent(fallback, key -> new HashSet<>()).add(component);
            }
        }
        for (Map.Entry<TransformKey, Set<String>> entry : sortedPieces.entrySet()) {
            TransformKey key = entry.getKey();
            ModelState transform = modelTransform;
            if (key.dir != null) {
                //If the part should be rotated, then we need to use a custom IModelTransform
                transform = new TransmitterModelTransform(transform, key.dir, key.angle);
            }
            BakedModel model = objModel.bake(new VisibleModelConfiguration(configuration, entry.getValue()), bakery, spriteGetter, transform, ItemOverrides.EMPTY, modelLocation);
            model.getQuads(null, null, rand, extraData, renderType).forEach(builder::addUnculledFace);
            for (Direction side : EnumUtils.DIRECTIONS) {
                model.getQuads(null, side, rand, extraData, renderType).forEach(face -> builder.addCulledFace(side, face));
            }
        }
    }

    @Nullable
    private static Direction directionForPiece(String piece) {
        return Arrays.stream(EnumUtils.DIRECTIONS).filter(dir -> piece.startsWith(dir.getName())).findFirst().orElse(null);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return glass == null ? CUTOUT : FULL;
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        if (glass == null) {
            return List.of(Sheets.cutoutBlockSheet());
        }
        return List.of(Sheets.cutoutBlockSheet(), fabulous ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet());
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return Collections.singletonList(this);
    }

    public record QuickHash(Object... objs) {

        @Override
        public int hashCode() {
            //TODO: Cache the hashcode?
            return Arrays.hashCode(objs);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof QuickHash other && Arrays.deepEquals(objs, other.objs);
        }
    }
}