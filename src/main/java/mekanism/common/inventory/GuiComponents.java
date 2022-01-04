package mekanism.common.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class GuiComponents {

    public interface IDropdownEnum<TYPE extends Enum<TYPE> & IDropdownEnum<TYPE>> {

        Component getShortName();

        Component getTooltip();

        default ResourceLocation getIcon() {
            return null;
        }
    }

    public interface IToggleEnum<TYPE extends Enum<TYPE> & IToggleEnum<TYPE>> {

        Component getTooltip();

        ResourceLocation getIcon();
    }
}
