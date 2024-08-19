package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.attribute.IChemicalAttributeContainer;
import mekanism.api.chemical.attribute.ChemicalAttributes.Radiation;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class Chemical implements IChemicalProvider, IChemicalAttributeContainer<Chemical> {

    /**
     * A codec which can (de)encode chemicals.
     *
     * @since 10.6.0
     */
    public static final Codec<Chemical> CODEC = MekanismAPI.CHEMICAL_REGISTRY.byNameCodec();
    /**
     * A stream codec which can be used to encode and decode chemicals over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Chemical> STREAM_CODEC = ByteBufCodecs.registry(MekanismAPI.CHEMICAL_REGISTRY_NAME);

    /**
     * Tries to parse a chemical.
     *
     * @since 10.6.10
     */
    public static Optional<Chemical> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(error -> MekanismAPI.logger.error("Tried to load invalid chemical: '{}'", error));
    }


    /**
     * Tries to parse a chemical stack, defaulting to {@link MekanismAPI#EMPTY_CHEMICAL} on parsing failure.
     *
     * @since 10.6.10
     */
    public static Chemical parseOptional(HolderLookup.Provider lookupProvider, String tag) {
        if (tag.isEmpty()) {
            return MekanismAPI.EMPTY_CHEMICAL;
        }
        ResourceLocation name = ResourceLocation.tryParse(tag);
        return name == null ? MekanismAPI.EMPTY_CHEMICAL : MekanismAPI.CHEMICAL_REGISTRY.get(name);
    }

    private final Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> attributeMap;

    private final ResourceLocation iconLocation;
    private final int tint;
    private boolean isRadioactive;
    private boolean hasAttributesWithValidation;
    @Nullable
    private final TagKey<Item> oreTag;
    private final boolean isGaseous;

    @Nullable
    private String translationKey;

    public Chemical(ChemicalBuilder builder) {
        //Copy the map to support addAttribute
        this.attributeMap = new HashMap<>(builder.getAttributeMap());
        this.iconLocation = builder.getTexture();
        this.tint = builder.getTint();
        this.isRadioactive = attributeMap.containsKey(Radiation.class);
        this.hasAttributesWithValidation = isRadioactive || attributeMap.values().stream().anyMatch(ChemicalAttribute::needsValidation);
        this.oreTag = builder.getOreTag();
        this.isGaseous = builder.isGaseous();
    }

    @Override
    public String toString() {
        return "[Chemical: " + getRegistryName() + "]";
    }

    @NotNull
    @Override
    public Chemical getChemical() {
        return this;
    }

    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("chemical", getRegistryName());
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
     * Checks if this chemical is in a given tag.
     *
     * @param tag The tag to check.
     *
     * @return {@code true} if the chemical is in the tag, {@code false} otherwise.
     */
    public boolean is(TagKey<Chemical> tag) {
        return getAsHolder().is(tag);
    }

    /**
     * Gets the tags that this chemical is a part of.
     *
     * @return All the tags this chemical is a part of.
     */
    public Stream<TagKey<Chemical>> getTags() {
        return getAsHolder().tags();
    }

    /**
     * Helper method to get the holder for this chemical. Unlike {@link net.minecraft.world.item.Item#builtInRegistryHolder()} and similar, this looks up the holder from
     * the registry when called.
     *
     * @since 10.6.0
     */
    public Holder<Chemical> getAsHolder() {
        return MekanismAPI.CHEMICAL_REGISTRY.wrapAsHolder(this);
    }

    /**
     * Gets whether this chemical is the empty instance.
     *
     * @return {@code true} if this chemical is the empty instance, {@code false} otherwise.
     */
    public boolean isEmptyType() {
        return this == MekanismAPI.EMPTY_CHEMICAL;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return MekanismAPI.CHEMICAL_REGISTRY.getKey(this);
    }

    @Override
    public ChemicalStack getStack(long size) {
        return new ChemicalStack(this, size);
    }

    /**
     * Gets the item tag representing the ore for this slurry.
     *
     * @return The tag for the item the slurry goes with. May be null.
     */
    @Nullable
    public TagKey<Item> getOreTag() {
        return oreTag;
    }

    /**
     * {@return whether this chemical should render as a gas or more like a fluid}
     *
     * @since 10.6.10
     */
    public boolean isGaseous() {
        return isGaseous;
    }

    /**
     * Saves this chemical to a new tag.
     *
     * @throws IllegalStateException if this chemical is empty
     * @since 10.6.10
     */
    public Tag save(HolderLookup.Provider lookupProvider) {
        if (isEmptyType()) {
            throw new IllegalStateException("Cannot encode empty Chemical");
        }
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    /**
     * Saves this chemical to a new tag. Empty chemicals are supported and will be saved as an empty tag.
     *
     * @since 10.6.10
     */
    public Tag saveOptional(HolderLookup.Provider lookupProvider) {
        return isEmptyType() ? new CompoundTag() : save(lookupProvider);
    }
}