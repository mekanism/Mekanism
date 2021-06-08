package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockBounding;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
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
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStack.TooltipDisplayFlags;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.IFluidBlock;

public class ItemMekaTool extends ItemEnergized implements IModuleContainerItem, IModeItem {

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
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @Nonnull
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        for (Module<?> module : getModules(context.getItemInHand())) {
            if (module.isEnabled()) {
                ActionResultType result = onModuleUse(module, context);
                if (result != ActionResultType.PASS) {
                    return result;
                }
            }
        }
        return ActionResultType.PASS;
    }

    private <MODULE extends ICustomModule<MODULE>> ActionResultType onModuleUse(IModule<MODULE> module, ItemUseContext context) {
        return module.getCustomInstance().onItemUse(module, context);
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        IModule<ModuleExcavationEscalationUnit> module = getModule(stack, MekanismModules.EXCAVATION_ESCALATION_UNIT);
        double efficiency = module == null || !module.isEnabled() ? MekanismConfig.gear.mekaToolBaseEfficiency.get() : module.getCustomInstance().getEfficiency();
        return energyContainer == null || energyContainer.isEmpty() ? 1 : (float) efficiency;
    }

    @Override
    public boolean mineBlock(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull LivingEntity entityliving) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null) {
            energyContainer.extract(getDestroyEnergy(stack, state.getDestroySpeed(world, pos), false), Action.EXECUTE, AutomationType.MANUAL);
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(@Nonnull ItemStack stack, @Nonnull LivingEntity target, @Nonnull LivingEntity attacker) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        FloatingLong energy = energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
        FloatingLong energyCost = FloatingLong.ZERO;
        int minDamage = MekanismConfig.gear.mekaToolBaseDamage.get(), maxDamage = minDamage;
        IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null && attackAmplificationUnit.isEnabled()) {
            maxDamage = attackAmplificationUnit.getCustomInstance().getDamage();
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
        if (attacker instanceof PlayerEntity) {
            target.hurt(DamageSource.playerAttack((PlayerEntity) attacker), damage);
        } else {
            target.hurt(DamageSource.mobAttack(attacker), damage);
        }
        if (energyContainer != null && !energy.isZero()) {
            energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
        }
        return false;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, PlayerEntity player) {
        World world = player.level;
        if (!world.isClientSide && !player.isCreative()) {
            BlockState state = world.getBlockState(pos);
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            if (energyContainer == null) {
                //If something went wrong and we don't have an energy container, just go to super
                return super.onBlockStartBreak(stack, pos, player);
            }
            boolean silk = isModuleEnabled(stack, MekanismModules.SILK_TOUCH_UNIT);
            if (silk) {
                // if we can't break the initial block, terminate
                if (!breakBlock(stack, world, pos, (ServerPlayerEntity) player, energyContainer, true)) {
                    return super.onBlockStartBreak(stack, pos, player);
                }
            }
            IModule<ModuleVeinMiningUnit> veinMiningUnit = getModule(stack, MekanismModules.VEIN_MINING_UNIT);
            if (veinMiningUnit != null && veinMiningUnit.isEnabled()) {
                boolean extended = veinMiningUnit.getCustomInstance().isExtended();
                if (state.getBlock() instanceof BlockBounding) {
                    //Even though we now handle breaking bounding blocks properly, don't allow vein mining
                    // them as an added safety measure
                    return silk;
                }
                //If it is extended or should be treated as an ore
                if (extended || state.is(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE)) {
                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                    Set<BlockPos> found = ModuleVeinMiningUnit.findPositions(state, pos, world, extended ? veinMiningUnit.getCustomInstance().getExcavationRange() : -1);
                    for (BlockPos foundPos : found) {
                        if (pos.equals(foundPos)) {
                            continue;
                        }
                        breakBlock(stack, world, foundPos, serverPlayerEntity, energyContainer, silk);
                    }
                }
            }
        }
        return super.onBlockStartBreak(stack, pos, player);
    }

    private boolean breakBlock(ItemStack stack, World world, BlockPos pos, ServerPlayerEntity player, IEnergyContainer energyContainer, boolean silk) {
        BlockState state = world.getBlockState(pos);
        FloatingLong destroyEnergy = getDestroyEnergy(stack, state.getDestroySpeed(world, pos), silk);
        if (energyContainer.extract(destroyEnergy, Action.SIMULATE, AutomationType.MANUAL).smallerThan(destroyEnergy)) {
            return false;
        }
        int exp = ForgeHooks.onBlockBreakEvent(world, player.gameMode.getGameModeForPlayer(), player, pos);
        if (exp == -1) {
            //If we can't actually break the block continue (this allows mods to stop us from vein mining into protected land)
            return false;
        }
        //Otherwise break the block
        Block block = state.getBlock();
        //Get the tile now so that we have it for when we try to harvest the block
        TileEntity tileEntity = WorldUtils.getTileEntity(world, pos);
        //Remove the block
        boolean removed = state.removedByPlayer(world, pos, player, true, state.getFluidState());
        if (removed) {
            block.destroy(world, pos, state);
            //Harvest the block allowing it to handle block drops, incrementing block mined count, and adding exhaustion
            ItemStack harvestTool = stack.copy();
            if (silk) {
                harvestTool.enchant(Enchantments.SILK_TOUCH, 1);
                //Calculate the proper amount of xp that the state would drop if broken with a silk touch tool
                //Note: This fixes ores and the like dropping xp when broken with silk touch but makes it so that
                // BlockEvent.BreakEvent is unable to be used to adjust the amount of xp dropped.
                //TODO: look into if there is a better way for us to handle this as BlockEvent.BreakEvent goes based
                // of the item in the main hand so there isn't an easy way to change this without actually enchanting the meka-tool
                exp = state.getExpDrop(world, pos, 0, 1);
            }
            block.playerDestroy(world, player, pos, state, tileEntity, harvestTool);
            player.awardStat(Stats.ITEM_USED.get(this));
            if (exp > 0) {
                //If we have xp drop it
                block.popExperience((ServerWorld) world, pos, exp);
            }
            //Use energy
            energyContainer.extract(destroyEnergy, Action.EXECUTE, AutomationType.MANUAL);
        }

        return true;
    }

    private FloatingLong getDestroyEnergy(ItemStack itemStack, float hardness, boolean silk) {
        FloatingLong destroyEnergy = silk ? MekanismConfig.gear.mekaToolEnergyUsageSilk.get() : MekanismConfig.gear.mekaToolEnergyUsage.get();
        IModule<ModuleExcavationEscalationUnit> module = getModule(itemStack, MekanismModules.EXCAVATION_ESCALATION_UNIT);
        double efficiency = module == null || !module.isEnabled() ? MekanismConfig.gear.mekaToolBaseEfficiency.get() : module.getCustomInstance().getEfficiency();
        destroyEnergy = destroyEnergy.multiply(efficiency);
        return hardness == 0 ? destroyEnergy.divide(2) : destroyEnergy;
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, @Nonnull ItemStack stack) {
        return slot == EquipmentSlotType.MAINHAND ? attributes : super.getAttributeModifiers(slot, stack);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            IModule<ModuleTeleportationUnit> module = getModule(stack, MekanismModules.TELEPORTATION_UNIT);
            if (module != null && module.isEnabled()) {
                BlockRayTraceResult result = MekanismUtils.rayTrace(player, MekanismConfig.gear.mekaToolMaxTeleportReach.get());
                //If we don't require a block target or are not a miss, allow teleporting
                if (!module.getCustomInstance().requiresBlockTarget() || result.getType() != RayTraceResult.Type.MISS) {
                    BlockPos pos = result.getBlockPos();
                    // make sure we fit
                    if (isValidDestinationBlock(world, pos.above()) && isValidDestinationBlock(world, pos.above(2))) {
                        double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                        if (distance < 5) {
                            return new ActionResult<>(ActionResultType.PASS, stack);
                        }
                        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                        FloatingLong energyNeeded = MekanismConfig.gear.mekaToolEnergyUsageTeleport.get().multiply(distance / 10D);
                        if (energyContainer == null || energyContainer.getEnergy().smallerThan(energyNeeded)) {
                            return new ActionResult<>(ActionResultType.FAIL, stack);
                        }
                        energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
                        if (player.isPassenger()) {
                            player.stopRiding();
                        }
                        player.teleportTo(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
                        player.fallDistance = 0.0F;
                        Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(pos.above()), world, pos);
                        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        return new ActionResult<>(ActionResultType.SUCCESS, stack);
                    }
                }
            }
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    private boolean isValidDestinationBlock(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        //Allow teleporting into air or fluids
        return blockState.isAir(world, pos) || blockState.getBlock() instanceof FlowingFluidBlock || blockState.getBlock() instanceof IFluidBlock;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        stack.hideTooltipPart(TooltipDisplayFlags.MODIFIERS);
        return super.initCapabilities(stack, nbt);
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
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
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