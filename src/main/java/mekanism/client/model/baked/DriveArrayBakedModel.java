package mekanism.client.model.baked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadTransformation.TextureFilteredTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.IModelData;

public class DriveArrayBakedModel extends ExtensionBakedModel<byte[]> {

    private static float[][] DRIVE_PLACEMENTS = new float[][] {
        {0, 6F/16}, {-2F/16, 6F/16}, {-4F/16, 6F/16}, {-7F/16, 6F/16}, {-9F/16, 6F/16}, {-11F/16, 6F/16},
        {0, 0}, {-2F/16, 0}, {-4F/16, 0}, {-7F/16, 0}, {-9F/16, 0}, {-11F/16, 0}
    };

    private Map<DriveStatus, IBakedModel> driveModels = new Object2ObjectOpenHashMap<>();

    public DriveArrayBakedModel(IBakedModel original, ModelBakeEvent evt) {
        super(original);

        for (DriveStatus status : DriveStatus.values()) {
            if (status == DriveStatus.NONE)
                continue;
            driveModels.put(status, evt.getModelRegistry().get(status.getModel()));
        }
    }

    @Override
    public List<BakedQuad> createQuads(QuadsKey<byte[]> key) {
        byte[] driveStatus = key.getData();
        List<BakedQuad> ret = key.getQuads();
        if (key.getSide() == Attribute.getFacing(key.getBlockState())) {
            ret = new ArrayList<>(ret);
            List<BakedQuad> driveQuads = new ArrayList<>();
            for (int i = 0; i < driveStatus.length; i++) {
                DriveStatus status = DriveStatus.STATUSES[driveStatus[i]];
                if (status != DriveStatus.NONE) {
                    driveQuads.addAll(getDriveQuads(i, status, key));
                }
            }
            ret.addAll(QuadUtils.transformBakedQuads(driveQuads, QuadTransformation.rotate(key.getSide())));
        }
        return QuadUtils.transformBakedQuads(ret, TextureFilteredTransformation.of(QuadTransformation.fullbright, rl -> rl.getPath().contains("led")));
    }

    private List<BakedQuad> getDriveQuads(int index, DriveStatus status, QuadsKey<byte[]> key) {
        List<BakedQuad> ret = driveModels.get(status).getQuads(key.getBlockState(), null, key.getRandom());
        float[] translation = DRIVE_PLACEMENTS[index];
        return QuadUtils.transformBakedQuads(ret, QuadTransformation.translate(new Vector3d(translation[0], translation[1], 0)));
    }

    @Override
    public QuadsKey<byte[]> createKey(QuadsKey<byte[]> key, IModelData data) {
        byte[] driveStatus = data.getData(TileEntityQIODriveArray.DRIVE_STATUS_PROPERTY);
        if (driveStatus == null) {
            return null;
        }
        return key.data(driveStatus, Arrays.hashCode(driveStatus), Arrays::equals);
    }
}
