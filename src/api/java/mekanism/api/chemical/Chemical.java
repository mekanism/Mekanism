package mekanism.api.chemical;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.tags.IReverseTag;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class Chemical<CHEMICAL extends Chemical<CHEMICAL>> extends ForgeRegistryEntry<CHEMICAL> implements IChemicalProvider<CHEMICAL> {

    private final ChemicalTags<CHEMICAL> chemicalTags;
    private final Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> attributeMap;

    private final ResourceLocation iconLocation;
    private final boolean hidden;
    private final int tint;

    private String translationKey;

    protected Chemical(ChemicalBuilder<CHEMICAL, ?> builder, ChemicalTags<CHEMICAL> chemicalTags) {
        this.chemicalTags = chemicalTags;
        //Copy the map to support addAttribute
        this.attributeMap = new HashMap<>(builder.getAttributeMap());
        this.iconLocation = builder.getTexture();
        this.tint = builder.getColor();
        this.hidden = builder.isHidden();
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
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
    public abstract CompoundTag write(CompoundTag nbtTags);

    /**
     * Gets the default translation key for this chemical.
     */
    protected abstract String getDefaultTranslationKey();

    @Override
    public Component getTextComponent() {
        return TextComponentUtil.translate(getTranslationKey());
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
     * Get the color representation used for displaying in things like durability bars of chemical tanks.
     *
     * @return int representation of color in 0xRRGGBB format
     */
    public int getColorRepresentation() {
        return getTint();
    }

    /**
     * Whether this chemical is hidden.
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
    public boolean is(TagKey<CHEMICAL> tag) {
        return getReverseTag().map(reverseTag -> reverseTag.containsTag(tag))
              .orElse(false);
    }

    /**
     * Gets the tags that this chemical is a part of.
     *
     * @return All the tags this chemical is a part of.
     */
    public Stream<TagKey<CHEMICAL>> getTags() {
        return getReverseTag().map(IReverseTag::getTagKeys).orElseGet(Stream::empty);
    }

    /**
     * Used to look-up the reverse tag that corresponds with this chemical.
     *
     * @return Corresponding reverse tag or empty.
     */
    protected Optional<IReverseTag<CHEMICAL>> getReverseTag() {
        return chemicalTags.getManager().flatMap(manager -> manager.getReverseTag(getChemical()));
    }

    /**
     * Gets whether this chemical is the empty instance.
     *
     * @return {@code true} if this chemical is the empty instance, {@code false} otherwise.
     */
    public abstract boolean isEmptyType();
}