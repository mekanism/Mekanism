package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismPreconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.transfer.resource.RegisteredResource;
import org.jspecify.annotations.Nullable;

//TODO - 26.2: Update docs on this
public final class ChemicalResource implements RegisteredResource<Chemical>, ChemicalInstance {

    /// The empty resource instance of a [ChemicalResource]
    public static final ChemicalResource EMPTY = new ChemicalResource(null);

    /// Codec for a chemical resource. Does **not** accept empty resources.
    public static final Codec<ChemicalResource> CODEC = CHEMICAL_HOLDER_CODEC.xmap(ChemicalResource::of, ChemicalResource::typeHolder);

    /// Codec for a chemical resource. Same format as [#CODEC], and also accepts empty resources.
    public static final Codec<ChemicalResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
          optional -> optional.orElse(EMPTY),
          resource -> resource.isEmpty() ? Optional.empty() : Optional.of(resource));

    /// Stream codec for a chemical resource. Accepts empty resources.
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalResource> STREAM_CODEC = CHEMICAL_HOLDER_STREAM_CODEC.map(ChemicalResource::of, ChemicalResource::typeHolder);

    /// Creates a [ChemicalResource] for the given chemical stack template. Note the amount is lost.
    ///
    /// @param template template to get the holder of
    public static ChemicalResource of(ChemicalStackTemplate template) {
        return of(template.typeHolder());
    }

    /// Creates a [ChemicalResource] for the given chemical stack. Note the amount is lost.
    ///
    /// @param stack stack to get the holder of
    public static ChemicalResource of(ChemicalStack stack) {
        //Skip trying to get the type holder if the stack is empty
        return stack.isEmpty() ? EMPTY : of(stack.typeHolder());
    }

    /// **Note:** This cannot be called before your chemical is registered
    ///
    /// @throws IllegalStateException If the backing registry is unavailable or not yet ready.
    /// @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
    public static ChemicalResource of(Holder<Chemical> chemical) {
        if (chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
            return EMPTY;
        }
        return new ChemicalResource(chemical);
    }

    @Nullable
    private final Holder<Chemical> chemicalType;

    private ChemicalResource(@Nullable Holder<Chemical> chemicalType) {
        this.chemicalType = chemicalType;
    }

    @Override
    public Chemical value() {
        return typeHolder().value();
    }

    /// @return The [Chemical] of this resource from the inner [ChemicalStack]
    public Chemical getChemical() {
        return value();
    }

    /// @return the chemical holder of this resource
    @Override
    public Holder<Chemical> typeHolder() {
        return chemicalType == null ? getEmptyHolder() : chemicalType;
    }

    /// Checks if this resource is empty. The resource will be empty if the chemical is [MekanismAPI#EMPTY_CHEMICAL_KEY].
    ///
    /// @return if this resource is empty
    @Override
    public boolean isEmpty() {
        //Note: We can skip checking if the
        return chemicalType == null;
    }

    /// Creates a [ChemicalStack] of the specified amount.
    ///
    /// @param amount The amount of the chemical the stack should have. Must be non-negative.
    ///
    /// @throws IllegalArgumentException when amount is negative.
    public ChemicalStack toStack(int amount) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0 || chemicalType == null) {
            return ChemicalStack.EMPTY;
        }
        return new ChemicalStack(chemicalType, amount);
    }

    /// {@return true if this resource matches the chemical of the passed stack}
    ///
    /// @param stack the chemical stack to check
    public boolean matches(ChemicalStack stack) {
        //Note: We avoid looking up the type holder when it isn't necessary
        if (isEmpty()) {
            return stack.isEmpty();
        } else if (stack.isEmpty()) {
            return false;
        }
        return is(stack.typeHolder());
    }

    /// {@return true if this resource matches the chemical of the passed template}
    ///
    /// @param template the chemical template to check
    public boolean matches(@Nullable ChemicalStackTemplate template) {
        //Note: We avoid looking up the type holder when it isn't necessary
        if (template == null) {
            return isEmpty();
        } else if (isEmpty()) {
            return false;
        }
        return is(template.typeHolder());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        ChemicalResource other = (ChemicalResource) obj;
        if (chemicalType == null) {
            return other.chemicalType == null;
        } else if (other.chemicalType == null) {
            return false;
        }
        return is(other.chemicalType.value());
    }

    @Override
    public int hashCode() {
        return this.chemicalType == null ? 0 : this.chemicalType.hashCode();
    }

    @Override
    public String toString() {
        if (this.chemicalType == null) {
            return MekanismAPI.EMPTY_CHEMICAL_KEY.identifier().toString();
        }
        return this.chemicalType.getRegisteredName();
    }

    @Nullable
    @Override
    public <DATA> DATA getData(@Nullable RegistryAccess registryAccess, DataMapType<Chemical, DATA> type) {
        if (isEmpty()) {
            return null;
        }
        return ChemicalInstance.super.getData(registryAccess, type);
    }

    private static Holder<Chemical> getEmptyHolder() {
        //TODO - 26.2: Re-evaluate this method, and how it hard fails if it can't actually find it. Also see if we should be caching this in any way
        RegistryAccess registryAccess;
        if (FMLEnvironment.getDist().isClient()) {
            ClientLevel level = Minecraft.getInstance().level;
            registryAccess = Objects.requireNonNull(level).registryAccess();
        } else {
            registryAccess = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer()).registryAccess();
        }
        return registryAccess.getOrThrow(MekanismAPI.EMPTY_CHEMICAL_KEY);
    }
}