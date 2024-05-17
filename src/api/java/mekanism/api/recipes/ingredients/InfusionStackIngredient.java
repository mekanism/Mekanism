package mekanism.api.recipes.ingredients;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.infuse.IEmptyInfusionProvider;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Standard implementation for an InfusionIngredient with an amount.
 *
 * <p>{@link IInfusionIngredient}, like its item counterpart, explicitly does not perform count checks,
 * so this class is used to (a) wrap a standard InfusionIngredient with an amount and (b) provide a standard serialization format for mods to use.
 * <p>
 * * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#infusion()}.
 *
 * @see net.neoforged.neoforge.common.crafting.SizedIngredient
 */
public final class InfusionStackIngredient extends ChemicalStackIngredient<InfuseType, InfusionStack, IInfusionIngredient> implements IEmptyInfusionProvider {

    /**
     * The "flat" codec for {@link InfusionStackIngredient}.
     *
     * <p>The amount is serialized inline with the rest of the ingredient, for example:
     *
     * <pre>{@code
     * {
     *     "infuse_type": "mekanism:carbon",
     *     "amount": 250
     * }
     * }</pre>
     *
     * <p>
     * <p>
     * Compound infusion ingredients are always serialized using the map codec, i.e.
     *
     * <pre>{@code
     * {
     *     "type": "mekanism:compound",
     *     "ingredients": [
     *         { "infuse_type": "mekanism:carbon" },
     *         { "infuse_type": "mekanism:redstone" }
     *     ],
     *     "amount": 500
     * }
     * }</pre>
     *
     * @since 10.6.0
     */
    public static final Codec<InfusionStackIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          IngredientCreatorAccess.basicInfusion().mapCodecNonEmpty().forGetter(InfusionStackIngredient::ingredient),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(NBTConstants.AMOUNT).forGetter(InfusionStackIngredient::amount)
    ).apply(instance, InfusionStackIngredient::new));

    /**
     * The "nested" codec for {@link InfusionStackIngredient}.
     *
     * <p>With this codec, the amount is <i>always</i> serialized separately from the ingredient itself, for example:
     *
     * <pre>{@code
     * {
     *     "ingredient": {
     *         "infuse_type": "mekanism:carbon"
     *     },
     *     "amount": 1000
     * }
     * }</pre>
     *
     * @since 10.6.0
     */
    public static final Codec<InfusionStackIngredient> NESTED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          IngredientCreatorAccess.basicInfusion().codecNonEmpty().fieldOf("ingredient").forGetter(InfusionStackIngredient::ingredient),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(NBTConstants.AMOUNT).forGetter(InfusionStackIngredient::amount)
    ).apply(instance, InfusionStackIngredient::new));

    /**
     * A stream codec for sending {@link InfusionStackIngredient}s over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionStackIngredient> STREAM_CODEC = StreamCodec.composite(
          IngredientCreatorAccess.basicInfusion().streamCodec(), InfusionStackIngredient::ingredient,
          ByteBufCodecs.VAR_LONG, InfusionStackIngredient::amount,
          InfusionStackIngredient::new
    );

    /**
     * Creates an Infusion Stack Ingredient that matches a given ingredient and amount. Prefer calling via
     * {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#infusion()} and
     * {@link mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator#from(IChemicalIngredient, long)}.
     *
     * @param ingredient Ingredient to match.
     * @param amount     Amount to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty.
     * @since 10.6.0
     */
    public static InfusionStackIngredient of(IInfusionIngredient ingredient, long amount) {
        Objects.requireNonNull(ingredient, "InfusionStackIngredients cannot be created from a null ingredient.");
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("InfusionStackIngredients cannot be created using the empty ingredient.");
        }
        return new InfusionStackIngredient(ingredient, amount);
    }

    private InfusionStackIngredient(IInfusionIngredient ingredient, long amount) {
        super(ingredient, amount);
    }
}