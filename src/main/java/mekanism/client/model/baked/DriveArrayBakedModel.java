package mekanism.client.model.baked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.qio.DriveMetadata;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DriveArrayBakedModel extends ExtensionOverrideBakedModel<byte[]> {

    private static final BiPredicate<byte[], byte[]> DATA_EQUALITY_CHECK = Arrays::equals;
    private static final float[][] DRIVE_PLACEMENTS = {
          {0, 6F / 16}, {-2F / 16, 6F / 16}, {-4F / 16, 6F / 16}, {-7F / 16, 6F / 16}, {-9F / 16, 6F / 16}, {-11F / 16, 6F / 16},
          {0, 0}, {-2F / 16, 0}, {-4F / 16, 0}, {-7F / 16, 0}, {-9F / 16, 0}, {-11F / 16, 0}
    };

    public DriveArrayBakedModel(BakedModel original) {
        super(original, DriveArrayOverrideList::new);
    }

    @Override
    public List<BakedQuad> createQuads(QuadsKey<byte[]> key) {
        byte[] driveStatus = Objects.requireNonNull(key.getData());
        BlockState blockState = Objects.requireNonNull(key.getBlockState());
        RenderType renderType = key.getLayer();
        QuadTransformation rotation = QuadTransformation.rotate(Attribute.getFacing(blockState));
        //Side will always be null as we validate it when creating the key as we don't currently have any of the sides get culled
        Direction side = key.getSide();
        List<BakedQuad> driveQuads = new ArrayList<>();
        for (int i = 0; i < driveStatus.length; i++) {
            DriveStatus status = DriveStatus.BY_ID.apply(driveStatus[i]);
            if (status != DriveStatus.NONE) {
                float[] translation = DRIVE_PLACEMENTS[i];
                QuadTransformation transformation = QuadTransformation.translate(translation[0], translation[1], 0);
                for (BakedQuad bakedQuad : MekanismModelCache.INSTANCE.QIO_DRIVES[status.ordinal()].getQuads(blockState, side, key.getRandom(), ModelData.EMPTY, renderType)) {
                    Quad quad = new Quad(bakedQuad);
                    if (quad.transform(transformation, rotation)) {
                        //Bake and add the quad if we transformed it
                        driveQuads.add(quad.bake());
                    } else {
                        // otherwise, just add the source quad
                        driveQuads.add(bakedQuad);
                    }
                }
            }
        }
        if (!driveQuads.isEmpty()) {
            List<BakedQuad> ret = new ArrayList<>(key.getQuads());
            ret.addAll(driveQuads);
            return ret;
        }
        return key.getQuads();
    }

    @Nullable
    @Override
    public QuadsKey<byte[]> createKey(QuadsKey<byte[]> key, ModelData data) {
        //Skip if we don't have a blockstate or we aren't for the null side (unculled)
        if (key.getBlockState() != null && key.getSide() == null) {
            byte[] driveStatus = data.get(TileEntityQIODriveArray.DRIVE_STATUS_PROPERTY);
            if (driveStatus != null) {
                return key.data(driveStatus, Arrays.hashCode(driveStatus), DATA_EQUALITY_CHECK);
            }
        }
        return null;
    }

    @Override
    protected DriveArrayBakedModel wrapModel(BakedModel model) {
        return new DriveArrayBakedModel(model);
    }

    private static class DriveArrayOverrideList extends ExtendedItemOverrides {

        DriveArrayOverrideList(ItemOverrides original) {
            super(original);
        }

        @Nullable
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            if (!stack.isEmpty() && stack.is(MekanismBlocks.QIO_DRIVE_ARRAY.getItemHolder())) {
                List<IInventorySlot> inventorySlots = ContainerType.ITEM.getAttachmentContainersIfPresent(stack);
                byte[] driveStatus = new byte[TileEntityQIODriveArray.DRIVE_SLOTS];
                boolean hasFrequency = hasFrequency(stack);
                boolean allEmpty = true;
                for (int i = 0; i < driveStatus.length; i++) {
                    DriveStatus status;
                    ItemStack driveStack;
                    if (i < inventorySlots.size()) {
                        driveStack = inventorySlots.get(i).getStack();
                    } else {
                        driveStack = ItemStack.EMPTY;
                    }
                    if (driveStack.isEmpty() || !(driveStack.getItem() instanceof IQIODriveItem driveItem)) {
                        status = DriveStatus.NONE;
                    } else {
                        DriveMetadata metadata = driveStack.getOrDefault(MekanismDataComponents.DRIVE_METADATA, DriveMetadata.EMPTY);
                        if (metadata.isEmpty()) {
                            status = DriveStatus.NONE;
                        } else if (hasFrequency) {
                            allEmpty = false;
                            long countCapacity = driveItem.getCountCapacity(driveStack);
                            if (metadata.count() == countCapacity) {
                                //If we are at max item capacity: Full
                                status = DriveStatus.FULL;
                            } else if (metadata.types() == driveItem.getTypeCapacity(driveStack) || metadata.count() >= countCapacity * 0.75) {
                                //If we are at max type capacity OR we are at 75% or more capacity: Near full
                                status = DriveStatus.NEAR_FULL;
                            } else {
                                //Otherwise: Ready
                                status = DriveStatus.READY;
                            }
                        } else {
                            allEmpty = false;
                            status = DriveStatus.OFFLINE;
                        }
                    }
                    driveStatus[i] = status.status();
                }
                if (!allEmpty) {//Only bother actually applying an override if there are some drives that aren't empty
                    ModelData modelData = ModelData.of(TileEntityQIODriveArray.DRIVE_STATUS_PROPERTY, driveStatus);
                    //TODO: At some point we may want to evaluate caching this
                    return wrap(model, stack, world, entity, seed, modelData, DriveStatusBakedModel::new);
                }
            }
            return original.resolve(model, stack, world, entity, seed);
        }

        private boolean hasFrequency(ItemStack stack) {
            if (stack.getItem() instanceof IFrequencyItem frequencyItem && frequencyItem.getFrequencyType() == FrequencyType.QIO) {
                FrequencyAware<QIOFrequency> frequencyAware = stack.getOrDefault(MekanismDataComponents.QIO_FREQUENCY, FrequencyAware.none());
                return frequencyAware.identity().isPresent() && frequencyAware.getOwner() != null;
            }
            return false;
        }

        private static class DriveStatusBakedModel extends ModelDataBakedModel {

            private final BlockState targetState;

            public DriveStatusBakedModel(BakedModel original, ModelData data) {
                super(original, data);
                this.targetState = MekanismBlocks.QIO_DRIVE_ARRAY.defaultState();
            }

            @NotNull
            @Override
            public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
                return super.getQuads(state == null ? targetState : state, side, rand, data, renderType);
            }
        }
    }
}
