package mekanism.api.chemical.merged;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

/**
 * Helper class to keep track of chemicals and what specific type they are
 */
@NothingNullByDefault
public class BoxedChemical implements IHasTextComponent {

    /**
     * Empty Boxed Chemical instance.
     */
    public static final BoxedChemical EMPTY = new BoxedChemical(ChemicalType.GAS, MekanismAPI.EMPTY_GAS);
    /**
     * Codec to get any kind of chemical (that does not accept empty types) as a boxed chemical.
     *
     * @see Chemical#BOXED_CODEC
     * @since 10.6.0
     */
    public static final Codec<BoxedChemical> CODEC = Chemical.BOXED_CODEC.xmap(BoxedChemical::box, BoxedChemical::getChemical);
    /**
     * Codec to get any kind of chemical as a boxed chemical.
     *
     * @see Chemical#BOXED_OPTIONAL_CODEC
     * @since 10.6.0
     */
    public static final Codec<BoxedChemical> OPTIONAL_CODEC = Chemical.BOXED_OPTIONAL_CODEC.xmap(BoxedChemical::box, BoxedChemical::getChemical);
    /**
     * StreamCodec to get any kind of chemical (that does not accept the empty type) as a boxed chemical.
     *
     * @see Chemical#BOXED_STREAM_CODEC
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, BoxedChemical> STREAM_CODEC = Chemical.BOXED_STREAM_CODEC.map(BoxedChemical::box, BoxedChemical::getChemical);
    /**
     * StreamCodec to get any kind of chemical as a boxed chemical.
     *
     * @see Chemical#BOXED_OPTIONAL_STREAM_CODEC
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, BoxedChemical> OPTIONAL_STREAM_CODEC = Chemical.BOXED_OPTIONAL_STREAM_CODEC.map(BoxedChemical::box, BoxedChemical::getChemical);

    /**
     * Boxes a Chemical.
     *
     * @param chemical Chemical to box.
     *
     * @return Boxed Chemical.
     */
    public static BoxedChemical box(Chemical<?> chemical) {
        if (chemical.isEmptyType()) {
            //TODO: Do we care that this can lose the type of chemical as it uses a single instance that always has a type of gas?
            return EMPTY;
        }
        return new BoxedChemical(ChemicalType.getTypeFor(chemical), chemical);
    }

    /**
     * Tries to parse a boxed chemical. Empty boxed chemicals cannot be parsed with this method.
     *
     * @since 10.6.0
     */
    public static Optional<BoxedChemical> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(error -> MekanismAPI.logger.error("Tried to load invalid boxed chemical: '{}'", error));
    }

    /**
     * Tries to parse a boxed chemical, defaulting to {@link #EMPTY} on parsing failure.
     *
     * @since 10.6.0
     */
    public static BoxedChemical parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
    }

    private final ChemicalType chemicalType;
    private final Chemical<?> chemical;

    protected BoxedChemical(ChemicalType chemicalType, Chemical<?> chemical) {
        this.chemicalType = chemicalType;
        this.chemical = chemical;
    }

    /**
     * Gets whether this boxed chemical is empty.
     *
     * @return {@code true} if this boxed chemical is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this == EMPTY || chemical.isEmptyType();
    }

    /**
     * Gets the chemical type.
     */
    public ChemicalType getChemicalType() {
        return chemicalType;
    }

    /**
     * Saves this boxed chemical to a tag, directly writing the keys into the passed tag.
     *
     * @throws IllegalStateException if this boxed chemical is empty
     * @since 10.6.0
     */
    public Tag save(Provider lookupProvider, Tag prefix) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty BoxedChemical");
        }
        return CODEC.encode(this, lookupProvider.createSerializationContext(NbtOps.INSTANCE), prefix).getOrThrow();
    }

    /**
     * Saves this boxed chemical to a new tag.
     *
     * @throws IllegalStateException if this boxed chemical is empty
     * @since 10.6.0
     */
    public Tag save(Provider lookupProvider) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty BoxedChemical");
        }
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    /**
     * Saves this boxed chemical to a new tag. Empty boxed chemical are supported and will be saved as an empty tag.
     *
     * @since 10.6.0
     */
    public Tag saveOptional(HolderLookup.Provider lookupProvider) {
        return isEmpty() ? new CompoundTag() : save(lookupProvider);
    }

    /**
     * Gets the internal chemical that was boxed.
     */
    public Chemical<?> getChemical() {
        return chemical;
    }

    @Override
    public Component getTextComponent() {
        return chemical.getTextComponent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BoxedChemical other = (BoxedChemical) o;
        return chemicalType == other.chemicalType && chemical == other.chemical;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chemicalType, chemical);
    }
}