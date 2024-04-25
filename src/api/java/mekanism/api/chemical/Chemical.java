package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import mekanism.api.JsonConstants;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.attribute.IChemicalAttributeContainer;
import mekanism.api.chemical.gas.attribute.GasAttributes.Radiation;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class Chemical<CHEMICAL extends Chemical<CHEMICAL>> implements IChemicalProvider<CHEMICAL>, IChemicalAttributeContainer<CHEMICAL> {

    /**
     * Codec to get any kind of chemical, based on a "chemicalType" field.
     *
     * @see ChemicalType
     * @see mekanism.api.chemical.merged.BoxedChemical
     * @since 10.6.0
     */
    public static final Codec<Chemical<?>> BOXED_OPTIONAL_CODEC = ChemicalType.CODEC.dispatch(JsonConstants.CHEMICAL_TYPE, ChemicalType::getTypeFor, type -> switch (type) {
        case GAS -> MekanismAPI.GAS_REGISTRY.byNameCodec().fieldOf(JsonConstants.GAS);
        case INFUSION -> MekanismAPI.INFUSE_TYPE_REGISTRY.byNameCodec().fieldOf(JsonConstants.INFUSE_TYPE);
        case PIGMENT -> MekanismAPI.PIGMENT_REGISTRY.byNameCodec().fieldOf(JsonConstants.PIGMENT);
        case SLURRY -> MekanismAPI.SLURRY_REGISTRY.byNameCodec().fieldOf(JsonConstants.SLURRY);
    });
    /**
     * Codec to get any kind of chemical (that does not accept empty types), based on a "chemicalType" field.
     *
     * @see ChemicalType
     * @see mekanism.api.chemical.merged.BoxedChemical
     * @since 10.6.0
     */
    public static final Codec<Chemical<?>> BOXED_CODEC = BOXED_OPTIONAL_CODEC.validate(chemical -> chemical.isEmptyType() ? DataResult.error(() -> "Chemical must not be mekanism:empty") : DataResult.success(chemical));
    /**
     * StreamCodec to get any kind of chemical stack, based on a "chemicalType" field.
     *
     * @see ChemicalType
     * @see mekanism.api.chemical.merged.BoxedChemical
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Chemical<?>> BOXED_OPTIONAL_STREAM_CODEC = ChemicalType.STREAM_CODEC.<RegistryFriendlyByteBuf>cast()
          .dispatch(ChemicalType::getTypeFor, type -> switch (type) {
              case GAS -> ByteBufCodecs.registry(MekanismAPI.GAS_REGISTRY_NAME);
              case INFUSION -> ByteBufCodecs.registry(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME);
              case PIGMENT -> ByteBufCodecs.registry(MekanismAPI.PIGMENT_REGISTRY_NAME);
              case SLURRY -> ByteBufCodecs.registry(MekanismAPI.SLURRY_REGISTRY_NAME);
          });
    /**
     * StreamCodec to get any kind of chemical (that does not accept the empty type), based on a "chemicalType" field.
     *
     * @see ChemicalType
     * @see mekanism.api.chemical.merged.BoxedChemical
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Chemical<?>> BOXED_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Chemical<?> decode(RegistryFriendlyByteBuf buf) {
            Chemical<?> chemical = BOXED_OPTIONAL_STREAM_CODEC.decode(buf);
            if (chemical.isEmptyType()) {
                throw new DecoderException("Empty Chemicals are not allowed");
            }
            return chemical;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Chemical<?> chemical) {
            if (chemical.isEmptyType()) {
                throw new EncoderException("Empty Chemicals are not allowed");
            }
            BOXED_OPTIONAL_STREAM_CODEC.encode(buf, chemical);
        }
    };

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