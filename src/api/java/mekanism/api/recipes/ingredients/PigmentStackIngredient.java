package mekanism.api.recipes.ingredients;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.pigment.IEmptyPigmentProvider;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Standard implementation for a PigmentIngredient with an amount.
 *
 * <p>{@link IPigmentIngredient}, like its item counterpart, explicitly does not perform count checks,
 * so this class is used to (a) wrap a standard PigmentIngredient with an amount and (b) provide a standard serialization format for mods to use.
 * <p>
 * * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#pigment()}.
 *
 * @see net.neoforged.neoforge.common.crafting.SizedIngredient
 */
public final class PigmentStackIngredient extends ChemicalStackIngredient<Pigment, PigmentStack, IPigmentIngredient> implements IEmptyPigmentProvider {

    /**
     * The "flat" codec for {@link PigmentStackIngredient}.
     *
     * <p>The amount is serialized inline with the rest of the ingredient, for example:
     *
     * <pre>{@code
     * {
     *     "pigment": "mekanism:red",
     *     "amount": 250
     * }
     * }</pre>
     *
     * <p>
     * <p>
     * Compound pigment ingredients are always serialized using the map codec, i.e.
     *
     * <pre>{@code
     * {
     *     "type": "mekanism:compound",
     *     "ingredients": [
     *         { "pigment": "mekanism:red" },
     *         { "pigment": "mekanism:blue" }
     *     ],
     *     "amount": 500
     * }
     * }</pre>
     *
     * @since 10.6.0
     */
    public static final Codec<PigmentStackIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          IngredientCreatorAccess.basicPigment().mapCodecNonEmpty().forGetter(PigmentStackIngredient::ingredient),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(NBTConstants.AMOUNT).forGetter(PigmentStackIngredient::amount)
    ).apply(instance, PigmentStackIngredient::new));

    /**
     * The "nested" codec for {@link PigmentStackIngredient}.
     *
     * <p>With this codec, the amount is <i>always</i> serialized separately from the ingredient itself, for example:
     *
     * <pre>{@code
     * {
     *     "ingredient": {
     *         "pigment": "mekanism:red"
     *     },
     *     "amount": 1000
     * }
     * }</pre>
     *
     * @since 10.6.0
     */
    public static final Codec<PigmentStackIngredient> NESTED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          IngredientCreatorAccess.basicPigment().codecNonEmpty().fieldOf("ingredient").forGetter(PigmentStackIngredient::ingredient),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(NBTConstants.AMOUNT).forGetter(PigmentStackIngredient::amount)
    ).apply(instance, PigmentStackIngredient::new));

    /**
     * A stream codec for sending {@link PigmentStackIngredient}s over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, PigmentStackIngredient> STREAM_CODEC = StreamCodec.composite(
          IngredientCreatorAccess.basicPigment().streamCodec(), PigmentStackIngredient::ingredient,
          ByteBufCodecs.VAR_LONG, PigmentStackIngredient::amount,
          PigmentStackIngredient::new
    );

    /**
     * Creates a Pigment Stack Ingredient that matches a given ingredient and amount. Prefer calling via
     * {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#pigment()} and
     * {@link mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator#from(IChemicalIngredient, long)}.
     *
     * @param ingredient Ingredient to match.
     * @param amount     Amount to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty.
     * @since 10.6.0
     */
    public static PigmentStackIngredient of(IPigmentIngredient ingredient, long amount) {
        Objects.requireNonNull(ingredient, "PigmentStackIngredients cannot be created from a null ingredient.");
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("PigmentStackIngredients cannot be created using the empty ingredient.");
        }
        return new PigmentStackIngredient(ingredient, amount);
    }

    private PigmentStackIngredient(IPigmentIngredient ingredient, long amount) {
        super(ingredient, amount);
    }
}