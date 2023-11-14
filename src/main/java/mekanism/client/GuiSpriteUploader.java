package mekanism.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.client.gui.GuiUtils;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiSpriteUploader extends TextureAtlasHolder {

    //public static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(ATLAS_LOCATION);
    public static GuiSpriteUploader UPLOADER;
    private static Map<ResourceLocation, ResourceLocation> MAPPED_LOCATIONS = new HashMap<>();

    public GuiSpriteUploader(TextureManager textureManager) {
        super(textureManager, GuiUtils.GUI_ATLAS, GuiUtils.GUI_ATLAS);
        UPLOADER = this;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getSprite(@NotNull ResourceLocation location) {
        location = MAPPED_LOCATIONS.computeIfAbsent(location, loc->{
            if (loc.getPath().endsWith(".png")) {
                loc = loc.withPath(loc.getPath().substring(0, loc.getPath().length()-4));
            }
            if (loc.getPath().startsWith("textures/mekgui/")) {
                loc = loc.withPath(loc.getPath().substring(16));
            }
            return loc;
        });
        return super.getSprite(location);
    }
}