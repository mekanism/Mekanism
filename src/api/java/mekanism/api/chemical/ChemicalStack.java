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
import mekanism.api.JsonConstants;
import mekanism.api.NBTConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.attribute.IChemicalAttributeContainer;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
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
public abstract class ChemicalStack<CHEMICAL extends Chemical<CHEMICAL>> implements IHasTextComponent, IHasTranslationKey, IChemicalAttributeContainer<ChemicalStack<CHEMICAL>> {

    /**
     * A standard codec for chemicals.
     *
     * @implNote Unlike for fluids we do this on the objects instead of on the holders, as we don't have builtin holders.
     * @since 10.6.0
     */
    protected static <CHEMICAL extends Chemical<CHEMICAL>> Codec<CHEMICAL> chemicalNonEmptyCodec(Registry<CHEMICAL> registry) {
        return registry.byNameCodec().validate(chemical -> chemical.isEmptyType() ? DataResult.error(() -> "Chemical must not be mekanism:empty")
                                                                                  : DataResult.success(chemical));
    }

    /**
     * A standard codec for chemical stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> MapCodec<STACK> codec(Codec<CHEMICAL> nonEmptyCodec,
          BiFunction<CHEMICAL, Long, STACK> constructor) {
        //TODO - 1.20.%: Figure out if this needs to be lazily initialized. I don't think it does, but for fluids and items it is, probably because of components?
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              nonEmptyCodec.fieldOf(NBTConstants.ID).forGetter(ChemicalStack::getChemical),
              SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(JsonConstants.AMOUNT).forGetter(ChemicalStack::getAmount)
        ).apply(instance, constructor));
    }

    /**
     * A standard codec for chemical stacks that always deserializes with a fixed amount, and does not accept empty stacks.
     * <p>
     * Chemical equivalent of {@link ItemStack#SINGLE_ITEM_CODEC}.
     *
     * @since 10.6.0
     */
    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Codec<STACK> fixedAmountCodec(
          Codec<CHEMICAL> chemicalNonEmptyCodec, BiFunction<CHEMICAL, Long, STACK> constructor, long amount) {
        //TODO - 1.20.%: Figure out if this needs to be lazily initialized. I don't think it does, but for fluids and items it is, probably because of components?
        return RecordCodecBuilder.create(instance -> instance.group(
              chemicalNonEmptyCodec.fieldOf(NBTConstants.ID).forGetter(ChemicalStack::getChemical)
        ).apply(instance, holder -> constructor.apply(holder, amount)));
    }

    /**
     * A standard codec for chemical stacks that accepts empty stacks, serializing them as {@code {}}.
     *
     * @since 10.6.0
     */
    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Codec<STACK> optionalCodec(Codec<STACK> codec, STACK empty) {
        return ExtraCodecs.optionalEmptyMap(codec).xmap(optional -> optional.orElse(empty), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    }

    /**
     * A stream codec for chemical stacks that accepts empty stacks.
     *
     * @since 10.6.0
     */
    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> StreamCodec<RegistryFriendlyByteBuf, STACK> optionalStreamCodec(
          ResourceKey<? extends Registry<CHEMICAL>> registry, BiFunction<CHEMICAL, Long, STACK> constructor, STACK empty) {
        StreamCodec<RegistryFriendlyByteBuf, CHEMICAL> chemicalStreamCodec = ByteBufCodecs.registry(registry);
        return new StreamCodec<>() {
            @Override
            public STACK decode(RegistryFriendlyByteBuf buf) {
                long amount = buf.readVarLong();
                if (amount <= 0) {
                    return empty;
                }
                CHEMICAL chemical = chemicalStreamCodec.decode(buf);
                return constructor.apply(chemical, amount);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, STACK stack) {
                buf.writeVarLong(stack.getAmount());
                if (!stack.isEmpty()) {
                    chemicalStreamCodec.encode(buf, stack.getChemical());
                }
            }
        };
    }

    /**
     * A stream codec for chemical stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> StreamCodec<RegistryFriendlyByteBuf, STACK> streamCodec(
          StreamCodec<RegistryFriendlyByteBuf, STACK> optionalStreamCodec) {
        return new StreamCodec<>() {
            @Override
            public STACK decode(RegistryFriendlyByteBuf buf) {
                STACK stack = optionalStreamCodec.decode(buf);
                if (stack.isEmpty()) {
                    throw new DecoderException("Empty ChemicalStack not allowed");
                }
                return stack;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, STACK stack) {
                if (stack.isEmpty()) {
                    throw new EncoderException("Empty ChemicalStack not allowed");
                }
                optionalStreamCodec.encode(buf, stack);
            }
        };
    }

    /**
     * Codec to get any kind of chemical stack (that does not accept empty stacks), based on a "chemicalType" field.
     *
     * @see ChemicalType
     * @see mekanism.api.chemical.merged.BoxedChemicalStack
     * @since 10.6.0
     */
    public static final Codec<ChemicalStack<?>> BOXED_CODEC = ChemicalType.CODEC.dispatch(JsonConstants.CHEMICAL_TYPE, ChemicalType::getTypeFor, type -> switch (type) {
        case GAS -> GasStack.MAP_CODEC;
        case INFUSION -> InfusionStack.MAP_CODEC;
        case PIGMENT -> PigmentStack.MAP_CODEC;
        case SLURRY -> SlurryStack.MAP_CODEC;
    });
    /**
     * StreamCodec to get any kind of chemical stack (that does not accept empty stacks), based on a "chemicalType" field.
     *
     * @see ChemicalType
     * @see mekanism.api.chemical.merged.BoxedChemicalStack
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalStack<?>> BOXED_STREAM_CODEC = ChemicalType.STREAM_CODEC.<RegistryFriendlyByteBuf>cast()
          .dispatch(ChemicalType::getTypeFor, type -> switch (type) {
              case GAS -> GasStack.STREAM_CODEC;
              case INFUSION -> InfusionStack.STREAM_CODEC;
              case PIGMENT -> PigmentStack.STREAM_CODEC;
              case SLURRY -> SlurryStack.STREAM_CODEC;
          });
    /**
     * StreamCodec to get any kind of chemical stack, based on a "chemicalType" field.
     *
     * @see ChemicalType
     * @see mekanism.api.chemical.merged.BoxedChemicalStack
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalStack<?>> BOXED_OPTIONAL_STREAM_CODEC = ChemicalType.STREAM_CODEC.<RegistryFriendlyByteBuf>cast()
          .dispatch(ChemicalType::getTypeFor, type -> switch (type) {
              case GAS -> GasStack.OPTIONAL_STREAM_CODEC;
              case INFUSION -> InfusionStack.OPTIONAL_STREAM_CODEC;
              case PIGMENT -> PigmentStack.OPTIONAL_STREAM_CODEC;
              case SLURRY -> SlurryStack.OPTIONAL_STREAM_CODEC;
          });

    private final CHEMICAL chemical;
    private long amount;

    protected ChemicalStack(Holder<CHEMICAL> chemical, long amount) {
        this(chemical.value(), amount);
    }

    protected ChemicalStack(CHEMICAL chemical, long amount) {
        Objects.requireNonNull(chemical, "Cannot create a ChemicalStack from a null chemical");
        this.chemical = chemical;
        this.amount = amount;
    }

    protected ChemicalStack(@Nullable Void unused) {
        this.chemical = null;
    }

    /**
     * Registry the chemical is a part of.
     */
    protected abstract Registry<CHEMICAL> getRegistry();

    /**
     * Helper ot get the empty version of this chemical.
     */
    protected abstract CHEMICAL getEmptyChemical();

    /**
     * Copies this chemical stack into a new chemical stack.
     */
    public abstract ChemicalStack<CHEMICAL> copy();

    /**
     * Copies this chemical stack into a new chemical stack ith the given amount.
     *
     * @param amount New Amount
     *
     * @since 10.6.0
     */
    public abstract ChemicalStack<CHEMICAL> copyWithAmount(long amount);

    /**
     * Splits off a stack of the given amount of this stack and reduces this stack by the amount.
     *
     * @since 10.6.0
     */
    public abstract ChemicalStack<CHEMICAL> split(long amount);

    /**
     * Creates a copy of this stack with {@code 0} amount.
     */
    public abstract ChemicalStack<CHEMICAL> copyAndClear();

    /**
     * Gets the chemical represented by this stack.
     *
     * @return Backing chemical.
     *
     * @since 10.6.0 Previously was getType
     */
    public final CHEMICAL getChemical() {
        return isEmpty() ? getEmptyChemical() : chemical;
    }

    /**
     * Gets the holder for chemical represented by this stack.
     *
     * @return Backing chemical's holder.
     *
     * @since 10.6.0
     */
    public Holder<CHEMICAL> getChemicalHolder() {
        return getRegistry().wrapAsHolder(getChemical());
    }

    /**
     * Checks if the chemical for this stack is in the given tag.
     *
     * @return Tag to check.
     *
     * @since 10.6.0
     */
    public boolean is(TagKey<CHEMICAL> tag) {
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
    public boolean is(CHEMICAL chemical) {
        return getChemical() == chemical;
    }

    /**
     * Whether this ChemicalStack's chemical type matches the given predicate.
     *
     * @param predicate - Predicate to test
     *
     * @return if the ChemicalStack's type matches the given predicate
     */
    public boolean is(Predicate<Holder<CHEMICAL>> predicate) {
        return predicate.test(getChemicalHolder());
    }

    /**
     * Whether this ChemicalStack's chemical type is equal to the other holder's chemical.
     *
     * @param holder - Chemical holder to check
     *
     * @return if the ChemicalStack's type is the same as the given holder's chemical
     */
    public boolean is(Holder<CHEMICAL> holder) {
        return is(holder.value());
    }

    /**
     * Checks if the chemical for this stack is part of the given holder set.
     *
     * @return Holder set to check.
     *
     * @since 10.6.0
     */
    public boolean is(HolderSet<CHEMICAL> holderSet) {
        return holderSet.contains(getChemicalHolder());
    }

    /**
     * Gets the tags that this chemical is a part of.
     *
     * @return All the tags this chemical is a part of.
     *
     * @since 10.6.0
     */
    public Stream<TagKey<CHEMICAL>> getTags() {
        return getChemicalHolder().tags();
    }

    /**
     * Whether this ChemicalStack's chemical type is equal to the other defined ChemicalStack.
     *
     * @param stack - ChemicalStack to check
     *
     * @return if the ChemicalStacks contain the same chemical type
     */
    public boolean isTypeEqual(ChemicalStack<CHEMICAL> stack) {
        //TODO - 1.20.5: Should we replace this with the static method?
        return is(stack.getChemical());
    }

    /**
     * Saves this stack to a tag, directly writing the keys into the passed tag.
     *
     * @throws IllegalStateException if this stack is empty
     * @since 10.6.0
     */
    public abstract Tag save(HolderLookup.Provider lookupProvider, Tag prefix);

    /**
     * Saves this stack to a new tag.
     *
     * @throws IllegalStateException if this stack is empty
     * @since 10.6.0
     */
    public abstract Tag save(HolderLookup.Provider lookupProvider);

    /**
     * Saves this stack to a new tag. Empty stacks are supported and will be saved as an empty tag.
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
        ChemicalStack<?> other = (ChemicalStack<?>) o;
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
     * Writes this ChemicalStack to a Packet Buffer.
     *
     * @param buffer - Buffer to write to.
     */
    public void writeToPacket(FriendlyByteBuf buffer) {
        buffer.writeById(getRegistry()::getId, getChemical());
        if (!isEmpty()) {
            buffer.writeVarLong(getAmount());
        }
    }

    /**
     * Checks if the two chemical stacks have the same chemical type. Ignores amount.
     *
     * @return {@code true} if the two chemical stacks have the same chemical
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>> boolean isSameChemical(ChemicalStack<CHEMICAL> first, ChemicalStack<CHEMICAL> second) {
        return first.is(second.getChemical());
    }
}