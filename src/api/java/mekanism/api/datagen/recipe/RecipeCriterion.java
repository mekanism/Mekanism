package mekanism.api.datagen.recipe;

import java.util.Objects;
import net.minecraft.advancements.triggers.Criterion;

/// Helper class to declare named criteria for repeated use.
///
/// @param name      Name of the Recipe Criterion.
/// @param criterion Criterion Instance.
public record RecipeCriterion(String name, Criterion<?> criterion) {

    /// @param name      Name of the Recipe Criterion.
    /// @param criterion Criterion Instance.
    public RecipeCriterion {
        Objects.requireNonNull(name, "Criterion must have a name.");
        Objects.requireNonNull(criterion, "Recipe criterion's must have a criterion to match.");
    }
}