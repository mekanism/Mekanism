package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.IWithData;
import net.neoforged.neoforge.transfer.resource.RegisteredResource;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: Update docs on this
@NothingNullByDefault
public final class ChemicalResource implements RegisteredResource<Chemical>, IHasTextComponent, IHasTranslationKey, IWithData<Chemical> {

    /**
     * The empty resource instance of a {@link ChemicalResource}
     */
    public static final ChemicalResource EMPTY = new ChemicalResource(MekanismAPI.EMPTY_CHEMICAL_HOLDER);

    /**
     * Codec for a chemical resource. Does <b>not</b> accept empty resources.
     */
    public static final Codec<ChemicalResource> CODEC = ChemicalInstance.CHEMICAL_HOLDER_CODEC.xmap(ChemicalResource::of, ChemicalResource::typeHolder);

    /**
     * Codec for a chemical resource. Same format as {@link #CODEC}, and also accepts empty resources.
     */
    public static final Codec<ChemicalResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
          optional -> optional.orElse(EMPTY),
          resource -> resource.isEmpty() ? Optional.empty() : Optional.of(resource));

    /**
     * Stream codec for a chemical resource. Accepts empty resources.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalResource> STREAM_CODEC = Chemical.STREAM_CODEC.map(ChemicalResource::of, ChemicalResource::typeHolder);

    /**
     * Creates a {@link ChemicalResource} for the given chemical stack. Note the amount is lost.
     *
     * @param stack stack to get the holder of
     */
    public static ChemicalResource of(ChemicalStack stack) {
        return of(stack.typeHolder());
    }

    /**
     * <strong>Note:</strong> This cannot be called before your chemical is registered
     *
     * @throws IllegalStateException If the backing registry is unavailable or not yet ready.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static ChemicalResource of(Chemical chemical) {
        //TODO - 26.1: Re-evaluate this
        return of(MekanismAPI.CHEMICAL_REGISTRY.wrapAsHolder(chemical));
    }

    /**
     * <strong>Note:</strong> This cannot be called before your chemical is registered
     *
     * @throws IllegalStateException If the backing registry is unavailable or not yet ready.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static ChemicalResource of(Holder<Chemical> chemical) {
        if (chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
            return EMPTY;
        }
        if (chemical.kind() == Holder.Kind.DIRECT) {//TODO - 26.1: Re-evaluate and maybe make a util method for this as it is copied from the ChemicalStack constructor
            if (!chemical.isBound()) {//This should always be true, unless someone made a custom direct holder for some reason
                throw new IllegalArgumentException("Cannot create a ChemicalStack from an unbound direct holder");
            }
            //Try to look up the reference holder from the registry
            chemical = MekanismAPI.CHEMICAL_REGISTRY.wrapAsHolder(chemical.value());
            if (chemical.kind() == Holder.Kind.DIRECT) {
                throw new IllegalArgumentException("Cannot create a ChemicalStack from a direct holder for a chemical that is not yet registered");
            }
        }
        return new ChemicalResource(chemical);
    }

    private final Holder<Chemical> chemicalType;

    private ChemicalResource(Holder<Chemical> chemicalType) {
        this.chemicalType = chemicalType;
    }

    @Override
    public Chemical value() {
        return chemicalType.value();
    }

    /**
     * @return The {@link Chemical} of this resource from the inner {@link ChemicalStack}
     */
    public Chemical getChemical() {
        return value();
    }

    @Nullable
    @Override
    public <T> T getData(DataMapType<Chemical, T> type) {
        //Note: We only accept reference holders, and reference holders can be queried directly for data
        return typeHolder().getData(type);
    }

    /**
     * @return the chemical holder of this resource
     */
    @Override
    public Holder<Chemical> typeHolder() {
        return chemicalType;
    }

    /**
     * Checks if this resource is empty. The resource will be empty if the chemical is {@link MekanismAPI#EMPTY_CHEMICAL_KEY}.
     *
     * @return if this resource is empty
     */
    @Override
    public boolean isEmpty() {
        return chemicalType.is(MekanismAPI.EMPTY_CHEMICAL_KEY);
    }

    /**
     * Creates a {@link ChemicalStack} of the specified amount.
     *
     * @param amount The amount of the chemical the stack should have. Must be non-negative.
     *
     * @throws IllegalArgumentException when amount is negative.
     */
    public ChemicalStack toStack(long amount) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0) {
            return ChemicalStack.EMPTY;
        }
        return new ChemicalStack(typeHolder(), amount);
    }

    /**
     * {@return true if this resource matches the chemical of the passed stack}
     *
     * @param stack the chemical stack to check
     */
    public boolean matches(ChemicalStack stack) {//TODO - 26.1: Re-evaluate this
        return is(stack.typeHolder());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        ChemicalResource other = (ChemicalResource) obj;
        return this.chemicalType.is(other.chemicalType);
    }

    @Override
    public int hashCode() {
        return this.chemicalType.hashCode();
    }

    @Override
    public String toString() {
        return this.chemicalType.unwrapKey().map(key -> key.identifier().toString()).orElse("[unregistered]");
    }

    @Override
    public Component getTextComponent() {
        //Wrapper to get display name of the chemical type easier
        return value().getTextComponent();
    }

    @Override
    public String getTranslationKey() {
        //Wrapper to get translation key of the chemical type easier
        return value().getTranslationKey();
    }

    /**
     * Helper to check if this chemical is radioactive without having to look it up from the attributes.
     *
     * @return {@code true} if this chemical is radioactive.
     */
    public boolean isRadioactive() {
        return value().isRadioactive();
    }

    /**
     * {@return radiation level of this chemical, or zero if it is not radioactive}.
     */
    public double getRadioactivity() {
        return value().getRadioactivity();
    }
}