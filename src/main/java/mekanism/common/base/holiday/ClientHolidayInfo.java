package mekanism.common.base.holiday;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadTransformation.TextureFilteredTransformation;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import org.jetbrains.annotations.Nullable;

/**
 * @apiNote Only reference this class from the client side. It is in the common package to allow for keeping holidays package private
 */
@EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT)
public final class ClientHolidayInfo {

    private ClientHolidayInfo() {
    }

    private static final ResourceLocation MINER_SCREEN = Mekanism.rl("block/models/digital_miner_screen_");
    private static final Predicate<ResourceLocation> IS_HELLO_SCREEN = s -> s.getPath().contains("screen_hello");
    private static final Predicate<ResourceLocation> IS_CMD_SCREEN = s -> s.getPath().contains("screen_cmd");
    private static final Predicate<ResourceLocation> IS_BLANK_SCREEN = s -> s.getPath().contains("screen_blank");
    private static Map<Holiday, QuadTransformation> HOLIDAY_MINER_TRANSFORMS = Collections.emptyMap();

    @SubscribeEvent
    public static void onStitch(TextureAtlasStitchedEvent event) {
        TextureAtlas atlas = event.getAtlas();
        if (!atlas.location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            return;
        }
        HOLIDAY_MINER_TRANSFORMS = Map.of(
              AprilFools.INSTANCE, QuadTransformation.list(
                    TextureFilteredTransformation.of(minerTexture(atlas, "afd_sad"), IS_HELLO_SCREEN.or(IS_CMD_SCREEN)),
                    TextureFilteredTransformation.of(minerTexture(atlas, "afd_text"), IS_BLANK_SCREEN)
              ),
              May4.INSTANCE, TextureFilteredTransformation.of(minerTexture(atlas, "may4th"), IS_HELLO_SCREEN)
        );
    }

    private static QuadTransformation minerTexture(TextureAtlas atlas, String screen) {
        return texture(atlas, MINER_SCREEN.withSuffix(screen));
    }

    private static QuadTransformation texture(TextureAtlas atlas, ResourceLocation location) {
        return QuadTransformation.texture(atlas.getSprite(location));
    }

    @Nullable
    public static QuadTransformation getMinerTransform() {
        if (HolidayManager.areHolidaysEnabled()) {
            for (Map.Entry<Holiday, QuadTransformation> entry : HOLIDAY_MINER_TRANSFORMS.entrySet()) {
                if (entry.getKey().isToday()) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
}