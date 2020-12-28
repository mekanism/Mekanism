/**
 * DeveloperCapes by Jadar
 * License: MIT License
 * (https://raw.github.com/jadar/DeveloperCapes/master/LICENSE)
 * version 4.0.0.x
 */
package com.jadarstudios.developercapes.cape;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

/**
 * Abstract Implementation of ICape used within Dev. Capes
 * 
 * @author jadar
 */
public abstract class AbstractCape implements ICape {
    protected String name;
    protected ITextureObject texture;
    protected ResourceLocation location;

    public AbstractCape(String name) {
        this.name = name;
    }

    public AbstractCape() {}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ITextureObject getTexture() {
        return this.texture;
    }

    @Override
    public ResourceLocation getLocation() {
        return this.location;
    }
}