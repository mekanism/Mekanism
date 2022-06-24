package mekanism.common.content.gear.mekasuit;

import javax.annotation.Nonnull;
import mekanism.api.gear.EnchantmentBasedModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit.ExcavationRange;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class ModuleHydrostaticRepulsorUnit extends EnchantmentBasedModule<ModuleHydrostaticRepulsorUnit> {

    public static final int BOOST_STACKS = 4;

    private IModuleConfigItem<Boolean> swimBoost;

    @Override
    public void init(IModule<ModuleHydrostaticRepulsorUnit> module, ModuleConfigItemCreator configItemCreator) {
        swimBoost = configItemCreator.createDisableableConfigItem("swim_boost", MekanismLang.MODULE_SWIM_BOOST, true, () -> module.getInstalledCount() >= BOOST_STACKS);
    }

    @Nonnull
    @Override
    public Enchantment getEnchantment() {
        return Enchantments.DEPTH_STRIDER;
    }

    @Override
    public void tickServer(IModule<ModuleHydrostaticRepulsorUnit> module, Player player) {
        if (isSwimBoost(module)) {
            module.useEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageHydrostaticRepulsion.get());
        }
    }

    public boolean isSwimBoost(IModule<ModuleHydrostaticRepulsorUnit> module) {
        return swimBoost.get() && module.getInstalledCount() >= BOOST_STACKS;
    }
}