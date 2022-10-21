package mekanism.client.model.baked;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadTransformation.TextureFilteredTransformation;
import mekanism.common.Mekanism;
import mekanism.common.base.HolidayManager;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DigitalMinerBakedModel extends ExtensionBakedModel<Void> {

    @Nullable
    private static TextureAtlasSprite AFD_SAD, AFD_TEXT, MAY_4TH;

    public static void preStitch(TextureStitchEvent.Pre event) {
        event.addSprite(Mekanism.rl("block/models/digital_miner_screen_afd_sad"));
        event.addSprite(Mekanism.rl("block/models/digital_miner_screen_afd_text"));
        event.addSprite(Mekanism.rl("block/models/digital_miner_screen_may4th"));
    }

    public static void onStitch(TextureStitchEvent.Post event) {
        AFD_SAD = event.getAtlas().getSprite(Mekanism.rl("block/models/digital_miner_screen_afd_sad"));
        AFD_TEXT = event.getAtlas().getSprite(Mekanism.rl("block/models/digital_miner_screen_afd_text"));
        MAY_4TH = event.getAtlas().getSprite(Mekanism.rl("block/models/digital_miner_screen_may4th"));
    }

    private final QuadTransformation APRIL_FOOLS_TRANSFORM = QuadTransformation.list(
          TextureFilteredTransformation.of(QuadTransformation.texture(AFD_SAD), s -> s.getPath().contains("screen_hello") || s.getPath().contains("screen_cmd")),
          TextureFilteredTransformation.of(QuadTransformation.texture(AFD_TEXT), s -> s.getPath().contains("screen_blank")));
    private final QuadTransformation MAY_4TH_TRANSFORM = TextureFilteredTransformation.of(QuadTransformation.texture(MAY_4TH),
          s -> s.getPath().contains("screen_hello"));

    public DigitalMinerBakedModel(BakedModel original) {
        super(original);
    }

    @Nullable
    @Override
    protected QuadsKey<Void> createKey(QuadsKey<Void> key, ModelData data) {
        if (MekanismConfig.client.holidays.get()) {
            if (HolidayManager.MAY_4.isToday()) {
                return key.transform(MAY_4TH_TRANSFORM);
            } else if (HolidayManager.APRIL_FOOLS.isToday()) {
                return key.transform(APRIL_FOOLS_TRANSFORM);
            }
        }
        return null;
    }

    @Override
    protected DigitalMinerBakedModel wrapModel(BakedModel model) {
        return new DigitalMinerBakedModel(model);
    }
}
