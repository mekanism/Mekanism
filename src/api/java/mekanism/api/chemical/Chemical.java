package mekanism.api.chemical;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.ReverseTagWrapper;
import net.minecraftforge.registries.ForgeRegistryEntry;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class Chemical<CHEMICAL extends Chemical<CHEMICAL>> extends ForgeRegistryEntry<CHEMICAL> implements IChemicalProvider<CHEMICAL>, IHasTextComponent,
      IHasTranslationKey {

    private final ReverseTagWrapper<CHEMICAL> reverseTags;
    private final Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> attributeMap;

    private final ResourceLocation iconLocation;
    private final boolean hidden;
    private final int tint;

    private String translationKey;

    protected Chemical(ChemicalBuilder<CHEMICAL, ?> builder, ChemicalTags<CHEMICAL> chemicalTags) {
        reverseTags = new ReverseTagWrapper<>(getChemical(), chemicalTags::getCollection);
        this.attributeMap = builder.getAttributeMap();
        this.iconLocation = builder.getTexture();
        this.tint = builder.getColor();
        this.hidden = builder.isHidden();
    }

    @Override
    public CHEMICAL getChemical() {
        return (CHEMICAL) this;
    }

    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = getDefaultTranslationKey();
        }
        return translationKey;
    }

    /**
     * Whether this chemical has an attribute of a certain type.
     *
     * @param type attribute type to check
     *
     * @return if this chemical has the attribute
     */
    public boolean has(Class<? extends ChemicalAttribute> type) {
        return attributeMap.containsKey(type);
    }

    /**
     * Gets the attribute instance of a certain type, or null if it doesn't exist.
     *
     * @param type attribute type to get
     *
     * @return attribute instance
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends ChemicalAttribute> T get(Class<T> type) {
        return (T) attributeMap.get(type);
    }

    /**
     * Adds an attribute to this chemical's attribute map. Will overwrite any existing attribute with the same type.
     *
     * @param attribute attribute to add to this chemical
     */
    public void addAttribute(ChemicalAttribute attribute) {
        attributeMap.put(attribute.getClass(), attribute);
    }

    /**
     * Gets all attribute instances associated with this chemical type.
     *
     * @return collection of attribute instances
     */
    public Collection<ChemicalAttribute> getAttributes() {
        return attributeMap.values();
    }

    /**
     * Gets all attribute types associated with this chemical type.
     *
     * @return collection of attribute types
     */
    public Collection<Class<? extends ChemicalAttribute>> getAttributeTypes() {
        return attributeMap.keySet();
    }

    /**
     * Writes this Chemical to a defined tag compound.
     *
     * @param nbtTags - tag compound to write this Chemical to
     *
     * @return the tag compound this Chemical was written to
     */
    public abstract CompoundNBT write(CompoundNBT nbtTags);

    protected abstract String getDefaultTranslationKey();

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

    /**
     * Whether or not this chemical is hidden.
     *
     * @return if this chemical is hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Checks if this chemical is in a given tag.
     *
     * @param tag The tag to check.
     *
     * @return {@code true} if the chemical is in the tag, {@code false} otherwise.
     */
    public boolean isIn(ITag<CHEMICAL> tag) {
        return tag.contains(getChemical());
    }

    /**
     * Gets the tags that this chemical is a part of.
     *
     * @return All the tags this chemical is a part of.
     */
    public Set<ResourceLocation> getTags() {
        return reverseTags.getTagNames();
    }

    public abstract boolean isEmptyType();
}