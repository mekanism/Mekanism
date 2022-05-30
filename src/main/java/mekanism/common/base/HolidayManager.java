package mekanism.common.base;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Set;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Range;

public final class HolidayManager {

    public static final Holiday CHRISTMAS = new Christmas();
    public static final Holiday NEW_YEAR = new NewYear();
    public static final Holiday MAY_4 = new May4();
    public static final Holiday APRIL_FOOLS = new AprilFools();
    private static final Set<Holiday> holidays = Set.of(
          CHRISTMAS,
          NEW_YEAR,
          MAY_4,
          APRIL_FOOLS
    );

    public static void init() {
        LocalDate time = LocalDate.now();
        YearlyDate date = new YearlyDate(time.getMonth(), time.getDayOfMonth());
        for (Holiday holiday : holidays) {
            holiday.updateIsToday(date);
        }
        //TODO: Eventually we want to make it so it updates each day via a secondary thread whether or not the holiday is today
        Mekanism.logger.info("Initialized HolidayManager.");
    }

    public static void notify(Player player) {
        if (MekanismConfig.client.holidays.get()) {
            for (Holiday holiday : holidays) {
                if (holiday.isToday() && !holiday.hasNotified()) {
                    holiday.notify(player);
                }
            }
        }
    }

    public static SoundEventRegistryObject<SoundEvent> filterSound(SoundEventRegistryObject<SoundEvent> sound) {
        if (MekanismConfig.client.holidays.get()) {
            for (Holiday holiday : holidays) {
                if (holiday.isToday()) {
                    return holiday.filterSound(sound);
                }
            }
        }
        return sound;
    }

    private static String getThemedLines(int amount, EnumColor... colors) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            builder.append(colors[i % colors.length]).append("-");
        }
        return builder.toString();
    }

    public abstract static class Holiday {

        private final YearlyDate date;
        private boolean hasNotified;
        private boolean isToday;

        protected Holiday(YearlyDate date) {
            this.date = date;
        }

        public YearlyDate getDate() {
            return date;
        }

        protected boolean checkIsToday(YearlyDate date) {
            return getDate().equals(date);
        }

        @Nullable
        protected HolidayMessage getMessage(Player player) {
            return null;
        }

        public SoundEventRegistryObject<SoundEvent> filterSound(SoundEventRegistryObject<SoundEvent> sound) {
            return sound;
        }

        private boolean hasNotified() {
            return hasNotified;
        }

        private void notify(Player player) {
            HolidayMessage message = getMessage(player);
            if (message != null) {
                player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(message.themedLines, EnumColor.DARK_BLUE,
                      MekanismLang.GENERIC_SQUARE_BRACKET.translate(MekanismLang.MEKANISM)), Util.NIL_UUID);
                for (Component line : message.lines) {
                    player.sendMessage(line, Util.NIL_UUID);
                }
                player.sendMessage(MekanismLang.HOLIDAY_BORDER.translate(message.themedLines, EnumColor.DARK_BLUE, "[=======]"), Util.NIL_UUID);
            }
            hasNotified = true;
        }

        private void updateIsToday(YearlyDate date) {
            isToday = checkIsToday(date);
            if (!isToday) {
                //If we are updating whether it is today or not, and it is no longer today (if it even was before)
                // then we want to reset whether we have sent a notification about the date yet
                hasNotified = false;
            }
        }

        public boolean isToday() {
            return isToday;
        }
    }

    private static class Christmas extends Holiday {

        private Christmas() {
            super(new YearlyDate(Month.DECEMBER, 25));
        }

        @Nullable
        @Override
        protected HolidayMessage getMessage(Player player) {
            return new HolidayMessage(getThemedLines(13, EnumColor.DARK_GREEN, EnumColor.DARK_RED),
                  MekanismLang.CHRISTMAS_LINE_ONE.translateColored(EnumColor.RED, EnumColor.DARK_BLUE, player.getName()),
                  MekanismLang.CHRISTMAS_LINE_TWO.translateColored(EnumColor.RED),
                  MekanismLang.CHRISTMAS_LINE_THREE.translateColored(EnumColor.RED),
                  MekanismLang.CHRISTMAS_LINE_FOUR.translateColored(EnumColor.RED),
                  MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY)
            );
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
            return super.filterSound(sound);
        }
    }

    private static class NewYear extends Holiday {

        private NewYear() {
            super(new YearlyDate(Month.JANUARY, 1));
        }

        @Nullable
        @Override
        protected HolidayMessage getMessage(Player player) {
            return new HolidayMessage(getThemedLines(13, EnumColor.WHITE, EnumColor.YELLOW),
                  MekanismLang.NEW_YEAR_LINE_ONE.translateColored(EnumColor.AQUA, EnumColor.DARK_BLUE, player.getName()),
                  MekanismLang.NEW_YEAR_LINE_TWO.translateColored(EnumColor.AQUA),
                  MekanismLang.NEW_YEAR_LINE_THREE.translateColored(EnumColor.AQUA, LocalDate.now().getYear()),
                  MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY)
            );
        }
    }

    private static class May4 extends Holiday {

        private May4() {
            super(new YearlyDate(Month.MAY, 4));
        }

        @Nullable
        @Override
        protected HolidayMessage getMessage(Player player) {
            return new HolidayMessage(getThemedLines(15, EnumColor.BLACK, EnumColor.GRAY, EnumColor.BLACK, EnumColor.YELLOW, EnumColor.BLACK),
                  MekanismLang.MAY_4_LINE_ONE.translateColored(EnumColor.GRAY, EnumColor.DARK_BLUE, player.getName())
            );
        }
    }

    private static class AprilFools extends Holiday {

        private AprilFools() {
            super(new YearlyDate(Month.APRIL, 1));
        }
    }

    private record HolidayMessage(String themedLines, Component... lines) {

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o instanceof HolidayMessage other && themedLines.equals(other.themedLines) && Arrays.equals(lines, other.lines);
        }

        @Override
        public int hashCode() {
            int result = themedLines.hashCode();
            result = 31 * result + Arrays.hashCode(lines);
            return result;
        }
    }

    public record YearlyDate(Month month, @Range(from = 1, to = 31) int day) {
    }
}