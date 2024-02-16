package mekanism.common.base.holiday;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class HolidayManager {

    private HolidayManager() {
    }

    private static final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "HolidayManager Day Checker");
        t.setDaemon(true);
        return t;
    });

    private static final Set<Holiday> holidays = Set.of(
          Christmas.INSTANCE,
          NewYear.INSTANCE,
          May4.INSTANCE,
          AprilFools.INSTANCE,
          Pride.INSTANCE
    );
    private static final Map<Holder<SoundEvent>, Supplier<SoundEvent>> filterableSounds = new HashMap<>();

    private static boolean holidaysNotified = false;
    @Nullable
    private static IRobitSkinRandomizerHoliday robitSkinHoliday;
    @Nullable
    private static IFilterableSoundHoliday soundHoliday;

    public static boolean areHolidaysEnabled() {
        return MekanismConfig.common.holidays.get();
    }

    public static void init() {
        timer.scheduleAtFixedRate(HolidayManager::updateToday,
              LocalTime.now().until(LocalTime.MIDNIGHT, ChronoUnit.MILLIS),
              TimeUnit.DAYS.toMillis(1),
              TimeUnit.MILLISECONDS);
        updateToday();
        //Figure out what sounds we need to wrap because they might be filterable
        for (Holiday holiday : holidays) {
            if (holiday instanceof IFilterableSoundHoliday filterableSoundHoliday) {
                for (Holder<SoundEvent> soundEvent : filterableSoundHoliday.getFilterableSounds().keySet()) {
                    filterableSounds.computeIfAbsent(soundEvent, sound -> () -> {
                        if (areHolidaysEnabled() && soundHoliday != null) {
                            return soundHoliday.getFilterableSounds().getOrDefault(sound, sound).value();
                        }
                        return sound.value();
                    });
                }
            }
        }
        Mekanism.logger.info("Initialized HolidayManager.");
    }

    private static void updateToday() {
        //Mark that we haven't notified holidays yet today, and reset the filtering holidays
        holidaysNotified = false;
        robitSkinHoliday = null;
        soundHoliday = null;
        YearlyDate date = YearlyDate.now();
        for (Holiday holiday : holidays) {
            if (holiday.updateIsToday(date)) {
                if (robitSkinHoliday == null && holiday instanceof IRobitSkinRandomizerHoliday randomizerHoliday) {
                    robitSkinHoliday = randomizerHoliday;
                }
                if (soundHoliday == null && holiday instanceof IFilterableSoundHoliday filterableSoundHoliday) {
                    soundHoliday = filterableSoundHoliday;
                }
            }
        }
    }

    /**
     * @apiNote Only call on the client side
     */
    public static void notify(Player player) {
        if (!holidaysNotified) {
            //Mark as notified even if messages are configured to off, so that we don't have to try notifying for the rest of the day
            holidaysNotified = true;
            if (areHolidaysEnabled()) {
                for (Holiday holiday : holidays) {
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

    interface IFilterableSoundHoliday {

        Map<Holder<SoundEvent>, Holder<SoundEvent>> getFilterableSounds();
    }

    interface IRobitSkinRandomizerHoliday {

        ResourceKey<RobitSkin> randomBaseSkin(RandomSource random);
    }
}