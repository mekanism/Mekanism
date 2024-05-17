package mekanism.api.recipes.ingredients;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.slurry.IEmptySlurryProvider;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Standard implementation for a SlurryIngredient with an amount.
 *
 * <p>{@link ISlurryIngredient}, like its item counterpart, explicitly does not perform count checks,
 * so this class is used to (a) wrap a standard SlurryIngredient with an amount and (b) provide a standard serialization format for mods to use.
 * <p>
 * * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#slurryStack()}.
 *
 * @see net.neoforged.neoforge.common.crafting.SizedIngredient
 */
public final class SlurryStackIngredient extends ChemicalStackIngredient<Slurry, SlurryStack, ISlurryIngredient> implements IEmptySlurryProvider {

    /**
     * The "flat" codec for {@link SlurryStackIngredient}.
     *
     * <p>The amount is serialized inline with the rest of the ingredient, for example:
     *
     * <pre>{@code
     * {
     *     "slurry": "mekanism:dirty_copper",
     *     "amount": 250
     * }
     * }</pre>
     *
     * <p>
     * <p>
     * Compound slurry ingredients are always serialized using the map codec, i.e.
     *
     * <pre>{@code
     * {
     *     "type": "mekanism:compound",
     *     "ingredients": [
     *         { "slurry": "mekanism:dirty_copper" },
     *         { "slurry": "mekanism:clean_copper" }
     *     ],
     *     "amount": 500
     * }
     * }</pre>
     *
     * @since 10.6.0
     */
    public static final Codec<SlurryStackIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          IngredientCreatorAccess.slurry().mapCodecNonEmpty().forGetter(SlurryStackIngredient::ingredient),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(NBTConstants.AMOUNT).forGetter(SlurryStackIngredient::amount)
    ).apply(instance, SlurryStackIngredient::new));

    /**
     * The "nested" codec for {@link SlurryStackIngredient}.
     *
     * <p>With this codec, the amount is <i>always</i> serialized separately from the ingredient itself, for example:
     *
     * <pre>{@code
     * {
     *     "ingredient": {
     *         "slurry": "mekanism:dirty_copper"
     *     },
     *     "amount": 1000
     * }
     * }</pre>
     *
     * @since 10.6.0
     */
    public static final Codec<SlurryStackIngredient> NESTED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          IngredientCreatorAccess.slurry().codecNonEmpty().fieldOf("ingredient").forGetter(SlurryStackIngredient::ingredient),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(NBTConstants.AMOUNT).forGetter(SlurryStackIngredient::amount)
    ).apply(instance, SlurryStackIngredient::new));

    /**
     * A stream codec for sending {@link SlurryStackIngredient}s over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, SlurryStackIngredient> STREAM_CODEC = StreamCodec.composite(
          IngredientCreatorAccess.slurry().streamCodec(), SlurryStackIngredient::ingredient,
          ByteBufCodecs.VAR_LONG, SlurryStackIngredient::amount,
          SlurryStackIngredient::new
    );

    /**
     * Creates a Slurry Stack Ingredient that matches a given ingredient and amount. Prefer calling via
     * {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#slurryStack()} and
     * {@link mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator#from(IChemicalIngredient, long)}.
     *
     * @param ingredient Ingredient to match.
     * @param amount     Amount to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty.
     * @since 10.6.0
     */
    public static SlurryStackIngredient of(ISlurryIngredient ingredient, long amount) {
        Objects.requireNonNull(ingredient, "SlurryStackIngredients cannot be created from a null ingredient.");
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("SlurryStackIngredients cannot be created using the empty ingredient.");
        }
        return new SlurryStackIngredient(ingredient, amount);
    }

    private SlurryStackIngredient(ISlurryIngredient ingredient, long amount) {
        super(ingredient, amount);
    }
}