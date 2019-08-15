package mekanism.common.util.text;

import mekanism.api.text.IHasTextComponent;
import net.minecraft.util.text.ITextComponent;

public class Translation implements IHasTextComponent {

    private final String key;
    private final Object[] args;

    private Translation(String key, Object... args) {
        this.key = key;
        this.args = args;
    }

    public static Translation of(String key, Object... args) {
        return new Translation(key, args);
    }

    @Override
    public ITextComponent getTextComponent() {
        return TextComponentUtil.translate(key, args);
    }
}