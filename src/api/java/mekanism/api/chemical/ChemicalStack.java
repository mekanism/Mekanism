package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.attribute.IChemicalAttributeContainer;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class ChemicalStack implements IHasTextComponent, IHasTranslationKey, IChemicalAttributeContainer<ChemicalStack> {

    /**
     * Empty ChemicalStack instance.
     */
    public static final ChemicalStack EMPTY = new ChemicalStack(null);

    /**
     * A standard codec for non-empty Chemicals.
     *
     * @since 10.6.0
     */
    public static final Codec<Chemical> CHEMICAL_NON_EMPTY_CODEC = MekanismAPI.CHEMICAL_REGISTRY.byNameCodec().validate(chemical -> chemical.isEmptyType() ? DataResult.error(() -> "Chemical must not be mekanism:empty") : DataResult.success(chemical));
    /**
     * A standard codec for non-empty Chemical holders.
     *
     * @since 10.6.0
     */
    public static final Codec<Holder<Chemical>> CHEMICAL_NON_EMPTY_HOLDER_CODEC = MekanismAPI.CHEMICAL_REGISTRY.holderByNameCodec().validate(chemical -> chemical.value().isEmptyType() ? DataResult.error(() -> "Chemical must not be mekanism:empty") : DataResult.success(chemical));
    /**
     * A standard map codec for Chemical stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final MapCodec<ChemicalStack> MAP_CODEC = codec(CHEMICAL_NON_EMPTY_CODEC, ChemicalStack::new);
    /**
     * A standard codec for Chemical stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final Codec<ChemicalStack> CODEC = MAP_CODEC.codec();
    /**
     * A standard codec for Chemical stacks that accepts empty stacks, serializing them as {@code {}}.
     *
     * @since 10.6.0
     */
    public static final Codec<ChemicalStack> OPTIONAL_CODEC = optionalCodec(CODEC, EMPTY);
    /**
     * A stream codec for Chemical stacks that accepts empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalStack> OPTIONAL_STREAM_CODEC = optionalStreamCodec(MekanismAPI.CHEMICAL_REGISTRY_NAME, ChemicalStack::new, EMPTY);
    /**
     * A stream codec for Chemical stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalStack> STREAM_CODEC = streamCodec(OPTIONAL_STREAM_CODEC);

    /**
     * A standard codec for chemical stacks that always deserializes with a fixed amount, and does not accept empty stacks.
     * <p>
     * Chemical equivalent of {@link ItemStack#SINGLE_ITEM_CODEC}.
     *
     * @since 10.6.0
     */
    public static Codec<ChemicalStack> fixedAmountCodec(int amount) {
        return fixedAmountCodec(CHEMICAL_NON_EMPTY_CODEC, ChemicalStack::new, amount);
    }


    /**
     * A standard codec for chemical stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    protected static MapCodec<ChemicalStack> codec(Codec<Chemical> nonEmptyCodec,
          BiFunction<Chemical, Long, ChemicalStack> constructor) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              nonEmptyCodec.fieldOf(SerializationConstants.ID).forGetter(ChemicalStack::getChemical),
              SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(SerializationConstants.AMOUNT).forGetter(ChemicalStack::getAmount)
        ).apply(instance, constructor));
    }

    /**
     * A standard codec for chemical stacks that always deserializes with a fixed amount, and does not accept empty stacks.
     * <p>
     * Chemical equivalent of {@link ItemStack#SINGLE_ITEM_CODEC}.
     *
     * @since 10.6.0
     */
    protected static Codec<ChemicalStack> fixedAmountCodec(
          Codec<Chemical> chemicalNonEmptyCodec, BiFunction<Chemical, Long, ChemicalStack> constructor, long amount) {
        return RecordCodecBuilder.create(instance -> instance.group(
              chemicalNonEmptyCodec.fieldOf(SerializationConstants.ID).forGetter(ChemicalStack::getChemical)
        ).apply(instance, holder -> constructor.apply(holder, amount)));
    }

    /**
     * A standard codec for chemical stacks that accepts empty stacks, serializing them as {@code {}}.
     *
     * @since 10.6.0
     */
    protected static Codec<ChemicalStack> optionalCodec(Codec<ChemicalStack> codec, ChemicalStack empty) {
        return ExtraCodecs.optionalEmptyMap(codec).xmap(optional -> optional.orElse(empty), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    }

    /**
     * A stream codec for chemical stacks that accepts empty stacks.
     *
     * @since 10.6.0
     */
    protected static StreamCodec<RegistryFriendlyByteBuf, ChemicalStack> optionalStreamCodec(
          ResourceKey<? extends Registry<Chemical>> registry, BiFunction<Chemical, Long, ChemicalStack> constructor, ChemicalStack empty) {
        StreamCodec<RegistryFriendlyByteBuf, Chemical> chemicalStreamCodec = ByteBufCodecs.registry(registry);
        return new StreamCodec<>() {
            @Override
            public ChemicalStack decode(RegistryFriendlyByteBuf buffer) {
                long amount = buffer.readVarLong();
                if (amount <= 0) {
                    return empty;
                }
                Chemical chemical = chemicalStreamCodec.decode(buffer);
                return constructor.apply(chemical, amount);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ChemicalStack stack) {
                buffer.writeVarLong(stack.getAmount());
                if (!stack.isEmpty()) {
                    chemicalStreamCodec.encode(buffer, stack.getChemical());
                }
            }
        };
    }

    /**
     * A stream codec for chemical stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    protected static StreamCodec<RegistryFriendlyByteBuf, ChemicalStack> streamCodec(
          StreamCodec<RegistryFriendlyByteBuf, ChemicalStack> optionalStreamCodec) {
        return new StreamCodec<>() {
            @Override
            public ChemicalStack decode(RegistryFriendlyByteBuf buffer) {
                ChemicalStack stack = optionalStreamCodec.decode(buffer);
                if (stack.isEmpty()) {
                    throw new DecoderException("Empty ChemicalStack not allowed");
                }
                return stack;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ChemicalStack stack) {
                if (stack.isEmpty()) {
                    throw new EncoderException("Empty ChemicalStack not allowed");
                }
                optionalStreamCodec.encode(buffer, stack);
            }
        };
    }

    private final Chemical chemical;
    private long amount;

    public ChemicalStack(Holder<Chemical> chemical, long amount) {
        this(chemical.value(), amount);
    }

    public ChemicalStack(Chemical chemical, long amount) {
        Objects.requireNonNull(chemical, "Cannot create a ChemicalStack from a null chemical");
        this.chemical = chemical;
        this.amount = amount;
    }

    private ChemicalStack(@Nullable Void unused) {
        this.chemical = null;
    }

    /**
     * Copies this chemical stack into a new chemical stack.
     */
    public ChemicalStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new ChemicalStack(getChemical(), getAmount());
    }

    /**
     * Copies this chemical stack into a new chemical stack ith the given amount.
     *
     * @param amount New Amount
     *
     * @since 10.6.0
     */
    public ChemicalStack copyWithAmount(long amount) {
        if (isEmpty() || amount == 0) {
            return EMPTY;
        }
        return new ChemicalStack(getChemical(), amount);
    }

    /**
     * Splits off a stack of the given amount of this stack and reduces this stack by the amount.
     *
     * @since 10.6.0
     */
    public ChemicalStack split(long amount) {
        long i = Math.min(amount, getAmount());
        ChemicalStack stack = copyWithAmount(i);
        this.shrink(i);
        return stack;
    }

    /**
     * Creates a copy of this stack with {@code 0} amount.
     *
     * @since 10.6.0
     */
    public ChemicalStack copyAndClear() {
        if (isEmpty()) {
            return EMPTY;
        }
        ChemicalStack stack = copy();
        this.setAmount(0);
        return stack;
    }

    /**
     * Gets the chemical represented by this stack.
     *
     * @return Backing chemical.
     *
     * @since 10.6.0 Previously was getType
     */
    public Chemical getChemical() {
        return isEmpty() ? MekanismAPI.EMPTY_CHEMICAL : chemical;
    }

    /**
     * Gets the holder for chemical represented by this stack.
     *
     * @return Backing chemical's holder.
     *
     * @since 10.6.0
     */
    public Holder<Chemical> getChemicalHolder() {
        return getChemical().getAsHolder();
    }

    /**
     * Checks if the chemical for this stack is in the given tag.
     *
     * @return Tag to check.
     *
     * @since 10.6.0
     */
    public boolean is(TagKey<Chemical> tag) {
        return getChemicalHolder().is(tag);
    }

    /**
     * Whether this ChemicalStack's chemical type is equal to the other defined Chemical.
     *
     * @param chemical - Chemical to check
     *
     * @return if the ChemicalStack's type is the same as the given chemical
     *
     * @since 10.6.0 Previously was isTypeEqual
     */
    public boolean is(Chemical chemical) {
        return getChemical() == chemical;
    }

    /**
     * Whether this ChemicalStack's chemical type matches the given predicate.
     *
     * @param predicate - Predicate to test
     *
     * @return if the ChemicalStack's type matches the given predicate
     *
     * @since 10.6.0
     */
    public boolean is(Predicate<Holder<Chemical>> predicate) {
        return predicate.test(getChemicalHolder());
    }

    /**
     * Whether this ChemicalStack's chemical type is equal to the other holder's chemical.
     *
     * @param holder - Chemical holder to check
     *
     * @return if the ChemicalStack's type is the same as the given holder's chemical
     *
     * @since 10.6.0
     */
    public boolean is(Holder<Chemical> holder) {
        return is(holder.value());
    }

    /**
     * Checks if the chemical for this stack is part of the given holder set.
     *
     * @return Holder set to check.
     *
     * @since 10.6.0
     */
    public boolean is(HolderSet<Chemical> holderSet) {
        return holderSet.contains(getChemicalHolder());
    }

    /**
     * Gets the tags that this chemical is a part of.
     *
     * @return All the tags this chemical is a part of.
     *
     * @since 10.6.0
     */
    public Stream<TagKey<Chemical>> getTags() {
        return getChemicalHolder().tags();
    }

    /**
     * Saves this stack to a tag, directly writing the keys into the passed tag.
     *
     * @throws IllegalStateException if this stack is empty
     * @since 10.6.0
     */
    public Tag save(HolderLookup.Provider lookupProvider, Tag prefix) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ChemicalStack");
        }
        return CODEC.encode(this, lookupProvider.createSerializationContext(NbtOps.INSTANCE), prefix).getOrThrow();
    }

    /**
     * Saves this stack to a new tag.
     *
     * @throws IllegalStateException if this stack is empty
     * @since 10.6.0
     */
    public Tag save(HolderLookup.Provider lookupProvider) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ChemicalStack");
        }
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    /**
     * Saves this stack to a new tag. Empty stacks are supported and will be saved as an empty tag.
     *
     * @since 10.6.0
     */
    public Tag saveOptional(HolderLookup.Provider lookupProvider) {
        return isEmpty() ? new CompoundTag() : save(lookupProvider);
    }

    /**
     * Helper to retrieve the registry name of the stored chemical. This is equivalent to calling {@code getType().getRegistryName()}
     *
     * @return The registry name of the stored chemical.
     */
    public ResourceLocation getTypeRegistryName() {
        return getChemical().getRegistryName();
    }

    /**
     * Helper to get the tint of the stored chemical. This is equivalent to calling {@code getType().getTint()}
     *
     * @return The tint of the stored chemical.
     *
     * @apiNote Does not have any special handling for when the stack is empty.
     */
    public int getChemicalTint() {
        return getChemical().getTint();
    }

    /**
     * Helper to get the color representation of the stored chemical. This is equivalent to calling {@code getType().getColorRepresentation()} and is used for things like
     * durability bars of chemical tanks.
     *
     * @return The color representation of the stored chemical.
     *
     * @apiNote Does not have any special handling for when the stack is empty.
     */
    public int getChemicalColorRepresentation() {
        return getChemical().getColorRepresentation();
    }

    /**
     * Gets whether this chemical stack is empty.
     *
     * @return {@code true} if this stack is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        //Empty instance has the chemical being null
        return chemical == null || chemical.isEmptyType() || this.amount <= 0;
    }

    /**
     * Gets the size of this chemical stack.
     *
     * @return The size of this chemical stack or zero if it is empty
     */
    public long getAmount() {
        return isEmpty() ? 0 : amount;
    }

    /**
     * Sets this stack's amount to the given amount.
     *
     * @param amount The amount to set this stack's amount to.
     */
    public void setAmount(long amount) {
        this.amount = amount;
    }

    /**
     * Limits the amount of this stack to at most the given amount.
     *
     * @param amount Amount to max the stack out at.
     *
     * @since 10.6.0
     */
    public void limitSize(long amount) {
        if (!isEmpty() && getAmount() > amount) {
            setAmount(amount);
        }
    }

    /**
     * Grows this stack's amount by the given amount.
     *
     * @param amount The amount to grow this stack by.
     *
     * @apiNote Negative values are valid and will instead shrink the stack.
     * @implNote No checks are made to ensure that the long does not overflow.
     */
    public void grow(long amount) {
        setAmount(this.amount + amount);
    }

    /**
     * Shrinks this stack's amount by the given amount.
     *
     * @param amount The amount to shrink this stack by.
     *
     * @apiNote Negative values are valid and will instead grow the stack.
     * @implNote No checks are made to ensure that the long does not underflow.
     */
    public void shrink(long amount) {
        setAmount(this.amount - amount);
    }

    @Override
    public boolean has(Class<? extends ChemicalAttribute> type) {
        return getChemical().has(type);
    }

    /**
     * Helper to check if this chemical is radioactive without having to look it up from the attributes.
     *
     * @return {@code true} if this chemical is radioactive.
     *
     * @since 10.5.15
     */
    public boolean isRadioactive() {
        return getChemical().isRadioactive();
    }

    @Nullable
    @Override
    public <ATTRIBUTE extends ChemicalAttribute> ATTRIBUTE get(Class<ATTRIBUTE> type) {
        return getChemical().get(type);
    }

    @Override
    public Collection<ChemicalAttribute> getAttributes() {
        return getChemical().getAttributes();
    }

    @Override
    public Collection<Class<? extends ChemicalAttribute>> getAttributeTypes() {
        return getChemical().getAttributeTypes();
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + getChemical().hashCode();
        code = 31 * code + Long.hashCode(getAmount());
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChemicalStack other = (ChemicalStack) o;
        return getChemical() == other.getChemical() && getAmount() == other.getAmount();
    }

    @Override
    public String toString() {
        return "[" + getChemical() + ", " + amount + "]";
    }

    @Override
    public Component getTextComponent() {
        //Wrapper to get display name of the chemical type easier
        return getChemical().getTextComponent();
    }

    @Override
    public String getTranslationKey() {
        //Wrapper to get translation key of the chemical type easier
        return getChemical().getTranslationKey();
    }

    /**
     * Checks if the two chemical stacks have the same chemical type. Ignores amount.
     *
     * @return {@code true} if the two chemical stacks have the same chemical
     *
     * @since 10.6.0 Previously was isTypeEqual
     */
    public static boolean isSameChemical(ChemicalStack first, ChemicalStack second) {
        return first.is(second.getChemical());
    }

    /**
     * Tries to parse a chemical stack. Empty stacks cannot be parsed with this method.
     *
     * @since 10.6.0
     */
    public static Optional<ChemicalStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(error -> MekanismAPI.logger.error("Tried to load invalid chemical: '{}'", error));
    }

    /**
     * Tries to parse a chemical stack, defaulting to {@link #EMPTY} on parsing failure.
     *
     * @since 10.6.0
     */
    public static ChemicalStack parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
    }
}