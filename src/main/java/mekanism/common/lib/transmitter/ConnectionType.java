package mekanism.common.lib.transmitter;

import java.util.Locale;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

@NothingNullByDefault
public enum ConnectionType implements IIncrementalEnum<ConnectionType>, StringRepresentable, IHasTranslationKey, IHasTextComponent {
    NORMAL(MekanismLang.CONNECTION_NORMAL, EnumColor.ORANGE),
    PUSH(MekanismLang.CONNECTION_PUSH, EnumColor.BRIGHT_GREEN),
    PULL(MekanismLang.CONNECTION_PULL, EnumColor.YELLOW),
    NONE(MekanismLang.CONNECTION_NONE, EnumColor.WHITE);

    private static final ConnectionType[] TYPES = values();
    private final ILangEntry langEntry;
    private final EnumColor color;

    ConnectionType(ILangEntry langEntry, EnumColor color) {
        this.langEntry = langEntry;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public Component getTextComponent() {
        return langEntry.translateColored(color);
    }

    @Override
    public String getTranslationKey() {
        return langEntry.getTranslationKey();
    }

    @Override
    public ConnectionType byIndex(int index) {
        return byIndexStatic(index);
    }

    public static ConnectionType byIndexStatic(int index) {
        return MathUtils.getByIndexMod(TYPES, index);
    }
}