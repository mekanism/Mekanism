package mekanism.common.recipe.upgrade;

public enum RecipeUpgradeType {
    ENERGY,
    FLUID,
    GAS,
    INFUSION,
    PIGMENT,
    SLURRY,
    ITEM,
    LOCK,//Note: Must be somewhere below item to ensure item gets ran first
    SECURITY,
    SORTING,
    UPGRADE,
    QIO_DRIVE;
}