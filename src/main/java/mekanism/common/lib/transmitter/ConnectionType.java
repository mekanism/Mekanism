package mekanism.common.lib.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.util.IStringSerializable;

public enum ConnectionType implements IIncrementalEnum<ConnectionType>, IStringSerializable, IHasTranslationKey {
    NORMAL(MekanismLang.CONNECTION_NORMAL),
    PUSH(MekanismLang.CONNECTION_PUSH),
    PULL(MekanismLang.CONNECTION_PULL),
    NONE(MekanismLang.CONNECTION_NONE);

    private static final ConnectionType[] TYPES = values();
    private final ILangEntry langEntry;

    ConnectionType(ILangEntry langEntry) {
        this.langEntry = langEntry;
    }

    @Override
    public String getString() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getTranslationKey() {
        return langEntry.getTranslationKey();
    }

    @Nonnull
    @Override
    public ConnectionType byIndex(int index) {
        return byIndexStatic(index);
    }

    public static ConnectionType byIndexStatic(int index) {
        return MathUtils.getByIndexMod(TYPES, index);
    }
}