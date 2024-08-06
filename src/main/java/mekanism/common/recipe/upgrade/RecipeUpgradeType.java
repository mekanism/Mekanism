package mekanism.common.recipe.upgrade;

public enum RecipeUpgradeType {
    SECURITY,//Note: Must be above item to ensure it gets copied first in case someone adds a recipe that outputs personal storage blocks
    ENERGY,
    FLUID,
    CHEMICAL,
    ITEM,
    LOCK,//Note: Must be somewhere below item to ensure item gets ran first
    SORTING,
    UPGRADE,
    QIO_DRIVE;
}