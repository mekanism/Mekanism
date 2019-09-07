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
public abstract class InfusionIngredient {

    public static InfusionIngredient from(@NonNull InfuseType infuseType, int minAmount) {
        return new SingleType(infuseType, minAmount);
    }

    public abstract boolean test(@NonNull InfuseType infuseType, int i);

    public boolean test(@NonNull InfusionContainer input) {
        if (input.getType() == null){
            return false;
        }
        return test(input.getType(), input.getAmount());
    }

    /**
     * Primarily for JEI, a list of valid instances of the stack (i.e. a resolved InfuseObject(s))
     * @return List (empty means no valid registrations found and recipe is to be hidden)
     */
    public abstract @NonNull List<InfuseObject> getRepresentations();

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
        public boolean test(@NonNull InfuseType infuseType, int amount) {
            return Objects.requireNonNull(infuseType) == this.infuseType && amount >= this.minAmount;
        }

        @Override
        public @NonNull List<InfuseObject> getRepresentations() {
            return Collections.singletonList(infuseObject);
        }
    }

    //TODO: 1.14 Add one that is based off of tags so as to allow multiple types
}
