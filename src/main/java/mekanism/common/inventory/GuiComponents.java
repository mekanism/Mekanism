package mekanism.common.inventory;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiComponents {

    public interface IDropdownEnum {

        ITextComponent getShortName();

        ITextComponent getTooltip();

        default ResourceLocation getIcon() {
            return null;
        }
    }

    public interface IToggleEnum {

        ITextComponent getTooltip();

        ResourceLocation getIcon();
    }
}
