package mekanism.common.recipe.ingredients;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismRegistries;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalInstance;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.CustomDisplayChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IntersectionChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.SimpleChemicalIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class ChemicalIngredientCreator implements IChemicalIngredientCreator {

    public static final ChemicalIngredientCreator INSTANCE = new ChemicalIngredientCreator();

    private static final Codec<HolderSet<Chemical>> HOLDER_SET_NO_EMPTY_CHEMICAL = HolderSetCodec.create(MekanismRegistries.Keys.CHEMICAL, ChemicalInstance.CHEMICAL_HOLDER_CODEC, false);

    private static final Codec<SimpleChemicalIngredient> SIMPLE_CODEC = ExtraCodecs.nonEmptyHolderSet(HOLDER_SET_NO_EMPTY_CHEMICAL)
          .xmap(SimpleChemicalIngredient::new, SimpleChemicalIngredient::chemicalSet);

    @SuppressWarnings("RedundantTypeArguments")
    private static final Codec<ChemicalIngredient> CODEC = Codec.xor(
          MekanismRegistries.CHEMICAL_INGREDIENT_TYPES.byNameCodec().<ChemicalIngredient>dispatch(SerializationConstants.TYPE, ChemicalIngredient::codec, Function.identity()),
          SIMPLE_CODEC
    ).xmap(either -> either.map(Function.identity(), Function.identity()), ingredient -> switch (ingredient) {
        case SimpleChemicalIngredient simple -> Either.right(simple);
        default -> Either.left(ingredient);
    });
    private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalIngredient> STREAM_CODEC = ByteBufCodecs.holderSet(MekanismRegistries.Keys.CHEMICAL)
          .map(SimpleChemicalIngredient::new, ChemicalIngredientCreator::valuesForSync);
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<ChemicalIngredient>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.holderSet(MekanismRegistries.Keys.CHEMICAL).map(
          ingredient -> ingredient.size() == 0 ? Optional.empty() : Optional.of(INSTANCE.of(ingredient)),
          ingredient -> ingredient.map(ChemicalIngredientCreator::valuesForSync).orElse(HolderSet.empty())
    );

    private static HolderSet<Chemical> valuesForSync(ChemicalIngredient ingredient) {
        if (ingredient instanceof SimpleChemicalIngredient simple) {
            return simple.chemicalSet();
        }
        return HolderSet.direct(ingredient.chemicals());
    }

    @Override
    public Codec<ChemicalIngredient> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ChemicalIngredient> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Optional<ChemicalIngredient>> optionalStreamCodec() {
        return OPTIONAL_STREAM_CODEC;
    }

    @Override
    public SimpleChemicalIngredient of(HolderSet<Chemical> chemicals) {
        return new SimpleChemicalIngredient(chemicals);
    }

    @Override
    public ChemicalIngredient ofIngredients(List<? extends ChemicalIngredient> children) {
        Objects.requireNonNull(children, "children cannot be null");
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Ingredient creation requires at least one ingredient");
        } else if (children.size() == 1) {
            return children.getFirst();
        }
        return new CompoundChemicalIngredient(List.copyOf(children));
    }

    @Override
    public ChemicalIngredient difference(ChemicalIngredient base, ChemicalIngredient subtracted) {
        return new DifferenceChemicalIngredient(base, subtracted);
    }

    @Override
    public ChemicalIngredient intersection(ChemicalIngredient... ingredients) {
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create an IntersectionChemicalIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        return new IntersectionChemicalIngredient(List.of(ingredients));
    }

    @Override
    public ChemicalIngredient intersection(List<? extends ChemicalIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an IntersectionChemicalIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.size() == 1) {
            return ingredients.getFirst();
        }
        return new IntersectionChemicalIngredient(List.copyOf(ingredients));
    }

    @Override
    public ChemicalIngredient customDisplay(ChemicalIngredient base, SlotDisplay display) {
        return new CustomDisplayChemicalIngredient(base, display);
    }
}
