package mekanism.api.chemical;

import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.text.IHasTextComponent;
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

public abstract class Chemical<TYPE extends Chemical<TYPE>> extends ForgeRegistryEntry<TYPE> implements IHasTextComponent, IHasTranslationKey {

    private String translationKey;
    private ResourceLocation iconLocation;
    private TextureAtlasSprite sprite;

    private int tint = 0xFFFFFF;

    protected Chemical(ResourceLocation iconLocation) {
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

    //TODO: Make sure we use getTextComponent where we can instead of the translation key (might already be done)
    @Override
    public ITextComponent getTextComponent() {
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
        if (sprite == null) {
            sprite = Minecraft.getInstance().getTextureMap().getAtlasSprite(getIcon().toString());
        }
        return sprite;
    }

    public void registerIcon(TextureStitchEvent.Pre event) {
        event.addSprite(getIcon());
    }

    public void updateIcon(AtlasTexture map) {
        sprite = map.getSprite(getIcon());
    }

    /**
     * Get the tint for rendering the chemical
     *
     * @return int representation of color in 0xRRGGBB format
     */
    public int getTint() {
        return tint;
    }

    /**
     * Sets the tint for the chemical
     *
     * @param tint int representation of color in 0xRRGGBB format
     */
    public void setTint(int tint) {
        this.tint = tint;
    }

    public abstract boolean isIn(Tag<TYPE> tag);

    public abstract Set<ResourceLocation> getTags();

    public abstract boolean isEmptyType();
}