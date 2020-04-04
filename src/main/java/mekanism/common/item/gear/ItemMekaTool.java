package mekanism.common.item.gear;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockBounding;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.mekatool.ModuleExcavationEscalationUnit;
import mekanism.common.content.gear.mekatool.ModuleMekaTool;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.IItemHUDProvider;
import mekanism.common.item.IModeItem;
import mekanism.common.item.ItemEnergized;
import mekanism.common.network.PacketPortalFX;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemMekaTool extends ItemEnergized implements IModuleContainerItem, IModeItem, IItemHUDProvider {

    private static final FloatingLong MAX_ENERGY = FloatingLong.createConst(1_000_000_000);

    public ItemMekaTool(Properties properties) {
        super(MAX_ENERGY, properties.setNoRepair().setISTER(ISTERProvider::disassembler));
        Modules.setSupported(this, Modules.ENERGY_UNIT, Modules.ATTACK_AMPLIFICATION_UNIT, Modules.SILK_TOUCH_UNIT, Modules.VEIN_MINING_UNIT, Modules.FARMING_UNIT,
            Modules.TELEPORTATION_UNIT, Modules.EXCAVATION_ESCALATION_UNIT);
    }

    @Override
    public boolean canHarvestBlock(@Nonnull BlockState state) {
        return state.getBlock() != Blocks.BEDROCK;
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return super.getDisplayName(stack).applyTextStyle(EnumColor.PURPLE.textFormatting);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.detailsKey)) {
            for (Module module : Modules.loadAll(stack)) {
                ITextComponent component = module.getData().getLangEntry().translateColored(EnumColor.GRAY);
                if (module.getInstalledCount() > 1) {
                    component.appendSibling(MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.GRAY, "", Integer.toString(module.getInstalledCount())));
                }
                tooltip.add(module.getData().getLangEntry().translateColored(EnumColor.GRAY));
            }
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getLocalizedName()));
        }
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        for (Module module : Modules.loadAll(context.getItem())) {
            if (module.isEnabled() && module instanceof ModuleMekaTool) {
                ActionResultType result = ((ModuleMekaTool) module).onItemUse(context);
                if (result != ActionResultType.PASS) {
                    return result;
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        ModuleExcavationEscalationUnit module = Modules.load(stack, Modules.EXCAVATION_ESCALATION_UNIT);
        double efficiency = module == null || !module.isEnabled() ? MekanismConfig.general.mekaToolBaseEfficiency.get() : module.getEfficiency();
        return energyContainer == null || energyContainer.isEmpty() ? 1 : (float) efficiency;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity entityliving) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null) {
            energyContainer.extract(getDestroyEnergy(stack, state.getBlockHardness(world, pos), false), Action.EXECUTE, AutomationType.MANUAL);
        }
        return true;
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        FloatingLong energy = energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
        FloatingLong energyCost = FloatingLong.ZERO;
        int minDamage = MekanismConfig.general.mekaToolBaseDamage.get(), maxDamage = minDamage;
        if (isModuleEnabled(stack, Modules.ATTACK_AMPLIFICATION_UNIT)) {
            maxDamage = Modules.load(stack, Modules.ATTACK_AMPLIFICATION_UNIT).getDamage();
            if (maxDamage > minDamage) {
                energyCost = MekanismConfig.general.mekaToolEnergyUsageWeapon.get().multiply((maxDamage - minDamage) / 4F);
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
            target.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) attacker), damage);
        } else {
            target.attackEntityFrom(DamageSource.causeMobDamage(attacker), damage);
        }
        if (energyContainer != null && !energy.isZero()) {
            energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
        }
        return false;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, PlayerEntity player) {
        World world = player.world;
        if (!world.isRemote && !player.isCreative()) {
            BlockState state = world.getBlockState(pos);
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            if (energyContainer == null) {
                //If something went wrong and we don't have an energy container, just go to super
                return super.onBlockStartBreak(stack, pos, player);
            }
            boolean silk = isModuleEnabled(stack, Modules.SILK_TOUCH_UNIT);
            if (silk) {
                // if we can't break the initial block, terminate
                if (!breakBlock(stack, world, pos, (ServerPlayerEntity) player, energyContainer, true)) {
                    return super.onBlockStartBreak(stack, pos, player);
                }
            }
            if (isModuleEnabled(stack, Modules.VEIN_MINING_UNIT)) {
                boolean extended = Modules.load(stack, Modules.VEIN_MINING_UNIT).isExtended();
                if (state.getBlock() instanceof BlockBounding) {
                    //Even though we now handle breaking bounding blocks properly, don't allow vein mining
                    // them as an added safety measure
                    return silk;
                }
                //If it is extended or should be treated as an ore
                if (extended || state.isIn(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE)) {
                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                    List<BlockPos> found = findPositions(state, pos, world, extended ? MekanismConfig.general.disassemblerMiningRange.get() : -1);
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
        FloatingLong destroyEnergy = getDestroyEnergy(stack, state.getBlockHardness(world, pos), silk);
        if (energyContainer.extract(destroyEnergy, Action.SIMULATE, AutomationType.MANUAL).smallerThan(destroyEnergy)) {
            return false;
        }
        int exp = ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, pos);
        if (exp == -1) {
            //If we can't actually break the block continue (this allows mods to stop us from vein mining into protected land)
            return false;
        }
        //Otherwise break the block
        Block block = state.getBlock();
        //Get the tile now so that we have it for when we try to harvest the block
        TileEntity tileEntity = MekanismUtils.getTileEntity(world, pos);
        //Remove the block
        boolean removed = state.removedByPlayer(world, pos, player, true, world.getFluidState(pos));
        if (removed) {
            block.onPlayerDestroy(world, pos, state);
            //Harvest the block allowing it to handle block drops, incrementing block mined count, and adding exhaustion
            ItemStack harvestTool = stack.copy();
            if (silk) {
                harvestTool.addEnchantment(Enchantments.SILK_TOUCH, 1);
            }
            block.harvestBlock(world, player, pos, state, tileEntity, harvestTool);
            player.addStat(Stats.ITEM_USED.get(this));
            if (exp > 0) {
                //If we have xp drop it
                block.dropXpOnBlockBreak(world, pos, exp);
            }
            //Use energy
            energyContainer.extract(destroyEnergy, Action.EXECUTE, AutomationType.MANUAL);
        }

        return true;
    }

    private static List<BlockPos> findPositions(BlockState state, BlockPos location, World world, int maxRange) {
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> checked = new ObjectOpenHashSet<>();
        found.add(location);
        Block startBlock = state.getBlock();
        int maxCount = MekanismConfig.general.disassemblerMiningCount.get() - 1;
        for (int i = 0; i < found.size(); i++) {
            BlockPos blockPos = found.get(i);
            checked.add(blockPos);
            for (BlockPos pos : BlockPos.getAllInBoxMutable(blockPos.add(-1, -1, -1), blockPos.add(1, 1, 1))) {
                //We can check contains as mutable
                if (!checked.contains(pos)) {
                    if (maxRange == -1 || Math.sqrt(location.distanceSq(pos)) <= maxRange) {
                        if (world.isBlockPresent(pos) && startBlock == world.getBlockState(pos).getBlock()) {
                            //Make sure to add it as immutable
                            found.add(pos.toImmutable());
                            if (found.size() > maxCount) {
                                return found;
                            }
                        }
                    }
                }
            }
        }
        return found;
    }

    private FloatingLong getDestroyEnergy(ItemStack itemStack, float hardness, boolean silk) {
        FloatingLong destroyEnergy = silk ? MekanismConfig.general.mekaToolEnergyUsageSilk.get() : MekanismConfig.general.mekaToolEnergyUsage.get();
        ModuleExcavationEscalationUnit module = Modules.load(itemStack, Modules.EXCAVATION_ESCALATION_UNIT);
        double efficiency = module == null || !module.isEnabled() ? MekanismConfig.general.mekaToolBaseEfficiency.get() : module.getEfficiency();
        destroyEnergy = destroyEnergy.multiply(efficiency);
        return hardness == 0 ? destroyEnergy.divide(2) : destroyEnergy;
    }

    @Nonnull
    @Override
    public ITextComponent getScrollTextComponent(@Nonnull ItemStack stack) {
        return null;
    }


    @Nonnull
    @Override
    @Deprecated
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<String, AttributeModifier> multiMap = super.getAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EquipmentSlotType.MAINHAND) {
            multiMap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4D, Operation.ADDITION));
        }
        return multiMap;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote() && isModuleEnabled(stack, Modules.TELEPORTATION_UNIT)) {
            BlockRayTraceResult pos = MekanismUtils.rayTrace(player, MekanismConfig.general.mekaToolMaxTeleportReach.get());
            if (pos.getType() != RayTraceResult.Type.MISS) {
                // make sure we fit
                if (world.isAirBlock(pos.getPos().add(0, 1, 0)) && world.isAirBlock(pos.getPos().add(0, 2, 0))) {
                    double distance = player.getDistanceSq(pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ());
                    if (distance < 5) {
                        return new ActionResult<>(ActionResultType.PASS, stack);
                    }
                    IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                    FloatingLong energyNeeded = MekanismConfig.general.mekaToolEnergyUsageTeleport.get().multiply(distance / 10D);
                    if (energyContainer == null || energyContainer.getEnergy().smallerThan(energyNeeded)) {
                        return new ActionResult<>(ActionResultType.FAIL, stack);
                    }
                    energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
                    player.setPositionAndUpdate(pos.getPos().getX() + 0.5, pos.getPos().getY() + 1.5, pos.getPos().getZ() + 0.5);
                    player.fallDistance = 0.0F;
                    Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(new Coord4D(pos.getPos().add(0, 1, 0), world)), world, pos.getPos());
                    world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }
            }
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (stack.getTag() == null) {
            stack.setTag(new CompoundNBT());
        }
        stack.getTag().putInt("HideFlags", 2);
        return super.initCapabilities(stack, nbt);
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, ItemStack stack, EquipmentSlotType slotType) {
        for (Module module : Modules.loadAll(stack)) {
            if (module.isEnabled() && module.renderHUD()) {
                module.addHUDStrings(list);
            }
        }
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        for (Module module : Modules.loadAll(stack)) {
            if (module.isEnabled() && module.handlesModeChange()) {
                module.changeMode(player, stack, shift, displayChangeMessage);
                return;
            }
        }
    }

    @Override
    protected FloatingLong getMaxEnergy(ItemStack stack) {
        ModuleEnergyUnit module = Modules.load(stack, Modules.ENERGY_UNIT);
        return module != null ? module.getEnergyCapacity() : MekanismConfig.general.mekaToolBaseEnergyCapacity.get();
    }
}
