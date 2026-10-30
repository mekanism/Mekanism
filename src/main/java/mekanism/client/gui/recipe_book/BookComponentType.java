package mekanism.client.gui.recipe_book;

public enum BookComponentType {
    INPUT,
    EXTRA,
    OUTPUT;

    public boolean isResult() {
        return this == OUTPUT;
    }
}