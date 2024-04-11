package mekanism.client.gui.tooltip;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class TooltipUtils {

    public static final Tooltip BACK = create(MekanismLang.BACK);

    private TooltipUtils() {
    }

    public static Tooltip create(ILangEntry langEntry) {
        return create(langEntry.translate());
    }

    public static Tooltip create(Component messages) {
        Tooltip tooltip = Tooltip.create(messages);
        //Set the delay to -1 so that it appears immediately instead of after a single millisecond
        tooltip.setDelay(-1);
        return tooltip;
    }

    public static Tooltip create(ILangEntry... langEntries) {
        if (langEntries == null || langEntries.length == 0) {
            throw new IllegalArgumentException("Messages cannot be null or empty");
        } else if (langEntries.length == 1) {
            //Note: This should never happen unless we are manually called with an explicit array
            return create(langEntries[0]);
        }
        List<Component> messages = new ArrayList<>(langEntries.length);
        for (ILangEntry langEntry : langEntries) {
            messages.add(langEntry.translate());
        }
        return new MultiLineTooltip(List.copyOf(messages));
    }

    public static Tooltip create(Component... messages) {
        if (messages == null || messages.length == 0) {
            throw new IllegalArgumentException("Messages cannot be null or empty");
        } else if (messages.length == 1) {
            //Note: This should never happen unless we are manually called with an explicit array
            return create(messages[0]);
        }
        return new MultiLineTooltip(List.of(messages));
    }

    @Nullable
    public static Tooltip create(List<Component> messages) {
        if (messages.isEmpty()) {
            return null;
        } else if (messages.size() == 1) {
            return create(messages.get(0));
        }
        return new MultiLineTooltip(List.copyOf(messages));
    }
}