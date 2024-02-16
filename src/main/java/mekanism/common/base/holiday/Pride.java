package mekanism.common.base.holiday;

import java.time.Month;
import mekanism.api.robit.RobitSkin;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.holiday.HolidayManager.IRobitSkinRandomizerHoliday;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.util.EnumUtils;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

class Pride extends Holiday implements IRobitSkinRandomizerHoliday {

    public static final Pride INSTANCE = new Pride();

    private Pride() {
        super(new MonthlyDate(Month.JUNE));
    }

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
}