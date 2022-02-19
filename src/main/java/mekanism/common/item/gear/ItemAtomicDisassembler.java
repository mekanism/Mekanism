package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IDisableableEnum;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockBounding;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.gear.ItemAtomicDisassembler.DisassemblerMode;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IRadialModeItem;
import mekanism.common.item.interfaces.IRadialSelectorEnum;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.network.to_client.PacketLightningRender.LightningPreset;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
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
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemAtomicDisassembler extends ItemEnergized implements IItemHUDProvider, IRadialModeItem<DisassemblerMode> {

    private final Multimap<Attribute, AttributeModifier> attributes;

    public ItemAtomicDisassembler(Properties properties) {
        super(MekanismConfig.gear.disassemblerChargeRate, MekanismConfig.gear.disassemblerMaxEnergy, properties.rarity(Rarity.RARE).setNoRepair().setISTER(ISTERProvider::disassembler));
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
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        DisassemblerMode mode = getMode(stack);
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.INDIGO, mode));
        tooltip.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.INDIGO, mode.getEfficiency()));
    }

    @Override
    public boolean hurtEnemy(@Nonnull ItemStack stack, @Nonnull LivingEntity target, @Nonnull LivingEntity attacker) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        FloatingLong energy = energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
        FloatingLong energyCost = MekanismConfig.gear.disassemblerEnergyUsageWeapon.get();
        int minDamage = MekanismConfig.gear.disassemblerMinDamage.get();
        int damageDifference = MekanismConfig.gear.disassemblerMaxDamage.get() - minDamage;
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
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer == null) {
            return 0;
        }
        //Use raw hardness to get the best guess of if it is zero or not
        FloatingLong energyRequired = getDestroyEnergy(stack, state.destroySpeed);
        FloatingLong energyAvailable = energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL);
        if (energyAvailable.smallerThan(energyRequired)) {
            //If we can't extract all the energy we need to break it go at base speed reduced by how much we actually have available
            return DisassemblerMode.NORMAL.getEfficiency() * energyAvailable.divide(energyRequired).floatValue();
        }
        return getMode(stack).getEfficiency();
    }

    @Override
    public boolean mineBlock(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull LivingEntity entityliving) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null) {
            energyContainer.extract(getDestroyEnergy(stack, state.getDestroySpeed(world, pos)), Action.EXECUTE, AutomationType.MANUAL);
        }
        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, PlayerEntity player) {
        if (player.level.isClientSide || player.isCreative()) {
            return super.onBlockStartBreak(stack, pos, player);
        }
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null && getMode(stack) == DisassemblerMode.VEIN) {
            World world = player.level;
            BlockState state = world.getBlockState(pos);
            FloatingLong baseDestroyEnergy = getDestroyEnergy(stack);
            FloatingLong energyRequired = getDestroyEnergy(baseDestroyEnergy, state.getDestroySpeed(world, pos));
            if (energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL).greaterOrEqual(energyRequired)) {
                //Even though we now handle breaking bounding blocks properly, don't allow vein mining them
                // only allow mining things that are considered an ore
                if (!(state.getBlock() instanceof BlockBounding) && state.is(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE)) {
                    List<BlockPos> found = findPositions(state, pos, world);
                    MekanismUtils.veinMineArea(energyContainer, world, pos, (ServerPlayerEntity) player, stack, this, found, false,
                          hardness -> getDestroyEnergy(baseDestroyEnergy, hardness), distance -> 0.5 * Math.pow(distance, 1.5), state);
                }
            }
        }
        return super.onBlockStartBreak(stack, pos, player);
    }

    private static List<BlockPos> findPositions(BlockState state, BlockPos location, World world) {
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> checked = new ObjectOpenHashSet<>();
        found.add(location);
        Block startBlock = state.getBlock();
        int maxCount = MekanismConfig.gear.disassemblerMiningCount.get() - 1;
        for (int i = 0; i < found.size(); i++) {
            BlockPos blockPos = found.get(i);
            checked.add(blockPos);
            for (BlockPos pos : BlockPos.betweenClosed(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1))) {
                //We can check contains as mutable
                if (!checked.contains(pos)) {
                    Optional<BlockState> blockState = WorldUtils.getBlockState(world, pos);
                    if (blockState.isPresent() && startBlock == blockState.get().getBlock()) {
                        //Make sure to add it as immutable
                        found.add(pos.immutable());
                        //Note: We do this for all blocks we find/attempt to mine, not just ones we do mine, as it is a bit simpler
                        // and also represents those blocks getting checked by the vein mining for potentially being able to be mined
                        Mekanism.packetHandler.sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, Objects.hash(blockPos, pos),
                              Vector3d.atCenterOf(blockPos), Vector3d.atCenterOf(pos), 10), world, blockPos);
                        if (found.size() > maxCount) {
                            return found;
                        }
                    }
                }
            }
        }
        return found;
    }

    private FloatingLong getDestroyEnergy(ItemStack itemStack, float hardness) {
        return getDestroyEnergy(getDestroyEnergy(itemStack), hardness);
    }

    private FloatingLong getDestroyEnergy(FloatingLong baseDestroyEnergy, float hardness) {
        return hardness == 0 ? baseDestroyEnergy.divide(2) : baseDestroyEnergy;
    }

    private FloatingLong getDestroyEnergy(ItemStack itemStack) {
        return MekanismConfig.gear.disassemblerEnergyUsage.get().multiply(getMode(itemStack).getEfficiency());
    }

    @Override
    public DisassemblerMode getMode(ItemStack itemStack) {
        return DisassemblerMode.byIndexStatic(ItemDataUtils.getInt(itemStack, NBTConstants.MODE));
    }

    @Override
    public DisassemblerMode getModeByIndex(int ordinal) {
        return DisassemblerMode.byIndexStatic(ordinal);
    }

    @Override
    public void setMode(ItemStack stack, PlayerEntity player, DisassemblerMode mode) {
        ItemDataUtils.setInt(stack, NBTConstants.MODE, mode.ordinal());
    }

    @Override
    public Class<DisassemblerMode> getModeClass() {
        return DisassemblerMode.class;
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, @Nonnull ItemStack stack) {
        return slot == EquipmentSlotType.MAINHAND ? attributes : super.getAttributeModifiers(slot, stack);
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, PlayerEntity player, ItemStack stack, EquipmentSlotType slotType) {
        DisassemblerMode mode = getMode(stack);
        list.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, EnumColor.INDIGO, mode));
        list.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, mode.getEfficiency()));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        DisassemblerMode mode = getMode(stack);
        DisassemblerMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, player, newMode);
            if (displayChangeMessage) {
                player.sendMessage(MekanismUtils.logFormat(MekanismLang.DISASSEMBLER_MODE_CHANGE.translate(EnumColor.INDIGO, newMode, EnumColor.AQUA,
                      newMode.getEfficiency())), Util.NIL_UUID);
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

    public enum DisassemblerMode implements IDisableableEnum<DisassemblerMode>, IRadialSelectorEnum<DisassemblerMode>, IHasTextComponent {
        NORMAL(MekanismLang.DISASSEMBLER_NORMAL, 20, () -> true, EnumColor.BRIGHT_GREEN, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "disassembler_normal.png")),
        SLOW(MekanismLang.DISASSEMBLER_SLOW, 8, MekanismConfig.gear.disassemblerSlowMode, EnumColor.BRIGHT_GREEN, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "disassembler_slow.png")),
        FAST(MekanismLang.DISASSEMBLER_FAST, 128, MekanismConfig.gear.disassemblerFastMode, EnumColor.BRIGHT_GREEN, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "disassembler_fast.png")),
        VEIN(MekanismLang.DISASSEMBLER_VEIN, 20, MekanismConfig.gear.disassemblerVeinMining, EnumColor.BRIGHT_GREEN, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "disassembler_vein.png")),
        OFF(MekanismLang.DISASSEMBLER_OFF, 0, () -> true, EnumColor.BRIGHT_GREEN, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "void.png"));

        private static final DisassemblerMode[] MODES = values();

        private final BooleanSupplier checkEnabled;
        private final ILangEntry langEntry;
        private final int efficiency;
        private final EnumColor color;
        private final ResourceLocation icon;

        DisassemblerMode(ILangEntry langEntry, int efficiency, BooleanSupplier checkEnabled, EnumColor color, ResourceLocation icon) {
            this.langEntry = langEntry;
            this.efficiency = efficiency;
            this.checkEnabled = checkEnabled;
            this.color = color;
            this.icon = icon;
        }

        /**
         * Gets a Mode from its ordinal. NOTE: if this mode is not enabled then it will reset to NORMAL
         */
        public static DisassemblerMode byIndexStatic(int index) {
            DisassemblerMode mode = MathUtils.getByIndexMod(MODES, index);
            return mode.isEnabled() ? mode : NORMAL;
        }

        @Nonnull
        @Override
        public DisassemblerMode byIndex(int index) {
            //Note: We can't just use byIndexStatic, as we want to be able to return disabled modes
            return MathUtils.getByIndexMod(MODES, index);
        }

        @Override
        public ITextComponent getTextComponent() {
            return langEntry.translate(color);
        }

        @Override
        public ITextComponent getShortText() {
            return getTextComponent();
        }

        public int getEfficiency() {
            return efficiency;
        }

        @Override
        public boolean isEnabled() {
            return checkEnabled.getAsBoolean();
        }

        @Override
        public ResourceLocation getIcon() {
            return icon;
        }

        @Override
        public EnumColor getColor() {
            return color;
        }
    }
}