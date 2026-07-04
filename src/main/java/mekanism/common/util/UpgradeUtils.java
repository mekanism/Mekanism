package mekanism.common.util;

import com.google.common.primitives.Ints;
import mekanism.api.math.MathUtils;
import mekanism.api.upgrade.IUpgradeHelper;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.component.UpgradeType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.interfaces.IUpgradeTile;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class UpgradeUtils implements IUpgradeHelper {

    @Nullable
    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> Holder<Upgrade> fromInstance(ITEM instance) {
        UpgradeType upgradeType = instance.get(MekanismDataComponents.UPGRADE_TYPE);
        return upgradeType == null ? null : upgradeType.type();
    }

    private DataComponentPatch getPatch(Holder<Upgrade> upgrade) {
        return DataComponentPatch.builder()
              .set(MekanismDataComponents.UPGRADE_TYPE.get(), new UpgradeType(upgrade))
              .build();
    }

    @Override
    public ItemStackTemplate asTemplate(Holder<Upgrade> upgrade, int amount) {
        return new ItemStackTemplate(MekanismItems.UPGRADE, amount, getPatch(upgrade));
    }

    @Override
    public ItemStack asStack(Holder<Upgrade> upgrade, int amount) {
        return new ItemStack(MekanismItems.UPGRADE, amount, getPatch(upgrade));
    }

    @Override
    public ItemResource asResource(Holder<Upgrade> upgrade) {
        return ItemResource.of((Holder<Item>) MekanismItems.UPGRADE, getPatch(upgrade));
    }

    public static int getBaseUsage(HolderLookup.Provider registries, IUpgradeTile tile, int def) {
        //getGasPerTickMean * required ticks (not rounded)
        Holder.Reference<Upgrade> chemicalUpgrade = registries.get(UpgradeIds.CHEMICAL).orElse(null);
        if (chemicalUpgrade != null && tile.supportsUpgrade(chemicalUpgrade)) {
            // def * (upgradeMultiplier ^ ((2 * speed - gas) / 8)) * (upgradeMultiplier ^ (-speed / 8)) =
            // def * upgradeMultiplier ^ ((speed - gas) / 8)
            //TODO: We may want to validate this provides the numbers we desire if we ever end up with any machines
            // that use this that are not statistical and have gas upgrades so would go through this code path
            Holder.Reference<Upgrade> speedUpgrade = registries.get(UpgradeIds.SPEED).orElse(null);
            if (speedUpgrade != null) {
                //TODO - 26.2: Re-evaluate this cast
                return Ints.saturatedCast(Math.round(def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(),
                      fractionUpgrades(tile, speedUpgrade) - fractionUpgrades(tile, chemicalUpgrade))));
            }
        }
        //If it doesn't support gas upgrades, we can fall through to the default value as the math would be:
        // def * (upgradeMultiplier ^ (speed / 8)) * (upgradeMultiplier ^ (-speed / 8)) =
        // def * 1
        return def;
    }

    /// Gets the operating ticks required for a machine via its upgrades.
    ///
    /// @param def the original, default ticks required
    ///
    /// @return required operating ticks
    public static int getTicks(int def, Holder<Upgrade> speedUpgrade, int installedUpgrades) {
        return Math.max(1, MathUtils.clampToInt(getTicksD(def, speedUpgrade, installedUpgrades)));
    }

    /// Gets the operating ticks required for a machine via its upgrades.
    ///
    /// @param def the original, default ticks required
    ///
    /// @return required operating ticks
    public static double getTicksD(int def, Holder<Upgrade> speedUpgrade, int installedUpgrades) {
        return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), -fractionUpgrades(installedUpgrades, speedUpgrade));
    }

    /// Get the amount of operations per tick, accounting for bonus operations from non-default upgrade modifiers. Fractional operations are ignored
    ///
    /// @param defTicks          the original, default ticks required
    /// @param defaultOperations the original, default operations (usually 1)
    ///
    /// @return max operations to do in one tick. If speed is not < 1 tick return the default
    public static int getOperationsPerTick(int defTicks, int defaultOperations, Holder<Upgrade> speedUpgrade, int installedUpgrades) {
        double ticksD = getTicksD(defTicks, speedUpgrade, installedUpgrades);
        if (ticksD >= 1) {
            return defaultOperations;
        }
        return MathUtils.clampToInt(Math.max(1, 1 / ticksD) * defaultOperations);
    }

    /// Gets the energy required per tick for a machine via its upgrades.
    ///
    /// @param tile tile containing upgrades
    /// @param def  the original, default energy required
    ///
    /// @return required energy per tick
    public static int getEnergyPerTick(IUpgradeTile tile, int def, Holder<Upgrade> energyUpgrade, Holder<Upgrade> speedUpgrade) {
        return Mth.ceil(def * Math.pow(
              MekanismConfig.general.maxUpgradeMultiplier.get(),
              2 * fractionUpgrades(tile, speedUpgrade) - fractionUpgrades(tile, energyUpgrade)
        ));
    }

    /// Gets the secondary energy multiplier required per tick for a machine via upgrades.
    ///
    /// @param tile tile containing upgrades
    ///
    /// @return max secondary energy per tick
    public static double getGasPerTickMeanMultiplier(HolderLookup.Provider registries, IUpgradeTile tile) {
        Holder.Reference<Upgrade> speedUpgrade = registries.get(UpgradeIds.SPEED).orElse(null);
        if (speedUpgrade != null) {
            Holder.Reference<Upgrade> chemicalUpgrade = registries.get(UpgradeIds.CHEMICAL).orElse(null);
            if (chemicalUpgrade != null && tile.supportsUpgrade(chemicalUpgrade)) {
                return Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2 * fractionUpgrades(tile, speedUpgrade) - fractionUpgrades(tile, chemicalUpgrade));
            }
            return Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(tile, speedUpgrade));
        }
        return 1;
    }

    /// Gets the maximum energy for a machine via its upgrades.
    ///
    /// @param tile tile containing upgrades
    /// @param def  original, default max energy
    ///
    /// @return max energy
    public static long getMaxEnergy(IUpgradeTile tile, long def, Holder<Upgrade> energyUpgrade) {
        return MathUtils.clampToLong(def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(tile, energyUpgrade)));
    }

    private static double fractionUpgrades(IUpgradeTile tile, Holder<Upgrade> type) {
        return fractionUpgrades(tile.getUpgrades(type), type);
    }

    private static double fractionUpgrades(int installed, Holder<Upgrade> type) {
        return installed / (double) type.value().max();
    }
}