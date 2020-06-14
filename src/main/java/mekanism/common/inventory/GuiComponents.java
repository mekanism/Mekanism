package mekanism.common.inventory;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiComponents {

    public interface IDropdownEnum<TYPE extends Enum<TYPE> & IDropdownEnum<TYPE>> {

        ITextComponent getShortName();

        ITextComponent getTooltip();

        default ResourceLocation getIcon() {
            return null;
        }
    }

    public interface IToggleEnum<TYPE extends Enum<TYPE> & IToggleEnum<TYPE>> {

        ITextComponent getTooltip();

        ResourceLocation getIcon();
    }
}
