package mekanism.common.base.holiday;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import mekanism.api.robit.RobitSkin;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class HolidayManager {

    private HolidayManager() {
    }

    //TODO - 26.2: Fix shutdown handling of schedule executor causing the game to sometimes hang: https://github.com/mezz/JustEnoughItems/commit/9540c4861bdf9aee381a8d1d3827204cb3c69ffe
    private static final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "HolidayManager Day Checker");
        t.setDaemon(true);
        return t;
    });

    /// Map of sounds which _might_ return a different value than the original holder
    private static final Map<Holder<SoundEvent>, Supplier<SoundEvent>> filterableSounds = new HashMap<>();

    private static boolean holidaysNotified = false;
    @Nullable
    private static Holiday modIconHoliday;
    @Nullable
    private static Holiday robitSkinHoliday;
    @Nullable
    private static Holiday soundHoliday;

    public static boolean areHolidaysEnabled() {
        return MekanismConfig.common.holidays.get();
    }

    public static void init() {
        //Figure out what sounds we need to wrap because they might be filterable
        Set<Holder<SoundEvent>> allFilterableSounds = new HashSet<>();
        for (Holiday holiday : Holiday.VALUES) {
            allFilterableSounds.addAll(holiday.getFilterableSounds().keySet());
        }
        //now we've gathered possible items, generate the suppliers which make them dynamic and config-responsive
        for (Holder<SoundEvent> filterable : allFilterableSounds) {
            filterableSounds.put(filterable, getSoundEventSupplier(filterable));
        }

        //schedule the holiday updater, and run it the first time
        timer.scheduleAtFixedRate(HolidayManager::updateToday,
              LocalTime.now().until(LocalTime.MIDNIGHT, ChronoUnit.MILLIS),
              TimeUnit.DAYS.toMillis(1),
              TimeUnit.MILLISECONDS);
        updateToday();

        Mekanism.logger.info("Initialized HolidayManager.");
    }

    private static Supplier<SoundEvent> getSoundEventSupplier(Holder<SoundEvent> filterable) {
        return () -> {
            if (areHolidaysEnabled() && soundHoliday != null) {
                return soundHoliday.getFilterableSounds().getOrDefault(filterable, filterable).value();
            }
            return filterable.value();
        };
    }

    private static void updateToday() {
        //Mark that we haven't notified holidays yet today, and reset the filtering holidays
        holidaysNotified = false;
        modIconHoliday = null;
        robitSkinHoliday = null;
        soundHoliday = null;
        YearlyDate date = YearlyDate.now();
        for (Holiday holiday : Holiday.VALUES) {
            if (holiday.updateIsToday(date)) {
                if (modIconHoliday == null && holiday.customModIcon() != null) {
                    modIconHoliday = holiday;
                }
                if (robitSkinHoliday == null && holiday.isRobitSkinRandomizer()) {
                    robitSkinHoliday = holiday;
                }
                if (soundHoliday == null && !holiday.getFilterableSounds().isEmpty()) {
                    soundHoliday = holiday;
                }
            }
        }
    }

    /// @apiNote Only call on the client side
    public static void notify(Player player) {
        if (!holidaysNotified) {
            //Mark as notified even if messages are configured to off, so that we don't have to try notifying for the rest of the day
            holidaysNotified = true;
            if (areHolidaysEnabled()) {
                for (Holiday holiday : Holiday.VALUES) {
                    if (holiday.isToday() && !holiday.hasNotified()) {
                        holiday.notify(player);
                    }
                }
            }
        }
    }

    public static Supplier<SoundEvent> filterSound(SoundEventRegistryObject<SoundEvent> sound) {
        return filterableSounds.getOrDefault(sound, sound);
    }

    public static ResourceKey<RobitSkin> getRandomBaseSkin(RandomSource random) {
        if (areHolidaysEnabled() && robitSkinHoliday != null) {
            return robitSkinHoliday.randomBaseSkin(random);
        }
        return MekanismRobitSkins.BASE;
    }

    public static boolean hasRobitSkinsToday() {
        return areHolidaysEnabled() && robitSkinHoliday != null;
    }

    @Nullable
    public static Identifier getCustomModIconToday() {
        return areHolidaysEnabled() && modIconHoliday != null ? modIconHoliday.customModIcon() : null;
    }
}