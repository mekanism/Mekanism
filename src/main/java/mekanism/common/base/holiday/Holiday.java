package mekanism.common.base.holiday;

import com.mojang.serialization.Codec;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.Map;
import mekanism.api.robit.RobitSkin;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public enum Holiday implements StringRepresentable {
    AprilFools(new YearlyDate(Month.APRIL, 1)),
    Christmas(new YearlyDate(Month.DECEMBER, 25)) {
        @Override
        HolidayMessage getMessage(Player player) {
            return new HolidayMessage(getThemedLines(13, EnumColor.DARK_GREEN, EnumColor.DARK_RED),
                  MekanismLang.CHRISTMAS_LINE_ONE.translateColored(EnumColor.RED, EnumColor.DARK_BLUE, player.getName()),
                  MekanismLang.CHRISTMAS_LINE_TWO.translateColored(EnumColor.RED),
                  MekanismLang.CHRISTMAS_LINE_THREE.translateColored(EnumColor.RED),
                  MekanismLang.CHRISTMAS_LINE_FOUR.translateColored(EnumColor.RED),
                  MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY)
            );
        }

        @Override
        public Map<Holder<SoundEvent>, Holder<SoundEvent>> getFilterableSounds() {
            return XMAS_FILTERABLE_SOUNDS;
        }
    },
    May4(new YearlyDate(Month.MAY, 4)) {
        @Override
        HolidayMessage getMessage(Player player) {
            return new HolidayMessage(getThemedLines(15, EnumColor.BLACK, EnumColor.GRAY, EnumColor.BLACK, EnumColor.YELLOW, EnumColor.BLACK),
                  MekanismLang.MAY_4_LINE_ONE.translateColored(EnumColor.GRAY, EnumColor.DARK_BLUE, player.getName())
            );
        }
    },
    NewYear(new YearlyDate(Month.JANUARY, 1)) {
        @Override
        HolidayMessage getMessage(Player player) {
            return new HolidayMessage(getThemedLines(13, EnumColor.WHITE, EnumColor.YELLOW),
                  MekanismLang.NEW_YEAR_LINE_ONE.translateColored(EnumColor.AQUA, EnumColor.DARK_BLUE, player.getName()),
                  MekanismLang.NEW_YEAR_LINE_TWO.translateColored(EnumColor.AQUA),
                  MekanismLang.NEW_YEAR_LINE_THREE.translateColored(EnumColor.AQUA, LocalDate.now().getYear()),
                  MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY)
            );
        }
    },
    Pride(new MonthlyDate(Month.JUNE)) {
        @Override
        HolidayMessage getMessage(Player player) {
            return new HolidayMessage(getThemedLines(12, RobitPrideSkinData.PRIDE.getColor()),
                  MekanismLang.PRIDE_LINE_ONE.translateColored(EnumColor.PINK, EnumColor.DARK_BLUE, player.getName()),
                  MekanismLang.PRIDE_LINE_TWO.translateColored(EnumColor.PINK),
                  MekanismLang.PRIDE_LINE_THREE.translateColored(EnumColor.PINK),
                  MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY)
            );
        }

        @Override
        public ResourceKey<RobitSkin> randomBaseSkin(RandomSource random) {
            return MekanismRobitSkins.PRIDE_SKINS.get(Util.getRandom(EnumUtils.PRIDE_SKINS, random));
        }
    };

    public static final Holiday[] VALUES = values();
    public static final Codec<Holiday> CODEC = StringRepresentable.fromEnum(Holiday::values);

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

    final boolean hasNotified() {
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

    boolean isRobitSkinRandomizer() {
        return this == Pride;
    }

    public Map<Holder<SoundEvent>, Holder<SoundEvent>> getFilterableSounds() {
        return Collections.emptyMap();
    }

    public ResourceKey<RobitSkin> randomBaseSkin(RandomSource random) {
        return MekanismRobitSkins.BASE;
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

    protected static final Map<Holder<SoundEvent>, Holder<SoundEvent>> XMAS_FILTERABLE_SOUNDS = Map.of(
          MekanismSounds.ENRICHMENT_CHAMBER, MekanismSounds.CHRISTMAS1,
          MekanismSounds.METALLURGIC_INFUSER, MekanismSounds.CHRISTMAS2,
          MekanismSounds.PURIFICATION_CHAMBER, MekanismSounds.CHRISTMAS3,
          MekanismSounds.ENERGIZED_SMELTER, MekanismSounds.CHRISTMAS4,
          MekanismSounds.CRUSHER, MekanismSounds.CHRISTMAS5
    );

    @Override
    public String getSerializedName() {
        return name();
    }
}