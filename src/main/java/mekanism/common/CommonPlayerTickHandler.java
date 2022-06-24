package mekanism.common;

import java.util.Map;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.FloatSupplier;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.base.KeySync;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IBlastingItem;
import mekanism.common.content.gear.mekasuit.ModuleGravitationalModulatingUnit;
import mekanism.common.content.gear.mekasuit.ModuleHydraulicPropulsionUnit;
import mekanism.common.content.gear.mekasuit.ModuleHydrostaticRepulsorUnit;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemScubaMask;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonPlayerTickHandler {

    public static boolean isOnGroundOrSleeping(Player player) {
        return player.isOnGround() || player.isSleeping();
    }

    public static boolean isScubaMaskOn(Player player, ItemStack tank) {
        ItemStack mask = player.getItemBySlot(EquipmentSlot.HEAD);
        return !tank.isEmpty() && !mask.isEmpty() && tank.getItem() instanceof ItemScubaTank scubaTank &&
               mask.getItem() instanceof ItemScubaMask && ChemicalUtil.hasGas(tank) && scubaTank.getFlowing(tank);
    }

    private static boolean isFlamethrowerOn(Player player, ItemStack currentItem) {
        return Mekanism.playerState.isFlamethrowerOn(player) && !currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower;
    }

    public static float getStepBoost(Player player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.FEET);
        if (!stack.isEmpty() && !player.isShiftKeyDown()) {
            if (stack.getItem() instanceof ItemFreeRunners freeRunners && freeRunners.getMode(stack) == FreeRunnerMode.NORMAL) {
                return 0.5F;
            }
            IModule<ModuleHydraulicPropulsionUnit> module = MekanismAPI.getModuleHelper().load(stack, MekanismModules.HYDRAULIC_PROPULSION_UNIT);
            if (module != null && module.isEnabled()) {
                return module.getCustomInstance().getStepHeight();
            }
        }
        return 0;
    }

    public static float getSwimBoost(Player player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!stack.isEmpty()) {
            IModule<ModuleHydrostaticRepulsorUnit> module = MekanismAPI.getModuleHelper().load(stack, MekanismModules.HYDROSTATIC_REPULSOR_UNIT);
            if (module != null && module.isEnabled() && module.getCustomInstance().isSwimBoost(module)) {
                return 1F;
            }
        }
        return 0;
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent event) {
        if (event.phase == Phase.END && event.side.isServer()) {
            tickEnd(event.player);
        }
    }

    private void tickEnd(Player player) {
        Mekanism.playerState.updateStepAssist(player);
        Mekanism.playerState.updateSwimBoost(player);
        if (player instanceof ServerPlayer serverPlayer) {
            RadiationManager.INSTANCE.tickServer(serverPlayer);
        }

        ItemStack currentItem = player.getInventory().getSelected();
        if (isFlamethrowerOn(player, currentItem)) {
            EntityFlame flame = EntityFlame.create(player);
            if (flame != null) {
                if (flame.isAlive()) {
                    //If the flame is alive (and didn't just instantly hit a block while trying to spawn add it to the world)
                    player.level.addFreshEntity(flame);
                }
                if (MekanismUtils.isPlayingMode(player)) {
                    ((ItemFlamethrower) currentItem.getItem()).useGas(currentItem, 1);
                }
            }
        }

        ItemStack jetpack = IJetpackItem.getActiveJetpack(player);
        if (!jetpack.isEmpty()) {
            ItemStack primaryJetpack = IJetpackItem.getPrimaryJetpack(player);
            if (!primaryJetpack.isEmpty()) {
                JetpackMode primaryMode = ((IJetpackItem) primaryJetpack.getItem()).getJetpackMode(primaryJetpack);
                JetpackMode mode = IJetpackItem.getPlayerJetpackMode(player, primaryMode, () -> Mekanism.keyMap.has(player.getUUID(), KeySync.ASCEND));
                if (mode != JetpackMode.DISABLED) {
                    if (IJetpackItem.handleJetpackMotion(player, mode, () -> Mekanism.keyMap.has(player.getUUID(), KeySync.ASCEND))) {
                        player.fallDistance = 0.0F;
                        if (player instanceof ServerPlayer serverPlayer) {
                            serverPlayer.connection.aboveGroundTickCount = 0;
                        }
                    }
                    ((IJetpackItem) jetpack.getItem()).useJetpackFuel(jetpack);
                }
            }
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (isScubaMaskOn(player, chest)) {
            ItemScubaTank tank = (ItemScubaTank) chest.getItem();
            final int max = player.getMaxAirSupply();
            tank.useGas(chest, 1);
            GasStack received = tank.useGas(chest, max - player.getAirSupply());
            if (!received.isEmpty()) {
                player.setAirSupply(player.getAirSupply() + (int) received.getAmount());
            }
            if (player.getAirSupply() == max) {
                for (MobEffectInstance effect : player.getActiveEffects()) {
                    for (int i = 0; i < 9; i++) {
                        MekanismUtils.speedUpEffectSafely(player, effect);
                    }
                }
            }
        }

        Mekanism.playerState.updateFlightInfo(player);
    }

    public static boolean isGravitationalModulationReady(Player player) {
        IModule<ModuleGravitationalModulatingUnit> module = MekanismAPI.getModuleHelper().load(player.getItemBySlot(EquipmentSlot.CHEST), MekanismModules.GRAVITATIONAL_MODULATING_UNIT);
        FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get();
        return MekanismUtils.isPlayingMode(player) && module != null && module.isEnabled() && module.getContainerEnergy().greaterOrEqual(usage);
    }

    public static boolean isGravitationalModulationOn(Player player) {
        return isGravitationalModulationReady(player) && player.getAbilities().flying;
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (event.getAmount() <= 0 || !entity.isAlive()) {
            //If some mod does weird things and causes the damage value to be negative or zero then exit
            // as our logic assumes there is actually damage happening and can crash if someone tries to
            // use a negative number as the damage value. We also check to make sure that we don't do
            // anything if the entity is dead as living attack is still fired when the entity is dead
            // for things like fall damage if the entity dies before hitting the ground, and then energy
            // would be depleted regardless if keep inventory is on even if no damage was stopped as the
            // entity can't take damage while dead
            return;
        }
        //Gas Mask checks
        if (event.getSource().isMagic()) {
            ItemStack headStack = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (!headStack.isEmpty() && headStack.getItem() instanceof ItemScubaMask) {
                ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
                if (!chestStack.isEmpty() && chestStack.getItem() instanceof ItemScubaTank tank && tank.getFlowing(chestStack) && ChemicalUtil.hasGas(chestStack)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        //Note: We have this here in addition to listening to LivingHurt, so as if we can fully block the damage
        // then we don't play the hurt effect/sound, as cancelling LivingHurtEvent still causes that to happen
        if (event.getSource().isFall()) {
            //Free runner checks
            FallEnergyInfo info = getFallAbsorptionEnergyInfo(entity);
            if (info != null && tryAbsorbAll(event, info.container, info.damageRatio, info.energyCost)) {
                return;
            }
        }
        if (entity instanceof Player player) {
            if (ItemMekaSuitArmor.tryAbsorbAll(player, event.getSource(), event.getAmount())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (event.getAmount() <= 0 || !entity.isAlive()) {
            //If some mod does weird things and causes the damage value to be negative or zero then exit
            // as our logic assumes there is actually damage happening and can crash if someone tries to
            // use a negative number as the damage value. We also check to make sure that we don't do
            // anything if the entity is dead as living attack is still fired when the entity is dead
            // for things like fall damage if the entity dies before hitting the ground, and then energy
            // would be depleted regardless if keep inventory is on even if no damage was stopped as the
            // entity can't take damage while dead. While living hurt is not fired, we catch this case
            // just in case anyway because it is a simple boolean check and there is no guarantee that
            // other mods may not be firing the event manually even when the entity is dead
            return;
        }
        if (event.getSource().isFall()) {
            FallEnergyInfo info = getFallAbsorptionEnergyInfo(entity);
            if (info != null && handleDamage(event, info.container, info.damageRatio, info.energyCost)) {
                return;
            }
        }
        if (entity instanceof Player player) {
            float ratioAbsorbed = ItemMekaSuitArmor.getDamageAbsorbed(player, event.getSource(), event.getAmount());
            if (ratioAbsorbed > 0) {
                float damageRemaining = event.getAmount() * Math.max(0, 1 - ratioAbsorbed);
                if (damageRemaining <= 0) {
                    event.setCanceled(true);
                } else {
                    event.setAmount(damageRemaining);
                }
            }
        }
    }

    private boolean tryAbsorbAll(LivingAttackEvent event, @Nullable IEnergyContainer energyContainer, FloatSupplier absorptionRatio, FloatingLongSupplier energyCost) {
        if (energyContainer != null && absorptionRatio.getAsFloat() == 1) {
            FloatingLong energyRequirement = energyCost.get().multiply(event.getAmount());
            if (energyRequirement.isZero()) {
                //No energy is actually needed to absorb the damage, either because of the config
                // or how small the amount to absorb is
                event.setCanceled(true);
                return true;
            }
            FloatingLong simulatedExtract = energyContainer.extract(energyRequirement, Action.SIMULATE, AutomationType.MANUAL);
            if (simulatedExtract.equals(energyRequirement)) {
                //If we could fully negate the damage cancel the event and extract it
                energyContainer.extract(energyRequirement, Action.EXECUTE, AutomationType.MANUAL);
                event.setCanceled(true);
                return true;
            }
        }
        return false;
    }

    private boolean handleDamage(LivingHurtEvent event, @Nullable IEnergyContainer energyContainer, FloatSupplier absorptionRatio, FloatingLongSupplier energyCost) {
        if (energyContainer != null) {
            float absorption = absorptionRatio.getAsFloat();
            float amount = event.getAmount() * absorption;
            FloatingLong energyRequirement = energyCost.get().multiply(amount);
            float ratioAbsorbed;
            if (energyRequirement.isZero()) {
                //No energy is actually needed to absorb the damage, either because of the config
                // or how small the amount to absorb is
                ratioAbsorbed = absorption;
            } else {
                ratioAbsorbed = absorption * energyContainer.extract(energyRequirement, Action.EXECUTE, AutomationType.MANUAL).divide(amount).floatValue();
            }
            if (ratioAbsorbed > 0) {
                float damageRemaining = event.getAmount() * Math.max(0, 1 - ratioAbsorbed);
                if (damageRemaining <= 0) {
                    event.setCanceled(true);
                    return true;
                } else {
                    event.setAmount(damageRemaining);
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onLivingJump(LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof Player player) {
            IModule<ModuleHydraulicPropulsionUnit> module = MekanismAPI.getModuleHelper().load(player.getItemBySlot(EquipmentSlot.FEET), MekanismModules.HYDRAULIC_PROPULSION_UNIT);
            if (module != null && module.isEnabled() && Mekanism.keyMap.has(player.getUUID(), KeySync.BOOST)) {
                float boost = module.getCustomInstance().getBoost();
                FloatingLong usage = MekanismConfig.gear.mekaSuitBaseJumpEnergyUsage.get().multiply(boost / 0.1F);
                IEnergyContainer energyContainer = module.getEnergyContainer();
                if (module.canUseEnergy(player, energyContainer, usage, false)) {
                    // if we're sprinting with the boost module, limit the height
                    IModule<ModuleLocomotiveBoostingUnit> boostModule = MekanismAPI.getModuleHelper().load(player.getItemBySlot(EquipmentSlot.LEGS), MekanismModules.LOCOMOTIVE_BOOSTING_UNIT);
                    if (boostModule != null && boostModule.isEnabled() && boostModule.getCustomInstance().canFunction(boostModule, player)) {
                        boost = (float) Math.sqrt(boost);
                    }
                    player.setDeltaMovement(player.getDeltaMovement().add(0, boost, 0));
                    module.useEnergy(player, energyContainer, usage, true);
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
        if (!feetStack.isEmpty()) {
            if (feetStack.getItem() instanceof ItemFreeRunners boots) {
                if (boots.getMode(feetStack) == FreeRunnerMode.NORMAL) {
                    return new FallEnergyInfo(StorageUtils.getEnergyContainer(feetStack, 0), MekanismConfig.gear.freeRunnerFallDamageRatio,
                          MekanismConfig.gear.freeRunnerFallEnergyCost);
                }
            } else if (feetStack.getItem() instanceof ItemMekaSuitArmor) {
                return new FallEnergyInfo(StorageUtils.getEnergyContainer(feetStack, 0), MekanismConfig.gear.mekaSuitFallDamageRatio,
                      MekanismConfig.gear.mekaSuitEnergyUsageFall);
            }
        }
        return null;
    }

    private record FallEnergyInfo(@Nullable IEnergyContainer container, FloatSupplier damageRatio, FloatingLongSupplier energyCost) {
    }

    @SubscribeEvent
    public void getBreakSpeed(BreakSpeed event) {
        Player player = event.getPlayer();
        float speed = event.getNewSpeed();

        // Blasting item speed check
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof IBlastingItem tool) {
            Map<BlockPos, BlockState> blocks = tool.getBlastedBlocks(player.level, player, mainHand, event.getPos(), event.getState());
            if (!blocks.isEmpty()) {
                // Scales mining speed based on hardest block
                // Does not take into account the tool check for those blocks or other mining speed changes that don't apply to the target block.
                float targetHardness = event.getState().getDestroySpeed(player.level, event.getPos());
                float maxHardness = blocks.entrySet().stream()
                      .map(entry -> entry.getValue().getDestroySpeed(player.level, entry.getKey()))
                      .reduce(targetHardness, Float::max);
                speed *= (targetHardness / maxHardness);
            }
        }

        //Gyroscopic stabilization check
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!legs.isEmpty() && MekanismAPI.getModuleHelper().isEnabled(legs, MekanismModules.GYROSCOPIC_STABILIZATION_UNIT)) {
            if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
                speed *= 5.0F;
            }

            if (!player.isOnGround()) {
                speed *= 5.0F;
            }
        }

        event.setNewSpeed(speed);
    }
}
