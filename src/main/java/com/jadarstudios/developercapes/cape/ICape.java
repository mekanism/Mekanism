/**
 * DeveloperCapes by Jadar
 * License: MIT License
 * (https://raw.github.com/jadar/DeveloperCapes/master/LICENSE)
 * version 4.0.0.x
 */
package com.jadarstudios.developercapes.cape;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

/**
 * Any class implementing this will be requested to act as a cape.
 * 
 * @author jadar
 */
public interface ICape {

    public String getName();

    public ITextureObject getTexture();

    public ResourceLocation getLocation();

    public void loadTexture(AbstractClientPlayer player);

    public boolean isTextureLoaded(AbstractClientPlayer player);
}