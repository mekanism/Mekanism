package mekanism.api.recipes.ingredients;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IEmptyGasProvider;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Standard implementation for a GasIngredient with an amount.
 *
 * <p>{@link IGasIngredient}, like its item counterpart, explicitly does not perform count checks,
 * so this class is used to (a) wrap a standard GasIngredient with an amount and (b) provide a standard serialization format for mods to use.
 * <p>
 * * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#gasStack()}.
 *
 * @see net.neoforged.neoforge.common.crafting.SizedIngredient
 */
public final class GasStackIngredient extends ChemicalStackIngredient<Gas, GasStack, IGasIngredient> implements IEmptyGasProvider {

    /**
     * The "flat" codec for {@link GasStackIngredient}.
     *
     * <p>The amount is serialized inline with the rest of the ingredient, for example:
     *
     * <pre>{@code
     * {
     *     "gas": "mekanism:hydrogen",
     *     "amount": 250
     * }
     * }</pre>
     *
     * <p>
     * <p>
     * Compound gas ingredients are always serialized using the map codec, i.e.
     *
     * <pre>{@code
     * {
     *     "type": "mekanism:compound",
     *     "ingredients": [
     *         { "gas": "mekanism:hydrogen" },
     *         { "gas": "mekanism:oxygen" }
     *     ],
     *     "amount": 500
     * }
     * }</pre>
     *
     * @since 10.6.0
     */
    public static final Codec<GasStackIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          IngredientCreatorAccess.gas().mapCodecNonEmpty().forGetter(GasStackIngredient::ingredient),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(NBTConstants.AMOUNT).forGetter(GasStackIngredient::amount)
    ).apply(instance, GasStackIngredient::new));

    /**
     * The "nested" codec for {@link GasStackIngredient}.
     *
     * <p>With this codec, the amount is <i>always</i> serialized separately from the ingredient itself, for example:
     *
     * <pre>{@code
     * {
     *     "ingredient": {
     *         "gas": "mekanism:hydrogen"
     *     },
     *     "amount": 1000
     * }
     * }</pre>
     *
     * @since 10.6.0
     */
    public static final Codec<GasStackIngredient> NESTED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          IngredientCreatorAccess.gas().codecNonEmpty().fieldOf("ingredient").forGetter(GasStackIngredient::ingredient),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(NBTConstants.AMOUNT).forGetter(GasStackIngredient::amount)
    ).apply(instance, GasStackIngredient::new));

    /**
     * A stream codec for sending {@link GasStackIngredient}s over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, GasStackIngredient> STREAM_CODEC = StreamCodec.composite(
          IngredientCreatorAccess.gas().streamCodec(), GasStackIngredient::ingredient,
          ByteBufCodecs.VAR_LONG, GasStackIngredient::amount,
          GasStackIngredient::new
    );

    /**
     * Creates a Gas Stack Ingredient that matches a given ingredient and amount. Prefer calling via
     * {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#gasStack()} and
     * {@link mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator#from(IChemicalIngredient, long)}.
     *
     * @param ingredient Ingredient to match.
     * @param amount     Amount to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty.
     * @since 10.6.0
     */
    public static GasStackIngredient of(IGasIngredient ingredient, long amount) {
        Objects.requireNonNull(ingredient, "GasStackIngredients cannot be created from a null ingredient.");
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("GasStackIngredients cannot be created using the empty ingredient.");
        }
        return new GasStackIngredient(ingredient, amount);
    }

    private GasStackIngredient(IGasIngredient ingredient, long amount) {
        super(ingredient, amount);
    }
}