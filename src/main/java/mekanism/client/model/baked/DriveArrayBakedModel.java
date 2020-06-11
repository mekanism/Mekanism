package mekanism.client.model.baked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.IModelData;

public class DriveArrayBakedModel extends ExtensionBakedModel<byte[]> {

    private static TextureAtlasSprite baseTex;
    private static TextureAtlasSprite lightTex;

    public DriveArrayBakedModel(IBakedModel original) {
        super(original);
    }

    public static void preStitch(TextureStitchEvent.Pre event) {
        event.addSprite(Mekanism.rl("block/qio_drive_array/drive_base"));
        event.addSprite(Mekanism.rl("block/qio_drive_array/drive_led"));
    }

    public static void onStitch(AtlasTexture map) {
        baseTex = map.getSprite(Mekanism.rl("block/qio_drive_array/drive_base"));
        lightTex = map.getSprite(Mekanism.rl("block/qio_drive_array/drive_led"));
    }

    @Override
    public List<BakedQuad> createQuads(QuadsKey<byte[]> key) {
        byte[] driveStatus = key.getData();
        List<BakedQuad> ret = key.getQuads();
        if (key.getSide() == Attribute.getFacing(key.getBlockState())) {
            ret = new ArrayList<>(ret);
            List<Quad> driveQuads = new ArrayList<>();
            for (int i = 0; i < driveStatus.length; i++) {
                DriveStatus status = DriveStatus.STATUSES[driveStatus[i]];
                if (status != DriveStatus.NONE) {
                    double x = 1 + (i / 4) * 5;
                    double y = 3 + (3 - (i % 4)) * 3;
                    driveQuads.add(new Quad.Builder(baseTex, key.getSide()).rect(new Vec3d(x, y, 16.01), 4, 1).uv(0, 0, 4, 1F).build());
                    int ledIndex = status.ledIndex();
                    if (ledIndex >= 0) {
                        driveQuads.add(new Quad.Builder(lightTex, key.getSide()).rect(new Vec3d(x, y, 16.02), 1, 1).uv(ledIndex, 0, ledIndex + 1, 1F).light(1, 1).build());
                    }
                }
            }
            ret.addAll(QuadUtils.transformAndBake(driveQuads, QuadTransformation.rotate(key.getSide())));
        }
        return ret;
    }

    @Override
    public QuadsKey<byte[]> createKey(QuadsKey<byte[]> key, IModelData data) {
        byte[] driveStatus = data.getData(TileEntityQIODriveArray.DRIVE_STATUS_PROPERTY);
        if (driveStatus == null)
            return null;
        return key.data(driveStatus, Arrays.hashCode(driveStatus), Arrays::equals);
    }
}
