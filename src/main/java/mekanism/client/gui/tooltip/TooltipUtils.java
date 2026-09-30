package mekanism.client.gui.tooltip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class TooltipUtils {

    @Nullable
    public static final Tooltip BACK = create(MekanismLang.BACK);

    private TooltipUtils() {
    }

    @Nullable
    public static Tooltip create(ILangEntry langEntry) {
        return create(langEntry.translate());
    }

    @Nullable
    @Contract("null -> null")
    public static Tooltip create(@Nullable Component message) {
        if (message == null) {
            return null;
        }
        return Tooltip.create(message);
    }

    @Nullable
    public static Tooltip create(ILangEntry... langEntries) {
        Objects.requireNonNull(langEntries);
        if (langEntries.length == 0) {
            throw new IllegalArgumentException("Messages cannot be null or empty");
        } else if (langEntries.length == 1) {
            //Note: This should never happen unless we are manually called with an explicit array
            return create(langEntries[0]);
        }
        List<Component> messages = new ArrayList<>(langEntries.length);
        for (ILangEntry langEntry : langEntries) {
            messages.add(langEntry.translate());
        }
        return create(messages);
    }

    @Nullable
    public static Tooltip create(Component... messages) {
        Objects.requireNonNull(messages);
        return create(Arrays.asList(messages));
    }

    @Nullable
    public static Tooltip create(List<Component> messages) {
        if (messages.isEmpty()) {
            return null;
        } else if (messages.size() == 1) {
            return create(messages.getFirst());
        }
        return Tooltip.create(ComponentUtils.formatList(messages, CommonComponents.NEW_LINE));
    }
}