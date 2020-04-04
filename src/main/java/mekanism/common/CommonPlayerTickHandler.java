package mekanism.common;

import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleHydraulicAbsorptionUnit;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleInhalationPurificationUnit;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.item.gear.ItemGasMask;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.GasUtils;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonPlayerTickHandler {

    public static boolean isOnGround(PlayerEntity player) {
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

    public static boolean isGasMaskOn(PlayerEntity player) {
        ItemStack tank = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
        ItemStack mask = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        return !tank.isEmpty() && !mask.isEmpty() && tank.getItem() instanceof ItemScubaTank && mask.getItem() instanceof ItemGasMask && GasUtils.hasGas(tank) &&
               ((ItemScubaTank) tank.getItem()).getFlowing(tank);
    }

    public static boolean isFlamethrowerOn(PlayerEntity player) {
        if (Mekanism.playerState.isFlamethrowerOn(player)) {
            ItemStack currentItem = player.inventory.getCurrentItem();
            return !currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower;
        }
        return false;
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent event) {
        if (event.phase == Phase.END && event.side.isServer()) {
            tickEnd(event.player);
        }
    }

    public void tickEnd(PlayerEntity player) {
        ItemStack feetStack = player.getItemStackFromSlot(EquipmentSlotType.FEET);
        if (!feetStack.isEmpty() && feetStack.getItem() instanceof ItemFreeRunners && !player.isShiftKeyDown()) {
            player.stepHeight = 1.002F;
        } else if (player.stepHeight == 1.002F) {
            player.stepHeight = 0.6F;
        }

        Mekanism.radiationManager.tickServer(player);

        if (isFlamethrowerOn(player)) {
            player.world.addEntity(new EntityFlame(player));
            if (!player.isCreative() && !player.isSpectator()) {
                ItemStack currentItem = player.inventory.getCurrentItem();
                ((ItemFlamethrower) currentItem.getItem()).useGas(currentItem, 1);
            }
        }

        if (isJetpackOn(player)) {
            ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            JetpackMode mode = getJetpackMode(stack);
            Vec3d motion = player.getMotion();
            if (mode == JetpackMode.NORMAL) {
                player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0.5D), motion.getZ());
            } else if (mode == JetpackMode.HOVER) {
                boolean ascending = Mekanism.keyMap.has(player, KeySync.ASCEND);
                boolean descending = Mekanism.keyMap.has(player, KeySync.DESCEND);
                if ((!ascending && !descending) || (ascending && descending)) {
                    if (motion.getY() > 0) {
                        player.setMotion(motion.getX(), Math.max(motion.getY() - 0.15D, 0), motion.getZ());
                    } else if (motion.getY() < 0) {
                        if (!isOnGround(player)) {
                            player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0), motion.getZ());
                        }
                    }
                } else if (ascending) {
                    player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0.2D), motion.getZ());
                } else if (!isOnGround(player)) {
                    player.setMotion(motion.getX(), Math.max(motion.getY() - 0.15D, -0.2D), motion.getZ());
                }
            }
            player.fallDistance = 0.0F;
            if (player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) player).connection.floatingTickCount = 0;
            }
            if (stack.getItem() instanceof ItemJetpack) {
                ((ItemJetpack) stack.getItem()).useGas(stack, 1);
            } else {
                ((ItemMekaSuitArmor) stack.getItem()).useGas(stack, MekanismGases.HYDROGEN.get(), 1);
            }
        }

        if (isGasMaskOn(player)) {
            ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            ItemScubaTank tank = (ItemScubaTank) stack.getItem();
            final int max = 300;
            tank.useGas(stack, 1);
            GasStack received = tank.useGas(stack, max - player.getAir());
            if (!received.isEmpty()) {
                player.setAir(player.getAir() + received.getAmount());
            }
            if (player.getAir() == max) {
                for (EffectInstance effect : player.getActivePotionEffects()) {
                    for (int i = 0; i < 9; i++) {
                        effect.tick(player, () -> MekanismUtils.onChangedPotionEffect(player, effect, true));
                    }
                }
            }
        }
    }

    public static boolean isJetpackOn(PlayerEntity player) {
        if (!player.isCreative() && !player.isSpectator()) {
            ItemStack chest = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (!chest.isEmpty()) {
                JetpackMode mode = getJetpackMode(chest);
                if (mode == JetpackMode.NORMAL) {
                    return Mekanism.keyMap.has(player, KeySync.ASCEND);
                } else if (mode == JetpackMode.HOVER) {
                    boolean ascending = Mekanism.keyMap.has(player, KeySync.ASCEND);
                    boolean descending = Mekanism.keyMap.has(player, KeySync.DESCEND);
                    if (!ascending || descending) {
                        return !isOnGround(player);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /** Will return null if jetpack mode is not active */
    private static JetpackMode getJetpackMode(ItemStack stack) {
        if (stack.getItem() instanceof ItemJetpack && GasUtils.hasGas(stack)) {
            return ((ItemJetpack) stack.getItem()).getMode(stack);
        } else if (stack.getItem() instanceof IModuleContainerItem && GasUtils.hasGas(stack, MekanismGases.HYDROGEN.get())) {
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
        if (event.getSource() == DamageSource.MAGIC) {
            ItemStack headStack = base.getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (!headStack.isEmpty() && headStack.getItem() instanceof ItemGasMask) {
                ItemStack chestStack = base.getItemStackFromSlot(EquipmentSlotType.CHEST);
                if (!chestStack.isEmpty()) {
                    if (chestStack.getItem() instanceof ItemScubaTank && ((ItemScubaTank) chestStack.getItem()).getFlowing(chestStack) &&
                          GasUtils.hasGas(chestStack)) {
                        event.setCanceled(true);
                        return;
                    }
                    ModuleInhalationPurificationUnit module = Modules.load(chestStack, Modules.INHALATION_PURIFICATION_UNIT);
                    if (module != null && module.isEnabled() && module.getContainerEnergy().greaterOrEqual(ModuleInhalationPurificationUnit.ENERGY_USAGE_PER_MAGIC_PREVENT)) {
                        module.useEnergy(ModuleInhalationPurificationUnit.ENERGY_USAGE_PER_MAGIC_PREVENT);
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
        //Free runner checks
        //Note: We have this here in addition to listening to LivingHurt, so as if we can fully block the damage
        // then we don't play the hurt effect/sound, as cancelling LivingHurtEvent still causes that to happen
        if (event.getSource() == DamageSource.FALL) {
            IEnergyContainer energyContainer = getFreeRunnerEnergyContainer(base);
            if (energyContainer != null) {
                FloatingLong energyRequirement = MekanismConfig.general.freeRunnerFallEnergyCost.get().multiply(event.getAmount());
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
        if (event.getSource() == DamageSource.FALL) {
            //Free runner checks
            IEnergyContainer energyContainer = getFreeRunnerEnergyContainer(event.getEntityLiving());
            if (energyContainer != null) {
                FloatingLong energyRequirement = MekanismConfig.general.freeRunnerFallEnergyCost.get().multiply(event.getAmount());
                FloatingLong extracted = energyContainer.extract(energyRequirement, Action.EXECUTE, AutomationType.MANUAL);
                if (!extracted.isZero()) {
                    //If we managed to remove any power, then we want to lower (or negate) the amount of fall damage
                    FloatingLong remainder = energyRequirement.subtract(extracted);
                    if (remainder.isZero()) {
                        //If we used all the power we required, then cancel the event
                        event.setCanceled(true);
                    } else {
                        float newDamage = remainder.divide(MekanismConfig.general.freeRunnerFallEnergyCost.get()).floatValue();
                        if (newDamage == 0) {
                            //If we ended up being close enough that it rounds down to zero, just cancel it anyways
                            event.setCanceled(true);
                        } else {
                            //Otherwise reduce the damage
                            event.setAmount(newDamage);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingJump(LivingJumpEvent event) {
        // hydraulic propulsion
    }

    /**
     * @return null if free runners are not being worn or they don't have an energy container for some reason
     */
    @Nullable
    private IEnergyContainer getFreeRunnerEnergyContainer(LivingEntity base) {
        ItemStack feetStack = base.getItemStackFromSlot(EquipmentSlotType.FEET);
        if (!feetStack.isEmpty()) {
            if (feetStack.getItem() instanceof ItemFreeRunners) {
                ItemFreeRunners boots = (ItemFreeRunners) feetStack.getItem();
                if (boots.getMode(feetStack) == FreeRunnerMode.NORMAL) {
                    return StorageUtils.getEnergyContainer(feetStack, 0);
                }
            }
            ModuleHydraulicAbsorptionUnit module = Modules.load(feetStack, Modules.HYDRAULIC_ABSORPTION_UNIT);
            if (module != null && module.isEnabled()) {
                return StorageUtils.getEnergyContainer(feetStack, 0);
            }
        }
        return null;
    }
}