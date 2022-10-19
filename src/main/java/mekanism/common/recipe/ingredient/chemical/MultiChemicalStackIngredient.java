package mekanism.common.recipe.ingredient.chemical;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
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
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer.IngredientType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public abstract class MultiChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> implements ChemicalStackIngredient<CHEMICAL, STACK>, IMultiIngredient<STACK, INGREDIENT> {

    private final INGREDIENT[] ingredients;

    @SafeVarargs
    protected MultiChemicalStackIngredient(@NotNull INGREDIENT... ingredients) {
        this.ingredients = ingredients;
    }

    protected abstract ChemicalIngredientInfo<CHEMICAL, STACK> getIngredientInfo();

    @Override
    public final List<INGREDIENT> getIngredients() {
        return List.of(ingredients);
    }

    @Override
    public boolean test(@NotNull STACK stack) {
        return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
    }

    @Override
    public boolean testType(@NotNull STACK stack) {
        return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
    }

    @Override
    public boolean testType(@NotNull CHEMICAL chemical) {
        return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(chemical));
    }

    @NotNull
    @Override
    public STACK getMatchingInstance(@NotNull STACK stack) {
        for (INGREDIENT ingredient : ingredients) {
            STACK matchingInstance = ingredient.getMatchingInstance(stack);
            if (!matchingInstance.isEmpty()) {
                return matchingInstance;
            }
        }
        return getIngredientInfo().getEmptyStack();
    }

    @Override
    public long getNeededAmount(@NotNull STACK stack) {
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
        return Arrays.stream(ingredients).allMatch(InputIngredient::hasNoMatchingInstances);
    }

    @NotNull
    @Override
    public List<@NotNull STACK> getRepresentations() {
        List<@NotNull STACK> representations = new ArrayList<>();
        for (INGREDIENT ingredient : ingredients) {
            representations.addAll(ingredient.getRepresentations());
        }
        return representations;
    }

    @Override
    public boolean forEachIngredient(Predicate<INGREDIENT> checker) {
        boolean result = false;
        for (INGREDIENT ingredient : ingredients) {
            result |= checker.test(ingredient);
        }
        return result;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(IngredientType.MULTI);
        BasePacketHandler.writeArray(buffer, ingredients, InputIngredient::write);
    }

    @NotNull
    @Override
    public JsonElement serialize() {
        JsonArray json = new JsonArray();
        for (INGREDIENT ingredient : ingredients) {
            json.add(ingredient.serialize());
        }
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(ingredients, ((MultiChemicalStackIngredient<CHEMICAL, STACK, INGREDIENT>) o).ingredients);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ingredients);
    }

    public static class MultiGasStackIngredient extends MultiChemicalStackIngredient<Gas, GasStack, GasStackIngredient> implements GasStackIngredient {

        MultiGasStackIngredient(GasStackIngredient... ingredients) {
            super(ingredients);
        }

        @Override
        protected ChemicalIngredientInfo<Gas, GasStack> getIngredientInfo() {
            return ChemicalIngredientInfo.GAS;
        }
    }

    public static class MultiInfusionStackIngredient extends MultiChemicalStackIngredient<InfuseType, InfusionStack, InfusionStackIngredient> implements InfusionStackIngredient {

        MultiInfusionStackIngredient(InfusionStackIngredient... ingredients) {
            super(ingredients);
        }

        @Override
        protected ChemicalIngredientInfo<InfuseType, InfusionStack> getIngredientInfo() {
            return ChemicalIngredientInfo.INFUSION;
        }
    }

    public static class MultiPigmentStackIngredient extends MultiChemicalStackIngredient<Pigment, PigmentStack, PigmentStackIngredient> implements PigmentStackIngredient {

        MultiPigmentStackIngredient(PigmentStackIngredient... ingredients) {
            super(ingredients);
        }

        @Override
        protected ChemicalIngredientInfo<Pigment, PigmentStack> getIngredientInfo() {
            return ChemicalIngredientInfo.PIGMENT;
        }
    }

    public static class MultiSlurryStackIngredient extends MultiChemicalStackIngredient<Slurry, SlurryStack, SlurryStackIngredient> implements SlurryStackIngredient {

        MultiSlurryStackIngredient(SlurryStackIngredient... ingredients) {
            super(ingredients);
        }

        @Override
        protected ChemicalIngredientInfo<Slurry, SlurryStack> getIngredientInfo() {
            return ChemicalIngredientInfo.SLURRY;
        }
    }
}