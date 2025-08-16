package mekanism.common;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.LongSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.FloatSupplier;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.common.base.KeySync;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IBlastingItem;
import mekanism.common.content.gear.mekasuit.ModuleGravitationalModulatingUnit;
import mekanism.common.content.gear.mekasuit.ModuleHydraulicPropulsionUnit;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemScubaMask;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.item.interfaces.IFreeRunnerItem;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.lib.radiation.PlayerExposure;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

public class CommonPlayerTickHandler {

    public static boolean isOnGroundOrSleeping(Player player) {
        return player.onGround() || player.isSleeping() || player.getAbilities().flying;
    }

    public static boolean isScubaMaskOn(Player player, ItemStack tank) {
        ItemStack mask = player.getItemBySlot(EquipmentSlot.HEAD);
        return !tank.isEmpty() && !mask.isEmpty() && tank.getItem() instanceof ItemScubaTank scubaTank &&
               mask.getItem() instanceof ItemScubaMask && ChemicalUtil.hasAnyChemical(tank) && scubaTank.getMode(tank);
    }

    public static float getStepBoost(Player player) {
        if (player.isShiftKeyDown()) {
            return 0;
        }
        ItemStack stack = player.getItemBySlot(EquipmentSlot.FEET);
        if (stack.isEmpty()) {
            return 0;
        }
        IModule<ModuleHydraulicPropulsionUnit> hydraulic = IModuleHelper.INSTANCE.getIfEnabled(stack, MekanismModules.HYDRAULIC_PROPULSION_UNIT);
        if (hydraulic != null) {
            return hydraulic.getCustomInstance().getStepHeight();
        }
        ItemStack primaryFreeRunners = IFreeRunnerItem.getPrimaryFreeRunners(player);
        if (!primaryFreeRunners.isEmpty() && ((IFreeRunnerItem) primaryFreeRunners.getItem()).getFreeRunnerMode(primaryFreeRunners).providesStepBoost()) {
            return 0.5F;
        }
        return 0;
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent.Post event) {
        //Note: Player's can't be frozen with the tick rate manager, so we don't have to check it here
        if (!event.getEntity().level().isClientSide()) {
            tickEnd(event.getEntity());
        }
    }

    private void tickEnd(Player player) {
        Mekanism.playerState.updateStepAssist(player);
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerExposure.tickServer(serverPlayer);
        }

        ItemStack jetpack = IJetpackItem.getActiveJetpack(player);
        if (!jetpack.isEmpty()) {
            ItemStack primaryJetpack = IJetpackItem.getPrimaryJetpack(player);
            if (!primaryJetpack.isEmpty()) {
                IJetpackItem jetpackItem = (IJetpackItem) primaryJetpack.getItem();
                JetpackMode primaryMode = jetpackItem.getJetpackMode(primaryJetpack);
                JetpackMode mode = IJetpackItem.getPlayerJetpackMode(player, primaryMode, p -> Mekanism.keyMap.has(p.getUUID(), KeySync.ASCEND));
                if (mode != JetpackMode.DISABLED) {
                    double jetpackThrust = jetpackItem.getJetpackThrust(primaryJetpack);
                    if (IJetpackItem.handleJetpackMotion(player, mode, jetpackThrust, p -> Mekanism.keyMap.has(p.getUUID(), KeySync.ASCEND))) {
                        player.resetFallDistance();
                        if (player instanceof ServerPlayer serverPlayer) {
                            serverPlayer.connection.aboveGroundTickCount = 0;
                        }
                    }
                    ((IJetpackItem) jetpack.getItem()).useJetpackFuel(jetpack);
                    if (player.level().getGameTime() % MekanismUtils.TICKS_PER_HALF_SECOND == 0) {
                        player.gameEvent(MekanismGameEvents.JETPACK_BURN);
                    }
                }
            }
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (isScubaMaskOn(player, chest)) {
            ItemScubaTank tank = (ItemScubaTank) chest.getItem();
            final int max = player.getMaxAirSupply();
            tank.useChemical(chest, 1);
            ChemicalStack received = tank.useChemical(chest, max - player.getAirSupply());
            if (!received.isEmpty()) {
                player.setAirSupply(player.getAirSupply() + (int) received.getAmount());
            }
            if (player.getAirSupply() == max) {
                for (MobEffectInstance effect : player.getActiveEffects()) {
                    if (MekanismUtils.shouldSpeedUpEffect(effect)) {
                        for (int i = 0; i < 9; i++) {
                            MekanismUtils.speedUpEffectSafely(player, effect);
                        }
                    }
                }
            }
        }
    }

    public static boolean isGravitationalModulationOn(Player player) {
        if (ModuleGravitationalModulatingUnit.shouldProcess(player)) {
            ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
            IModule<ModuleGravitationalModulatingUnit> module = IModuleHelper.INSTANCE.getIfEnabled(stack, MekanismModules.GRAVITATIONAL_MODULATING_UNIT);
            return module != null && module.hasEnoughEnergy(stack, MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation);
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingDeath(LivingDeathEvent event) {
        if (!event.getEntity().isOnFire() && !event.getEntity().fireImmune() && MekanismDamageTypes.FLAMETHROWER.is(event.getSource())) {
            //If they took damage from a flamethrower, set that they are on fire so that they drop cooked food
            event.getEntity().igniteForSeconds(1);
        }
    }

    @SubscribeEvent
    public void checkEntityInvulnerability(EntityInvulnerabilityCheckEvent event) {
        if (!event.isInvulnerable() && event.getEntity() instanceof LivingEntity entity) {
            if (MekanismDamageTypes.RADIATION.is(event.getSource())) {
                //Note: As we only enter this block if it isn't invulnerable, there is no chance that this call makes it go from invulnerable to not
                event.setInvulnerable(entity.getType().is(MekanismAPITags.Entities.MEK_RADIATION_IMMUNE));
            }
        }
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageContainer damageContainer = event.getContainer();
        float damage = damageContainer.getNewDamage();
        if (damage <= 0 || !entity.isAlive()) {
            //If some mod does weird things and causes the damage value to be negative or zero then exit
            // as our logic assumes there is actually damage happening and can crash if someone tries to
            // use a negative number as the damage value. We also check to make sure that we don't do
            // anything if the entity is dead as living attack is still fired when the entity is dead
            // for things like fall damage if the entity dies before hitting the ground, and then energy
            // would be depleted regardless if keep inventory is on even if no damage was stopped as the
            // entity can't take damage while dead
            return;
        }
        DamageSource source = damageContainer.getSource();
        //Gas Mask checks
        if (source.is(MekanismAPITags.DamageTypes.IS_PREVENTABLE_MAGIC)) {
            ItemStack headStack = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (!headStack.isEmpty() && headStack.getItem() instanceof ItemScubaMask) {
                ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
                if (!chestStack.isEmpty() && chestStack.getItem() instanceof ItemScubaTank tank && tank.getMode(chestStack) && ChemicalUtil.hasAnyChemical(chestStack)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        if (entity instanceof Player player) {
            //TODO - 1.21: Should we rewrite this to try and take advantage of the new reduction system? It would be kind of nice to move this to the
            // spot that reduction from armor happens. Though then the base armor reduction will apply before our energy based reduction
            // Is that fine? Maybe it is better, or maybe it is worse from a balance standpoint
            float ratioAbsorbed = ItemMekaSuitArmor.getDamageAbsorbed(player, damageContainer.getSource(), damage);
            if (ratioAbsorbed > 0) {
                //TODO - 1.21: What should we set this to, and how does it behave if we also cancel the event
                //damageContainer.setPostAttackInvulnerabilityTicks();
                float damageRemaining = damage * Math.max(0, 1 - ratioAbsorbed);
                if (damageRemaining <= 0) {
                    event.setCanceled(true);
                } else {
                    damageContainer.setNewDamage(damageRemaining);
                }
            }
        }
    }

    /**
     * Based on the values and calculations that happen in {@link LivingEntity#calculateFallDamage(float, float)}
     */
    @SubscribeEvent
    public void livingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        float safeFallDistance = (float) entity.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
        float fallDistance = Math.max(event.getDistance() - safeFallDistance, 0);
        if (fallDistance <= Mth.EPSILON) {
            return;
        }
        double damageMultiplier = event.getDamageMultiplier() * entity.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER);
        int fallDamage = Mth.ceil(fallDistance * damageMultiplier);
        if (fallDamage <= 0) {//This may be the case for things like slime blocks that have a damage multiplier of zero
            return;
        }
        FallEnergyInfo info = getFallAbsorptionEnergyInfo(entity);
        if (info != null && info.container != null) {
            float absorption = info.damageRatio.getAsFloat();
            float amount = fallDamage * absorption;
            long energyRequirement = MathUtils.ceilToLong(info.energyCost.getAsLong() * amount);
            float ratioAbsorbed;
            if (energyRequirement == 0L) {
                //No energy is actually needed to absorb the damage, either because of the config
                // or how small the amount to absorb is
                ratioAbsorbed = absorption;
            } else {
                long extracted = info.container.extract(energyRequirement, Action.EXECUTE, AutomationType.MANUAL);
                ratioAbsorbed = (float) (absorption * ((double) extracted / energyRequirement));
            }
            if (ratioAbsorbed > 0) {
                float damageRemaining = fallDamage * Math.max(0, 1 - ratioAbsorbed);
                if (damageRemaining <= Mth.EPSILON) {
                    event.setCanceled(true);
                    BlockPos posOn = entity.getOnPos();
                    BlockState stateOn = entity.level().getBlockState(posOn);
                    if (entity instanceof Player player) {
                        player.playStepSound(posOn, stateOn);
                    } else {
                        //Fallback to default implementation
                        SoundType soundtype = stateOn.getSoundType(entity.level(), posOn, entity);
                        entity.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
                    }
                } else {
                    float distanceRemaining = (float) (damageRemaining / damageMultiplier);
                    event.setDistance(distanceRemaining + safeFallDistance);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingJump(LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            IModule<ModuleHydraulicPropulsionUnit> propulsionModule = IModuleHelper.INSTANCE.getIfEnabled(boots, MekanismModules.HYDRAULIC_PROPULSION_UNIT);
            if (propulsionModule != null && Mekanism.keyMap.has(player.getUUID(), KeySync.BOOST)) {
                float boost = propulsionModule.getCustomInstance().getBoost();
                long usage = MathUtils.ceilToLong(MekanismConfig.gear.mekaSuitBaseJumpEnergyUsage.get() * boost / 0.1F);
                if (propulsionModule.canUseEnergy(player, boots, usage)) {
                    // if we're sprinting with the boost module, limit the height
                    ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
                    IModule<ModuleLocomotiveBoostingUnit> boostModule = IModuleHelper.INSTANCE.getIfEnabled(legs, MekanismModules.LOCOMOTIVE_BOOSTING_UNIT);
                    if (boostModule != null && boostModule.getCustomInstance().canFunction(boostModule, legs, player)) {
                        boost = Mth.sqrt(boost);
                    }
                    player.addDeltaMovement(new Vec3(0, boost, 0));
                    propulsionModule.useEnergy(player, legs, usage, true);
                }
            }
        }
    }

    /**
     * @return null if free runners are not being worn, or they don't have an energy container for some reason
     */
    @Nullable
    private FallEnergyInfo getFallAbsorptionEnergyInfo(LivingEntity base) {
        ItemStack feetStack = base.getItemBySlot(EquipmentSlot.FEET);
        if (!feetStack.isEmpty() && feetStack.getItem() instanceof ItemMekaSuitArmor) {
            return new FallEnergyInfo(StorageUtils.getEnergyContainer(feetStack, 0), MekanismConfig.gear.mekaSuitFallDamageRatio,
                  MekanismConfig.gear.mekaSuitEnergyUsageFall);
        }
        ItemStack freeRunners = IFreeRunnerItem.getActiveFreeRunners(base);
        if (!freeRunners.isEmpty()) {
            ItemStack primaryFreeRunners = IFreeRunnerItem.getPrimaryFreeRunners(base);
            if (!primaryFreeRunners.isEmpty() && ((IFreeRunnerItem) primaryFreeRunners.getItem()).getFreeRunnerMode(primaryFreeRunners).preventsFallDamage()) {
                return new FallEnergyInfo(((IFreeRunnerItem) freeRunners.getItem()).getRunnerEnergyContainer(freeRunners), MekanismConfig.gear.freeRunnerFallDamageRatio,
                      MekanismConfig.gear.freeRunnerFallEnergyCost);
            }
        }
        return null;
    }

    private record FallEnergyInfo(@Nullable IEnergyContainer container, FloatSupplier damageRatio, LongSupplier energyCost) {
    }

    @SubscribeEvent
    public void getBreakSpeed(BreakSpeed event) {
        Player player = event.getEntity();
        float speed = event.getNewSpeed();

        Optional<BlockPos> position = event.getPosition();
        if (position.isPresent()) {
            BlockPos pos = position.get();
            // Blasting item speed check
            ItemStack mainHand = player.getMainHandItem();
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof IBlastingItem tool) {
                Map<BlockPos, BlockState> blocks = tool.getBlastedBlocks(player.level(), player, mainHand, pos, event.getState());
                if (!blocks.isEmpty()) {
                    // Scales mining speed based on hardest block
                    // Does not take into account the tool check for those blocks or other mining speed changes that don't apply to the target block.
                    float targetHardness = event.getState().getDestroySpeed(player.level(), pos);
                    float maxHardness = targetHardness;
                    for (Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                        float destroySpeed = entry.getValue().getDestroySpeed(player.level(), entry.getKey());
                        maxHardness = Math.max(maxHardness, destroySpeed);
                    }
                    speed *= (targetHardness / maxHardness);
                }
            }
        }

        //Gyroscopic stabilization check
        if (!player.onGround() && IModuleHelper.INSTANCE.isEnabled(player.getItemBySlot(EquipmentSlot.LEGS), MekanismModules.GYROSCOPIC_STABILIZATION_UNIT)) {
            speed *= 5.0F;
        }

        event.setNewSpeed(speed);
    }
}
