package mekanism.common.base;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Calendar;
import java.util.Set;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;

public final class HolidayManager {

    private static final Calendar calendar = Calendar.getInstance();

    private static final Set<Holiday> holidays = new ObjectOpenHashSet<>();

    public static final Holiday CHRISTMAS = register(new Christmas());
    public static final Holiday NEW_YEAR = register(new NewYear());
    public static final Holiday MAY_4 = register(new May4());
    public static final Holiday APRIL_FOOLS = register(new AprilFools());

    private static Holiday register(Holiday holiday) {
        holidays.add(holiday);
        return holiday;
    }

    public static void init() {
        if (MekanismConfig.client.holidays.get()) {
            YearlyDate date = getDate();
            for (Holiday holiday : holidays) {
                if (holiday.getDate().equals(date)) {
                    holiday.setIsToday();
                }
            }
            Mekanism.logger.info("Initialized HolidayManager.");
        }
    }

    public static void notify(PlayerEntity player) {
        for (Holiday holiday : holidays) {
            if (holiday.isToday() && !holiday.hasNotified()) {
                holiday.notify(player);
            }
        }
    }

    public static SoundEventRegistryObject<SoundEvent> filterSound(SoundEventRegistryObject<SoundEvent> sound) {
        if (!MekanismConfig.client.holidays.get()) {
            return sound;
        }
        for (Holiday holiday : holidays) {
            if (holiday.isToday()) {
                return holiday.filterSound(sound);
            }
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

    public abstract static class Holiday {

        private boolean hasNotified;
        private boolean isToday;

        public abstract YearlyDate getDate();

        public void onEvent(PlayerEntity player) {
        }

        public SoundEventRegistryObject<SoundEvent> filterSound(SoundEventRegistryObject<SoundEvent> sound) {
            return sound;
        }

        private boolean hasNotified() {
            return hasNotified;
        }

        private void notify(PlayerEntity player) {
            onEvent(player);
            hasNotified = true;
        }

        private void setIsToday() {
            isToday = true;
        }

        public boolean isToday() {
            return isToday;
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
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, EnumColor.DARK_BLUE, MekanismLang.GENERIC_SQUARE_BRACKET.translate(MekanismLang.MEKANISM)), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_ONE.translateColored(EnumColor.RED, EnumColor.DARK_BLUE, player.getName()), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_TWO.translateColored(EnumColor.RED), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_THREE.translateColored(EnumColor.RED), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.CHRISTMAS_LINE_FOUR.translateColored(EnumColor.RED), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, EnumColor.DARK_BLUE, "[=======]"), Util.DUMMY_UUID);
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
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, EnumColor.DARK_BLUE, MekanismLang.GENERIC_SQUARE_BRACKET.translate(MekanismLang.MEKANISM)), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.NEW_YEAR_LINE_ONE.translateColored(EnumColor.AQUA, EnumColor.DARK_BLUE, player.getName()), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.NEW_YEAR_LINE_TWO.translateColored(EnumColor.AQUA), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.NEW_YEAR_LINE_THREE.translateColored(EnumColor.AQUA, calendar.get(Calendar.YEAR)), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, EnumColor.DARK_BLUE, "[=======]"), Util.DUMMY_UUID);
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
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, EnumColor.DARK_BLUE, MekanismLang.GENERIC_SQUARE_BRACKET.translate(MekanismLang.MEKANISM)), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.MAY_4_LINE_ONE.translateColored(EnumColor.GRAY, EnumColor.DARK_BLUE, player.getName()), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(themedLines, EnumColor.DARK_BLUE, "[=======]"), Util.DUMMY_UUID);
        }
    }

    public static class AprilFools extends Holiday {

        @Override
        public YearlyDate getDate() {
            return new YearlyDate(4, 1);
        }
    }

    public static class YearlyDate {

        public final Month month;
        public final int day;

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