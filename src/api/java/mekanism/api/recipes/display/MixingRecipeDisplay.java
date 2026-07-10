package mekanism.api.recipes.display;

import java.util.Objects;
import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

/// Represents a recipe display where two inputs are combined in either order to create a result.
///
/// @see CombiningRecipeDisplay For when the order of the inputs matter.
/// @since 10.8.0
public record MixingRecipeDisplay(SlotDisplay leftInput, SlotDisplay rightInput, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<MixingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mixing"));

    @Override
    public Type<MixingRecipeDisplay> type() {
        return TYPE.value();
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.leftInput.isEnabled(enabledFeatures) && this.rightInput.isEnabled(enabledFeatures) && RecipeDisplay.super.isEnabled(enabledFeatures);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        MixingRecipeDisplay other = (MixingRecipeDisplay) obj;
        if (this.result.equals(other.result) && this.craftingStation.equals(other.craftingStation)) {
            if (this.leftInput.equals(other.leftInput) && this.rightInput.equals(other.rightInput)) {
                return true;
            }
            //Try the other order
            return this.leftInput.equals(other.rightInput) && this.rightInput.equals(other.leftInput);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.result, this.craftingStation);
        int leftHash = this.leftInput.hashCode();
        int rightHash = this.rightInput.hashCode();
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