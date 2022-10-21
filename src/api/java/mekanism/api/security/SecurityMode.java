package mekanism.api.security;

import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;

/**
 * Simple security enum for defining different access levels.
 *
 * @since 10.2.1
 */
@NothingNullByDefault
public enum SecurityMode implements IIncrementalEnum<SecurityMode>, IHasTextComponent {
    /**
     * Public Security: Everyone is allowed access.
     */
    PUBLIC(APILang.PUBLIC, EnumColor.BRIGHT_GREEN),
    /**
     * Private Security: Only the owner is allowed access.
     */
    PRIVATE(APILang.PRIVATE, EnumColor.RED),
    /**
     * Trusted Security: The owner and anyone they mark as trusted in their security desk are allowed access.
     */
    TRUSTED(APILang.TRUSTED, EnumColor.INDIGO);

    private static final SecurityMode[] MODES = values();

    private final ILangEntry langEntry;
    private final EnumColor color;

    SecurityMode(ILangEntry langEntry, EnumColor color) {
        this.langEntry = langEntry;
        this.color = color;
    }

    @Override
    public Component getTextComponent() {
        return langEntry.translateColored(color);
    }

    @Override
    public SecurityMode byIndex(int index) {
        return byIndexStatic(index);
    }

    /**
     * Gets a security mode by index.
     *
     * @param index Index of the security mode.
     */
    public static SecurityMode byIndexStatic(int index) {
        return MathUtils.getByIndexMod(MODES, index);
    }
}