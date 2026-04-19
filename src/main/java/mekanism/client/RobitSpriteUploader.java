package mekanism.client;

import java.util.Collections;
import java.util.List;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.sprite.AtlasManager.AtlasConfig;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: Maybe AtlasManager.AtlasConfig ?
public class RobitSpriteUploader {

    public static final Identifier ATLAS_LOCATION = Mekanism.rl("textures/atlas/robit.png");
    public static final RenderType RENDER_TYPE = RenderTypes.entityCutout(ATLAS_LOCATION);
    public static final List<RenderType> RENDER_TYPES = Collections.singletonList(RENDER_TYPE);
    @Nullable
    public static RobitSpriteUploader UPLOADER;

    @SubscribeEvent
    public static void registerAtlases(RegisterTextureAtlasesEvent event) {
        event.register(new AtlasConfig(ATLAS_LOCATION, Mekanism.rl("entity/robit"), false));
    }

    public static SpriteId getSpriteId(Identifier texture) {
        return new SpriteId(ATLAS_LOCATION, texture);
    }

    public TextureAtlasSprite getSprite(@NotNull Identifier location) {
        return super.getSprite(location);
    }
}