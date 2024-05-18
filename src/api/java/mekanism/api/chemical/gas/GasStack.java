package mekanism.api.chemical.gas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IGasProvider;
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

/**
 * GasStack - a specified amount of a defined Gas with certain properties.
 *
 * @author aidancbrady
 */
@NothingNullByDefault
public final class GasStack extends ChemicalStack<Gas> {

    /**
     * Empty GasStack instance.
     */
    public static final GasStack EMPTY = new GasStack(null);

    /**
     * A standard codec for non-empty gases.
     *
     * @since 10.6.0
     */
    public static final Codec<Gas> GAS_NON_EMPTY_CODEC = chemicalNonEmptyCodec(MekanismAPI.GAS_REGISTRY);
    /**
     * A standard codec for non-empty gas holders.
     *
     * @since 10.6.0
     */
    public static final Codec<Holder<Gas>> GAS_NON_EMPTY_HOLDER_CODEC = chemicalNonEmptyHolderCodec(MekanismAPI.GAS_REGISTRY);
    /**
     * A standard map codec for gas stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final MapCodec<GasStack> MAP_CODEC = codec(GAS_NON_EMPTY_CODEC, GasStack::new);
    /**
     * A standard codec for gas stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final Codec<GasStack> CODEC = MAP_CODEC.codec();
    /**
     * A standard codec for gas stacks that accepts empty stacks, serializing them as {@code {}}.
     *
     * @since 10.6.0
     */
    public static final Codec<GasStack> OPTIONAL_CODEC = optionalCodec(CODEC, EMPTY);
    /**
     * A stream codec for gas stacks that accepts empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, GasStack> OPTIONAL_STREAM_CODEC = optionalStreamCodec(MekanismAPI.GAS_REGISTRY_NAME, GasStack::new, EMPTY);
    /**
     * A stream codec for gas stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, GasStack> STREAM_CODEC = streamCodec(OPTIONAL_STREAM_CODEC);

    /**
     * A standard codec for gas stacks that always deserializes with a fixed amount, and does not accept empty stacks.
     * <p>
     * Gas equivalent of {@link ItemStack#SINGLE_ITEM_CODEC}.
     *
     * @since 10.6.0
     */
    public static Codec<GasStack> fixedAmountCodec(int amount) {
        return fixedAmountCodec(GAS_NON_EMPTY_CODEC, GasStack::new, amount);
    }

    /**
     * Creates a new GasStack with a defined Gas type and quantity.
     *
     * @param gasProvider - provides the gas type of the stack
     * @param amount      - amount of gas to be referenced in this GasStack
     */
    public GasStack(IGasProvider gasProvider, long amount) {
        super(gasProvider.getChemical(), amount);
    }

    /**
     * Creates a new GasStack with a defined Gas type and quantity.
     *
     * @param gasHolder - provides the gas type of the stack
     * @param amount    - amount of gas to be referenced in this GasStack
     *
     * @since 10.5.0
     */
    public GasStack(Holder<Gas> gasHolder, long amount) {
        this(gasHolder.value(), amount);
    }

    /**
     * Used for creating the empty stack
     *
     * @since 10.6.0
     */
    private GasStack(@Nullable Void unused) {
        super(unused);
    }

    /**
     * Tries to parse a gas stack. Empty stacks cannot be parsed with this method.
     *
     * @since 10.6.0
     */
    public static Optional<GasStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(error -> MekanismAPI.logger.error("Tried to load invalid gas: '{}'", error));
    }

    /**
     * Tries to parse a gas stack, defaulting to {@link #EMPTY} on parsing failure.
     *
     * @since 10.6.0
     */
    public static GasStack parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
    }

    @Override
    protected Gas getEmptyChemical() {
        return MekanismAPI.EMPTY_GAS;
    }

    /**
     * Returns a copied form of this GasStack.
     *
     * @return copied GasStack
     */
    @Override
    public GasStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new GasStack(getChemical(), getAmount());
    }

    @Override
    public GasStack copyWithAmount(long amount) {
        if (isEmpty()) {
            return EMPTY;
        }
        return new GasStack(getChemical(), amount);
    }

    @Override
    public GasStack split(long amount) {
        long i = Math.min(amount, getAmount());
        GasStack stack = copyWithAmount(i);
        this.shrink(i);
        return stack;
    }

    @Override
    public GasStack copyAndClear() {
        if (isEmpty()) {
            return EMPTY;
        }
        GasStack stack = copy();
        this.setAmount(0);
        return stack;
    }

    @Override
    public Tag save(Provider lookupProvider, Tag prefix) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty GasStack");
        }
        return CODEC.encode(this, lookupProvider.createSerializationContext(NbtOps.INSTANCE), prefix).getOrThrow();
    }

    @Override
    public Tag save(Provider lookupProvider) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty GasStack");
        }
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }
}