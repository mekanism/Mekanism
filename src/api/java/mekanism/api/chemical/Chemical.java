package mekanism.api.chemical;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.attribute.IChemicalAttributeContainer;
import mekanism.api.chemical.gas.attribute.GasAttributes.Radiation;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class Chemical<CHEMICAL extends Chemical<CHEMICAL>> implements IChemicalProvider<CHEMICAL>, IChemicalAttributeContainer<CHEMICAL> {

    private final Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> attributeMap;

    private final ResourceLocation iconLocation;
    private final boolean hidden;
    private final int tint;
    private boolean isRadioactive;
    private boolean hasAttributesWithValidation;

    @Nullable
    private String translationKey;

    protected Chemical(ChemicalBuilder<CHEMICAL, ?> builder) {
        //Copy the map to support addAttribute
        this.attributeMap = new HashMap<>(builder.getAttributeMap());
        this.iconLocation = builder.getTexture();
        this.tint = builder.getTint();
        this.hidden = builder.isHidden();
        this.isRadioactive = attributeMap.containsKey(Radiation.class);
        this.hasAttributesWithValidation = isRadioactive || attributeMap.values().stream().anyMatch(ChemicalAttribute::needsValidation);
    }

    @NotNull
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

    @Override
    public boolean has(Class<? extends ChemicalAttribute> type) {
        return attributeMap.containsKey(type);
    }

    /**
     * Helper to check if this chemical is radioactive without having to look it up from the attributes.
     *
     * @return {@code true} if this chemical is radioactive.
     *
     * @since 10.5.15
     */
    public boolean isRadioactive() {
        return isRadioactive;
    }

    /**
     * Helper to check if this chemical has any attributes that need validation.
     *
     * @return {@code true} if this chemical doesn't fit for {@link mekanism.api.chemical.attribute.ChemicalAttributeValidator#DEFAULT}.
     *
     * @since 10.5.15
     */
    public boolean hasAttributesWithValidation() {
        return hasAttributesWithValidation;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <ATTRIBUTE extends ChemicalAttribute> ATTRIBUTE get(Class<ATTRIBUTE> type) {
        return (ATTRIBUTE) attributeMap.get(type);
    }

    /**
     * Adds an attribute to this chemical's attribute map. Will overwrite any existing attribute with the same type.
     *
     * @param attribute attribute to add to this chemical
     */
    public void addAttribute(ChemicalAttribute attribute) {
        attributeMap.put(attribute.getClass(), attribute);
        if (attribute instanceof Radiation) {
            isRadioactive = true;
            hasAttributesWithValidation = true;
        } else if (attribute.needsValidation()) {
            hasAttributesWithValidation = true;
        }
    }

    @Override
    public Collection<ChemicalAttribute> getAttributes() {
        return attributeMap.values();
    }

    @Override
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
     * @return int representation of color in RRGGBB format
     */
    public int getTint() {
        return tint;
    }

    /**
     * Get the color representation used for displaying in things like durability bars of chemical tanks.
     *
     * @return int representation of color in RRGGBB format
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
        return getRegistry().wrapAsHolder((CHEMICAL) this).is(tag);
    }

    /**
     * Gets the tags that this chemical is a part of.
     *
     * @return All the tags this chemical is a part of.
     */
    public Stream<TagKey<CHEMICAL>> getTags() {
        return getRegistry().wrapAsHolder((CHEMICAL) this).tags();
    }

    /**
     * Gets whether this chemical is the empty instance.
     *
     * @return {@code true} if this chemical is the empty instance, {@code false} otherwise.
     */
    public abstract boolean isEmptyType();

    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public ResourceLocation getRegistryName() {
        //May be null if called before the object is registered
        return getRegistry().getKey((CHEMICAL) this);
    }

    /**
     * Gets the registry that this chemical will be registered to. (For use in helpers that look up the registry name and interact with tags).
     *
     * @return Registry this chemical will be registered to.
     *
     * @since 10.5.0
     */
    protected abstract Registry<CHEMICAL> getRegistry();
}