package mekanism.common;

import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.FloatSupplier;
import mekanism.api.gear.IModule;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.base.KeySync;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.mekasuit.ModuleGravitationalModulatingUnit;
import mekanism.common.content.gear.mekasuit.ModuleHydraulicPropulsionUnit;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemScubaMask;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonPlayerTickHandler {

    public static boolean isOnGroundOrSleeping(PlayerEntity player) {
        if (player.isSleeping()) {
            return true;
        }
        int x = MathHelper.floor(player.getX());
        int y = MathHelper.floor(player.getY() - 0.01);
        int z = MathHelper.floor(player.getZ());
        BlockPos pos = new BlockPos(x, y, z);
        BlockState s = player.level.getBlockState(pos);
        VoxelShape shape = s.getShape(player.level, pos);
        if (shape.isEmpty()) {
            return false;
        }
        AxisAlignedBB playerBox = player.getBoundingBox();
        return !s.isAir(player.level, pos) && playerBox.move(0, -0.01, 0).intersects(shape.bounds().move(pos));
    }

    public static boolean isScubaMaskOn(PlayerEntity player, ItemStack tank) {
        ItemStack mask = player.getItemBySlot(EquipmentSlotType.HEAD);
        return !tank.isEmpty() && !mask.isEmpty() && tank.getItem() instanceof ItemScubaTank && mask.getItem() instanceof ItemScubaMask && ChemicalUtil.hasGas(tank) &&
               ((ItemScubaTank) tank.getItem()).getFlowing(tank);
    }

    private static boolean isFlamethrowerOn(PlayerEntity player, ItemStack currentItem) {
        return Mekanism.playerState.isFlamethrowerOn(player) && !currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower;
    }

    public static float getStepBoost(PlayerEntity player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlotType.FEET);
        if (!stack.isEmpty() && !player.isShiftKeyDown()) {
            if (stack.getItem() instanceof ItemFreeRunners) {
                ItemFreeRunners freeRunners = (ItemFreeRunners) stack.getItem();
                if (freeRunners.getMode(stack) == ItemFreeRunners.FreeRunnerMode.NORMAL) {
                    return 0.5F;
                }
            }
            IModule<ModuleHydraulicPropulsionUnit> module = MekanismAPI.getModuleHelper().load(stack, MekanismModules.HYDRAULIC_PROPULSION_UNIT);
            if (module != null && module.isEnabled()) {
                return module.getCustomInstance().getStepHeight();
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

    private void tickEnd(PlayerEntity player) {
        Mekanism.playerState.updateStepAssist(player);
        if (player instanceof ServerPlayerEntity) {
            RadiationManager.INSTANCE.tickServer((ServerPlayerEntity) player);
        }

        ItemStack currentItem = player.inventory.getSelected();
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

        ItemStack chest = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (isJetpackOn(player, chest)) {
            JetpackMode mode = getJetpackMode(chest);
            if (handleJetpackMotion(player, mode, () -> Mekanism.keyMap.has(player.getUUID(), KeySync.ASCEND))) {
                player.fallDistance = 0.0F;
                if (player instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) player).connection.aboveGroundTickCount = 0;
                }
            }
            if (chest.getItem() instanceof ItemJetpack) {
                ((ItemJetpack) chest.getItem()).useGas(chest, 1);
            } else {
                ((ItemMekaSuitArmor) chest.getItem()).useGas(chest, MekanismGases.HYDROGEN.get(), 1);
            }
        }

        if (isScubaMaskOn(player, chest)) {
            ItemScubaTank tank = (ItemScubaTank) chest.getItem();
            final int max = player.getMaxAirSupply();
            tank.useGas(chest, 1);
            GasStack received = tank.useGas(chest, max - player.getAirSupply());
            if (!received.isEmpty()) {
                player.setAirSupply(player.getAirSupply() + (int) received.getAmount());
            }
            if (player.getAirSupply() == max) {
                for (EffectInstance effect : player.getActiveEffects()) {
                    for (int i = 0; i < 9; i++) {
                        MekanismUtils.speedUpEffectSafely(player, effect);
                    }
                }
            }
        }

        Mekanism.playerState.updateFlightInfo(player);
    }

    /**
     * @return If fall distance should get reset or not
     */
    public static boolean handleJetpackMotion(PlayerEntity player, JetpackMode mode, BooleanSupplier ascendingSupplier) {
        Vector3d motion = player.getDeltaMovement();
        if (mode == JetpackMode.NORMAL) {
            if (player.isFallFlying()) {
                Vector3d lookAngle = player.getLookAngle();
                Vector3d normalizedLook = lookAngle.normalize();
                double d1x = normalizedLook.x * 0.15;
                double d1y = normalizedLook.y * 0.15;
                double d1z = normalizedLook.z * 0.15;
                player.setDeltaMovement(motion.add(lookAngle.x * d1x + (lookAngle.x * 1.5 - motion.x) * 0.5,
                      lookAngle.y * d1y + (lookAngle.y * 1.5 - motion.y) * 0.5,
                      lookAngle.z * d1z + (lookAngle.z * 1.5 - motion.z) * 0.5));
                return false;
            } else {
                player.setDeltaMovement(motion.x(), Math.min(motion.y() + 0.15D, 0.5D), motion.z());
            }
        } else if (mode == JetpackMode.HOVER) {
            boolean ascending = ascendingSupplier.getAsBoolean();
            boolean descending = player.isDescending();
            if ((!ascending && !descending) || (ascending && descending)) {
                if (motion.y() > 0) {
                    player.setDeltaMovement(motion.x(), Math.max(motion.y() - 0.15D, 0), motion.z());
                } else if (motion.y() < 0) {
                    if (!isOnGroundOrSleeping(player)) {
                        player.setDeltaMovement(motion.x(), Math.min(motion.y() + 0.15D, 0), motion.z());
                    }
                }
            } else if (ascending) {
                player.setDeltaMovement(motion.x(), Math.min(motion.y() + 0.15D, 0.2D), motion.z());
            } else if (!isOnGroundOrSleeping(player)) {
                player.setDeltaMovement(motion.x(), Math.max(motion.y() - 0.15D, -0.2D), motion.z());
            }
        }
        return true;
    }

    private static boolean isJetpackOn(PlayerEntity player, ItemStack chest) {
        if (!chest.isEmpty() && !player.isSpectator()) {
            JetpackMode mode = getJetpackMode(chest);
            if (mode == JetpackMode.NORMAL) {
                return Mekanism.keyMap.has(player.getUUID(), KeySync.ASCEND);
            } else if (mode == JetpackMode.HOVER) {
                boolean ascending = Mekanism.keyMap.has(player.getUUID(), KeySync.ASCEND);
                boolean descending = player.isDescending();
                if (!ascending || descending) {
                    return !isOnGroundOrSleeping(player);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isGravitationalModulationReady(PlayerEntity player) {
        IModule<ModuleGravitationalModulatingUnit> module = MekanismAPI.getModuleHelper().load(player.getItemBySlot(EquipmentSlotType.CHEST), MekanismModules.GRAVITATIONAL_MODULATING_UNIT);
        FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get();
        return MekanismUtils.isPlayingMode(player) && module != null && module.isEnabled() && module.getContainerEnergy().greaterOrEqual(usage);
    }

    public static boolean isGravitationalModulationOn(PlayerEntity player) {
        return isGravitationalModulationReady(player) && player.abilities.flying;
    }

    /** Will return null if jetpack mode is not active */
    public static JetpackMode getJetpackMode(ItemStack stack) {
        if (stack.getItem() instanceof ItemJetpack && ChemicalUtil.hasGas(stack)) {
            return ((ItemJetpack) stack.getItem()).getMode(stack);
        } else if (stack.getItem() instanceof IModuleContainerItem && ChemicalUtil.hasChemical(stack, MekanismGases.HYDROGEN.get())) {
            IModule<ModuleJetpackUnit> module = MekanismAPI.getModuleHelper().load(stack, MekanismModules.JETPACK_UNIT);
            if (module != null && module.isEnabled()) {
                return module.getCustomInstance().getMode();
            }
        }
        return JetpackMode.DISABLED;
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
            ItemStack headStack = entity.getItemBySlot(EquipmentSlotType.HEAD);
            if (!headStack.isEmpty() && headStack.getItem() instanceof ItemScubaMask) {
                ItemStack chestStack = entity.getItemBySlot(EquipmentSlotType.CHEST);
                if (!chestStack.isEmpty() && chestStack.getItem() instanceof ItemScubaTank && ((ItemScubaTank) chestStack.getItem()).getFlowing(chestStack) &&
                    ChemicalUtil.hasGas(chestStack)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        //Note: We have this here in addition to listening to LivingHurt, so as if we can fully block the damage
        // then we don't play the hurt effect/sound, as cancelling LivingHurtEvent still causes that to happen
        if (event.getSource() == DamageSource.FALL) {
            //Free runner checks
            FallEnergyInfo info = getFallAbsorptionEnergyInfo(entity);
            if (info != null && tryAbsorbAll(event, info.container, info.damageRatio, info.energyCost)) {
                return;
            }
        }
        if (entity instanceof PlayerEntity) {
            if (ItemMekaSuitArmor.tryAbsorbAll((PlayerEntity) entity, event.getSource(), event.getAmount())) {
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
        if (event.getSource() == DamageSource.FALL) {
            FallEnergyInfo info = getFallAbsorptionEnergyInfo(entity);
            if (info != null && handleDamage(event, info.container, info.damageRatio, info.energyCost)) {
                return;
            }
        }
        if (entity instanceof PlayerEntity) {
            float ratioAbsorbed = ItemMekaSuitArmor.getDamageAbsorbed((PlayerEntity) entity, event.getSource(), event.getAmount());
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
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            IModule<ModuleHydraulicPropulsionUnit> module = MekanismAPI.getModuleHelper().load(player.getItemBySlot(EquipmentSlotType.FEET), MekanismModules.HYDRAULIC_PROPULSION_UNIT);
            if (module != null && module.isEnabled() && Mekanism.keyMap.has(player.getUUID(), KeySync.BOOST)) {
                float boost = module.getCustomInstance().getBoost();
                FloatingLong usage = MekanismConfig.gear.mekaSuitBaseJumpEnergyUsage.get().multiply(boost / 0.1F);
                IEnergyContainer energyContainer = module.getEnergyContainer();
                if (module.canUseEnergy(player, energyContainer, usage, false)) {
                    // if we're sprinting with the boost module, limit the height
                    IModule<ModuleLocomotiveBoostingUnit> boostModule = MekanismAPI.getModuleHelper().load(player.getItemBySlot(EquipmentSlotType.LEGS), MekanismModules.LOCOMOTIVE_BOOSTING_UNIT);
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
        ItemStack feetStack = base.getItemBySlot(EquipmentSlotType.FEET);
        if (!feetStack.isEmpty()) {
            if (feetStack.getItem() instanceof ItemFreeRunners) {
                ItemFreeRunners boots = (ItemFreeRunners) feetStack.getItem();
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

    private static class FallEnergyInfo {

        @Nullable
        private final IEnergyContainer container;
        private final FloatSupplier damageRatio;
        private final FloatingLongSupplier energyCost;

        public FallEnergyInfo(@Nullable IEnergyContainer container, FloatSupplier damageRatio, FloatingLongSupplier energyCost) {
            this.container = container;
            this.damageRatio = damageRatio;
            this.energyCost = energyCost;
        }
    }
}