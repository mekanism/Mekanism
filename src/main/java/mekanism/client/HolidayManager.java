package mekanism.client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

public final class HolidayManager {

    private static Calendar calendar = Calendar.getInstance();
    private static Minecraft mc = Minecraft.getMinecraft();

    private static List<Holiday> holidays = new ArrayList<>();
    private static List<Holiday> holidaysNotified = new ArrayList<>();

    public static void init() {
        if (MekanismConfig.current().client.holidays.val()) {
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
                        holiday.onEvent(mc.player);
                        holidaysNotified.add(holiday);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public static ResourceLocation filterSound(ResourceLocation sound) {
        if (!MekanismConfig.current().client.holidays.val()) {
            return sound;
        }

        try {
            YearlyDate date = getDate();

            for (Holiday holiday : holidays) {
                if (holiday.getDate().equals(date)) {
                    return holiday.filterSound(sound);
                }
            }
        } catch (Exception e) {
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

        public abstract void onEvent(EntityPlayer player);

        public ResourceLocation filterSound(ResourceLocation sound) {
            return sound;
        }
    }

    private static class Christmas extends Holiday {

        private String[] nutcracker = new String[]{"christmas.1", "christmas.2", "christmas.3", "christmas.4",
              "christmas.5"};

        @Override
        public YearlyDate getDate() {
            return new YearlyDate(12, 25);
        }

        @Override
        public void onEvent(EntityPlayer player) {
            String themedLines = getThemedLines(new EnumColor[]{EnumColor.DARK_GREEN, EnumColor.DARK_RED}, 13);
            player.sendMessage(new TextComponentString(themedLines + EnumColor.DARK_BLUE + "[Mekanism]" + themedLines));
            player.sendMessage(new TextComponentString(
                  EnumColor.RED + "Merry Christmas, " + EnumColor.DARK_BLUE + player.getName() + EnumColor.RED + "!"));
            player.sendMessage(new TextComponentString(EnumColor.RED + "May you have plenty of Christmas cheer"));
            player.sendMessage(new TextComponentString(EnumColor.RED + "and have a relaxing holiday with your"));
            player.sendMessage(new TextComponentString(EnumColor.RED + "family :)"));
            player.sendMessage(new TextComponentString(EnumColor.DARK_GREY + "-aidancbrady"));
            player.sendMessage(new TextComponentString(themedLines + EnumColor.DARK_BLUE + "[=======]" + themedLines));
        }

        @Override
        public ResourceLocation filterSound(ResourceLocation sound) {
            String soundResourceLocationString = sound.toString();

            if (soundResourceLocationString.contains("machine.enrichment")) {
                return new ResourceLocation(sound.toString().replace("machine.enrichment", nutcracker[0]));
            } else if (soundResourceLocationString.contains("machine.metalinfuser")) {
                return new ResourceLocation(sound.toString().replace("machine.metalinfuser", nutcracker[1]));
            } else if (soundResourceLocationString.contains("machine.purification")) {
                return new ResourceLocation(sound.toString().replace("machine.purification", nutcracker[2]));
            } else if (soundResourceLocationString.contains("machine.smelter")) {
                return new ResourceLocation(sound.toString().replace("machine.smelter", nutcracker[3]));
            } else if (soundResourceLocationString.contains("machine.dissolution")) {
                return new ResourceLocation(sound.toString().replace("machine.dissolution", nutcracker[4]));
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
        public void onEvent(EntityPlayer player) {
            String themedLines = getThemedLines(new EnumColor[]{EnumColor.WHITE, EnumColor.YELLOW}, 13);
            player.sendMessage(new TextComponentString(themedLines + EnumColor.DARK_BLUE + "[Mekanism]" + themedLines));
            player.sendMessage(new TextComponentString(
                  EnumColor.AQUA + "Happy New Year, " + EnumColor.DARK_BLUE + player.getName() + EnumColor.RED + "!"));
            player.sendMessage(new TextComponentString(EnumColor.AQUA + "Best wishes to you as we enter this"));
            player.sendMessage(new TextComponentString(
                  EnumColor.AQUA + "new and exciting year of " + calendar.get(Calendar.YEAR) + "! :)"));
            player.sendMessage(new TextComponentString(EnumColor.DARK_GREY + "-aidancbrady"));
            player.sendMessage(new TextComponentString(themedLines + EnumColor.DARK_BLUE + "[=======]" + themedLines));
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
