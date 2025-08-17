package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.ChemicalRadioactivity;
import mekanism.api.datamaps.chemical.attribute.IChemicalAttribute;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault//TODO - 1.22: Investigate making chemicals a datapack registry
public class Chemical implements IHasTranslationKey, IHasTextComponent {

    /**
     * A codec which can (de)encode chemical holders.
     *
     * @since 10.7.11
     */
    public static final Codec<Holder<Chemical>> CODEC = MekanismAPI.CHEMICAL_REGISTRY.holderByNameCodec();
    /**
     * A stream codec which can be used to encode and decode chemical holders over the network.
     *
     * @since 10.7.9
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Chemical>> STREAM_CODEC = ByteBufCodecs.holderRegistry(MekanismAPI.CHEMICAL_REGISTRY_NAME);

    /**
     * Tries to parse a chemical holder.
     *
     * @since 10.7.11
     */
    public static Optional<Holder<Chemical>> parseHolder(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
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
        Optional<? extends RegistryLookup<Chemical>> chemicalLookup = lookupProvider.lookup(MekanismAPI.CHEMICAL_REGISTRY_NAME);
        if (chemicalLookup.isPresent()) {
            ResourceLocation rl = ResourceLocation.tryParse(tag);
            if (rl != null) {
                Optional<Reference<Chemical>> chemicalReference = chemicalLookup.get().get(ResourceKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, rl));
                if (chemicalReference.isPresent()) {
                    return chemicalReference.get();
                }
            }
        }
        return MekanismAPI.EMPTY_CHEMICAL_HOLDER;
    }

    //TODO - 1.22: Figure out if we should we keep this cache or remove it?
    private final List<IChemicalAttribute> attributes = new ArrayList<>();
    private final List<IChemicalAttribute> attributesView = Collections.unmodifiableList(attributes);
    private final ResourceLocation iconLocation;
    private final int tint;
    private double radioactivity;
    private boolean hasAttributesWithValidation;

    @Nullable
    private String translationKey;

    public Chemical(ChemicalBuilder builder) {
        this.iconLocation = builder.getTexture();
        this.tint = builder.getTint();
    }

    @Override
    public final String toString() {
        //Note: Similar to vanilla we look up the holder and registered name from teh registry
        return MekanismAPI.CHEMICAL_REGISTRY.wrapAsHolder(this).getRegisteredName();
    }

    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("chemical", MekanismAPI.CHEMICAL_REGISTRY.getKeyOrNull(this));
        }
        return translationKey;
    }

    /**
     * Helper to check if this chemical is radioactive without having to look it up from the attributes.
     *
     * @return {@code true} if this chemical is radioactive.
     *
     * @since 10.5.15
     */
    public boolean isRadioactive() {
        return radioactivity > 0;
    }

    /**
     * {@return radiation level of this chemical, or zero if it is not radioactive}
     *
     * @since 10.7.11
     */
    public double getRadioactivity() {//TODO - 1.22: Do we want this to return the baseline instead of zero if it is missing?
        return radioactivity;
    }

    /**
     * Helper to check if this chemical has any attributes that need validation.
     *
     * @return {@code true} if this chemical doesn't fit for {@link mekanism.api.chemical.attribute.ChemicalAttributeValidator#DEFAULT}.
     *
     * @since 10.5.15
     */
    public boolean hasAttributesWithValidation() {
        //Note: We only treat radiation as needing validation if the radiation manager is enabled
        return hasAttributesWithValidation || isRadioactive() && IRadiationManager.INSTANCE.isRadiationEnabled();
    }

    /**
     * Gets an unmodifiable view of all attribute instances associated with this chemical type.
     *
     * @return collection of attribute instances.
     *
     * @since 10.7.11
     */
    public List<IChemicalAttribute> getAttributes() {
        return attributesView;
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

    @Internal//TODO - 1.22: Evaluate if we want to get rid of this or if caching the state of some of this is useful from a performance standpoint
    @MustBeInvokedByOverriders
    public void updateFromDataMap(Holder<Chemical> holder) {
        attributes.clear();
        trackAttribute(holder, IMekanismDataMapTypes.INSTANCE.chemicalFuel());
        trackAttribute(holder, IMekanismDataMapTypes.INSTANCE.chemicalRadioactivity());
        trackAttribute(holder, IMekanismDataMapTypes.INSTANCE.cooledChemicalCoolant());
        trackAttribute(holder, IMekanismDataMapTypes.INSTANCE.heatedChemicalCoolant());
    }

    /**
     * Tracks an attribute if it is present, and update any related cached states.
     *
     * @param holder      The reference holder for this chemical.
     * @param dataMapType The type of the attribute to check for and track.
     *
     * @since 10.7.11
     */
    protected void trackAttribute(Holder<Chemical> holder, DataMapType<Chemical, ? extends IChemicalAttribute> dataMapType) {
        IChemicalAttribute attribute = holder.getData(dataMapType);
        if (attribute != null) {
            attributes.add(attribute);
            if (attribute instanceof ChemicalRadioactivity(double rads)) {
                radioactivity = rads;
            } else {
                hasAttributesWithValidation |= attribute.needsValidation();
            }
        }
    }

    /**
     * Gathers any tooltips this chemical has, and adds them to the list.
     *
     * @param stack       Chemical stack.
     * @param context     Current tooltip context.
     * @param tooltips    List of tooltips to add to.
     * @param tooltipFlag Flag representing if advanced tooltips are to be shown.
     *
     * @see ChemicalStack#appendHoverText(TooltipContext, List, TooltipFlag)
     * @since 10.7.11
     */
    protected void appendHoverText(ChemicalStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
        for (IChemicalAttribute attribute : attributes) {
            attribute.collectTooltips(context, tooltips, tooltipFlag);
        }
    }
}