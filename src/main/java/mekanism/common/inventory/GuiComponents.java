package mekanism.common.inventory;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiComponents {

    public interface IDropdownEnum {

        public ITextComponent getShortName();

        public ITextComponent getTooltip();

        public default ResourceLocation getIcon() {
            return null;
        }
    }

    public interface IToggleEnum {

        public ITextComponent getTooltip();

        public ResourceLocation getIcon();
    }
}
