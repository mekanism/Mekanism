package mekanism.common.block.property;

import mekanism.api.EnumColor;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyColor implements IUnlistedProperty<PropertyColor> {

    public static PropertyColor INSTANCE = new PropertyColor();

    public EnumColor color;

    public PropertyColor() {
    }

    public PropertyColor(EnumColor c) {
        color = c;
    }

    @Override
    public String getName() {
        return "color";
    }

    @Override
    public boolean isValid(PropertyColor value) {
        return true;
    }

    @Override
    public Class<PropertyColor> getType() {
        return PropertyColor.class;
    }

    @Override
    public String valueToString(PropertyColor value) {
        return color.getName();
    }
}
