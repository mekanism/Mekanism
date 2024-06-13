package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.EnchantmentAwareModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.common.config.MekanismConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

@ParametersAreNotNullByDefault
public record ModuleHydrostaticRepulsorUnit(boolean swimBoost) implements EnchantmentAwareModule<ModuleHydrostaticRepulsorUnit> {

    public static final String SWIM_BOOST = "swim_boost";
    public static final int BOOST_STACKS = 4;

    public ModuleHydrostaticRepulsorUnit(IModule<ModuleHydrostaticRepulsorUnit> module) {
        this(module.getBooleanConfigOrFalse(SWIM_BOOST));
    }

    @NotNull
    @Override
    public ResourceKey<Enchantment> enchantment() {
        return Enchantments.DEPTH_STRIDER;
    }

    @Override
    public void tickServer(IModule<ModuleHydrostaticRepulsorUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        if (isSwimBoost(module, stack, player)) {
            module.useEnergy(player, stack, MekanismConfig.gear.mekaSuitEnergyUsageHydrostaticRepulsion.get());
        }
    }

    public boolean isSwimBoost(IModule<ModuleHydrostaticRepulsorUnit> module, ItemStack stack, Player player) {
        return swimBoost && module.getInstalledCount() >= BOOST_STACKS && !player.getMaxHeightFluidType().isAir() &&
               module.hasEnoughEnergy(stack, MekanismConfig.gear.mekaSuitEnergyUsageHydrostaticRepulsion);
    }
}