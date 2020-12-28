/**
 * DeveloperCapes by Jadar
 * License: MIT License
 * (https://raw.github.com/jadar/DeveloperCapes/master/LICENSE)
 * version 3.3.0.0
 */
package com.jadarstudios.developercapes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.IImageBuffer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * This class is an implementation of {@link IImageBuffer} that allows capes to be in HD
 *
 * @author Jadar
 */
@SideOnly(Side.CLIENT)
public class HDImageBuffer implements IImageBuffer {
    @Override
    public BufferedImage parseUserSkin(final BufferedImage texture) {
        if (texture == null)
            return null;
        int imageWidth = texture.getWidth(null) <= 64 ? 64 : texture.getWidth(null);
        int imageHeight = texture.getHeight(null) <= 32 ? 32 : texture.getHeight(null);

        BufferedImage capeImage = new BufferedImage(imageWidth, imageHeight, 2);

        Graphics graphics = capeImage.getGraphics();
        graphics.drawImage(texture, 0, 0, null);
        graphics.dispose();

        return capeImage;
    }

    @Override
    public void func_152634_a() {}
}