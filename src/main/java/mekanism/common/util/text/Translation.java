package mekanism.common.util.text;

import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.util.text.ITextComponent;

//TODO: Cache objects of this that get used in GUI's in static final variables
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

    public static Translation of(IHasTranslationKey key, Object... args) {
        return of(key.getTranslationKey(), args);
    }

    @Override
    public ITextComponent getTextComponent() {
        return TextComponentUtil.translate(key, args);
    }
}