package mekanism.generators.common.base;

import mekanism.api.text.EnumColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IReactorLogicMode<TYPE extends Enum<TYPE> & IReactorLogicMode<TYPE>> {

    Component getDescription();

    ItemStack getRenderStack();

    EnumColor getColor();
}