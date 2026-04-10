package mekanism.common.base.holiday;

import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public abstract class Holiday {

    private final KnownDate date;
    private boolean hasNotified;
    private boolean isToday;

    Holiday(KnownDate date) {
        this.date = date;
    }

    @Nullable
    HolidayMessage getMessage(Player player) {
        return null;
    }

    protected final boolean hasNotified() {
        return hasNotified;
    }

    final void notify(Player player) {
        HolidayMessage message = getMessage(player);
        if (message != null) {
            player.sendSystemMessage(MekanismLang.HOLIDAY_BORDER.translate(message.themedLines(), EnumColor.DARK_BLUE,
                  MekanismLang.GENERIC_SQUARE_BRACKET.translate(MekanismLang.MEKANISM)));
            for (Component line : message.lines()) {
                player.sendSystemMessage(line);
            }
            player.sendSystemMessage(MekanismLang.HOLIDAY_BORDER.translate(message.themedLines(), EnumColor.DARK_BLUE, "[=======]"));
        }
        hasNotified = true;
    }

    final boolean updateIsToday(YearlyDate today) {
        isToday = date.isToday(today);
        if (!isToday) {
            //If we are updating whether it is today or not, and it is no longer today (if it even was before)
            // then we want to reset whether we have sent a notification about the date yet
            hasNotified = false;
        }
        return isToday;
    }

    public final boolean isToday() {
        return isToday;
    }

    protected static Component getThemedLines(int amount, EnumColor... colors) {
        MutableComponent component = Component.empty();
        for (int i = 0; i < amount; i++) {
            component.append(TextComponentUtil.build(colors[i % colors.length], "-"));
        }
        return component;
    }

    protected static Component getThemedLines(int amount, int... colors) {
        MutableComponent component = Component.empty();
        for (int i = 0; i < amount; i++) {
            component.append(Component.literal("-").withColor(colors[i % colors.length]));
        }
        return component;
    }
}