package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPITags;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.attribute.ChemicalAttributes.Radiation;
import mekanism.api.chemical.attribute.IChemicalAttributeContainer;
import mekanism.api.datamaps.ChemicalOreTag;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault//TODO - 1.22: Debate if we want to remove the non holder based codecs. Maybe we even want to just make chemicals a data pack registry
public class Chemical implements IChemicalProvider, IChemicalAttributeContainer<Chemical> {

    /**
     * A codec which can (de)encode chemicals.
     *
     * @since 10.6.0
     */
    public static final Codec<Chemical> CODEC = MekanismAPI.CHEMICAL_REGISTRY.byNameCodec();
    /**
     * A codec which can (de)encode chemical holders.
     *
     * @since 10.7.11
     */
    public static final Codec<Holder<Chemical>> HOLDER_CODEC = MekanismAPI.CHEMICAL_REGISTRY.holderByNameCodec();
    /**
     * A stream codec which can be used to encode and decode chemicals over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Chemical> STREAM_CODEC = ByteBufCodecs.registry(MekanismAPI.CHEMICAL_REGISTRY_NAME);
    /**
     * A stream codec which can be used to encode and decode chemical holders over the network.
     *
     * @since 10.7.9
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Chemical>> HOLDER_STREAM_CODEC = ByteBufCodecs.holderRegistry(MekanismAPI.CHEMICAL_REGISTRY_NAME);

    /**
     * Tries to parse a chemical.
     *
     * @since 10.7.0
     * @deprecated Prefer accessing via holders {@link #parseHolder(Provider, Tag)}
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public static Optional<Chemical> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(error -> MekanismAPI.logger.error("Tried to load invalid chemical: '{}'", error));
    }


    /**
     * Tries to parse a chemical, defaulting to the empty chemical on parsing failure.
     *
     * @since 10.7.0
     * @deprecated Prefer accessing via holders {@link #parseOptionalHolder(Provider, String)}
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public static Chemical parseOptional(HolderLookup.Provider lookupProvider, String tag) {
        return parseOptionalHolder(lookupProvider, tag).value();
    }

    /**
     * Tries to parse a chemical holder.
     *
     * @since 10.7.11
     */
    public static Optional<Holder<Chemical>> parseHolder(HolderLookup.Provider lookupProvider, Tag tag) {
        return HOLDER_CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(error -> MekanismAPI.logger.error("Tried to load invalid chemical: '{}'", error));
    }

    /**
     * Tries to parse a chemical, defaulting to {@link MekanismAPI#EMPTY_CHEMICAL_HOLDER} on parsing failure.
     *
     * @since 10.7.11
     */
    public static Holder<Chemical> parseOptionalHolder(HolderLookup.Provider lookupProvider, String tag) {
        if (tag.isEmpty()) {
            return MekanismAPI.EMPTY_CHEMICAL_HOLDER;
        }
        Optional<RegistryLookup<Chemical>> chemicalLookup = lookupProvider.lookup(MekanismAPI.CHEMICAL_REGISTRY_NAME);
        //noinspection OptionalIsPresent - Capturing lambda
        if (chemicalLookup.isEmpty()) {
            return MekanismAPI.EMPTY_CHEMICAL_HOLDER;
        }
        return Optional.ofNullable(ResourceLocation.tryParse(tag))
              .map(rl -> ResourceKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, rl))
              .<Holder<Chemical>>flatMap(chemicalLookup.get()::get)
              .orElse(MekanismAPI.EMPTY_CHEMICAL_HOLDER);
    }

    //TODO - 1.21: Switch stream codecs to acting on holders
    private final Holder.Reference<Chemical> builtInRegistryHolder = MekanismAPI.CHEMICAL_REGISTRY.createIntrusiveHolder(this);
    private final Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> attributeMap;

    private final ResourceLocation iconLocation;
    private final int tint;
    private boolean isRadioactive;
    private boolean hasAttributesWithValidation;
    @Nullable
    @Deprecated(forRemoval = true, since = "10.7.11")
    private final TagKey<Item> legacyOreTag;
    @Nullable
    @Deprecated(forRemoval = true, since = "10.7.11")
    private TagKey<Item> oreTag;
    @Deprecated(forRemoval = true, since = "10.7.11")
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
        this.oreTag = this.legacyOreTag = builder.getOreTag();
        this.isGaseous = builder.isGaseous();
    }

    @Override
    public final String toString() {
        return builtInRegistryHolder().getRegisteredName();
    }

    @NotNull
    @Override
    public final Chemical getChemical() {
        return this;
    }

    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            //Note: Because chemical registry has a default key, we have to query the name via the resource key so that we can pass null
            // to makeDescriptionId in cases when our chemical is unregistered
            translationKey = Util.makeDescriptionId("chemical", MekanismAPI.CHEMICAL_REGISTRY.getResourceKey(this)
                  .map(ResourceKey::location)
                  .orElse(null)
            );
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
     *
     * @deprecated Access via {@link ChemicalStack#is(TagKey)} or as a holder.
     */
    @Deprecated(forRemoval = true, since = "10.7.9")
    public boolean is(TagKey<Chemical> tag) {
        return builtInRegistryHolder.is(tag);
    }

    /**
     * Gets the tags that this chemical is a part of.
     *
     * @return All the tags this chemical is a part of.
     *
     * @deprecated Access via {@link ChemicalStack#getTags()} or as a holder.
     */
    @Deprecated(forRemoval = true, since = "10.7.9")
    public Stream<TagKey<Chemical>> getTags() {
        return builtInRegistryHolder.tags();
    }

    /**
     * Helper method to get the holder for this chemical.
     *
     * @since 10.6.0
     * @deprecated If a holder is necessary use {@link #builtInRegistryHolder()}
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public Holder<Chemical> getAsHolder() {
        return builtInRegistryHolder();
    }

    /**
     * Intrusive holder, similar to vanilla this is deprecated and will eventually be moved away from.
     *
     * @since 10.7.11
     */
    @Deprecated
    public Holder.Reference<Chemical> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    /**
     * Gets whether this chemical is the empty instance.
     *
     * @return {@code true} if this chemical is the empty instance, {@code false} otherwise.
     *
     * @deprecated Prefer checking if against {@link MekanismAPI#EMPTY_CHEMICAL_KEY}
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public boolean isEmptyType() {//TODO - 1.21: Re-evaluate
        return this == MekanismAPI.EMPTY_CHEMICAL;
    }

    /**
     * Gets the item tag representing the ore for this slurry.
     *
     * @return The tag for the item the slurry goes with. May be null.
     *
     * @deprecated 10.7.11 Prefer checking against {@link IMekanismDataMapTypes#chemicalOreTag()}, though note it may not contain entries from mods that haven't updated
     * to declaring via datamaps.
     */
    @Nullable
    @Deprecated(forRemoval = true, since = "10.7.11")
    public TagKey<Item> getOreTag() {
        return oreTag;
    }

    /**
     * {@return whether this chemical should render as a gas or more like a fluid}
     *
     * @since 10.7.0
     * @deprecated 10.7.11 Prefer checking against {@link MekanismAPITags.Chemicals#GASEOUS}, though note it may not contain entries from mods that haven't updated to
     * declaring via tags.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public boolean isGaseous() {
        return isGaseous || is(MekanismAPITags.Chemicals.GASEOUS);
    }

    @Internal
    @Deprecated(forRemoval = true, since = "10.7.11")
    public final void updateFromDataMap() {
        ChemicalOreTag tag = builtInRegistryHolder().getData(IMekanismDataMapTypes.INSTANCE.chemicalOreTag());
        oreTag = tag == null ? legacyOreTag : tag.oreTag();
    }

    /**
     * Saves this chemical to a new tag.
     *
     * @throws IllegalStateException if this chemical is empty
     * @since 10.7.0
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public Tag save(HolderLookup.Provider lookupProvider) {
        if (isEmptyType()) {
            throw new IllegalStateException("Cannot encode empty Chemical");
        }
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    /**
     * Saves this chemical to a new tag. Empty chemicals are supported and will be saved as an empty tag.
     *
     * @since 10.7.0
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public Tag saveOptional(HolderLookup.Provider lookupProvider) {
        return isEmptyType() ? new CompoundTag() : save(lookupProvider);
    }
}