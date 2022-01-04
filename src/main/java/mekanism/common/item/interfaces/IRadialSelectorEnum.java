package mekanism.common.item.interfaces;

import mekanism.api.IIncrementalEnum;
import mekanism.api.text.EnumColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public interface IRadialSelectorEnum<TYPE extends Enum<TYPE> & IRadialSelectorEnum<TYPE>> extends IIncrementalEnum<TYPE> {

    Component getShortText();

    ResourceLocation getIcon();

    default EnumColor getColor() {
        return null;
    }
}