package mekanism.common.base.holiday;

import java.time.Month;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import net.minecraft.world.entity.player.Player;

class May4 extends Holiday {

    public static final May4 INSTANCE = new May4();

    private May4() {
        super(new YearlyDate(Month.MAY, 4));
    }

    @Override
    HolidayMessage getMessage(Player player) {
        return new HolidayMessage(getThemedLines(15, EnumColor.BLACK, EnumColor.GRAY, EnumColor.BLACK, EnumColor.YELLOW, EnumColor.BLACK),
              MekanismLang.MAY_4_LINE_ONE.translateColored(EnumColor.GRAY, EnumColor.DARK_BLUE, player.getName())
        );
    }
}