package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Range;

/// Immutable template for creating [chemical stacks][ChemicalStack].
///
/// @param chemical Chemical holder.
/// @param amount   Amount of the chemical.
///
/// @since 10.8.0
public record ChemicalStackTemplate(Holder<Chemical> chemical, @Range(from = 1, to = Integer.MAX_VALUE) int amount) implements SizedChemicalInstance {

    /// A standard map codec for chemical stack templates.
    public static final MapCodec<ChemicalStackTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
          CHEMICAL_HOLDER_CODEC.fieldOf(FIELD_ID).forGetter(ChemicalStackTemplate::chemical),
          ExtraCodecs.POSITIVE_INT.fieldOf(FIELD_AMOUNT).forGetter(ChemicalStackTemplate::amount)
    ).apply(i, ChemicalStackTemplate::new));

    /// A codec for chemical stack templates that supports both the [#MAP_CODEC] format, and [#CHEMICAL_HOLDER_CODEC]. If the latter is used, the template will be
    /// initialized with an amount of [FluidType#BUCKET_VOLUME]
    public static final Codec<ChemicalStackTemplate> CODEC = Codec.withAlternative(MAP_CODEC.codec(), CHEMICAL_HOLDER_CODEC,
          chemical -> new ChemicalStackTemplate(chemical, FluidType.BUCKET_VOLUME));

    /// A stream codec for chemical stack templates
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalStackTemplate> STREAM_CODEC = StreamCodec.composite(
          CHEMICAL_HOLDER_STREAM_CODEC, ChemicalStackTemplate::chemical,
          ByteBufCodecs.VAR_INT, ChemicalStackTemplate::amount,
          ChemicalStackTemplate::new);

    public ChemicalStackTemplate {
        if (amount <= 0 || chemical.is(ChemicalIds.EMPTY)) {
            throw new IllegalStateException("Chemical must be non-empty");
        }
    }

    /// {@return a chemical stack template for the given chemical stack}
    ///
    /// @param stack Stack to make a template from.
    ///
    /// @throws IllegalStateException if the passed stack is empty.
    public static ChemicalStackTemplate fromNonEmptyStack(ChemicalStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack must be non-empty");
        }
        return new ChemicalStackTemplate(stack.typeHolder(), stack.amount());
    }

    /// {@return a chemical stack template with the same type as the current one, but with the given amount}
    ///
    /// @param amount Amount of the chemical.
    public ChemicalStackTemplate withAmount(@Range(from = 1, to = Integer.MAX_VALUE) int amount) {
        return this.amount == amount ? this : new ChemicalStackTemplate(chemical, amount);
    }

    /// {@return chemical stack made from this template}
    public ChemicalStack create() {
        return new ChemicalStack(chemical, amount);
    }

    @Override
    public Holder<Chemical> typeHolder() {
        return chemical;
    }

    /// A standard codec for chemical stack templates that always deserializes with a fixed amount.
    ///
    /// Chemical equivalent of [net.neoforged.neoforge.fluids.FluidStackTemplate#fixedAmountCodec(int)]
    public static Codec<ChemicalStackTemplate> fixedAmountCodec(@Range(from = 1, to = Integer.MAX_VALUE) int amount) {
        return Codec.lazyInitialized(() -> RecordCodecBuilder.create(i -> i.group(
              CHEMICAL_HOLDER_CODEC.fieldOf(FIELD_ID).forGetter(ChemicalStackTemplate::chemical)
        ).apply(i, holder -> new ChemicalStackTemplate(holder, amount))));
    }

    /// A standard stream codec for chemical stack templates that always deserializes with a fixed amount.
    ///
    /// Chemical equivalent of [net.neoforged.neoforge.fluids.FluidStackTemplate#fixedAmountStreamCodec(int)]
    public static StreamCodec<RegistryFriendlyByteBuf, ChemicalStackTemplate> fixedAmountStreamCodec(@Range(from = 1, to = Integer.MAX_VALUE) int amount) {
        return StreamCodec.composite(
              CHEMICAL_HOLDER_STREAM_CODEC, ChemicalStackTemplate::chemical,
              holder -> new ChemicalStackTemplate(holder, amount)
        );
    }
}