package mekanism.client.model.item;

import com.google.common.base.Suppliers;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.resource.LargeResourceStack;
import mekanism.client.model.blockstate.QIODriveArrayBlockStateModel;
import mekanism.common.component.FrequencyAware;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.qio.DriveMetadata;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

///Copied and adapted from [CuboidItemModelWrapper]
public record QIODriveArrayItemModel(
      QIODriveArrayBlockStateModel blockStateModel,
      List<ItemTintSource> tints,
      ModelRenderProperties properties,
      Matrix4fc transformation,
      Supplier<Vector3fc[]> extents
) implements ItemModel {

    QIODriveArrayItemModel(
          QIODriveArrayBlockStateModel blockStateModel,
          List<ItemTintSource> tints,
          ModelRenderProperties properties,
          Matrix4fc transformation
    ) {
        this(blockStateModel, tints, properties, transformation, Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(allQuads(blockStateModel.basePart()))));
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        if (item.hasFoil()) {
            ItemStackRenderState.FoilType foilType = ItemStackRenderState.FoilType.STANDARD;
            layer.setFoilType(foilType);
            output.setAnimated();
            output.appendModelIdentityElement(foilType);
        }

        if (!this.tints.isEmpty()) {
            IntList tintLayers = layer.tintLayers();

            for (ItemTintSource tintSource : this.tints) {
                int tint = tintSource.calculate(item, level, owner == null ? null : owner.asLivingEntity());
                tintLayers.add(tint);
                output.appendModelIdentityElement(tint);
            }
        }

        layer.setExtents(this.extents);
        layer.setLocalTransform(this.transformation);
        this.properties.applyToLayer(layer, displayContext);
        List<BakedQuad> bakedQuads = layer.prepareQuadList();
        bakedQuads.addAll(allQuads(blockStateModel.basePart()));
        long driveStatus = getDriveStatus(item);
        if (driveStatus != 0) {
            output.appendModelIdentityElement(driveStatus);
            List<BlockStateModelPart> parts = new ArrayList<>();
            blockStateModel.collectDriveParts(parts, driveStatus);
            for (BlockStateModelPart drivePart : parts) {
                bakedQuads.addAll(allQuads(drivePart));
            }
        }
        //noinspection deprecation
        if (this.blockStateModel.hasMaterialFlag(BakedQuad.FLAG_ANIMATED)) {
            output.setAnimated();
        }
    }

    private static long getDriveStatus(ItemStack stack) {
        long driveStatus = 0;
        if (!stack.isEmpty() && stack.is(MekanismBlocks.QIO_DRIVE_ARRAY.getItemHolder())) {
            List<LargeResourceStack<ItemResource>> inventorySlots = ContainerType.ITEM.getAttachedContents(stack);
            boolean hasFrequency = hasFrequency(stack);
            for (int i = 0; i < TileEntityQIODriveArray.DRIVE_SLOTS; i++) {
                DriveStatus status;
                ItemResource driveData;
                if (i < inventorySlots.size()) {
                    driveData = inventorySlots.get(i).resource();
                } else {
                    break;
                }
                if (driveData.isEmpty() || !(driveData.getItem() instanceof IQIODriveItem driveItem)) {
                    continue;
                } else {
                    DriveMetadata metadata = driveData.getOrDefault(MekanismDataComponents.DRIVE_METADATA, DriveMetadata.EMPTY);
                    if (hasFrequency) {
                        long countCapacity = driveItem.getCountCapacity();
                        if (metadata.count() == countCapacity) {
                            //If we are at max item capacity: Full
                            status = DriveStatus.FULL;
                        } else if (metadata.types() == driveItem.getTypeCapacity() || metadata.count() >= countCapacity * 0.75) {
                            //If we are at max type capacity OR we are at 75% or more capacity: Near full
                            status = DriveStatus.NEAR_FULL;
                        } else {
                            //Otherwise: Ready
                            status = DriveStatus.READY;
                        }
                    } else {
                        status = DriveStatus.OFFLINE;
                    }
                }
                driveStatus = TileEntityQIODriveArray.updateStatus(i, status, driveStatus);
            }
        }
        return driveStatus;
    }

    private static boolean hasFrequency(ItemStack stack) {
        if (stack.getItem() instanceof IFrequencyItem frequencyItem && frequencyItem.getFrequencyType() == FrequencyTypes.QIO) {
            FrequencyAware<QIOFrequency> frequencyAware = stack.getOrDefault(MekanismDataComponents.QIO_FREQUENCY, FrequencyAware.none());
            return frequencyAware.identity().isPresent() && frequencyAware.getOwner() != null;
        }
        return false;
    }

    private static List<BakedQuad> allQuads(BlockStateModelPart modelPart) {
        //should be the case unless resource pack AND custom model classes
        if (modelPart instanceof SimpleModelWrapper simpleModelWrapper) {
            return simpleModelWrapper.quads().getAll();
        }
        List<BakedQuad> quads = new ArrayList<>(modelPart.getQuads(null));
        for (Direction direction : EnumUtils.DIRECTIONS) {
            quads.addAll(modelPart.getQuads(direction));
        }
        return quads;
    }

    public record Unbaked(CuboidItemModelWrapper.Unbaked cuboidUnbaked) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = CuboidItemModelWrapper.Unbaked.MAP_CODEC.xmap(Unbaked::new, Unbaked::cuboidUnbaked);

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(cuboidUnbaked.model());
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            QIODriveArrayBlockStateModel stateModel = new QIODriveArrayBlockStateModel.Unbaked(new Variant(cuboidUnbaked.model(), Variant.SimpleModelState.DEFAULT))
                  .bake(context.blockModelBaker());
            ResolvedModel resolvedBaseModel = baker.getModel(cuboidUnbaked.model());
            ModelRenderProperties properties = new ModelRenderProperties(resolvedBaseModel.getTopGuiLight().lightLikeBlock(), stateModel.particleMaterial(), resolvedBaseModel.getTopTransforms());
            Matrix4fc modelTransform = Transformation.compose(transformation, cuboidUnbaked.transformation());
            return new QIODriveArrayItemModel(stateModel, cuboidUnbaked.tints(), properties, modelTransform);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
