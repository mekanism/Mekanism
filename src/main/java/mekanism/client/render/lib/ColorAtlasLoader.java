package mekanism.client.render.lib;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import mekanism.common.Mekanism;
import mekanism.common.lib.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;

public class ColorAtlasLoader {

    private static final int ATLAS_SIZE = 16;

    public static List<Color> load(ResourceLocation rl, int count) {
        List<Color> ret = new ArrayList<>();
        try {
            loadColorAtlas(rl, count, ret);
        } catch (Exception e) {
            Mekanism.logger.error("Failed to load color atlas: " + rl, e);
        }
        return ret;
    }

    private static void loadColorAtlas(ResourceLocation rl, int count, List<Color> ret) throws IOException {
        IResource resource = Minecraft.getInstance().getResourceManager().getResource(rl);
        BufferedImage img = ImageIO.read(resource.getInputStream());
        for (int i = 0; i < count; i++) {
            ret.add(Color.argb(img.getRGB(i % ATLAS_SIZE, i / ATLAS_SIZE)));
        }
    }
}
