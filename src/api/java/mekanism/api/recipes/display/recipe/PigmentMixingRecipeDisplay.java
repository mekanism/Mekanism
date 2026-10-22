package mekanism.api.recipes.display.recipe;

import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

/// Represents a pigment mixing recipe display where two inputs are combined in either order to create a result.
///
/// @since 10.8.0
public record PigmentMixingRecipeDisplay(SlotDisplay ingredient, SlotDisplay secondIngredient, SlotDisplay result, SlotDisplay craftingStation) implements TwoInputRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<PigmentMixingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_PIGMENT_MIXING);

    public PigmentMixingRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(secondIngredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
    }

    @Override
    public Type<PigmentMixingRecipeDisplay> type() {
        return TYPE.value();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        PigmentMixingRecipeDisplay other = (PigmentMixingRecipeDisplay) obj;
        if (this.result.equals(other.result) && this.craftingStation.equals(other.craftingStation)) {
            if (this.ingredient.equals(other.ingredient) && this.secondIngredient.equals(other.secondIngredient)) {
                return true;
            }
            //Try the other order
            return this.ingredient.equals(other.secondIngredient) && this.secondIngredient.equals(other.ingredient);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.result, this.craftingStation);
        int leftHash = this.ingredient.hashCode();
        int rightHash = this.secondIngredient.hashCode();
        //Make the order of left and right input not matter for calculating the hashCode of the recipe display by taking the smaller hash as the first component
        if (leftHash < rightHash) {
            result = 31 * result + leftHash;
            result = 31 * result + rightHash;
        } else {
            result = 31 * result + rightHash;
            result = 31 * result + leftHash;
        }
        return result;
    }
}