package mekanism.api.chemical;

import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class Chemical<TYPE extends Chemical<TYPE>> extends ForgeRegistryEntry<TYPE> implements IHasTranslationKey {

    private String translationKey;
    private ResourceLocation iconLocation;
    private TextureAtlasSprite sprite;

    //TODO: Move tint here? Gas doesn't support transparency but infuse type does. Should we just make them both not support it or how should it be handled

    protected Chemical(ResourceLocation registryName, ResourceLocation iconLocation) {
        setRegistryName(registryName);
        this.iconLocation = iconLocation;
    }

    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = getDefaultTranslationKey();
        }
        return translationKey;
    }

    public abstract CompoundNBT write(CompoundNBT nbtTags);

    @Nonnull
    protected abstract String getDefaultTranslationKey();

    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(getTranslationKey());
    }

    /**
     * Gets the resource location of the icon associated with this Chemical.
     *
     * @return The resource location of the icon
     */
    public ResourceLocation getIcon() {
        return iconLocation;
    }

    /**
     * Gets the Sprite associated with this Gas.
     *
     * @return associated IIcon
     */
    public TextureAtlasSprite getSprite() {
        AtlasTexture texMap = Minecraft.getInstance().getTextureMap();
        if (sprite == null) {
            sprite = texMap.getAtlasSprite(getIcon().toString());
        }
        return sprite;
    }

    public void registerIcon(TextureStitchEvent.Pre event) {
        event.addSprite(iconLocation);
    }

    public void updateIcon(AtlasTexture map) {
        sprite = map.getSprite(iconLocation);
    }

    public abstract boolean isIn(Tag<TYPE> tags);

    public abstract Set<ResourceLocation> getTags();
}