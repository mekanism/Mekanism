package mekanism.common.base;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvent;

public final class HolidayManager {

    private static final Calendar calendar = Calendar.getInstance();

    private static final List<Holiday> holidays = new ArrayList<>();
    private static final List<Holiday> holidaysNotified = new ArrayList<>();

    public static void init() {
        if (MekanismConfig.client.holidays.get()) {
            holidays.add(new Christmas());
            holidays.add(new NewYear());
            holidays.add(new May4());
        }
        Mekanism.logger.info("Initialized HolidayManager.");
    }

    public static void check(PlayerEntity player) {
        try {
            YearlyDate date = getDate();

            for (Holiday holiday : holidays) {
                if (!holidaysNotified.contains(holiday)) {
                    if (holiday.getDate().equals(date)) {
                        holiday.onEvent(player);
                        holidaysNotified.add(holiday);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static Holiday getHoliday() {
        return holidaysNotified.isEmpty() ? null : holidaysNotified.get(0);
    }

    public static SoundEventRegistryObject<SoundEvent> filterSound(SoundEventRegistryObject<SoundEvent> sound) {
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

        private static final Month[] MONTHS = values();

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

        public static Month byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MONTHS, index);
        }
    }

    public static abstract class Holiday {

        public abstract YearlyDate getDate();

        public abstract void onEvent(PlayerEntity player);

        public SoundEventRegistryObject<SoundEvent> filterSound(SoundEventRegistryObject<SoundEvent> sound) {
            return sound;
        }
    }

    public static class Christmas extends Holiday {

        @Override
        public YearlyDate getDate() {
            return new YearlyDate(12, 25);
        }

        @Override
        public void onEvent(PlayerEntity player) {
            String themedLines = getThemedLines(new EnumColor[]{EnumColor.DARK_GREEN, EnumColor.DARK_RED}, 13);
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, MekanismLang.GENERIC_SQUARE_BRACKET.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM)));
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_ONE.translateColored(EnumColor.RED, EnumColor.DARK_BLUE, player.getName()));
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_TWO.translateColored(EnumColor.RED));
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_THREE.translateColored(EnumColor.RED));
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_FOUR.translateColored(EnumColor.RED));
            player.sendMessage(MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY));
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, EnumColor.DARK_BLUE, "[=======]"));
        }

        @Override
        public SoundEventRegistryObject<SoundEvent> filterSound(SoundEventRegistryObject<SoundEvent> sound) {
            if (sound == MekanismSounds.ENRICHMENT_CHAMBER) {
                return MekanismSounds.CHRISTMAS1;
            } else if (sound == MekanismSounds.METALLURGIC_INFUSER) {
                return MekanismSounds.CHRISTMAS2;
            } else if (sound == MekanismSounds.PURIFICATION_CHAMBER) {
                return MekanismSounds.CHRISTMAS3;
            } else if (sound == MekanismSounds.ENERGIZED_SMELTER) {
                return MekanismSounds.CHRISTMAS4;
            } else if (sound == MekanismSounds.CRUSHER) {
                return MekanismSounds.CHRISTMAS5;
            }
            return sound;
        }
    }

    public static class NewYear extends Holiday {

        @Override
        public YearlyDate getDate() {
            return new YearlyDate(1, 1);
        }

        @Override
        public void onEvent(PlayerEntity player) {
            String themedLines = getThemedLines(new EnumColor[]{EnumColor.WHITE, EnumColor.YELLOW}, 13);
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, MekanismLang.GENERIC_SQUARE_BRACKET.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM)));
            player.sendMessage(MekanismLang.NEW_YEAR_LINE_ONE.translateColored(EnumColor.AQUA, EnumColor.DARK_BLUE, player.getName()));
            player.sendMessage(MekanismLang.NEW_YEAR_LINE_TWO.translateColored(EnumColor.AQUA));
            player.sendMessage(MekanismLang.NEW_YEAR_LINE_THREE.translateColored(EnumColor.AQUA, calendar.get(Calendar.YEAR)));
            player.sendMessage(MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY));
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, EnumColor.DARK_BLUE, "[=======]"));
        }
    }

    public static class May4 extends Holiday {

        @Override
        public YearlyDate getDate() {
            return new YearlyDate(5, 4);
        }

        @Override
        public void onEvent(PlayerEntity player) {
            String themedLines = getThemedLines(new EnumColor[]{EnumColor.BLACK, EnumColor.GRAY, EnumColor.BLACK, EnumColor.YELLOW, EnumColor.BLACK}, 15);
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, MekanismLang.GENERIC_SQUARE_BRACKET.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM)));
            player.sendMessage(MekanismLang.MAY_4_LINE_ONE.translateColored(EnumColor.GRAY, EnumColor.DARK_BLUE, player.getName()));
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
            this(Month.byIndexStatic(m - 1), d);
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