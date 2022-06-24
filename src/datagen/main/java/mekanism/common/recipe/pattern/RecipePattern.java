package mekanism.common.recipe.pattern;

import mekanism.api.annotations.NothingNullByDefault;
import org.jetbrains.annotations.Nullable;

//Note: We don't have a 1x1 pattern as that makes more sense to be done via a shapeless recipe
@NothingNullByDefault
public class RecipePattern {

    public final String row1;
    @Nullable
    public final String row2;
    @Nullable
    public final String row3;

    private RecipePattern(String row1) {
        this(row1, null, null);
    }

    private RecipePattern(String row1, @Nullable String row2) {
        this(row1, row2, null);
    }

    private RecipePattern(String row1, @Nullable String row2, @Nullable String row3) {
        this.row1 = row1;
        this.row2 = row2;
        this.row3 = row3;
    }

    //For 1x2 recipes
    public static RecipePattern createPattern(DoubleLine row1) {
        return new RecipePattern(row1.columns);
    }

    //For 2x1 recipes
    public static RecipePattern createPattern(char row1, char row2) {
        return new RecipePattern(Character.toString(row1), Character.toString(row2));
    }

    //For 2x2 recipes
    public static RecipePattern createPattern(DoubleLine row1, DoubleLine row2) {
        return new RecipePattern(row1.columns, row2.columns);
    }

    //For 1x3 recipes
    public static RecipePattern createPattern(TripleLine row1) {
        return new RecipePattern(row1.columns);
    }

    //For 2x3 recipes
    public static RecipePattern createPattern(TripleLine row1, TripleLine row2) {
        return new RecipePattern(row1.columns, row2.columns);
    }

    //For 3x1 recipes
    public static RecipePattern createPattern(char row1, char row2, char row3) {
        return new RecipePattern(Character.toString(row1), Character.toString(row2), Character.toString(row3));
    }

    //For 3x2 recipes
    public static RecipePattern createPattern(DoubleLine row1, DoubleLine row2, DoubleLine row3) {
        return new RecipePattern(row1.columns, row2.columns, row3.columns);
    }

    //For 3x3 recipes
    public static RecipePattern createPattern(TripleLine row1, TripleLine row2, TripleLine row3) {
        return new RecipePattern(row1.columns, row2.columns, row3.columns);
    }

    public static class DoubleLine {

        private final String columns;

        private DoubleLine(String columns) {
            this.columns = columns;
        }

        public static DoubleLine of(char column1, char column2) {
            return new DoubleLine(Character.toString(column1) + column2);
        }
    }

    public static class TripleLine {

        private final String columns;

        private TripleLine(String columns) {
            this.columns = columns;
        }

        public static TripleLine of(char column1, char column2, char column3) {
            return new TripleLine(Character.toString(column1) + column2 + column3);
        }
    }
}