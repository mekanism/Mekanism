package mekanism.api.datagen.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import net.minecraft.advancements.ICriterionInstance;

/**
 * Helper class to declare named criteria for repeated use.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeCriterion {

    public final String name;
    public final ICriterionInstance criterion;

    private RecipeCriterion(String name, ICriterionInstance criterion) {
        this.name = name;
        this.criterion = criterion;
    }

    /**
     * Creates a Recipe Criterion with a given name for the given criterion instance.
     *
     * @param name      Name of the Recipe Criterion.
     * @param criterion Criterion Instance.
     *
     * @return Recipe Criterion.
     */
    public static RecipeCriterion of(String name, ICriterionInstance criterion) {
        return new RecipeCriterion(name, criterion);
    }
}