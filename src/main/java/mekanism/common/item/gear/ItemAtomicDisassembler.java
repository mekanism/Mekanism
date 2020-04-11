package mekanism.common.item.gear;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.IDisableableEnum;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockBounding;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.IItemHUDProvider;
import mekanism.common.item.IModeItem;
import mekanism.common.item.ItemEnergized;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemAtomicDisassembler extends ItemEnergized implements IItemHUDProvider, IModeItem {

    public ItemAtomicDisassembler(Properties properties) {
        //TODO: Set some tool types? What would we set the "harvest level to"
        super(MekanismConfig.general.disassemblerBatteryCapacity.get(), properties.setNoRepair().setISTER(ISTERProvider::disassembler));
    }

    @Override
    public boolean canHarvestBlock(@Nonnull BlockState state) {
        return state.getBlock() != Blocks.BEDROCK;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        DisassemblerMode mode = getMode(stack);
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.INDIGO, mode));
        tooltip.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.INDIGO, mode.getEfficiency()));
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        FloatingLong energy = energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
        FloatingLong energyCost = MekanismConfig.general.disassemblerEnergyUsageWeapon.get();
        int minDamage = MekanismConfig.general.disassemblerDamageMin.get();
        int damageDifference = MekanismConfig.general.disassemblerDamageMax.get() - minDamage;
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
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        return energyContainer == null || energyContainer.isEmpty() ? 1 : getMode(stack).getEfficiency();
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity entityliving) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null) {
            energyContainer.extract(getDestroyEnergy(stack, state.getBlockHardness(world, pos)), Action.EXECUTE, AutomationType.MANUAL);
        }
        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, PlayerEntity player) {
        World world = player.world;
        if (!world.isRemote && !player.isCreative()) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            if (energyContainer == null) {
                //If something went wrong and we don't have an energy container, just go to super
                return super.onBlockStartBreak(stack, pos, player);
            }
            DisassemblerMode mode = getMode(stack);
            boolean extended = mode == DisassemblerMode.EXTENDED_VEIN;
            if (extended || mode == DisassemblerMode.VEIN) {
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof BlockBounding) {
                    //Even though we now handle breaking bounding blocks properly, don't allow vein mining
                    // them as an added safety measure
                    return super.onBlockStartBreak(stack, pos, player);
                }
                //If it is extended or should be treated as an ore
                if (extended || state.isIn(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE)) {
                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                    List<BlockPos> found = findPositions(state, pos, world, extended ? MekanismConfig.general.disassemblerMiningRange.get() : -1);
                    for (BlockPos foundPos : found) {
                        if (pos.equals(foundPos)) {
                            continue;
                        }
                        BlockState foundState = world.getBlockState(foundPos);
                        FloatingLong destroyEnergy = getDestroyEnergy(stack, foundState.getBlockHardness(world, foundPos));
                        if (energyContainer.extract(destroyEnergy, Action.SIMULATE, AutomationType.MANUAL).smallerThan(destroyEnergy)) {
                            //If we don't have energy to break the block continue
                            //Note: We do not break as given the energy scales with hardness, so it is possible we still have energy to break another block
                            // Given we validate the blocks are the same but their block states may be different thus making them have different
                            // block hardness values in a modded context
                            continue;
                        }
                        int exp = ForgeHooks.onBlockBreakEvent(world, serverPlayerEntity.interactionManager.getGameType(), serverPlayerEntity, foundPos);
                        if (exp == -1) {
                            //If we can't actually break the block continue (this allows mods to stop us from vein mining into protected land)
                            continue;
                        }
                        //Otherwise break the block
                        Block block = foundState.getBlock();
                        //Get the tile now so that we have it for when we try to harvest the block
                        TileEntity tileEntity = MekanismUtils.getTileEntity(world, foundPos);
                        //Remove the block
                        boolean removed = foundState.removedByPlayer(world, foundPos, player, true, world.getFluidState(foundPos));
                        if (removed) {
                            block.onPlayerDestroy(world, foundPos, foundState);
                            //Harvest the block allowing it to handle block drops, incrementing block mined count, and adding exhaustion
                            block.harvestBlock(world, player, foundPos, foundState, tileEntity, stack);
                            player.addStat(Stats.ITEM_USED.get(this));
                            if (exp > 0) {
                                //If we have xp drop it
                                block.dropXpOnBlockBreak(world, foundPos, exp);
                            }
                            //Use energy
                            energyContainer.extract(destroyEnergy, Action.EXECUTE, AutomationType.MANUAL);
                        }
                    }
                }
            }
        }
        return super.onBlockStartBreak(stack, pos, player);
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

    private FloatingLong getDestroyEnergy(ItemStack itemStack, float hardness) {
        FloatingLong destroyEnergy = MekanismConfig.general.disassemblerEnergyUsage.get().multiply(getMode(itemStack).getEfficiency());
        return hardness == 0 ? destroyEnergy.divide(2) : destroyEnergy;
    }

    public DisassemblerMode getMode(ItemStack itemStack) {
        return DisassemblerMode.byIndexStatic(ItemDataUtils.getInt(itemStack, NBTConstants.MODE));
    }

    public void setMode(ItemStack itemStack, DisassemblerMode mode) {
        ItemDataUtils.setInt(itemStack, NBTConstants.MODE, mode.ordinal());
    }

    @Nonnull
    @Override
    @Deprecated
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<String, AttributeModifier> multiMap = super.getAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EquipmentSlotType.MAINHAND) {
            multiMap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, Operation.ADDITION));
        }
        return multiMap;
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, ItemStack stack, EquipmentSlotType slotType) {
        DisassemblerMode mode = getMode(stack);
        list.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, EnumColor.INDIGO, mode));
        list.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, mode.getEfficiency()));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        DisassemblerMode mode = getMode(stack);
        DisassemblerMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, newMode);
            if (displayChangeMessage) {
                player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                      MekanismLang.DISASSEMBLER_MODE_CHANGE.translateColored(EnumColor.GRAY, EnumColor.INDIGO, newMode, EnumColor.AQUA, newMode.getEfficiency())));
            }
        }
    }

    @Nonnull
    @Override
    public ITextComponent getScrollTextComponent(@Nonnull ItemStack stack) {
        DisassemblerMode mode = getMode(stack);
        return MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.INDIGO, mode, EnumColor.AQUA, mode.getEfficiency());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (stack.getTag() == null) {
            stack.setTag(new CompoundNBT());
        }
        stack.getTag().putInt("HideFlags", 2);
        return super.initCapabilities(stack, nbt);
    }

    public enum DisassemblerMode implements IDisableableEnum<DisassemblerMode>, IHasTranslationKey {
        NORMAL(MekanismLang.DISASSEMBLER_NORMAL, 20, () -> true),
        SLOW(MekanismLang.DISASSEMBLER_SLOW, 8, MekanismConfig.general.disassemblerSlowMode),
        FAST(MekanismLang.DISASSEMBLER_FAST, 128, MekanismConfig.general.disassemblerFastMode),
        VEIN(MekanismLang.DISASSEMBLER_VEIN, 20, MekanismConfig.general.disassemblerVeinMining),
        EXTENDED_VEIN(MekanismLang.DISASSEMBLER_EXTENDED_VEIN, 20, MekanismConfig.general.disassemblerExtendedMining),
        OFF(MekanismLang.DISASSEMBLER_OFF, 0, () -> true);

        private static DisassemblerMode[] MODES = values();

        private final BooleanSupplier checkEnabled;
        private final ILangEntry langEntry;
        private final int efficiency;

        DisassemblerMode(ILangEntry langEntry, int efficiency, BooleanSupplier checkEnabled) {
            this.langEntry = langEntry;
            this.efficiency = efficiency;
            this.checkEnabled = checkEnabled;
        }

        /**
         * Gets a Mode from its ordinal. NOTE: if this mode is not enabled then it will reset to NORMAL
         */
        public static DisassemblerMode byIndexStatic(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            DisassemblerMode mode = MODES[Math.floorMod(index, MODES.length)];
            return mode.isEnabled() ? mode : NORMAL;
        }

        @Nonnull
        @Override
        public DisassemblerMode byIndex(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            //Note: We can't just use byIndexStatic, as we want to be able to return disabled modes
            return MODES[Math.floorMod(index, MODES.length)];
        }

        @Override
        public String getTranslationKey() {
            return langEntry.getTranslationKey();
        }

        public int getEfficiency() {
            return efficiency;
        }

        @Override
        public boolean isEnabled() {
            return checkEnabled.getAsBoolean();
        }
    }
}