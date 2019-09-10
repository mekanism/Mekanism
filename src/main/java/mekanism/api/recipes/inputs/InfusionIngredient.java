package mekanism.api.recipes.inputs;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionContainer;

/**
 * Created by Thiakil on 12/07/2019.
 */
public abstract class InfusionIngredient implements InputPredicate<InfuseObject> {

    public static InfusionIngredient from(@NonNull InfuseType infuseType, int minAmount) {
        return new SingleType(infuseType, minAmount);
    }

    public boolean test(@NonNull InfusionContainer input) {
        if (input.getType() == null) {
            return false;
        }
        return test(new InfuseObject(input.getType(), input.getAmount()));
    }

    public static class SingleType extends InfusionIngredient {

        @NonNull
        private final InfuseType infuseType;

        private final int minAmount;
        private final InfuseObject infuseObject;

        public SingleType(@NonNull InfuseType infuseType, int minAmount) {
            this.infuseType = infuseType;
            this.minAmount = minAmount;
            infuseObject = new InfuseObject(infuseType, minAmount);
        }

        @Override
        public boolean test(InfuseObject infuseObject) {
            return testType(infuseObject) && infuseObject.getAmount() >= this.minAmount;
        }

        @Override
        public boolean testType(InfuseObject infuseObject) {
            return Objects.requireNonNull(infuseObject.getType()) == this.infuseType;
        }

        @Override
        public @NonNull List<InfuseObject> getRepresentations() {
            return Collections.singletonList(infuseObject);
        }
    }

    //TODO: 1.14 Add one that is based off of tags so as to allow multiple types
}
