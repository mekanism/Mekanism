package mekanism.common.content.gear;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class HUDElement {

    private ResourceLocation icon;
    private ITextComponent text;

    private HUDElement(ResourceLocation icon, ITextComponent text) {
        this.icon = icon;
        this.text = text;
    }

    public static HUDElement of(ResourceLocation rl, ITextComponent text) {
        return new HUDElement(rl, text);
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public ITextComponent getText() {
        return text;
    }
}
