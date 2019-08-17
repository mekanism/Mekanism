package mekanism.common.temporary;

import com.blamejared.crafttweaker.api.item.IIngredient;

//TODO: Remove when CrT has liquid support
public interface ILiquidStack extends IIngredient {

    String getName();

    int getAmount();
}