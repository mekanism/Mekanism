package mekanism.common.tile.interfaces;

import java.util.List;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

public interface ITileUpgradable extends IUpgradeTile {

    @Nullable
    TagKey<Upgrade> getSupportedUpgrade();

    default List<Component> getInfo(Holder<Upgrade> upgrade) {
        //TODO - 26.2: Can this be offloaded to the upgrade
        return upgrade.is(UpgradeIds.SPEED) ? UpgradeUtils.getExpScaledInfo(this, upgrade) : UpgradeUtils.getMultScaledInfo(this, upgrade);
    }
}