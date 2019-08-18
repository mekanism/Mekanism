package mekanism.common.temporary;

import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import net.minecraft.item.crafting.Ingredient;

//TODO: Remove this all together once we have a better idea of how CrT integration is going to work (after JSON recipes and stuff)
public class IngredientAny implements IIngredient {

    public static final IngredientAny INSTANCE = new IngredientAny();

    @Override
    public boolean matches(IItemStack stack) {
        return false;
    }

    @Override
    public Ingredient asVanillaIngredient() {
        return null;
    }

    @Override
    public String getCommandString() {
        return null;
    }

    @Override
    public IItemStack[] getItems() {
        return new IItemStack[0];
    }
}