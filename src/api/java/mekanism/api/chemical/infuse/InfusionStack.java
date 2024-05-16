package mekanism.api.chemical.infuse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IInfuseTypeProvider;
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
public final class InfusionStack extends ChemicalStack<InfuseType> {

    /**
     * Empty InfusionStack instance.
     */
    public static final InfusionStack EMPTY = new InfusionStack(null);

    /**
     * A standard codec for non-empty infuse types.
     *
     * @since 10.6.0
     */
    public static final Codec<InfuseType> INFUSE_TYPE_NON_EMPTY_CODEC = chemicalNonEmptyCodec(MekanismAPI.INFUSE_TYPE_REGISTRY);
    /**
     * A standard map codec for infusion stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final MapCodec<InfusionStack> MAP_CODEC = codec(INFUSE_TYPE_NON_EMPTY_CODEC, InfusionStack::new);
    /**
     * A standard codec for infusion stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final Codec<InfusionStack> CODEC = MAP_CODEC.codec();
    /**
     * A standard codec for infusion stacks that accepts empty stacks, serializing them as {@code {}}.
     *
     * @since 10.6.0
     */
    public static final Codec<InfusionStack> OPTIONAL_CODEC = optionalCodec(CODEC, EMPTY);
    /**
     * A stream codec for infusion stacks that accepts empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionStack> OPTIONAL_STREAM_CODEC = optionalStreamCodec(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, InfusionStack::new, EMPTY);
    /**
     * A stream codec for infusion stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionStack> STREAM_CODEC = streamCodec(OPTIONAL_STREAM_CODEC);

    /**
     * A standard codec for infusion stacks that always deserializes with a fixed amount, and does not accept empty stacks.
     * <p>
     * Infusion equivalent of {@link ItemStack#SINGLE_ITEM_CODEC}.
     *
     * @since 10.6.0
     */
    public static Codec<InfusionStack> fixedAmountCodec(int amount) {
        return fixedAmountCodec(INFUSE_TYPE_NON_EMPTY_CODEC, InfusionStack::new, amount);
    }

    /**
     * Creates a new InfusionStack with a defined infusion type and quantity.
     *
     * @param infuseTypeProvider - provides the infusion type of the stack
     * @param amount             - amount of the infusion type to be referenced in this InfusionStack
     */
    public InfusionStack(IInfuseTypeProvider infuseTypeProvider, long amount) {
        super(infuseTypeProvider.getChemical(), amount);
    }

    /**
     * Creates a new InfusionStack with a defined infusion type and quantity.
     *
     * @param infuseTypeHolder - provides the infusion type of the stack
     * @param amount           - amount of the infusion type to be referenced in this InfusionStack
     *
     * @since 10.5.0
     */
    public InfusionStack(Holder<InfuseType> infuseTypeHolder, long amount) {
        super(infuseTypeHolder, amount);
    }

    /**
     * Used for creating the empty stack
     *
     * @since 10.6.0
     */
    private InfusionStack(@Nullable Void unused) {
        super(unused);
    }

    /**
     * Tries to parse an infusion stack. Empty stacks cannot be parsed with this method.
     *
     * @since 10.6.0
     */
    public static Optional<InfusionStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(error -> MekanismAPI.logger.error("Tried to load invalid gas: '{}'", error));
    }

    /**
     * Tries to parse an infusion stack, defaulting to {@link #EMPTY} on parsing failure.
     *
     * @since 10.6.0
     */
    public static InfusionStack parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
    }

    @Override
    protected Registry<InfuseType> getRegistry() {
        return MekanismAPI.INFUSE_TYPE_REGISTRY;
    }

    @Override
    protected InfuseType getEmptyChemical() {
        return MekanismAPI.EMPTY_INFUSE_TYPE;
    }

    /**
     * Returns a copied form of this InfusionStack.
     *
     * @return copied InfusionStack
     */
    @Override
    public InfusionStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new InfusionStack(getChemical(), getAmount());
    }

    @Override
    public InfusionStack copyWithAmount(long amount) {
        if (isEmpty()) {
            return EMPTY;
        }
        return new InfusionStack(getChemical(), amount);
    }

    @Override
    public InfusionStack split(long amount) {
        long i = Math.min(amount, getAmount());
        InfusionStack stack = copyWithAmount(i);
        this.shrink(i);
        return stack;
    }

    @Override
    public InfusionStack copyAndClear() {
        if (isEmpty()) {
            return EMPTY;
        }
        InfusionStack stack = copy();
        this.setAmount(0);
        return stack;
    }

    @Override
    public Tag save(Provider lookupProvider, Tag prefix) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty InfusionStack");
        }
        return CODEC.encode(this, lookupProvider.createSerializationContext(NbtOps.INSTANCE), prefix).getOrThrow();
    }

    @Override
    public Tag save(Provider lookupProvider) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty InfusionStack");
        }
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }
}