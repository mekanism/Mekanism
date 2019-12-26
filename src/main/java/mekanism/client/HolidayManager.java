package mekanism.client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public final class HolidayManager {

    private static Calendar calendar = Calendar.getInstance();

    private static List<Holiday> holidays = new ArrayList<>();
    private static List<Holiday> holidaysNotified = new ArrayList<>();

    public static void init() {
        if (MekanismConfig.client.holidays.get()) {
            holidays.add(new Christmas());
            holidays.add(new NewYear());
        }
        Mekanism.logger.info("Initialized HolidayManager.");
    }

    public static void check() {
        try {
            YearlyDate date = getDate();

            for (Holiday holiday : holidays) {
                if (!holidaysNotified.contains(holiday)) {
                    if (holiday.getDate().equals(date)) {
                        holiday.onEvent(Minecraft.getInstance().player);
                        holidaysNotified.add(holiday);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    //TODO: Note this is not actually used currently?
    public static ResourceLocation filterSound(ResourceLocation sound) {
        if (!MekanismConfig.client.holidays.get()) {
            return sound;
        }
        try {
            YearlyDate date = getDate();
            for (Holiday holiday : holidays) {
                if (holiday.getDate().equals(date)) {
                    return holiday.filterSound(sound);
                }
            }
        } catch (Exception ignored) {
        }
        return sound;
    }

    private static YearlyDate getDate() {
        return new YearlyDate(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    private static String getThemedLines(EnumColor[] colors, int amount) {
        //TODO: Should this use text components?
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            builder.append(colors[i % colors.length]).append("-");
        }
        return builder.toString();
    }

    public enum Month {
        JANUARY("January"),
        FEBRUARY("February"),
        MARCH("March"),
        APRIL("April"),
        MAY("May"),
        JUNE("June"),
        JULY("July"),
        AUGUST("August"),
        SEPTEMBER("September"),
        OCTOBER("October"),
        NOVEMBER("November"),
        DECEMBER("December");

        private final String name;

        Month(String n) {
            name = n;
        }

        public String getName() {
            return name;
        }

        public int month() {
            return ordinal() + 1;
        }
    }

    public static abstract class Holiday {

        public abstract YearlyDate getDate();

        public abstract void onEvent(PlayerEntity player);

        public ResourceLocation filterSound(ResourceLocation sound) {
            return sound;
        }
    }

    private static class Christmas extends Holiday {

        private String[] nutcracker = new String[]{"christmas.1", "christmas.2", "christmas.3", "christmas.4", "christmas.5"};

        @Override
        public YearlyDate getDate() {
            return new YearlyDate(12, 25);
        }

        @Override
        public void onEvent(PlayerEntity player) {
            String themedLines = getThemedLines(new EnumColor[]{EnumColor.DARK_GREEN, EnumColor.DARK_RED}, 13);
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, MekanismLang.GENERIC_SQUARE_BRACKET.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM)));
            //TODO: Decide if this should be display name instead of name
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_ONE.translateColored(EnumColor.RED, EnumColor.DARK_BLUE, player.getName()));
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_TWO.translateColored(EnumColor.RED));
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_THREE.translateColored(EnumColor.RED));
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_FOUR.translateColored(EnumColor.RED));
            player.sendMessage(MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY));
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, EnumColor.DARK_BLUE, "[=======]"));
        }

        @Override
        public ResourceLocation filterSound(ResourceLocation sound) {
            String soundLocation = sound.toString();
            if (soundLocation.contains("machine.enrichment")) {
                return new ResourceLocation(soundLocation.replace("machine.enrichment", nutcracker[0]));
            } else if (soundLocation.contains("machine.metalinfuser")) {
                return new ResourceLocation(soundLocation.replace("machine.metalinfuser", nutcracker[1]));
            } else if (soundLocation.contains("machine.purification")) {
                return new ResourceLocation(soundLocation.replace("machine.purification", nutcracker[2]));
            } else if (soundLocation.contains("machine.smelter")) {
                return new ResourceLocation(soundLocation.replace("machine.smelter", nutcracker[3]));
            } else if (soundLocation.contains("machine.dissolution")) {
                return new ResourceLocation(soundLocation.replace("machine.dissolution", nutcracker[4]));
            }
            return sound;
        }
    }

    private static class NewYear extends Holiday {

        @Override
        public YearlyDate getDate() {
            return new YearlyDate(1, 1);
        }

        @Override
        public void onEvent(PlayerEntity player) {
            String themedLines = getThemedLines(new EnumColor[]{EnumColor.WHITE, EnumColor.YELLOW}, 13);
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, MekanismLang.GENERIC_SQUARE_BRACKET.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM)));
            //TODO: Decide if this should be display name instead of name
            player.sendMessage(MekanismLang.NEW_YEAR_LINE_ONE.translateColored(EnumColor.AQUA, EnumColor.DARK_BLUE, player.getName()));
            player.sendMessage(MekanismLang.NEW_YEAR_LINE_TWO.translateColored(EnumColor.AQUA));
            player.sendMessage(MekanismLang.NEW_YEAR_LINE_TWO.translateColored(EnumColor.AQUA, calendar.get(Calendar.YEAR)));
            player.sendMessage(MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY));
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, EnumColor.DARK_BLUE, "[=======]"));
        }
    }

    public static class YearlyDate {

        public Month month;

        public int day;

        public YearlyDate(Month m, int d) {
            month = m;
            day = d;
        }

        public YearlyDate(int m, int d) {
            this(Month.values()[m - 1], d);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof YearlyDate && ((YearlyDate) obj).month == month && ((YearlyDate) obj).day == day;
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + month.ordinal();
            code = 31 * code + day;
            return code;
        }
    }
}