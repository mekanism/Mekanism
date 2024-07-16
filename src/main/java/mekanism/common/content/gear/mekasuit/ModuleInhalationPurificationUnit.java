package mekanism.common.content.gear.mekasuit;

import java.util.List;
import mekanism.api.MekanismAPITags;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public record ModuleInhalationPurificationUnit(boolean beneficialEffects, boolean neutralEffects, boolean harmfulEffects) implements ICustomModule<ModuleInhalationPurificationUnit> {

    private static final ModuleDamageAbsorbInfo INHALATION_ABSORB_INFO = new ModuleDamageAbsorbInfo(MekanismConfig.gear.mekaSuitMagicDamageRatio,
          MekanismConfig.gear.mekaSuitEnergyUsageMagicReduce);

    public static final ResourceLocation BENEFICIAL_EFFECTS = Mekanism.rl("purification.beneficial");
    public static final ResourceLocation NEUTRAL_EFFECTS = Mekanism.rl("purification.neutral");
    public static final ResourceLocation HARMFUL_EFFECTS = Mekanism.rl("purification.harmful");

    public ModuleInhalationPurificationUnit(IModule<ModuleInhalationPurificationUnit> module) {
        this(module.getBooleanConfigOrFalse(BENEFICIAL_EFFECTS), module.getBooleanConfigOrFalse(NEUTRAL_EFFECTS), module.getBooleanConfigOrFalse(HARMFUL_EFFECTS));
    }

    @Override
    public void tickClient(IModule<ModuleInhalationPurificationUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        //Messy rough estimate version of tickServer so that the timer actually properly updates
        if (!player.isSpectator()) {
            long usage = MekanismConfig.gear.mekaSuitEnergyUsagePotionTick.get();
            boolean free = usage == 0L || player.isCreative();
            long energy = free ? 0L : module.getContainerEnergy(stack);
            if (free || energy >= usage) {
                //Gather all the active effects that we can handle, so that we have them in their own list and
                // don't run into any issues related to CMEs
                List<MobEffectInstance> effects = player.getActiveEffects().stream().filter(this::canHandle).toList();
                for (MobEffectInstance effect : effects) {
                    if (free) {
                        speedupEffect(player, effect);
                    } else {
                        energy -= usage;
                        speedupEffect(player, effect);
                        if (energy < usage) {
                            //If after using energy, our remaining energy is now smaller than how much we need to use, exit
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void tickServer(IModule<ModuleInhalationPurificationUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        long usage = MekanismConfig.gear.mekaSuitEnergyUsagePotionTick.get();
        boolean free = usage == 0L || player.isCreative();
        IEnergyContainer energyContainer = free ? null : module.getEnergyContainer(stack);
        if (free || (energyContainer != null && energyContainer.getEnergy() >= usage)) {
            //Gather all the active effects that we can handle, so that we have them in their own list and
            // don't run into any issues related to CMEs
            List<MobEffectInstance> effects = player.getActiveEffects().stream().filter(this::canHandle).toList();
            for (MobEffectInstance effect : effects) {
                if (free) {
                    speedupEffect(player, effect);
                } else if (module.useEnergy(player, energyContainer, usage, true) == 0L) {
                    //If we can't actually extract energy, exit
                    break;
                } else {
                    speedupEffect(player, effect);
                    if (energyContainer.getEnergy() < usage) {
                        //If after using energy, our remaining energy is now smaller than how much we need to use, exit
                        break;
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public ModuleDamageAbsorbInfo getDamageAbsorbInfo(IModule<ModuleInhalationPurificationUnit> module, DamageSource damageSource) {
        return damageSource.is(MekanismAPITags.DamageTypes.IS_PREVENTABLE_MAGIC) ? INHALATION_ABSORB_INFO : null;
    }

    private void speedupEffect(Player player, MobEffectInstance effect) {
        for (int i = 0; i < 9; i++) {
            MekanismUtils.speedUpEffectSafely(player, effect);
        }
    }

    private boolean canHandle(MobEffectInstance effectInstance) {
        return MekanismUtils.shouldSpeedUpEffect(effectInstance) && switch (effectInstance.getEffect().value().getCategory()) {
            case BENEFICIAL -> beneficialEffects;
            case HARMFUL -> harmfulEffects;
            case NEUTRAL -> neutralEffects;
        };
    }
}