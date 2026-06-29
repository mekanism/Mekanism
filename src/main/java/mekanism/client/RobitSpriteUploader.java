package mekanism.client;

import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.AtlasManager.AtlasConfig;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;

public class RobitSpriteUploader {

    public static final Identifier ATLAS_LOCATION = Mekanism.rl("textures/atlas/robit.png");
    public static final Identifier ATLAS_ID = Mekanism.rl("entity/robit");
    public static final RenderType RENDER_TYPE = RenderTypes.entityCutout(ATLAS_LOCATION);


    @SubscribeEvent
    public static void registerAtlases(RegisterTextureAtlasesEvent event) {
        event.register(new AtlasConfig(ATLAS_LOCATION, ATLAS_ID, false));
    }

    public static SpriteId getSpriteId(Identifier texture) {
        return new SpriteId(ATLAS_ID, texture);
    }

    public static TextureAtlas getAtlas() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(ATLAS_ID);
    }
}