package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IBlastingItem;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
import mekanism.common.content.gear.mekatool.ModuleBlastingUnit;
import mekanism.common.content.gear.mekatool.ModuleExcavationEscalationUnit;
import mekanism.common.content.gear.mekatool.ModuleTeleportationUnit;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.registries.MekanismModules;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.fluids.IFluidBlock;

public class ItemMekaTool extends ItemEnergized implements IModuleContainerItem, IModeItem, IBlastingItem {

    private final Multimap<Attribute, AttributeModifier> attributes;

    public ItemMekaTool(Properties properties) {
        super(MekanismConfig.gear.mekaToolBaseChargeRate, MekanismConfig.gear.mekaToolBaseEnergyCapacity, properties.rarity(Rarity.EPIC).setNoRepair());
        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4D, Operation.ADDITION));
        this.attributes = builder.build();
    }

    @Override
    public boolean isCorrectToolForDrops(@Nonnull BlockState state) {
        //Allow harvesting everything, things that are unbreakable are caught elsewhere
        return true;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        if (ItemAtomicDisassembler.ALWAYS_SUPPORTED_ACTIONS.contains(action)) {
            return true;
        }
        return getModules(stack).stream().anyMatch(module -> module.isEnabled() && canPerformAction(module, action));
    }

    private <MODULE extends ICustomModule<MODULE>> boolean canPerformAction(IModule<MODULE> module, ToolAction action) {
        return module.getCustomInstance().canPerformAction(module, action);
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && super.isFoil(stack) && IModuleContainerItem.hasOtherEnchants(stack);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        for (Module<?> module : getModules(context.getItemInHand())) {
            if (module.isEnabled()) {
                InteractionResult result = onModuleUse(module, context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return super.useOn(context);
    }

    private <MODULE extends ICustomModule<MODULE>> InteractionResult onModuleUse(IModule<MODULE> module, UseOnContext context) {
        return module.getCustomInstance().onItemUse(module, context);
    }

    @Nonnull
    @Override
    public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand) {
        for (Module<?> module : getModules(stack)) {
            if (module.isEnabled()) {
                InteractionResult result = onModuleInteract(module, player, entity, hand);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }

    private <MODULE extends ICustomModule<MODULE>> InteractionResult onModuleInteract(IModule<MODULE> module, @Nonnull Player player, @Nonnull LivingEntity entity,
          @Nonnull InteractionHand hand) {
        return module.getCustomInstance().onInteract(module, player, entity, hand);
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer == null) {
            return 0;
        }
        //Use raw hardness to get the best guess of if it is zero or not
        FloatingLong energyRequired = getDestroyEnergy(stack, state.destroySpeed, isModuleEnabled(stack, MekanismModules.SILK_TOUCH_UNIT));
        FloatingLong energyAvailable = energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL);
        if (energyAvailable.smallerThan(energyRequired)) {
            //If we can't extract all the energy we need to break it go at base speed reduced by how much we actually have available
            return MekanismConfig.gear.mekaToolBaseEfficiency.get() * energyAvailable.divide(energyRequired).floatValue();
        }
        IModule<ModuleExcavationEscalationUnit> module = getModule(stack, MekanismModules.EXCAVATION_ESCALATION_UNIT);
        return module == null || !module.isEnabled() ? MekanismConfig.gear.mekaToolBaseEfficiency.get() : module.getCustomInstance().getEfficiency();
    }

    @Override
    public boolean mineBlock(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull LivingEntity entityliving) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null) {
            FloatingLong energyRequired = getDestroyEnergy(stack, state.getDestroySpeed(world, pos), isModuleEnabled(stack, MekanismModules.SILK_TOUCH_UNIT));
            energyContainer.extract(energyRequired, Action.EXECUTE, AutomationType.MANUAL);
        }
        return true;
    }

    @Override
    public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull Entity target) {
        //If it is a vehicle that we want to damage
        if (target.getType().is(MekanismTags.Entities.HURTABLE_VEHICLES)) {
            if (target.isAttackable() && !target.skipAttackInteraction(player)) {
                int maxDamage = MekanismConfig.gear.mekaToolBaseDamage.get();
                IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
                if (attackAmplificationUnit != null && attackAmplificationUnit.isEnabled()) {
                    maxDamage += attackAmplificationUnit.getCustomInstance().getDamage();
                }
                //Always apply max damage to vehicles that can be hurt regardless of energy and don't actually
                target.hurt(DamageSource.playerAttack(player), maxDamage);
                //Note: We fall through and call super regardless so any other processing that may need to happen, happens,
                // this is similar to how we return super from hurtEnemy
            }
        }
        return super.onLeftClickEntity(stack, player, target);
    }

    @Override
    public boolean hurtEnemy(@Nonnull ItemStack stack, @Nonnull LivingEntity target, @Nonnull LivingEntity attacker) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        FloatingLong energy = energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
        FloatingLong energyCost = FloatingLong.ZERO;
        int minDamage = MekanismConfig.gear.mekaToolBaseDamage.get(), maxDamage = minDamage;
        IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null && attackAmplificationUnit.isEnabled()) {
            maxDamage += attackAmplificationUnit.getCustomInstance().getDamage();
            if (maxDamage > minDamage) {
                energyCost = MekanismConfig.gear.mekaToolEnergyUsageWeapon.get().multiply((maxDamage - minDamage) / 4F);
            }
            minDamage = Math.min(minDamage, maxDamage);
        }
        int damageDifference = maxDamage - minDamage;
        //If we don't have enough power use it at a reduced power level
        double percent = 1;
        if (energy.smallerThan(energyCost)) {
            percent = energy.divideToLevel(energyCost);
        }
        float damage = (float) (minDamage + damageDifference * percent);
        if (attacker instanceof Player player) {
            target.hurt(DamageSource.playerAttack(player), damage);
        } else {
            target.hurt(DamageSource.mobAttack(attacker), damage);
        }
        if (energyContainer != null && !energy.isZero()) {
            energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public Map<BlockPos, BlockState> getBlastedBlocks(Level world, Player player, ItemStack stack, BlockPos pos, BlockState state) {
        //Setup initial set for blasting
        if (!player.isShiftKeyDown()) {
            IModule<ModuleBlastingUnit> blastingUnit = getModule(stack, MekanismModules.BLASTING_UNIT);
            if (blastingUnit != null && blastingUnit.isEnabled()) {
                int radius = blastingUnit.getCustomInstance().getBlastRadius();
                if (radius > 0 && IBlastingItem.canBlastBlock(world, pos, state)) {
                    return IBlastingItem.findPositions(world, pos, player, radius);
                }
            }
        }
        return Collections.emptyMap();
    }

    private Object2IntMap<BlockPos> getVeinedBlocks(Level world, ItemStack stack, Map<BlockPos, BlockState> blocks, Object2BooleanMap<Block> oreTracker) {
        IModule<ModuleVeinMiningUnit> veinMiningUnit = getModule(stack, MekanismModules.VEIN_MINING_UNIT);
        if (veinMiningUnit != null && veinMiningUnit.isEnabled()) {
            ModuleVeinMiningUnit customInstance = veinMiningUnit.getCustomInstance();
            return ModuleVeinMiningUnit.findPositions(world, blocks, customInstance.isExtended() ? customInstance.getExcavationRange() : 0, oreTracker);
        }
        return blocks.entrySet().stream().collect(Collectors.toMap(Entry::getKey, be -> 0, (l, r) -> l, Object2IntArrayMap::new));
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.level.isClientSide || player.isCreative()) {
            return super.onBlockStartBreak(stack, pos, player);
        }
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null) {
            Level world = player.level;
            BlockState state = world.getBlockState(pos);
            boolean silk = isModuleEnabled(stack, MekanismModules.SILK_TOUCH_UNIT);
            FloatingLong modDestroyEnergy = getDestroyEnergy(stack, silk);
            FloatingLong energyRequired = getDestroyEnergy(modDestroyEnergy, state.getDestroySpeed(world, pos));
            if (energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL).greaterOrEqual(energyRequired)) {
                Map<BlockPos, BlockState> blocks = getBlastedBlocks(world, player, stack, pos, state);
                blocks = blocks.isEmpty() && ModuleVeinMiningUnit.canVeinBlock(state) ? Map.of(pos, state) : blocks;

                Object2BooleanMap<Block> oreTracker = blocks.values().stream().collect(Collectors.toMap(BlockStateBase::getBlock,
                      bs -> bs.is(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE), (l, r) -> l, Object2BooleanArrayMap::new));

                Object2IntMap<BlockPos> veinedBlocks = getVeinedBlocks(world, stack, blocks, oreTracker);
                if (!veinedBlocks.isEmpty()) {
                    //Don't include bonus energy required by efficiency modules when calculating energy of vein mining targets
                    FloatingLong baseDestroyEnergy = getDestroyEnergy(silk);
                    MekanismUtils.veinMineArea(energyContainer, energyRequired, world, pos, (ServerPlayer) player, stack, this, veinedBlocks,
                          hardness -> getDestroyEnergy(modDestroyEnergy, hardness),
                          (hardness, distance, bs) -> getDestroyEnergy(baseDestroyEnergy, hardness).multiply(0.5 * Math.pow(distance, oreTracker.getBoolean(bs.getBlock()) ? 1.5 : 2)));
                }
            }
        }
        return super.onBlockStartBreak(stack, pos, player);
    }

    private FloatingLong getDestroyEnergy(boolean silk) {
        return silk ? MekanismConfig.gear.mekaToolEnergyUsageSilk.get() : MekanismConfig.gear.mekaToolEnergyUsage.get();
    }

    public FloatingLong getDestroyEnergy(ItemStack itemStack, float hardness, boolean silk) {
        return getDestroyEnergy(getDestroyEnergy(itemStack, silk), hardness);
    }

    private FloatingLong getDestroyEnergy(FloatingLong baseDestroyEnergy, float hardness) {
        return hardness == 0 ? baseDestroyEnergy.divide(2) : baseDestroyEnergy;
    }

    private FloatingLong getDestroyEnergy(ItemStack itemStack, boolean silk) {
        FloatingLong destroyEnergy = getDestroyEnergy(silk);
        IModule<ModuleExcavationEscalationUnit> module = getModule(itemStack, MekanismModules.EXCAVATION_ESCALATION_UNIT);
        float efficiency = module == null || !module.isEnabled() ? MekanismConfig.gear.mekaToolBaseEfficiency.get() : module.getCustomInstance().getEfficiency();
        return destroyEnergy.multiply(efficiency);
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, @Nonnull ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? attributes : super.getAttributeModifiers(slot, stack);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            IModule<ModuleTeleportationUnit> module = getModule(stack, MekanismModules.TELEPORTATION_UNIT);
            if (module != null && module.isEnabled()) {
                BlockHitResult result = MekanismUtils.rayTrace(player, MekanismConfig.gear.mekaToolMaxTeleportReach.get());
                //If we don't require a block target or are not a miss, allow teleporting
                if (!module.getCustomInstance().requiresBlockTarget() || result.getType() != HitResult.Type.MISS) {
                    BlockPos pos = result.getBlockPos();
                    // make sure we fit
                    if (isValidDestinationBlock(world, pos.above()) && isValidDestinationBlock(world, pos.above(2))) {
                        double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                        if (distance < 5) {
                            return InteractionResultHolder.pass(stack);
                        }
                        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                        FloatingLong energyNeeded = MekanismConfig.gear.mekaToolEnergyUsageTeleport.get().multiply(distance / 10D);
                        if (energyContainer == null || energyContainer.getEnergy().smallerThan(energyNeeded)) {
                            return InteractionResultHolder.fail(stack);
                        }
                        energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
                        if (player.isPassenger()) {
                            player.stopRiding();
                        }
                        player.teleportTo(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
                        player.fallDistance = 0.0F;
                        Mekanism.packetHandler().sendToAllTracking(new PacketPortalFX(pos.above()), world, pos);
                        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                        return InteractionResultHolder.success(stack);
                    }
                }
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    private boolean isValidDestinationBlock(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        //Allow teleporting into air or fluids
        return blockState.isAir() || blockState.getBlock() instanceof LiquidBlock || blockState.getBlock() instanceof IFluidBlock;
    }

    @Override
    public int getDefaultTooltipHideFlags(@Nonnull ItemStack stack) {
        return super.getDefaultTooltipHideFlags(stack) | TooltipPart.MODIFIERS.getMask();
    }

    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @Nonnull EquipmentSlot slotType) {
        return IModeItem.super.supportsSlotType(stack, slotType) && getModules(stack).stream().anyMatch(Module::handlesModeChange);
    }

    @Override
    public void changeMode(@Nonnull Player player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesModeChange()) {
                module.changeMode(player, stack, shift, displayChangeMessage);
                return;
            }
        }
    }

    @Override
    protected FloatingLong getMaxEnergy(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        return module == null ? MekanismConfig.gear.mekaToolBaseEnergyCapacity.get() : module.getCustomInstance().getEnergyCapacity(module);
    }

    @Override
    protected FloatingLong getChargeRate(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        return module == null ? MekanismConfig.gear.mekaToolBaseChargeRate.get() : module.getCustomInstance().getChargeRate(module);
    }
}