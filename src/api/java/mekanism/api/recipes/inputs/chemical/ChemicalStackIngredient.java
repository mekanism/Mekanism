package mekanism.api.recipes.inputs.chemical;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.inputs.TagResolverHelper;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer.IngredientType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;

public interface ChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends
      IChemicalStackIngredient<CHEMICAL, STACK> {

    abstract class SingleIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements ChemicalStackIngredient<CHEMICAL, STACK> {

        @Nonnull
        private final STACK chemicalInstance;

        public SingleIngredient(@Nonnull STACK chemicalInstance) {
            this.chemicalInstance = chemicalInstance;
        }

        @Override
        public boolean test(@Nonnull STACK chemicalStack) {
            return testType(chemicalStack) && chemicalStack.getAmount() >= chemicalInstance.getAmount();
        }

        @Override
        public boolean testType(@Nonnull STACK chemicalStack) {
            return chemicalInstance.isTypeEqual(Objects.requireNonNull(chemicalStack));
        }

        @Override
        public boolean testType(@Nonnull CHEMICAL chemical) {
            return chemicalInstance.isTypeEqual(Objects.requireNonNull(chemical));
        }

        @Nonnull
        @Override
        public STACK getMatchingInstance(@Nonnull STACK chemicalStack) {
            if (test(chemicalStack)) {
                //Note: We manually "implement" the copy to ensure it returns the proper type as ChemicalStack#copy returns ChemicalStack<CHEMICAL> instead of STACK
                return getIngredientInfo().createStack(chemicalInstance, chemicalInstance.getAmount());
            }
            return getIngredientInfo().getEmptyStack();
        }

        @Override
        public long getNeededAmount(@Nonnull STACK stack) {
            return testType(stack) ? chemicalInstance.getAmount() : 0;
        }

        @Nonnull
        @Override
        public List<@NonNull STACK> getRepresentations() {
            return Collections.singletonList(chemicalInstance);
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.SINGLE);
            chemicalInstance.writeToPacket(buffer);
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty(JsonConstants.AMOUNT, chemicalInstance.getAmount());
            json.addProperty(getIngredientInfo().getSerializationKey(), chemicalInstance.getTypeRegistryName().toString());
            return json;
        }
    }

    abstract class TaggedIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements ChemicalStackIngredient<CHEMICAL, STACK> {

        @Nonnull
        private final ITag<CHEMICAL> tag;
        private final long amount;

        public TaggedIngredient(@Nonnull ITag<CHEMICAL> tag, long amount) {
            this.tag = tag;
            this.amount = amount;
        }

        @Override
        public boolean test(@Nonnull STACK chemicalStack) {
            return testType(chemicalStack) && chemicalStack.getAmount() >= amount;
        }

        @Override
        public boolean testType(@Nonnull STACK chemicalStack) {
            return testType(Objects.requireNonNull(chemicalStack).getType());
        }

        @Override
        public boolean testType(@Nonnull CHEMICAL chemical) {
            return Objects.requireNonNull(chemical).isIn(tag);
        }

        @Nonnull
        @Override
        public STACK getMatchingInstance(@Nonnull STACK chemicalStack) {
            if (test(chemicalStack)) {
                //Our chemical is in the tag so we make a new stack with the given amount
                return getIngredientInfo().createStack(chemicalStack, amount);
            }
            return getIngredientInfo().getEmptyStack();
        }

        @Override
        public long getNeededAmount(@Nonnull STACK stack) {
            return testType(stack) ? amount : 0;
        }

        @Nonnull
        @Override
        public List<@NonNull STACK> getRepresentations() {
            ChemicalIngredientInfo<CHEMICAL, STACK> ingredientInfo = getIngredientInfo();
            //TODO: Can this be cached some how
            List<@NonNull STACK> representations = new ArrayList<>();
            for (CHEMICAL chemical : TagResolverHelper.getRepresentations(tag)) {
                representations.add(ingredientInfo.createStack(chemical, amount));
            }
            return representations;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.TAGGED);
            buffer.writeResourceLocation(getIngredientInfo().getTagLocation(tag));
            buffer.writeVarLong(amount);
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty(JsonConstants.AMOUNT, amount);
            json.addProperty(JsonConstants.TAG, getIngredientInfo().getTagLocation(tag).toString());
            return json;
        }
    }

    abstract class MultiIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> implements ChemicalStackIngredient<CHEMICAL, STACK> {

        private final INGREDIENT[] ingredients;

        @SafeVarargs
        protected MultiIngredient(@Nonnull INGREDIENT... ingredients) {
            this.ingredients = ingredients;
        }

        /**
         * @apiNote For use in flattening multi ingredients
         */
        List<INGREDIENT> getIngredients() {
            return Arrays.asList(ingredients);
        }

        @Override
        public boolean test(@Nonnull STACK stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(@Nonnull STACK stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Override
        public boolean testType(@Nonnull CHEMICAL chemical) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(chemical));
        }

        @Nonnull
        @Override
        public STACK getMatchingInstance(@Nonnull STACK stack) {
            for (INGREDIENT ingredient : ingredients) {
                STACK matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return getIngredientInfo().getEmptyStack();
        }

        @Override
        public long getNeededAmount(@Nonnull STACK stack) {
            for (INGREDIENT ingredient : ingredients) {
                long amount = ingredient.getNeededAmount(stack);
                if (amount > 0) {
                    return amount;
                }
            }
            return 0;
        }

        @Nonnull
        @Override
        public List<@NonNull STACK> getRepresentations() {
            List<@NonNull STACK> representations = new ArrayList<>();
            for (INGREDIENT ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.MULTI);
            buffer.writeVarInt(ingredients.length);
            for (INGREDIENT ingredient : ingredients) {
                ingredient.write(buffer);
            }
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonArray json = new JsonArray();
            for (INGREDIENT ingredient : ingredients) {
                json.add(ingredient.serialize());
            }
            return json;
        }
    }
}