package mekanism.common.content.gear.mekasuit;

import java.util.List;
import mekanism.api.MekanismAPITags;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public record ModuleInhalationPurificationUnit(boolean beneficialEffects, boolean neutralEffects, boolean harmfulEffects)
      implements ICustomModule<ModuleInhalationPurificationUnit> {

    private static final ModuleDamageAbsorbInfo INHALATION_ABSORB_INFO = new ModuleDamageAbsorbInfo(MekanismConfig.gear.mekaSuitMagicDamageRatio,
          MekanismConfig.gear.mekaSuitEnergyUsageMagicReduce);

    public static final Identifier BENEFICIAL_EFFECTS = Mekanism.rl("purification.beneficial");
    public static final Identifier NEUTRAL_EFFECTS = Mekanism.rl("purification.neutral");
    public static final Identifier HARMFUL_EFFECTS = Mekanism.rl("purification.harmful");

    public ModuleInhalationPurificationUnit(IModule<ModuleInhalationPurificationUnit> module) {
        this(module.getBooleanConfigOrFalse(BENEFICIAL_EFFECTS), module.getBooleanConfigOrFalse(NEUTRAL_EFFECTS), module.getBooleanConfigOrFalse(HARMFUL_EFFECTS));
    }

    @Override
    public void tickClient(IModule<ModuleInhalationPurificationUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        try (Transaction simulation = Transaction.open(transaction)) {
            //Version of tickServer that doesn't commit so that the timer actually properly updates
            tick(module, itemAccess, player, simulation);
        }
    }

    @Override
    public void tickServer(IModule<ModuleInhalationPurificationUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        tick(module, itemAccess, player, transaction);
    }

    private void tick(IModule<ModuleInhalationPurificationUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        int usage = MekanismConfig.gear.mekaSuitEnergyUsagePotionTick.get();
        try (Transaction simulation = Transaction.open(transaction)) {
            if (!module.useAllEnergy(player, itemAccess, usage, simulation)) {
                //Not enough energy, just exit
                return;
            }
        }
        //Gather all the active effects that we can handle, so that we have them in their own list and
        // don't run into any issues related to CMEs
        List<MobEffectInstance> effects = player.getActiveEffects().stream().filter(this::canHandle).toList();
        for (MobEffectInstance effect : effects) {
            if (module.useAllEnergy(player, itemAccess, usage, transaction)) {
                speedupEffect(player, effect);
            } else {
                //If we can't able to actually extract energy, exit
                break;
            }
        }
    }

    @Nullable
    @Override
    public ICustomModule.ModuleDamageAbsorbInfo getDamageAbsorbInfo(IModule<ModuleInhalationPurificationUnit> module, DamageSource damageSource) {
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