package mekanism.common.tile.interfaces;

import java.util.Collections;
import java.util.List;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.component.TileComponentUpgrade;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

public interface IUpgradeTile {

    default boolean supportsUpgrades() {
        return true;
    }

    @Nullable
    TagKey<Upgrade> getSupportedUpgrade();

    default boolean supportsUpgrade(Holder<Upgrade> upgradeType) {
        if (supportsUpgrades()) {
            TileComponentUpgrade component = getComponent();
            //Note: This should never be null given supportsUpgrades is true, but if it is, handle it gracefully
            return component != null && component.supports(upgradeType);
        }
        return false;
    }

    default int getUpgrades(@Nullable Holder<Upgrade> upgradeType) {
        if (upgradeType == null) {
            return 0;
        }
        TileComponentUpgrade component = getComponent();
        return component == null ? 0 : component.getUpgrades(upgradeType);
    }

    default int addUpgrades(HolderLookup.Provider registries, Holder<Upgrade> upgrade, int maxAvailable) {
        TileComponentUpgrade component = getComponent();
        return component == null ? 0 : component.addUpgrades(registries, upgrade, maxAvailable);
    }

    float getVolumeFactor();

    @Nullable
    TileComponentUpgrade getComponent();

    void recalculateUpgrades(HolderGetter<Upgrade> upgrades, Holder<Upgrade> upgradeType, int totalInstalled);

    default boolean upgradeInfoIsExponential(Holder<Upgrade> upgrade) {
        return false;
    }

    default List<Component> getUpgradeWindowInfo(Holder<Upgrade> upgrade) {
        if (supportsUpgrade(upgrade) && upgrade.value().supportsMultiple()) {
            float installed = getUpgrades(upgrade);
            if (upgradeInfoIsExponential(upgrade)) {
                return Collections.singletonList(MekanismLang.UPGRADES_EFFECT.translate(Math.pow(2, installed)));
            }
            double effect = Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), installed / upgrade.value().max());
            return Collections.singletonList(MekanismLang.UPGRADES_EFFECT.translate(Math.round(effect * 100) / 100F));
        }
        return Collections.emptyList();
    }
}