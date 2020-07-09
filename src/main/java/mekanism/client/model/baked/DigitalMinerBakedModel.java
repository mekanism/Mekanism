package mekanism.client.model.baked;

import java.util.List;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadTransformation.TextureFilteredTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.Mekanism;
import mekanism.common.base.HolidayManager;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.TextureStitchEvent;

public class DigitalMinerBakedModel extends ExtensionBakedModel<Void> {

    private final QuadTransformation APRIL_FOOLS_TRANSFORM = QuadTransformation.list(
          TextureFilteredTransformation.of(QuadTransformation.texture(AFD_SAD), s -> s.getPath().contains("screen_hello") || s.getPath().contains("screen_cmd")),
          TextureFilteredTransformation.of(QuadTransformation.texture(AFD_TEXT), s -> s.getPath().contains("screen_blank")));
    private final QuadTransformation MAY_4TH_TRANSFORM = TextureFilteredTransformation.of(QuadTransformation.texture(MAY_4TH),
          s -> s.getPath().contains("screen_hello"));

    private static TextureAtlasSprite AFD_SAD, AFD_TEXT, MAY_4TH;

    public DigitalMinerBakedModel(IBakedModel original) {
        super(original);
    }

    public static void preStitch(TextureStitchEvent.Pre event) {
        event.addSprite(Mekanism.rl("block/models/digital_miner_screen_afd_sad"));
        event.addSprite(Mekanism.rl("block/models/digital_miner_screen_afd_text"));
        event.addSprite(Mekanism.rl("block/models/digital_miner_screen_may4th"));
    }

    public static void onStitch(TextureStitchEvent.Post event) {
        AFD_SAD = event.getMap().getSprite(Mekanism.rl("block/models/digital_miner_screen_afd_sad"));
        AFD_TEXT = event.getMap().getSprite(Mekanism.rl("block/models/digital_miner_screen_afd_text"));
        MAY_4TH = event.getMap().getSprite(Mekanism.rl("block/models/digital_miner_screen_may4th"));
    }

    @Override
    public List<BakedQuad> createQuads(QuadsKey<Void> key) {
        List<BakedQuad> quads = key.getQuads();
        if (HolidayManager.MAY_4.isToday()) {
            quads = QuadUtils.transformBakedQuads(quads, MAY_4TH_TRANSFORM);
        } else if (HolidayManager.APRIL_FOOLS.isToday()) {
            quads = QuadUtils.transformBakedQuads(quads, APRIL_FOOLS_TRANSFORM);
        }
        return quads;
    }

    @Override
    protected DigitalMinerBakedModel wrapModel(IBakedModel model) {
        return new DigitalMinerBakedModel(model);
    }
}
