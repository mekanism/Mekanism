package mekanism.client.model.baked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DriveArrayBakedModel extends ExtensionBakedModel<byte[]> {

    private static final float[][] DRIVE_PLACEMENTS = {
          {0, 6F / 16}, {-2F / 16, 6F / 16}, {-4F / 16, 6F / 16}, {-7F / 16, 6F / 16}, {-9F / 16, 6F / 16}, {-11F / 16, 6F / 16},
          {0, 0}, {-2F / 16, 0}, {-4F / 16, 0}, {-7F / 16, 0}, {-9F / 16, 0}, {-11F / 16, 0}
    };

    public DriveArrayBakedModel(BakedModel original) {
        super(original);
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
            DriveStatus status = DriveStatus.STATUSES[driveStatus[i]];
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
                return key.data(driveStatus, Arrays.hashCode(driveStatus), Arrays::equals);
            }
        }
        return null;
    }

    @Override
    protected DriveArrayBakedModel wrapModel(BakedModel model) {
        return new DriveArrayBakedModel(model);
    }
}
