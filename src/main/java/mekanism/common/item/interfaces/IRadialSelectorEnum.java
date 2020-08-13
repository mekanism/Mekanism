package mekanism.common.item.interfaces;

import mekanism.api.IIncrementalEnum;
import mekanism.api.text.EnumColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public interface IRadialSelectorEnum<TYPE extends Enum<TYPE> & IRadialSelectorEnum<TYPE>> extends IIncrementalEnum<TYPE> {

    ITextComponent getShortText();

    ResourceLocation getIcon();

    default EnumColor getColor() {
        return null;
    }
}