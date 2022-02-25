package mekanism.client;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;

public class RobitSpriteUploader extends TextureAtlasHolder {

    public static final ResourceLocation ATLAS_LOCATION = Mekanism.rl("textures/atlas/robit.png");
    public static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(ATLAS_LOCATION);
    @Nullable
    public static RobitSpriteUploader UPLOADER;

    public RobitSpriteUploader(TextureManager textureManager) {
        super(textureManager, ATLAS_LOCATION, "entity/robit");
        UPLOADER = this;
    }

    @Nonnull
    @Override
    protected Stream<ResourceLocation> getResourcesToLoad() {
        return MekanismAPI.robitSkinRegistry().getValues().stream().flatMap(skin -> skin.getTextures().stream());
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getSprite(@Nonnull ResourceLocation location) {
        return super.getSprite(location);
    }
}