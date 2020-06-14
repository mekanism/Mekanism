package mekanism.generators.common.base;

import mekanism.api.text.EnumColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public interface IReactorLogicMode<TYPE extends Enum<TYPE> & IReactorLogicMode<TYPE>> {

    ITextComponent getDescription();

    ItemStack getRenderStack();

    EnumColor getColor();
}