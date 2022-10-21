package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
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
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.lib.radial.data.NestingRadialData;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.registries.MekanismModules;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ToolAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemMekaTool extends ItemEnergized implements IModuleContainerItem, IBlastingItem, IGenericRadialModeItem {

    private static final ResourceLocation RADIAL_ID = Mekanism.rl("meka_tool");

    private final Int2ObjectMap<AttributeCache> attributeCaches = new Int2ObjectArrayMap<>(ModuleAttackAmplificationUnit.AttackDamage.values().length);

    public ItemMekaTool(Properties properties) {
        super(MekanismConfig.gear.mekaToolBaseChargeRate, MekanismConfig.gear.mekaToolBaseEnergyCapacity, properties.rarity(Rarity.EPIC).setNoRepair());
    }

    @Override
    public boolean isCorrectToolForDrops(@NotNull BlockState state) {
        //Allow harvesting everything, things that are unbreakable are caught elsewhere
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
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
    public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        //Try to avoid replacing this item if there are any modules currently installed
        return super.isNotReplaceableByPickAction(stack, player, inventorySlot) || ItemDataUtils.hasData(stack, NBTConstants.MODULES, Tag.TAG_COMPOUND);
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        if (stack.isEmpty()) {
            return 0;
        }
        //Enchantments in our data
        ListTag enchantments = ItemDataUtils.getList(stack, NBTConstants.ENCHANTMENTS);
        return Math.max(MekanismUtils.getEnchantmentLevel(enchantments, enchantment), super.getEnchantmentLevel(stack, enchantment));
    }

    @Override
    public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.deserializeEnchantments(ItemDataUtils.getList(stack, NBTConstants.ENCHANTMENTS));
        super.getAllEnchantments(stack).forEach((enchantment, level) -> enchantments.merge(enchantment, level, Math::max));
        return enchantments;
    }

    @NotNull
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

    @NotNull
    @Override
    public InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
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

    private <MODULE extends ICustomModule<MODULE>> InteractionResult onModuleInteract(IModule<MODULE> module, @NotNull Player player, @NotNull LivingEntity entity,
          @NotNull InteractionHand hand) {
        return module.getCustomInstance().onInteract(module, player, entity, hand);
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
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
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level world, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entityliving) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null) {
            FloatingLong energyRequired = getDestroyEnergy(stack, state.getDestroySpeed(world, pos), isModuleEnabled(stack, MekanismModules.SILK_TOUCH_UNIT));
            energyContainer.extract(energyRequired, Action.EXECUTE, AutomationType.MANUAL);
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null && attackAmplificationUnit.isEnabled()) {
            //Note: We only have an energy cost if the damage is above base, so we can skip all those checks
            // if we don't have an enabled attack amplification unit
            int unitDamage = attackAmplificationUnit.getCustomInstance().getDamage();
            if (unitDamage > 0) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null && !energyContainer.isEmpty()) {
                    //Try to extract full energy, even if we have a lower damage amount this is fine as that just means
                    // we don't have enough energy, but we will remove as much as we can, which is how much corresponds
                    // to the amount of damage we will actually do
                    energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageWeapon.get().multiply(unitDamage / 4D), Action.EXECUTE, AutomationType.MANUAL);
                }
            }
        }
        return true;
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

    @NotNull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            int unitDamage = 0;
            IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
            if (attackAmplificationUnit != null && attackAmplificationUnit.isEnabled()) {
                unitDamage = attackAmplificationUnit.getCustomInstance().getDamage();
                if (unitDamage > 0) {
                    FloatingLong energyCost = MekanismConfig.gear.mekaToolEnergyUsageWeapon.get().multiply(unitDamage / 4D);
                    IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                    FloatingLong energy = energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
                    if (energy.smallerThan(energyCost)) {
                        //If we don't have enough power use it at a reduced power level (this will be false the majority of the time)
                        double bonusDamage = unitDamage * energy.divideToLevel(energyCost);
                        if (bonusDamage > 0) {
                            //If we actually have bonus damage (as we might not if we don't have any energy stored, and then
                            // we can just use the cache for as if there was no bonus damage)
                            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
                            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                                  MekanismConfig.gear.mekaToolBaseDamage.get() + bonusDamage, Operation.ADDITION));
                            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                                  MekanismConfig.gear.mekaToolAttackSpeed.get(), Operation.ADDITION));
                            return builder.build();
                        }
                        //Use cached attribute map for just doing the base damage
                        unitDamage = 0;
                    }
                }
            }
            //Retrieve a cached map if we have enough energy to attack at the full damage value based on configured damage
            return attributeCaches.computeIfAbsent(unitDamage, damage -> new AttributeCache(builder -> {
                builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                      MekanismConfig.gear.mekaToolBaseDamage.get() + damage, Operation.ADDITION));
                builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                      MekanismConfig.gear.mekaToolAttackSpeed.get(), Operation.ADDITION));
            }, MekanismConfig.gear.mekaToolBaseDamage, MekanismConfig.gear.mekaToolAttackSpeed)).get();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {
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
        return blockState.isAir() || MekanismUtils.isLiquidBlock(blockState.getBlock());
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
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
    public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        return IGenericRadialModeItem.super.supportsSlotType(stack, slotType) && getModules(stack).stream().anyMatch(Module::handlesAnyModeChange);
    }

    @Nullable
    @Override
    public Component getScrollTextComponent(@NotNull ItemStack stack) {
        return getModules(stack).stream().filter(Module::handlesModeChange).findFirst().map(module -> module.getModeScrollComponent(stack)).orElse(null);
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, boolean displayChangeMessage) {
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

    @Nullable
    @Override
    public RadialData<?> getRadialData(ItemStack stack) {
        List<NestedRadialMode> nestedModes = new ArrayList<>();
        Consumer<NestedRadialMode> adder = nestedModes::add;
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange()) {
                module.addRadialModes(stack, adder);
            }
        }
        if (nestedModes.isEmpty()) {
            //No modes available, return that we don't actually currently support radials
            return null;
        } else if (nestedModes.size() == 1) {
            //If we only have one mode available, just return it rather than having to select the singular mode
            return nestedModes.get(0).nestedData();
        }
        return new NestingRadialData(RADIAL_ID, nestedModes);
    }

    @Nullable
    @Override
    public <M extends IRadialMode> M getMode(ItemStack stack, RadialData<M> radialData) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange()) {
                M mode = module.getMode(stack, radialData);
                if (mode != null) {
                    return mode;
                }
            }
        }
        return null;
    }

    @Override
    public <M extends IRadialMode> void setMode(ItemStack stack, Player player, RadialData<M> radialData, M mode) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange() && module.setMode(player, stack, radialData, mode)) {
                return;
            }
        }
    }
}