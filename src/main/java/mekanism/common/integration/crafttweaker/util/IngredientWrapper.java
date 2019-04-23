package mekanism.common.integration.crafttweaker.util;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IngredientAny;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;

public class IngredientWrapper {

    private final IIngredient left;
    private final IIngredient middle;
    private final IIngredient right;
    private final String infuseType;

    public IngredientWrapper(IIngredient ingredient) {
        this(ingredient, IngredientAny.INSTANCE);
    }

    public IngredientWrapper(IIngredient left, IIngredient right) {
        this(left, IngredientAny.INSTANCE, right);
    }

    public IngredientWrapper(IIngredient left, IIngredient middle, IIngredient right) {
        this.left = IngredientHelper.optionalIngredient(left);
        this.middle = IngredientHelper.optionalIngredient(middle);
        this.right = IngredientHelper.optionalIngredient(right);
        this.infuseType = "";
    }

    public IngredientWrapper(IIngredient ingredient, String infuseType) {
        this.left = IngredientHelper.optionalIngredient(ingredient);
        this.middle = IngredientAny.INSTANCE;
        this.right = IngredientAny.INSTANCE;
        this.infuseType = infuseType == null ? "" : infuseType;
    }

    public String getInfuseType() {
        return this.infuseType;
    }

    public IIngredient getIngredient() {
        return this.left;
    }

    public IIngredient getLeft() {
        return this.left;
    }

    public IIngredient getMiddle() {
        return this.middle;
    }

    public IIngredient getRight() {
        return this.right;
    }

    public int getAmount() {
        //TODO: Make this method actually do something if we ever need to use IntegerInput as an input type
        return 0;
    }

    @Override
    public String toString() {
        String output = "";
        if (!left.equals(IngredientAny.INSTANCE)) {
            output += getDescriptor(left);
        }
        if (!middle.equals(IngredientAny.INSTANCE)) {
            output += ", " + getDescriptor(middle);
        }
        if (!right.equals(IngredientAny.INSTANCE)) {
            output += ", " + getDescriptor(right);
        }
        if (!infuseType.isEmpty()) {
            output += ", " + infuseType;
        }
        return output;
    }

    public boolean isEmpty() {
        return left.equals(IngredientAny.INSTANCE) && middle.equals(IngredientAny.INSTANCE) && right
              .equals(IngredientAny.INSTANCE) && infuseType.isEmpty();
    }

    private String getDescriptor(IIngredient ingredient) {
        return ingredient.toCommandString();
    }
}