package mekanism.common.item.gear;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.event.MekanismTeleportEvent;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IBlastingItem;
import mekanism.common.content.gear.IRadialModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
import mekanism.common.content.gear.mekatool.ModuleBlastingUnit;
import mekanism.common.content.gear.mekatool.ModuleExcavationEscalationUnit;
import mekanism.common.content.gear.mekatool.ModuleTeleportationUnit;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.item.ItemEnergized;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.registries.MekanismModules;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemMekaTool extends ItemEnergized implements IRadialModuleContainerItem, IBlastingItem {

    private static final ResourceLocation RADIAL_ID = Mekanism.rl("meka_tool");

    public ItemMekaTool(Properties properties) {
        super(IModuleHelper.INSTANCE.applyModuleContainerProperties(properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1)
              .component(DataComponents.TOOL, new Tool(List.of(
                    Tool.Rule.deniesDrops(MekanismTags.Blocks.INCORRECT_FOR_MEKA_TOOL),
                    new Tool.Rule(new AnyHolderSet<>(BuiltInRegistries.BLOCK.asLookup()), Optional.empty(), Optional.of(true))
              ), 1, 0))
        ));
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        ModuleHelper.INSTANCE.dropModuleContainerContents(item, damageSource);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        if (ItemAtomicDisassembler.ALWAYS_SUPPORTED_ACTIONS.contains(action)) {
            IModuleContainer container = moduleContainer(stack);
            return container != null && hasEnergyForDigAction(container, StorageUtils.getEnergyContainer(stack, 0));
        }
        IModuleContainer moduleContainer = moduleContainer(stack);
        if (moduleContainer != null) {
            for (IModule<?> module : moduleContainer.modules()) {
                if (module.isEnabled() && canPerformAction(module, moduleContainer, stack, action)) {
                    return true;
                }
            }
        }
        return false;
    }

    private <MODULE extends ICustomModule<MODULE>> boolean canPerformAction(IModule<MODULE> module, IModuleContainer moduleContainer, ItemStack stack, ItemAbility action) {
        return module.getCustomInstance().canPerformAction(module, moduleContainer, stack, action);
    }

    public static boolean hasEnergyForDigAction(IModuleContainer container, @Nullable IEnergyContainer energyContainer) {
        if (energyContainer != null) {
            //Note: We use a hardness of zero here as that will get the minimum potential destroy energy required
            // as that is the best guess we can currently give whether the corresponding dig action is supported
            long energyRequired = getDestroyEnergy(container, 0, container.hasEnabled(MekanismModules.SILK_TOUCH_UNIT));
            long energyAvailable = energyContainer.getEnergy();
            //If we don't have enough energy to break at full speed check if the reduced speed could actually mine
            return energyRequired <= energyAvailable || ((double) energyAvailable / energyRequired) > Mth.EPSILON;
        }
        return false;
    }

    public static long getDestroyEnergy(IModuleContainer container, float hardness, boolean silk) {
        return getDestroyEnergy(getDestroyEnergy(container, silk), hardness);
    }

    private static long getDestroyEnergy(IModuleContainer container, boolean silk) {
        long destroyEnergy = getDestroyEnergy(silk);
        IModule<ModuleExcavationEscalationUnit> module = container.getIfEnabled(MekanismModules.EXCAVATION_ESCALATION_UNIT);
        float efficiency = module == null ? MekanismConfig.gear.mekaToolBaseEfficiency.get() : module.getCustomInstance().getEfficiency();
        return MathUtils.clampToLong(destroyEnergy * efficiency);
    }

    @Override
    public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        //Try to avoid replacing this item if there are any modules currently installed
        return super.isNotReplaceableByPickAction(stack, player, inventorySlot) || hasInstalledModules(stack);
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        //Enchantments in our data
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
        int moduleLevel = container == null ? 0 : container.getModuleEnchantmentLevel(enchantment);
        return Math.max(moduleLevel, super.getEnchantmentLevel(stack, enchantment));
    }

    @NotNull
    @Override
    public ItemEnchantments getAllEnchantments(@NotNull ItemStack stack, RegistryLookup<Enchantment> lookup) {
        ItemEnchantments enchantments = super.getAllEnchantments(stack, lookup);
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
        if (container != null) {
            ItemEnchantments moduleEnchantments = container.moduleBasedEnchantments();
            if (enchantments.isEmpty()) {
                //Skip copying if there are no builtin enchantments
                return moduleEnchantments;
            } else if (!moduleEnchantments.isEmpty()) {
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : moduleEnchantments.entrySet()) {
                    mutable.upgrade(entry.getKey(), entry.getIntValue());
                }
                return mutable.toImmutable();
            }
        }
        return enchantments;
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        for (IModule<?> module : getModules(context.getItemInHand())) {
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

    @NotNull
    @Override
    public InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        IModuleContainer moduleContainer = moduleContainer(stack);
        if (moduleContainer != null) {
            for (IModule<?> module : moduleContainer.modules()) {
                if (module.isEnabled()) {
                    InteractionResult result = onModuleInteract(module, player, entity, hand, moduleContainer, stack);
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
            }
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }

    private <MODULE extends ICustomModule<MODULE>> InteractionResult onModuleInteract(IModule<MODULE> module, @NotNull Player player, @NotNull LivingEntity entity,
          @NotNull InteractionHand hand, IModuleContainer moduleContainer, ItemStack stack) {
        return module.getCustomInstance().onInteract(module, player, entity, hand, moduleContainer, stack);
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer == null) {
            return 0;
        }
        //Use raw hardness to get the best guess of if it is zero or not
        long energyRequired = getDestroyEnergy(stack, state.destroySpeed, isModuleEnabled(stack, MekanismModules.SILK_TOUCH_UNIT));
        long energyAvailable = energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL);
        if (energyAvailable < energyRequired) {
            //If we can't extract all the energy we need to break it go at base speed reduced by how much we actually have available
            return (float) (MekanismConfig.gear.mekaToolBaseEfficiency.get() * ((double) energyAvailable / energyRequired));
        }
        IModule<ModuleExcavationEscalationUnit> module = getEnabledModule(stack, MekanismModules.EXCAVATION_ESCALATION_UNIT);
        return module == null ? MekanismConfig.gear.mekaToolBaseEfficiency.get() : module.getCustomInstance().getEfficiency();
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level world, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entity) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null) {
            boolean silk = isModuleEnabled(stack, MekanismModules.SILK_TOUCH_UNIT);
            long modDestroyEnergy = getDestroyEnergy(stack, silk);
            long energyRequired = getDestroyEnergy(modDestroyEnergy, state.getDestroySpeed(world, pos));
            energyContainer.extract(energyRequired, Action.EXECUTE, AutomationType.MANUAL);
            //AOE/vein mining handling
            if (!world.isClientSide && entity instanceof ServerPlayer player && !player.isCreative() &&
                energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL) >= energyRequired) {
                Map<BlockPos, BlockState> blocks = getBlastedBlocks(world, player, stack, pos, state);
                blocks = blocks.isEmpty() && ModuleVeinMiningUnit.canVeinBlock(state) ? Map.of(pos, state) : blocks;

                Reference2BooleanMap<Block> oreTracker = blocks.values().stream().collect(Collectors.toMap(BlockStateBase::getBlock,
                      bs -> bs.is(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE), (l, r) -> l, Reference2BooleanArrayMap::new));

                Object2IntMap<BlockPos> veinedBlocks = getVeinedBlocks(world, stack, blocks, oreTracker);
                if (!veinedBlocks.isEmpty()) {
                    //Don't include bonus energy required by efficiency modules when calculating energy of vein mining targets
                    long baseDestroyEnergy = getDestroyEnergy(silk);
                    MekanismUtils.veinMineArea(energyContainer, energyRequired, modDestroyEnergy, baseDestroyEnergy, world, pos, player, stack, this, veinedBlocks,
                          ItemMekaTool::getDestroyEnergy, (base, hardness, distance, bs) -> {
                              double multiplier = 0.5 * Math.pow(distance, bs.is(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE) ? 1.5 : 2);
                              return MathUtils.ceilToLong(getDestroyEnergy(base, hardness) * multiplier);
                          });
                }
            }
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null) {
            //Note: We only have an energy cost if the damage is above base, so we can skip all those checks
            // if we don't have an enabled attack amplification unit
            int unitDamage = attackAmplificationUnit.getCustomInstance().getDamage();
            if (unitDamage > 0) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null && !energyContainer.isEmpty()) {
                    //Try to extract full energy, even if we have a lower damage amount this is fine as that just means
                    // we don't have enough energy, but we will remove as much as we can, which is how much corresponds
                    // to the amount of damage we will actually do
                    energyContainer.extract(MathUtils.clampToLong(MekanismConfig.gear.mekaToolEnergyUsageWeapon.get() * (unitDamage / 4D)), Action.EXECUTE, AutomationType.MANUAL);
                }
            }
        }
        return true;
    }

    @Override
    public Map<BlockPos, BlockState> getBlastedBlocks(Level world, Player player, ItemStack stack, BlockPos pos, BlockState state) {
        //Setup initial set for blasting
        if (!player.isShiftKeyDown()) {
            IModule<ModuleBlastingUnit> blastingUnit = getEnabledModule(stack, MekanismModules.BLASTING_UNIT);
            if (blastingUnit != null) {
                int radius = blastingUnit.getCustomInstance().getBlastRadius();
                if (radius > 0 && IBlastingItem.canBlastBlock(world, pos, state)) {
                    return IBlastingItem.findPositions(world, pos, player, radius);
                }
            }
        }
        return Collections.emptyMap();
    }

    private Object2IntMap<BlockPos> getVeinedBlocks(Level world, ItemStack stack, Map<BlockPos, BlockState> blocks, Reference2BooleanMap<Block> oreTracker) {
        IModule<ModuleVeinMiningUnit> veinMiningUnit = getEnabledModule(stack, MekanismModules.VEIN_MINING_UNIT);
        if (veinMiningUnit != null) {
            ModuleVeinMiningUnit customInstance = veinMiningUnit.getCustomInstance();
            return ModuleVeinMiningUnit.findPositions(world, blocks, customInstance.extended() ? customInstance.getExcavationRange() : 0, oreTracker);
        }
        return blocks.entrySet().stream().collect(Collectors.toMap(Entry::getKey, be -> 0, (l, r) -> l, Object2IntArrayMap::new));
    }

    private static long getDestroyEnergy(boolean silk) {
        return silk ? MekanismConfig.gear.mekaToolEnergyUsageSilk.get() : MekanismConfig.gear.mekaToolEnergyUsage.get();
    }

    public static long getDestroyEnergy(ItemStack itemStack, float hardness, boolean silk) {
        return getDestroyEnergy(getDestroyEnergy(itemStack, silk), hardness);
    }

    private static long getDestroyEnergy(long baseDestroyEnergy, float hardness) {
        return hardness == 0 ? Math.max(baseDestroyEnergy / 2, 1) : baseDestroyEnergy;
    }

    private static long getDestroyEnergy(ItemStack itemStack, boolean silk) {
        long destroyEnergy = getDestroyEnergy(silk);
        IModule<ModuleExcavationEscalationUnit> module = IModuleHelper.INSTANCE.getIfEnabled(itemStack, MekanismModules.EXCAVATION_ESCALATION_UNIT);
        float efficiency = module == null ? MekanismConfig.gear.mekaToolBaseEfficiency.get() : module.getCustomInstance().getEfficiency();
        return MathUtils.clampToLong(destroyEnergy * efficiency);
    }

    @Override
    public void adjustAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        double damage = MekanismConfig.gear.mekaToolBaseDamage.get();
        double attackSpeed = MekanismConfig.gear.mekaToolAttackSpeed.get();
        IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null) {
            int unitDamage = attackAmplificationUnit.getCustomInstance().getDamage();
            if (unitDamage > 0) {
                long energyCost = MathUtils.clampToLong(MekanismConfig.gear.mekaToolEnergyUsageWeapon.get() * (unitDamage / 4D));
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                long energy = energyContainer == null ? 0L : energyContainer.getEnergy();
                if (energy < energyCost) {
                    //If we don't have enough power use it at a reduced power level (this will be false the majority of the time)
                    damage += unitDamage * MathUtils.divideToLevel(energy, energyCost);
                } else {
                    damage += unitDamage;
                }
            }
        }
        //Retrieve a cached map if we have enough energy to attack at the full damage value based on configured damage
        event.replaceModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        event.replaceModifier(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        IRadialModuleContainerItem.super.adjustAttributes(event);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            IModule<ModuleTeleportationUnit> module = getEnabledModule(stack, MekanismModules.TELEPORTATION_UNIT);
            if (module != null) {
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
                        long energyNeeded = MathUtils.ceilToLong(MekanismConfig.gear.mekaToolEnergyUsageTeleport.get() * (distance / 10D));
                        if (energyContainer == null || energyContainer.getEnergy() < energyNeeded) {
                            return InteractionResultHolder.fail(stack);
                        }
                        double targetX = pos.getX() + 0.5;
                        double targetY = pos.getY() + 1.5;
                        double targetZ = pos.getZ() + 0.5;
                        MekanismTeleportEvent.MekaTool event = new MekanismTeleportEvent.MekaTool(player, targetX, targetY, targetZ, stack, result);
                        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                            //Fail if the event was cancelled
                            return InteractionResultHolder.fail(stack);
                        }
                        //Note: We intentionally don't use the event's coordinates as we do not support changing the location the Meka-Tool is teleporting to
                        energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
                        if (player.isPassenger()) {
                            player.dismountTo(targetX, targetY, targetZ);
                        } else {
                            player.teleportTo(targetX, targetY, targetZ);
                        }
                        player.resetFallDistance();
                        PacketUtils.sendToAllTracking(new PacketPortalFX(pos.above()), world, pos);
                        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
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
        return blockState.isAir() || MekanismUtils.isLiquidBlock(blockState.getBlock());
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) {
        return isEnchantable(stack) && super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return isEnchantable(stack) && super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return isEnchantable(stack) && super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public ResourceLocation getRadialIdentifier() {
        return RADIAL_ID;
    }
}