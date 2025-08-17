package mekanism.common.lib.radiation.capability;

import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.util.MekanismUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class RadiationEntity implements IRadiationEntity {

    private final LivingEntity entity;

    public RadiationEntity(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public double getRadiation() {
        return entity.getData(MekanismAttachmentTypes.RADIATION);
    }

    @Override
    public void radiate(double magnitude) {
        if (magnitude > 0) {
            entity.setData(MekanismAttachmentTypes.RADIATION, getRadiation() + magnitude);
        }
    }

    @Override
    public void update() {
        if (!entity.isAlive() || entity instanceof Player player && !MekanismUtils.isPlayingMode(player)) {
            return;
        }
        double radiation = getRadiation();
        double severityScale = RadiationScale.getScaledDoseSeverity(radiation);
        if (severityScale <= IRadiationManager.INSTANCE.baselineRadiation()) {
            //NO-OP, the entity isn't actually irradiated enough to notice
            return;
        }

        RandomSource rand = entity.level().getRandom();
        double minSeverity = MekanismConfig.general.radiationNegativeEffectsMinSeverity.get();
        double chance = minSeverity + rand.nextDouble() * (1 - minSeverity);

        if (severityScale > chance) {
            //Calculate effect strength based on radiation severity
            float strength = Math.max(1, (float) Math.log1p(radiation));
            //Hurt randomly
            if (rand.nextBoolean()) {
                if (entity instanceof ServerPlayer player) {
                    MinecraftServer server = entity.getServer();
                    int totemTimesUsed = -1;
                    if (server != null && server.isHardcore()) {//Only allow totems to count on hardcore
                        totemTimesUsed = player.getStats().getValue(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                    }
                    if (entity.hurt(MekanismDamageTypes.RADIATION.source(entity.level()), strength)) {
                        //If the damage actually went through fire the trigger
                        boolean hardcoreTotem = totemTimesUsed != -1 && totemTimesUsed < player.getStats().getValue(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                        MekanismCriteriaTriggers.DAMAGE.value().trigger(player, MekanismDamageTypes.RADIATION, hardcoreTotem);
                    }
                } else {
                    entity.hurt(MekanismDamageTypes.RADIATION.source(entity.level()), strength);
                }
            }
            if (entity instanceof ServerPlayer player) {
                player.getFoodData().addExhaustion(strength);
            }
        }
    }

    @Override
    public void set(double magnitude) {
        entity.setData(MekanismAttachmentTypes.RADIATION, Math.max(IRadiationManager.INSTANCE.baselineRadiation(), magnitude));
    }

    @Override
    public void decay() {
        set(getRadiation() * MekanismConfig.general.radiationTargetDecayRate.get());
    }
}