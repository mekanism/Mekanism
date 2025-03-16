package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPITags;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.attribute.IChemicalAttributeContainer;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.IWithData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class ChemicalStack implements IHasTextComponent, IHasTranslationKey, IChemicalAttributeContainer<ChemicalStack>, IWithData<Chemical> {

    private static final Consumer<String> ON_STACK_LOAD_ERROR = error -> MekanismAPI.logger.error("Tried to load invalid chemical: '{}'", error);

    /**
     * Empty ChemicalStack instance.
     */
    public static final ChemicalStack EMPTY = new ChemicalStack(null);

    /**
     * A standard codec for non-empty Chemical holders.
     *
     * @since 10.7.11
     */
    public static final Codec<Holder<Chemical>> CHEMICAL_NON_EMPTY_HOLDER_CODEC = Chemical.HOLDER_CODEC//TODO - 1.22: Rename this to CHEMICAL_NON_EMPTY_CODEC
          .validate(chemical -> chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY) ? DataResult.error(() -> "Chemical must not be mekanism:empty") : DataResult.success(chemical));
    /**
     * A standard codec for non-empty Chemicals.
     *
     * @since 10.6.0
     * @deprecated Use {@link #CHEMICAL_NON_EMPTY_HOLDER_CODEC} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public static final Codec<Chemical> CHEMICAL_NON_EMPTY_CODEC = CHEMICAL_NON_EMPTY_HOLDER_CODEC.xmap(Holder::value, Chemical::getAsHolder);
    /**
     * A standard map codec for Chemical stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final MapCodec<ChemicalStack> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          CHEMICAL_NON_EMPTY_HOLDER_CODEC.fieldOf(SerializationConstants.ID).forGetter(ChemicalStack::getChemicalHolder),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(SerializationConstants.AMOUNT).forGetter(ChemicalStack::getAmount)
    ).apply(instance, ChemicalStack::new));
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
    public static final Codec<ChemicalStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
          .xmap(optional -> optional.orElse(EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    /**
     * A standard codec for Chemical stacks that accepts empty stacks, serializing them as {@code {}}.
     *
     * @since 10.7.9
     */
    public static final Codec<ChemicalStack> LENIENT_OPTIONAL_CODEC = OPTIONAL_CODEC.promotePartial(ON_STACK_LOAD_ERROR).orElse(EMPTY);
    /**
     * A stream codec for Chemical stacks that accepts empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ChemicalStack decode(RegistryFriendlyByteBuf buffer) {
            long amount = buffer.readVarLong();
            if (amount <= 0) {
                return EMPTY;
            }
            return new ChemicalStack(Chemical.HOLDER_STREAM_CODEC.decode(buffer), amount);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ChemicalStack stack) {
            buffer.writeVarLong(stack.getAmount());
            if (!stack.isEmpty()) {
                Chemical.HOLDER_STREAM_CODEC.encode(buffer, stack.getChemicalHolder());
            }
        }
    };
    /**
     * A stream codec for Chemical stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalStack> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ChemicalStack decode(RegistryFriendlyByteBuf buffer) {
            ChemicalStack stack = OPTIONAL_STREAM_CODEC.decode(buffer);
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
            OPTIONAL_STREAM_CODEC.encode(buffer, stack);
        }
    };

    /**
     * A standard codec for chemical stacks that always deserializes with a fixed amount, and does not accept empty stacks.
     * <p>
     * Chemical equivalent of {@link ItemStack#SINGLE_ITEM_CODEC}. and {@link net.neoforged.neoforge.fluids.FluidStack#fixedAmountCodec(int)}
     *
     * @since 10.6.0
     */
    public static Codec<ChemicalStack> fixedAmountCodec(long amount) {
        return RecordCodecBuilder.create(instance -> instance.group(
              CHEMICAL_NON_EMPTY_HOLDER_CODEC.fieldOf(SerializationConstants.ID).forGetter(ChemicalStack::getChemicalHolder)
        ).apply(instance, holder -> new ChemicalStack(holder, amount)));
    }

    @Nullable
    private final Holder<Chemical> chemical;
    private long amount;

    /**
     * Creates a chemical stack from a holder and a given amount.
     *
     * @param chemical Holder representing the chemical this stack is for. It is recommended to use a reference holder ({@link net.minecraft.core.Holder.Reference} or
     *                 {@link net.neoforged.neoforge.registries.DeferredHolder}, but if a direct holder is used the stack will attempt to look up the corresponding
     *                 reference holder.
     * @param amount   Amount of chemical in this stack. If this is less than or equal to zero the stack will be considered empty.
     *
     * @throws NullPointerException     If the chemical holder is null.
     * @throws IllegalArgumentException If the chemical holder is a direct holder that is either: not bound, the value it is bound to doesn't have a registered reference
     *                                  in the chemical registry.
     */
    public ChemicalStack(Holder<Chemical> chemical, long amount) {
        Objects.requireNonNull(chemical, "Cannot create a ChemicalStack from a null chemical holder");
        if (chemical.kind() == Holder.Kind.DIRECT) {
            if (!chemical.isBound()) {//This should always be true, unless someone made a custom direct holder for some reason
                throw new IllegalArgumentException("Cannot create a ChemicalStack from an unbound direct holder");
            }
            //Try to look up the reference holder from the registry
            chemical = MekanismAPI.CHEMICAL_REGISTRY.wrapAsHolder(chemical.value());
            if (chemical.kind() == Holder.Kind.DIRECT) {
                throw new IllegalArgumentException("Cannot create a ChemicalStack from a direct holder for a chemical that is not yet registered");
            }
        }
        this.chemical = chemical;
        this.amount = amount;
    }

    /**
     * Creates a chemical stack from a chemical and a given amount.
     *
     * @param chemical Chemical this stack is for.
     * @param amount   Amount of chemical in this stack. If this is less than or equal to zero the stack will be considered empty.
     *
     * @throws NullPointerException If the chemical is null.
     * @deprecated Use {@link #ChemicalStack(Holder, long)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public ChemicalStack(Chemical chemical, long amount) {
        this(Objects.requireNonNull(chemical, "Cannot create a ChemicalStack from a null chemical").getAsHolder(), amount);
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
        return new ChemicalStack(getChemicalHolder(), getAmount());
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
        return new ChemicalStack(getChemicalHolder(), amount);
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
        return getChemicalHolder().value();
    }

    /**
     * Gets the holder for chemical represented by this stack.
     *
     * @return Backing chemical's holder.
     *
     * @since 10.6.0
     */
    public Holder<Chemical> getChemicalHolder() {
        //Note: We know chemical is not null here as that gets checked as part of isEmpty
        return isEmpty() ? MekanismAPI.EMPTY_CHEMICAL_HOLDER : chemical;
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
    @Deprecated(forRemoval = true, since = "10.7.11")
    public ResourceLocation getTypeRegistryName() {
        ResourceKey<?> key = getChemicalHolder().getKey();
        return key == null ? MekanismAPI.CHEMICAL_REGISTRY.getDefaultKey() : key.location();
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
        return chemical == null || chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY) || this.amount <= 0;
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
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public boolean has(Class<? extends ChemicalAttribute> type) {
        return getChemical().has(type);
    }

    @Override
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public boolean hasLegacy(Class<? extends ChemicalAttribute> type) {
        return getChemical().hasLegacy(type);
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

    /**
     * {@return radiation level of this chemical (scaled based on amount), or zero if it is not radioactive}
     *
     * @since 10.7.11
     */
    public double getRadioactivity() {
        return getChemical().getRadioactivity() * getAmount();
    }

    @Nullable
    @Override
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public <ATTRIBUTE extends ChemicalAttribute> ATTRIBUTE get(Class<ATTRIBUTE> type) {
        return getChemical().get(type);
    }

    @Nullable
    @Override
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public <ATTRIBUTE extends ChemicalAttribute> ATTRIBUTE getLegacy(Class<ATTRIBUTE> type) {
        return getChemical().getLegacy(type);
    }

    @Override
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public Collection<ChemicalAttribute> getAttributes() {
        return getChemical().getAttributes();
    }

    @Override
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public Collection<Class<? extends ChemicalAttribute>> getAttributeTypes() {
        return getChemical().getAttributeTypes();
    }

    /**
     * Gathers any tooltips this chemical stack has, and adds them to the list. This includes things like if the chemical is immune to decay, or the registry name
     *
     * @param context     Current tooltip context.
     * @param tooltips    List of tooltips to add to.
     * @param tooltipFlag Flag representing if advanced tooltips are to be shown.
     *
     * @see Chemical#appendHoverText(ChemicalStack, TooltipContext, List, TooltipFlag)
     * @since 10.7.11
     */
    public void appendHoverText(TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
        Holder<Chemical> chemicalHolder = getChemicalHolder();
        if (chemicalHolder.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
            return;
        }
        chemicalHolder.value().appendHoverText(this, context, tooltips, tooltipFlag);
        if (chemicalHolder.is(MekanismAPITags.Chemicals.WASTE_BARREL_DECAY_BLACKLIST)) {
            tooltips.add(APILang.DECAY_IMMUNE.translateColored(EnumColor.AQUA));
        }
        if (tooltipFlag.isAdvanced()) {
            //If advanced tooltips are on, display the registry name
            tooltips.add(TextComponentUtil.build(ChatFormatting.DARK_GRAY, getChemicalHolder().getRegisteredName()));
        }
    }

    @Nullable
    @Override
    public <T> T getData(DataMapType<Chemical, T> type) {
        //Note: We only accept reference holders, and reference holders can be queried directly for data
        return getChemicalHolder().getData(type);
    }

    @Override
    public int hashCode() {
        if (isEmpty()) {
            return 0;
        }
        //Note: chemical is not null here, and we know it isn't empty so we can just directly reference it
        // rather than having to check if it is empty again
        int hash = chemical.hashCode();
        return 31 * hash + Long.hashCode(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChemicalStack other = (ChemicalStack) o;
        return getAmount() == other.getAmount() && is(other.getChemicalHolder());
    }

    @Override
    public String toString() {
        return getAmount() + " " + getChemicalHolder().getRegisteredName();
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
        return first.is(second.getChemicalHolder());
    }

    /**
     * Tries to parse a chemical stack. Empty stacks cannot be parsed with this method.
     *
     * @since 10.6.0
     */
    public static Optional<ChemicalStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag).resultOrPartial(ON_STACK_LOAD_ERROR);
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