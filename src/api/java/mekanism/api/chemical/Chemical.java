package mekanism.api.chemical;

import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class Chemical<TYPE extends Chemical<TYPE>> extends ForgeRegistryEntry<TYPE> implements IHasTextComponent, IHasTranslationKey {

    private final ResourceLocation iconLocation;
    private final int tint;

    private String translationKey;

    protected Chemical(ChemicalAttributes<TYPE, ?> attributes) {
        this.iconLocation = attributes.getTexture();
        this.tint = attributes.getColor();
    }

    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = getDefaultTranslationKey();
        }
        return translationKey;
    }

    public abstract CompoundNBT write(CompoundNBT nbtTags);

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
     * Get the tint for rendering the chemical
     *
     * @return int representation of color in 0xRRGGBB format
     */
    public int getTint() {
        return tint;
    }

    public abstract boolean isIn(Tag<TYPE> tag);

    public abstract Set<ResourceLocation> getTags();

    public abstract boolean isEmptyType();
}