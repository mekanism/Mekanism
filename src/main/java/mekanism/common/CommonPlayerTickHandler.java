package mekanism.common;

import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.base.KeySync;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleGravitationalModulatingUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleHydraulicPropulsionUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleInhalationPurificationUnit;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemScubaMask;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.registries.MekanismGases;
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
        int x = MathHelper.floor(player.getPosX());
        int y = MathHelper.floor(player.getPosY() - 0.01);
        int z = MathHelper.floor(player.getPosZ());
        BlockPos pos = new BlockPos(x, y, z);
        BlockState s = player.world.getBlockState(pos);
        VoxelShape shape = s.getShape(player.world, pos);
        if (shape.isEmpty()) {
            return false;
        }
        AxisAlignedBB playerBox = player.getBoundingBox();
        return !s.isAir(player.world, pos) && playerBox.offset(0, -0.01, 0).intersects(shape.getBoundingBox().offset(pos));

    }

    public static boolean isScubaMaskOn(PlayerEntity player, ItemStack tank) {
        ItemStack mask = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        return !tank.isEmpty() && !mask.isEmpty() && tank.getItem() instanceof ItemScubaTank && mask.getItem() instanceof ItemScubaMask && ChemicalUtil.hasGas(tank) &&
               ((ItemScubaTank) tank.getItem()).getFlowing(tank);
    }

    private static boolean isFlamethrowerOn(PlayerEntity player, ItemStack currentItem) {
        return Mekanism.playerState.isFlamethrowerOn(player) && !currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower;
    }

    public static float getStepBoost(PlayerEntity player) {
        ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.FEET);
        if (!stack.isEmpty() && !player.isSneaking()) {
            if (stack.getItem() instanceof ItemFreeRunners) {
                ItemFreeRunners freeRunners = (ItemFreeRunners) stack.getItem();
                if (freeRunners.getMode(stack) == ItemFreeRunners.FreeRunnerMode.NORMAL) {
                    return 0.5F;
                }
            }
            ModuleHydraulicPropulsionUnit module = Modules.load(stack, Modules.HYDRAULIC_PROPULSION_UNIT);
            if (module != null && module.isEnabled()) {
                return module.getStepHeight();
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
            Mekanism.radiationManager.tickServer((ServerPlayerEntity) player);
        }

        ItemStack currentItem = player.inventory.getCurrentItem();
        if (isFlamethrowerOn(player, currentItem)) {
            player.world.addEntity(new EntityFlame(player));
            if (MekanismUtils.isPlayingMode(player)) {
                ((ItemFlamethrower) currentItem.getItem()).useGas(currentItem, 1);
            }
        }

        ItemStack chest = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
        if (isJetpackOn(player, chest)) {
            JetpackMode mode = getJetpackMode(chest);
            Vector3d motion = player.getMotion();
            if (mode == JetpackMode.NORMAL) {
                player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0.5D), motion.getZ());
            } else if (mode == JetpackMode.HOVER) {
                boolean ascending = Mekanism.keyMap.has(player.getUniqueID(), KeySync.ASCEND);
                boolean descending = Mekanism.keyMap.has(player.getUniqueID(), KeySync.DESCEND);
                if ((!ascending && !descending) || (ascending && descending)) {
                    if (motion.getY() > 0) {
                        player.setMotion(motion.getX(), Math.max(motion.getY() - 0.15D, 0), motion.getZ());
                    } else if (motion.getY() < 0) {
                        if (!isOnGroundOrSleeping(player)) {
                            player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0), motion.getZ());
                        }
                    }
                } else if (ascending) {
                    player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0.2D), motion.getZ());
                } else if (!isOnGroundOrSleeping(player)) {
                    player.setMotion(motion.getX(), Math.max(motion.getY() - 0.15D, -0.2D), motion.getZ());
                }
            }
            player.fallDistance = 0.0F;
            if (player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) player).connection.floatingTickCount = 0;
            }
            if (chest.getItem() instanceof ItemJetpack) {
                ((ItemJetpack) chest.getItem()).useGas(chest, 1);
            } else {
                ((ItemMekaSuitArmor) chest.getItem()).useGas(chest, MekanismGases.HYDROGEN.get(), 1);
            }
        }

        if (isScubaMaskOn(player, chest)) {
            ItemScubaTank tank = (ItemScubaTank) chest.getItem();
            final int max = 300;
            tank.useGas(chest, 1);
            GasStack received = tank.useGas(chest, max - player.getAir());
            if (!received.isEmpty()) {
                player.setAir(player.getAir() + (int) received.getAmount());
            }
            if (player.getAir() == max) {
                for (EffectInstance effect : player.getActivePotionEffects()) {
                    for (int i = 0; i < 9; i++) {
                        effect.tick(player, () -> MekanismUtils.onChangedPotionEffect(player, effect, true));
                    }
                }
            }
        }

        Mekanism.playerState.updateFlightInfo(player);
    }

    private static boolean isJetpackOn(PlayerEntity player, ItemStack chest) {
        if (MekanismUtils.isPlayingMode(player) && !chest.isEmpty()) {
            JetpackMode mode = getJetpackMode(chest);
            if (mode == JetpackMode.NORMAL) {
                return Mekanism.keyMap.has(player.getUniqueID(), KeySync.ASCEND);
            } else if (mode == JetpackMode.HOVER) {
                boolean ascending = Mekanism.keyMap.has(player.getUniqueID(), KeySync.ASCEND);
                boolean descending = Mekanism.keyMap.has(player.getUniqueID(), KeySync.DESCEND);
                if (!ascending || descending) {
                    return !isOnGroundOrSleeping(player);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isGravitationalModulationReady(PlayerEntity player) {
        ModuleGravitationalModulatingUnit module = Modules.load(player.getItemStackFromSlot(EquipmentSlotType.CHEST), Modules.GRAVITATIONAL_MODULATING_UNIT);
        FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get();
        return MekanismUtils.isPlayingMode(player) && module != null && module.isEnabled() && module.getContainerEnergy().greaterOrEqual(usage);
    }

    public static boolean isGravitationalModulationOn(PlayerEntity player) {
        return isGravitationalModulationReady(player) && player.abilities.isFlying;
    }

    /** Will return null if jetpack mode is not active */
    public static JetpackMode getJetpackMode(ItemStack stack) {
        if (stack.getItem() instanceof ItemJetpack && ChemicalUtil.hasGas(stack)) {
            return ((ItemJetpack) stack.getItem()).getMode(stack);
        } else if (stack.getItem() instanceof IModuleContainerItem && ChemicalUtil.hasChemical(stack, MekanismGases.HYDROGEN.get())) {
            ModuleJetpackUnit module = Modules.load(stack, Modules.JETPACK_UNIT);
            if (module != null && module.isEnabled()) {
                return module.getMode();
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event) {
        LivingEntity base = event.getEntityLiving();
        //Gas Mask checks
        if (event.getSource().isMagicDamage()) {
            ItemStack headStack = base.getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (!headStack.isEmpty()) {
                if (headStack.getItem() instanceof ItemScubaMask) {
                    ItemStack chestStack = base.getItemStackFromSlot(EquipmentSlotType.CHEST);
                    if (!chestStack.isEmpty()) {
                        if (chestStack.getItem() instanceof ItemScubaTank && ((ItemScubaTank) chestStack.getItem()).getFlowing(chestStack) &&
                            ChemicalUtil.hasGas(chestStack)) {
                            event.setCanceled(true);
                            return;
                        }
                    }
                } else {
                    //Note: We have this here in addition to listening to LivingHurt, so as if we can fully block the damage
                    // then we don't play the hurt effect/sound, as cancelling LivingHurtEvent still causes that to happen
                    ModuleInhalationPurificationUnit module = Modules.load(headStack, Modules.INHALATION_PURIFICATION_UNIT);
                    if (module != null && module.isEnabled()) {
                        FloatingLong energyRequirement = MekanismConfig.gear.mekaSuitEnergyUsageMagicReduce.get().multiply(event.getAmount());
                        if (module.canUseEnergy(base, energyRequirement)) {
                            //If we could fully negate the damage cancel the event
                            module.useEnergy(base, energyRequirement);
                            event.setCanceled(true);
                            return;
                        }
                    }
                }
            }
        }
        //Free runner checks
        //Note: We have this here in addition to listening to LivingHurt, so as if we can fully block the damage
        // then we don't play the hurt effect/sound, as cancelling LivingHurtEvent still causes that to happen
        if (event.getSource() == DamageSource.FALL) {
            IEnergyContainer energyContainer = getFallAbsorptionEnergyContainer(base);
            if (energyContainer != null) {
                FloatingLong energyRequirement = MekanismConfig.gear.freeRunnerFallEnergyCost.get().multiply(event.getAmount());
                FloatingLong simulatedExtract = energyContainer.extract(energyRequirement, Action.SIMULATE, AutomationType.MANUAL);
                if (simulatedExtract.equals(energyRequirement)) {
                    //If we could fully negate the damage cancel the event
                    energyContainer.extract(energyRequirement, Action.EXECUTE, AutomationType.MANUAL);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (event.getSource() == DamageSource.FALL) {
            IEnergyContainer energyContainer = getFallAbsorptionEnergyContainer(entity);
            if (energyContainer != null) {
                FloatingLong energyRequirement = MekanismConfig.gear.freeRunnerFallEnergyCost.get().multiply(event.getAmount());
                FloatingLong extracted = energyContainer.extract(energyRequirement, Action.EXECUTE, AutomationType.MANUAL);
                if (!extracted.isZero()) {
                    //If we managed to remove any power, then we want to lower (or negate) the amount of fall damage
                    FloatingLong remainder = energyRequirement.subtract(extracted);
                    if (remainder.isZero()) {
                        //If we used all the power we required, then cancel the event
                        event.setCanceled(true);
                        return;
                    } else {
                        float newDamage = remainder.divide(MekanismConfig.gear.freeRunnerFallEnergyCost.get()).floatValue();
                        if (newDamage == 0) {
                            //If we ended up being close enough that it rounds down to zero, just cancel it anyways
                            event.setCanceled(true);
                            return;
                        } else {
                            //Otherwise reduce the damage
                            event.setAmount(newDamage);
                        }
                    }
                }
            }
        } else if (event.getSource().isMagicDamage()) {
            ItemStack headStack = entity.getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (!headStack.isEmpty()) {
                ModuleInhalationPurificationUnit module = Modules.load(headStack, Modules.INHALATION_PURIFICATION_UNIT);
                if (module != null && module.isEnabled()) {
                    FloatingLong energyRequirement = MekanismConfig.gear.mekaSuitEnergyUsageMagicReduce.get().multiply(event.getAmount());
                    FloatingLong extracted = module.useEnergy(entity, energyRequirement);
                    if (!extracted.isZero()) {
                        //If we managed to remove any power, then we want to lower (or negate) the amount of fall damage
                        FloatingLong remainder = energyRequirement.subtract(extracted);
                        if (remainder.isZero()) {
                            //If we used all the power we required, then cancel the event
                            event.setCanceled(true);
                            return;
                        } else {
                            float newDamage = remainder.divide(MekanismConfig.gear.mekaSuitEnergyUsageMagicReduce.get()).floatValue();
                            if (newDamage == 0) {
                                //If we ended up being close enough that it rounds down to zero, just cancel it anyways
                                event.setCanceled(true);
                                return;
                            } else {
                                //Otherwise reduce the damage
                                event.setAmount(newDamage);
                            }
                        }
                    }
                }
            }
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            float ratioAbsorbed = 0;
            for (ItemStack stack : player.inventory.armorInventory) {
                if (stack.getItem() instanceof ItemMekaSuitArmor) {
                    ratioAbsorbed += ((ItemMekaSuitArmor) stack.getItem()).getDamageAbsorbed(stack, event.getSource(), event.getAmount());
                }
            }
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

    @SubscribeEvent
    public void onLivingJump(LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            ModuleHydraulicPropulsionUnit module = Modules.load(player.getItemStackFromSlot(EquipmentSlotType.FEET), Modules.HYDRAULIC_PROPULSION_UNIT);
            if (module != null && module.isEnabled() && Mekanism.keyMap.has(player.getUniqueID(), KeySync.BOOST)) {
                FloatingLong usage = MekanismConfig.gear.mekaSuitBaseJumpEnergyUsage.get().multiply(module.getBoost() / 0.1F);
                if (module.getContainerEnergy().greaterOrEqual(usage)) {
                    float boost = module.getBoost();
                    // if we're sprinting with the boost module, limit the height
                    ModuleLocomotiveBoostingUnit boostModule = Modules.load(player.getItemStackFromSlot(EquipmentSlotType.LEGS), Modules.LOCOMOTIVE_BOOSTING_UNIT);
                    if (boostModule != null && boostModule.isEnabled() && boostModule.canFunction(player)) {
                        boost = (float) Math.sqrt(boost);
                    }
                    player.setMotion(player.getMotion().add(0, boost, 0));
                    module.useEnergy(player, usage);
                }
            }
        }
    }

    /**
     * @return null if free runners are not being worn or they don't have an energy container for some reason
     */
    @Nullable
    private IEnergyContainer getFallAbsorptionEnergyContainer(LivingEntity base) {
        ItemStack feetStack = base.getItemStackFromSlot(EquipmentSlotType.FEET);
        if (!feetStack.isEmpty()) {
            if (feetStack.getItem() instanceof ItemFreeRunners) {
                ItemFreeRunners boots = (ItemFreeRunners) feetStack.getItem();
                if (boots.getMode(feetStack) == FreeRunnerMode.NORMAL) {
                    return StorageUtils.getEnergyContainer(feetStack, 0);
                }
            } else if (feetStack.getItem() instanceof ItemMekaSuitArmor) {
                return StorageUtils.getEnergyContainer(feetStack, 0);
            }
        }
        return null;
    }
}