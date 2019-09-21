package mekanism.api.recipes.inputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionContainer;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.tags.Tag;

/**
 * Created by Thiakil on 12/07/2019.
 */
public abstract class InfusionIngredient implements InputIngredient<@NonNull InfusionStack> {

    public static InfusionIngredient from(@NonNull IInfuseTypeProvider infuseType, int minAmount) {
        return new Instance(infuseType.getInfuseType(), minAmount);
    }

    public static InfusionIngredient from(@NonNull Tag<InfuseType> infuseTypeTag, int minAmount) {
        return new Tagged(infuseTypeTag, minAmount);
    }

    public boolean test(@NonNull InfusionContainer input) {
        if (input.isEmpty()) {
            return false;
        }
        return test(new InfusionStack(input.getType(), input.getAmount()));
    }

    public abstract boolean testType(@NonNull InfuseType infuseType);

    public static class Instance extends InfusionIngredient {

        @NonNull
        private final InfuseType infuseType;

        private final int minAmount;
        private final InfusionStack infuseObject;

        public Instance(@NonNull InfuseType infuseType, int minAmount) {
            this.infuseType = infuseType;
            this.minAmount = minAmount;
            infuseObject = new InfusionStack(infuseType, minAmount);
        }

        @Override
        public boolean test(@NonNull InfusionStack infuseObject) {
            return testType(infuseObject) && infuseObject.getAmount() >= this.minAmount;
        }

        @Override
        public boolean testType(@NonNull InfusionStack infuseObject) {
            return testType(Objects.requireNonNull(infuseObject).getType());
        }

        @Override
        public boolean testType(@NonNull InfuseType infuseType) {
            return Objects.requireNonNull(infuseType) == this.infuseType;
        }

        @Override
        public @NonNull InfusionStack getMatchingInstance(@NonNull InfusionStack infuseObject) {
            return test(infuseObject) ? this.infuseObject : InfusionStack.EMPTY;
        }

        @Override
        public @NonNull List<@NonNull InfusionStack> getRepresentations() {
            return Collections.singletonList(infuseObject);
        }

        //TODO: A InfuseType representations thing
    }

    public static class Tagged extends InfusionIngredient {

        @Nonnull
        private final Tag<InfuseType> tag;
        private final int minAmount;

        public Tagged(@Nonnull Tag<InfuseType> tag, int minAmount) {
            this.tag = tag;
            this.minAmount = minAmount;
        }

        @Override
        public boolean test(@NonNull InfusionStack infusionStack) {
            return testType(infusionStack) && infusionStack.getAmount() >= minAmount;
        }

        @Override
        public boolean testType(@NonNull InfusionStack infusionStack) {
            return testType(Objects.requireNonNull(infusionStack).getType());
        }

        @Override
        public boolean testType(@NonNull InfuseType infuseType) {
            return Objects.requireNonNull(infuseType).isIn(tag);
        }

        @Override
        public @NonNull InfusionStack getMatchingInstance(@NonNull InfusionStack infusionStack) {
            if (test(infusionStack)) {
                //Our infusion type is in the tag so we make a new stack with the given amount
                return new InfusionStack(infusionStack, minAmount);
            }
            return InfusionStack.EMPTY;
        }

        @Override
        @NonNull
        public List<@NonNull InfusionStack> getRepresentations() {
            //TODO: Can this be cached some how
            List<@NonNull InfusionStack> representations = new ArrayList<>();
            for (InfuseType infuseType : tag.getAllElements()) {
                representations.add(new InfusionStack(infuseType, minAmount));
            }
            return representations;
        }
    }

    //TODO: Maybe name this better, at the very least make it easier/possible to create new instances of this
    // Also cleanup the javadoc comment about this, and try to make the helpers that create a new instance
    // return a normal InfusionIngredient (Single), if we only have a singular one
    public static class Multi extends InfusionIngredient {

        private final InfusionIngredient[] ingredients;

        protected Multi(@NonNull InfusionIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(@NonNull InfusionStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(@NonNull InfusionStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Override
        public boolean testType(@NonNull InfuseType infuseType) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(infuseType));
        }

        @Override
        public @NonNull InfusionStack getMatchingInstance(@NonNull InfusionStack stack) {
            for (InfusionIngredient ingredient : ingredients) {
                InfusionStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return InfusionStack.EMPTY;
        }

        @NonNull
        @Override
        public List<@NonNull InfusionStack> getRepresentations() {
            List<@NonNull InfusionStack> representations = new ArrayList<>();
            for (InfusionIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }
    }
}
