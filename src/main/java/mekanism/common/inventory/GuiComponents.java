package mekanism.common.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class GuiComponents {

    public interface IDropdownEnum<TYPE extends Enum<TYPE> & IDropdownEnum<TYPE>> {

        Component getShortName();

        Component getTooltip();

        default Identifier getIcon() {
            return null;
        }
    }

    public interface IToggleEnum<TYPE extends Enum<TYPE> & IToggleEnum<TYPE>> {

        Component getTooltip();

        Identifier getIcon();
    }
}
