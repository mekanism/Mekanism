package mekanism.api.chemical.slurry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.ISlurryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class SlurryStack extends ChemicalStack<Slurry> {

    /**
     * Empty SlurryStack instance.
     */
    public static final SlurryStack EMPTY = new SlurryStack(null);

    /**
     * A standard codec for non-empty slurries.
     *
     * @since 10.6.0
     */
    public static final Codec<Slurry> SLURRY_NON_EMPTY_CODEC = chemicalNonEmptyCodec(MekanismAPI.SLURRY_REGISTRY);
    /**
     * A standard map codec for slurry stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final MapCodec<SlurryStack> MAP_CODEC = codec(SLURRY_NON_EMPTY_CODEC, SlurryStack::new);
    /**
     * A standard codec for slurry stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final Codec<SlurryStack> CODEC = MAP_CODEC.codec();
    /**
     * A standard codec for slurry stacks that accepts empty stacks, serializing them as {@code {}}.
     *
     * @since 10.6.0
     */
    public static final Codec<SlurryStack> OPTIONAL_CODEC = optionalCodec(CODEC, EMPTY);
    /**
     * A stream codec for slurry stacks that accepts empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, SlurryStack> OPTIONAL_STREAM_CODEC = optionalStreamCodec(MekanismAPI.SLURRY_REGISTRY_NAME, SlurryStack::new, EMPTY);
    /**
     * A stream codec for slurry stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, SlurryStack> STREAM_CODEC = streamCodec(OPTIONAL_STREAM_CODEC);

    /**
     * A standard codec for slurry stacks that always deserializes with a fixed amount, and does not accept empty stacks.
     * <p>
     * Slurry equivalent of {@link ItemStack#SINGLE_ITEM_CODEC}.
     *
     * @since 10.6.0
     */
    public static Codec<SlurryStack> fixedAmountCodec(int amount) {
        return fixedAmountCodec(SLURRY_NON_EMPTY_CODEC, SlurryStack::new, amount);
    }

    /**
     * Creates a new SlurryStack with a defined slurry type and quantity.
     *
     * @param slurryProvider - provides the slurry type of the stack
     * @param amount         - amount of the slurry to be referenced in this SlurryStack
     */
    public SlurryStack(ISlurryProvider slurryProvider, long amount) {
        super(slurryProvider.getChemical(), amount);
    }

    /**
     * Creates a new SlurryStack with a defined slurry type and quantity.
     *
     * @param slurryHolder - provides the slurry type of the stack
     * @param amount       - amount of the slurry to be referenced in this SlurryStack
     *
     * @since 10.5.0
     */
    public SlurryStack(Holder<Slurry> slurryHolder, long amount) {
        super(slurryHolder, amount);
    }

    /**
     * Used for creating the empty stack
     *
     * @since 10.6.0
     */
    private SlurryStack(@Nullable Void unused) {
        super(unused);
    }

    /**
     * Tries to parse a slurry stack. Empty stacks cannot be parsed with this method.
     *
     * @since 10.6.0
     */
    public static Optional<SlurryStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(error -> MekanismAPI.logger.error("Tried to load invalid gas: '{}'", error));
    }

    /**
     * Tries to parse a slurry stack, defaulting to {@link #EMPTY} on parsing failure.
     *
     * @since 10.6.0
     */
    public static SlurryStack parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
    }

    @Override
    protected Registry<Slurry> getRegistry() {
        return MekanismAPI.SLURRY_REGISTRY;
    }

    @Override
    protected Slurry getEmptyChemical() {
        return MekanismAPI.EMPTY_SLURRY;
    }

    /**
     * Returns a copied form of this SlurryStack.
     *
     * @return copied SlurryStack
     */
    @Override
    public SlurryStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new SlurryStack(getChemical(), getAmount());
    }

    @Override
    public SlurryStack copyWithAmount(long amount) {
        if (isEmpty()) {
            return EMPTY;
        }
        return new SlurryStack(getChemical(), amount);
    }

    @Override
    public SlurryStack split(long amount) {
        long i = Math.min(amount, getAmount());
        SlurryStack stack = copyWithAmount(i);
        this.shrink(i);
        return stack;
    }

    @Override
    public SlurryStack copyAndClear() {
        if (isEmpty()) {
            return EMPTY;
        }
        SlurryStack stack = copy();
        this.setAmount(0);
        return stack;
    }

    @Override
    public Tag save(Provider lookupProvider, Tag prefix) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty SlurryStack");
        }
        return CODEC.encode(this, lookupProvider.createSerializationContext(NbtOps.INSTANCE), prefix).getOrThrow();
    }

    @Override
    public Tag save(Provider lookupProvider) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty SlurryStack");
        }
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }
}