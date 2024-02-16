package mekanism.common.base.holiday;

import java.time.LocalDate;
import java.time.Month;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import net.minecraft.world.entity.player.Player;

class NewYear extends Holiday {

    public static final NewYear INSTANCE = new NewYear();

    private NewYear() {
        super(new YearlyDate(Month.JANUARY, 1));
    }

    @Override
    HolidayMessage getMessage(Player player) {
        return new HolidayMessage(getThemedLines(13, EnumColor.WHITE, EnumColor.YELLOW),
              MekanismLang.NEW_YEAR_LINE_ONE.translateColored(EnumColor.AQUA, EnumColor.DARK_BLUE, player.getName()),
              MekanismLang.NEW_YEAR_LINE_TWO.translateColored(EnumColor.AQUA),
              MekanismLang.NEW_YEAR_LINE_THREE.translateColored(EnumColor.AQUA, LocalDate.now().getYear()),
              MekanismLang.HOLIDAY_SIGNATURE.translateColored(EnumColor.DARK_GRAY)
        );
    }
}