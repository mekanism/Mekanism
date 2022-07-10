package mekanism.client;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RobitSpriteUploader extends TextureAtlasHolder {

    public static final ResourceLocation ATLAS_LOCATION = Mekanism.rl("textures/atlas/robit.png");
    public static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(ATLAS_LOCATION);
    public static final List<RenderType> RENDER_TYPES = Collections.singletonList(RENDER_TYPE);
    @Nullable
    public static RobitSpriteUploader UPLOADER;

    public RobitSpriteUploader(TextureManager textureManager) {
        super(textureManager, ATLAS_LOCATION, "entity/robit");
        UPLOADER = this;
    }

    @NotNull
    @Override
    protected Stream<ResourceLocation> getResourcesToLoad() {
        return MekanismAPI.robitSkinRegistry().getValues().stream().flatMap(skin -> skin.getTextures().stream());
    }

    @NotNull
    @Override
    public TextureAtlasSprite getSprite(@NotNull ResourceLocation location) {
        return super.getSprite(location);
    }
}