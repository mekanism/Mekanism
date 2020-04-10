package mekanism.common.util.text;

import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import net.minecraft.util.text.ITextComponent;

public class UpgradeDisplay implements IHasTextComponent {

    private final Upgrade upgrade;
    private final int level;

    private UpgradeDisplay(Upgrade upgrade, int level) {
        this.upgrade = upgrade;
        this.level = level;
    }

    public static UpgradeDisplay of(Upgrade upgrade) {
        return of(upgrade, 0);
    }

    public static UpgradeDisplay of(Upgrade upgrade, int level) {
        return new UpgradeDisplay(upgrade, level);
    }

    @Override
    public ITextComponent getTextComponent() {
        if (upgrade.canMultiply() && level > 0) {
            return MekanismLang.UPGRADE_DISPLAY_LEVEL.translateColored(upgrade.getColor(), upgrade, EnumColor.GRAY, level);
        }
        return MekanismLang.UPGRADE_DISPLAY.translateColored(upgrade.getColor(), upgrade);
    }
}