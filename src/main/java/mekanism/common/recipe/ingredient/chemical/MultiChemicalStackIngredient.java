package mekanism.common.recipe.ingredient.chemical;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.IngredientType;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class MultiChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> implements ChemicalStackIngredient<CHEMICAL, STACK> {

    public static <
          CHEMICAL extends Chemical<CHEMICAL>,
          STACK extends ChemicalStack<CHEMICAL>,
          INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>,
          MULTI extends MultiChemicalStackIngredient<CHEMICAL, STACK, INGREDIENT>
          > Codec<MULTI> makeCodec(Codec<INGREDIENT> singleJoinedCodec, Function<List<INGREDIENT>, MULTI> multiConstructor) {
        return ExtraCodecs.nonEmptyList(singleJoinedCodec.listOf()).xmap(multiConstructor, MultiChemicalStackIngredient::getIngredients);
    }

    private final INGREDIENT[] ingredients;

    @SafeVarargs
    protected MultiChemicalStackIngredient(INGREDIENT... ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * @apiNote For use in flattening multi ingredients, this should return an immutable view.
     */
    public final List<INGREDIENT> getIngredients() {
        return List.of(ingredients);
    }

    @Override
    public boolean test(STACK stack) {
        for (INGREDIENT ingredient : ingredients) {
            if (ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean testType(STACK stack) {
        for (INGREDIENT ingredient : ingredients) {
            if (ingredient.testType(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean testType(CHEMICAL chemical) {
        for (INGREDIENT ingredient : ingredients) {
            if (ingredient.testType(chemical)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public STACK getMatchingInstance(STACK stack) {
        for (INGREDIENT ingredient : ingredients) {
            STACK matchingInstance = ingredient.getMatchingInstance(stack);
            if (!matchingInstance.isEmpty()) {
                return matchingInstance;
            }
        }
        return getEmptyStack();
    }

    @Override
    public long getNeededAmount(STACK stack) {
        for (INGREDIENT ingredient : ingredients) {
            long amount = ingredient.getNeededAmount(stack);
            if (amount > 0) {
                return amount;
            }
        }
        return 0;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        for (INGREDIENT ingredient : ingredients) {
            if (!ingredient.hasNoMatchingInstances()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<@NotNull STACK> getRepresentations() {
        List<@NotNull STACK> representations = new ArrayList<>();
        for (INGREDIENT ingredient : ingredients) {
            representations.addAll(ingredient.getRepresentations());
        }
        return representations;
    }

    /**
     * For use in recipe input caching, checks all ingredients even if some match.
     *
     * @return {@code true} if any ingredient matches.
     */
    public boolean forEachIngredient(Predicate<INGREDIENT> checker) {
        boolean result = false;
        for (INGREDIENT ingredient : ingredients) {
            result |= checker.test(ingredient);
        }
        return result;
    }

    /**
     * For use in recipe input caching, checks all ingredients even if some match.
     *
     * @return {@code true} if any ingredient matches.
     */
    public <DATA> boolean forEachIngredient(DATA data, BiPredicate<DATA, INGREDIENT> checker) {
        boolean result = false;
        for (INGREDIENT ingredient : ingredients) {
            result |= checker.test(data, ingredient);
        }
        return result;
    }

    @Override
    public IngredientType getType() {
        return IngredientType.MULTI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(ingredients, ((MultiChemicalStackIngredient<?, ?, ?>) o).ingredients);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ingredients);
    }

    public static class MultiGasStackIngredient extends MultiChemicalStackIngredient<Gas, GasStack, GasStackIngredient> implements GasStackIngredient {

        //This must be lazy as the base stream codec isn't initialized until after this line happens
        public static final StreamCodec<RegistryFriendlyByteBuf, MultiGasStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              IngredientCreatorAccess.gas().streamCodec().apply(ByteBufCodecs.list()).map(
                    MultiGasStackIngredient::new, MultiGasStackIngredient::getIngredients
              ));

        MultiGasStackIngredient(GasStackIngredient... ingredients) {
            super(ingredients);
        }

        @Internal
        public MultiGasStackIngredient(List<GasStackIngredient> ingredients) {
            this(ingredients.toArray(new GasStackIngredient[0]));
        }
    }

    public static class MultiInfusionStackIngredient extends MultiChemicalStackIngredient<InfuseType, InfusionStack, InfusionStackIngredient> implements
          InfusionStackIngredient {

        //This must be lazy as the base stream codec isn't initialized until after this line happens
        public static final StreamCodec<RegistryFriendlyByteBuf, MultiInfusionStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              IngredientCreatorAccess.infusion().streamCodec().apply(ByteBufCodecs.list()).map(
                    MultiInfusionStackIngredient::new, MultiInfusionStackIngredient::getIngredients
              ));

        MultiInfusionStackIngredient(InfusionStackIngredient... ingredients) {
            super(ingredients);
        }

        @Internal
        public MultiInfusionStackIngredient(List<InfusionStackIngredient> ingredients) {
            this(ingredients.toArray(new InfusionStackIngredient[0]));
        }
    }

    public static class MultiPigmentStackIngredient extends MultiChemicalStackIngredient<Pigment, PigmentStack, PigmentStackIngredient> implements PigmentStackIngredient {

        //This must be lazy as the base stream codec isn't initialized until after this line happens
        public static final StreamCodec<RegistryFriendlyByteBuf, MultiPigmentStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              IngredientCreatorAccess.pigment().streamCodec().apply(ByteBufCodecs.list()).map(
                    MultiPigmentStackIngredient::new, MultiPigmentStackIngredient::getIngredients
              ));

        MultiPigmentStackIngredient(PigmentStackIngredient... ingredients) {
            super(ingredients);
        }

        @Internal
        public MultiPigmentStackIngredient(List<PigmentStackIngredient> ingredients) {
            this(ingredients.toArray(new PigmentStackIngredient[0]));
        }
    }

    public static class MultiSlurryStackIngredient extends MultiChemicalStackIngredient<Slurry, SlurryStack, SlurryStackIngredient> implements SlurryStackIngredient {

        //This must be lazy as the base stream codec isn't initialized until after this line happens
        public static final StreamCodec<RegistryFriendlyByteBuf, MultiSlurryStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              IngredientCreatorAccess.slurry().streamCodec().apply(ByteBufCodecs.list()).map(
                    MultiSlurryStackIngredient::new, MultiSlurryStackIngredient::getIngredients
              ));

        MultiSlurryStackIngredient(SlurryStackIngredient... ingredients) {
            super(ingredients);
        }

        @Internal
        public MultiSlurryStackIngredient(List<SlurryStackIngredient> ingredients) {
            this(ingredients.toArray(new SlurryStackIngredient[0]));
        }
    }
}