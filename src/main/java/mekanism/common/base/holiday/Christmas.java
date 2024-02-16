package mekanism.common.base.holiday;

import java.time.Month;
import java.util.Map;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.holiday.HolidayManager.IFilterableSoundHoliday;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

class Christmas extends Holiday implements IFilterableSoundHoliday {

    public static final Christmas INSTANCE = new Christmas();

    private final Map<Holder<SoundEvent>, Holder<SoundEvent>> filterableSounds = Map.of(
          MekanismSounds.ENRICHMENT_CHAMBER, MekanismSounds.CHRISTMAS1,
          MekanismSounds.METALLURGIC_INFUSER, MekanismSounds.CHRISTMAS2,
          MekanismSounds.PURIFICATION_CHAMBER, MekanismSounds.CHRISTMAS3,
          MekanismSounds.ENERGIZED_SMELTER, MekanismSounds.CHRISTMAS4,
          MekanismSounds.CRUSHER, MekanismSounds.CHRISTMAS5
    );

    private Christmas() {
        super(new YearlyDate(Month.DECEMBER, 25));
    }

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
        return filterableSounds;
    }
}