package mekanism.api.chemical.pigment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IPigmentProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class PigmentStack extends ChemicalStack<Pigment> {

    /**
     * Empty PigmentStack instance.
     */
    public static final PigmentStack EMPTY = new PigmentStack(null);

    /**
     * A standard codec for non-empty pigments.
     *
     * @since 10.6.0
     */
    public static final Codec<Pigment> PIGMENT_NON_EMPTY_CODEC = chemicalNonEmptyCodec(MekanismAPI.PIGMENT_REGISTRY);
    /**
     * A standard codec for non-empty pigment holders.
     *
     * @since 10.6.0
     */
    public static final Codec<Holder<Pigment>> PIGMENT_NON_EMPTY_HOLDER_CODEC = chemicalNonEmptyHolderCodec(MekanismAPI.PIGMENT_REGISTRY);
    /**
     * A standard map codec for pigment stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final MapCodec<PigmentStack> MAP_CODEC = codec(PIGMENT_NON_EMPTY_CODEC, PigmentStack::new);
    /**
     * A standard codec for pigment stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final Codec<PigmentStack> CODEC = MAP_CODEC.codec();
    /**
     * A standard codec for pigment stacks that accepts empty stacks, serializing them as {@code {}}.
     *
     * @since 10.6.0
     */
    public static final Codec<PigmentStack> OPTIONAL_CODEC = optionalCodec(CODEC, EMPTY);
    /**
     * A stream codec for pigment stacks that accepts empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, PigmentStack> OPTIONAL_STREAM_CODEC = optionalStreamCodec(MekanismAPI.PIGMENT_REGISTRY_NAME, PigmentStack::new, EMPTY);
    /**
     * A stream codec for pigment stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, PigmentStack> STREAM_CODEC = streamCodec(OPTIONAL_STREAM_CODEC);

    /**
     * A standard codec for pigment stacks that always deserializes with a fixed amount, and does not accept empty stacks.
     * <p>
     * Pigment equivalent of {@link ItemStack#SINGLE_ITEM_CODEC}.
     *
     * @since 10.6.0
     */
    public static Codec<PigmentStack> fixedAmountCodec(int amount) {
        return fixedAmountCodec(PIGMENT_NON_EMPTY_CODEC, PigmentStack::new, amount);
    }

    /**
     * Creates a new PigmentStack with a defined pigment type and quantity.
     *
     * @param pigmentProvider - provides the pigment type of the stack
     * @param amount          - amount of the pigment to be referenced in this PigmentStack
     */
    public PigmentStack(IPigmentProvider pigmentProvider, long amount) {
        super(pigmentProvider.getChemical(), amount);
    }

    /**
     * Creates a new PigmentStack with a defined pigment type and quantity.
     *
     * @param pigmentHolder - provides the pigment type of the stack
     * @param amount        - amount of the pigment to be referenced in this PigmentStack
     *
     * @since 10.5.0
     */
    public PigmentStack(Holder<Pigment> pigmentHolder, long amount) {
        super(pigmentHolder, amount);
    }

    /**
     * Used for creating the empty stack
     *
     * @since 10.6.0
     */
    private PigmentStack(@Nullable Void unused) {
        super(unused);
    }

    /**
     * Tries to parse a pigment stack. Empty stacks cannot be parsed with this method.
     *
     * @since 10.6.0
     */
    public static Optional<PigmentStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(error -> MekanismAPI.logger.error("Tried to load invalid gas: '{}'", error));
    }

    /**
     * Tries to parse a pigment stack, defaulting to {@link #EMPTY} on parsing failure.
     *
     * @since 10.6.0
     */
    public static PigmentStack parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
    }

    @Override
    protected Pigment getEmptyChemical() {
        return MekanismAPI.EMPTY_PIGMENT;
    }

    /**
     * Returns a copied form of this PigmentStack.
     *
     * @return copied PigmentStack
     */
    @Override
    public PigmentStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new PigmentStack(getChemical(), getAmount());
    }

    @Override
    public PigmentStack copyWithAmount(long amount) {
        if (isEmpty()) {
            return EMPTY;
        }
        return new PigmentStack(getChemical(), amount);
    }

    @Override
    public PigmentStack split(long amount) {
        long i = Math.min(amount, getAmount());
        PigmentStack stack = copyWithAmount(i);
        this.shrink(i);
        return stack;
    }

    @Override
    public PigmentStack copyAndClear() {
        if (isEmpty()) {
            return EMPTY;
        }
        PigmentStack stack = copy();
        this.setAmount(0);
        return stack;
    }

    @Override
    public Tag save(Provider lookupProvider, Tag prefix) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty PigmentStack");
        }
        return CODEC.encode(this, lookupProvider.createSerializationContext(NbtOps.INSTANCE), prefix).getOrThrow();
    }

    @Override
    public Tag save(Provider lookupProvider) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty PigmentStack");
        }
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }
}