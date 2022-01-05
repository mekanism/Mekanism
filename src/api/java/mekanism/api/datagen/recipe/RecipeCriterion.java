package mekanism.api.datagen.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.advancements.CriterionTriggerInstance;

/**
 * Helper class to declare named criteria for repeated use.
 *
 * @param name      Name of the Recipe Criterion.
 * @param criterion Criterion Instance.
 */
@ParametersAreNonnullByDefault
public record RecipeCriterion(String name, CriterionTriggerInstance criterion) {
}