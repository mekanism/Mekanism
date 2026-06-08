package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPITags;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class ChemicalStack implements ChemicalInstance, IHasTextComponent, IHasTranslationKey {

    /**
     * Empty ChemicalStack instance.
     */
    public static final ChemicalStack EMPTY = new ChemicalStack(null);

    /**
     * A standard map codec for Chemical stacks that does not accept empty stacks.
     *
     * @since 10.6.0
     */
    public static final MapCodec<ChemicalStack> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          CHEMICAL_HOLDER_CODEC.fieldOf(FIELD_ID).forGetter(ChemicalStack::typeHolder),
          ExtraCodecs.POSITIVE_INT.fieldOf(FIELD_AMOUNT).forGetter(ChemicalStack::amount)
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
     * A stream codec for Chemical stacks that accepts empty stacks.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ChemicalStack decode(RegistryFriendlyByteBuf buffer) {
            int amount = buffer.readVarInt();
            if (amount <= 0) {
                return EMPTY;
            }
            return new ChemicalStack(CHEMICAL_HOLDER_STREAM_CODEC.decode(buffer), amount);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ChemicalStack stack) {
            buffer.writeVarInt(stack.amount());
            if (!stack.isEmpty()) {
                CHEMICAL_HOLDER_STREAM_CODEC.encode(buffer, stack.typeHolder());
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
     * Chemical equivalent of {@link net.neoforged.neoforge.fluids.FluidStack#fixedAmountCodec(int)}
     *
     * @since 10.6.0
     */
    public static Codec<ChemicalStack> fixedAmountCodec(int amount) {
        return RecordCodecBuilder.create(instance -> instance.group(
              CHEMICAL_HOLDER_CODEC.fieldOf(FIELD_ID).forGetter(ChemicalStack::typeHolder)
        ).apply(instance, holder -> new ChemicalStack(holder, amount)));
    }

    @Nullable
    private final Holder<Chemical> chemical;
    private int amount;

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
    public ChemicalStack(Holder<Chemical> chemical, int amount) {
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
        return new ChemicalStack(typeHolder(), amount());
    }

    /**
     * Copies this chemical stack into a new chemical stack ith the given amount.
     *
     * @param amount New Amount
     *
     * @since 10.6.0
     */
    public ChemicalStack copyWithAmount(int amount) {
        if (isEmpty() || amount == 0) {
            return EMPTY;
        }
        return new ChemicalStack(typeHolder(), amount);
    }

    /**
     * Splits off a stack of the given amount of this stack and reduces this stack by the amount.
     *
     * @since 10.6.0
     */
    public ChemicalStack split(int amount) {
        int i = Math.min(amount, amount());
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
        return typeHolder().value();
    }

    /**
     * Gets the holder for chemical represented by this stack.
     *
     * @return Backing chemical's holder.
     */
    @Override
    public Holder<Chemical> typeHolder() {
        //Note: We know chemical is not null here as that gets checked as part of isEmpty
        return isEmpty() ? MekanismAPI.EMPTY_CHEMICAL_HOLDER : chemical;
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
    @Override
    public int amount() {
        return isEmpty() ? 0 : amount;
    }

    /**
     * Sets this stack's amount to the given amount.
     *
     * @param amount The amount to set this stack's amount to.
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Limits the amount of this stack to at most the given amount.
     *
     * @param amount Amount to max the stack out at.
     *
     * @since 10.6.0
     */
    public void limitSize(int amount) {
        if (!isEmpty() && amount() > amount) {
            setAmount(amount);
        }
    }

    /**
     * Grows this stack's amount by the given amount.
     *
     * @param amount The amount to grow this stack by.
     *
     * @apiNote Negative values are valid and will instead shrink the stack.
     * @implNote No checks are made to ensure that the int does not overflow.
     */
    public void grow(int amount) {
        setAmount(this.amount + amount);
    }

    /**
     * Shrinks this stack's amount by the given amount.
     *
     * @param amount The amount to shrink this stack by.
     *
     * @apiNote Negative values are valid and will instead grow the stack.
     * @implNote No checks are made to ensure that the int does not underflow.
     */
    public void shrink(int amount) {
        setAmount(this.amount - amount);
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
        Holder<Chemical> chemicalHolder = typeHolder();
        if (chemicalHolder.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
            return;
        }
        //TODO - 26.1: Do we want to fire an event similar to fluid stacks?
        chemicalHolder.value().appendHoverText(this, context, tooltips, tooltipFlag);
        if (chemicalHolder.is(MekanismAPITags.Chemicals.WASTE_BARREL_DECAY_BLACKLIST)) {
            tooltips.add(APILang.DECAY_IMMUNE.translateColored(EnumColor.AQUA));
        }
        if (tooltipFlag.isAdvanced()) {
            //If advanced tooltips are on, display the registry name
            tooltips.add(TextComponentUtil.build(ChatFormatting.DARK_GRAY, typeHolder().getRegisteredName()));
        }
    }

    @Override
    public int hashCode() {
        if (isEmpty()) {
            return 0;
        }
        //Note: chemical is not null here, and we know it isn't empty so we can just directly reference it
        // rather than having to check if it is empty again
        int hash = chemical.hashCode();
        return 31 * hash + amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChemicalStack other = (ChemicalStack) o;
        return amount() == other.amount() && is(other.typeHolder());
    }

    @Override
    public String toString() {
        return amount() + " " + typeHolder().getRegisteredName();
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
        return first.is(second.typeHolder());
    }
}