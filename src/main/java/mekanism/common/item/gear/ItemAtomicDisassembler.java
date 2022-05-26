package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IDisableableEnum;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.gear.ItemAtomicDisassembler.DisassemblerMode;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IRadialModeItem;
import mekanism.common.item.interfaces.IRadialSelectorEnum;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class ItemAtomicDisassembler extends ItemEnergized implements IItemHUDProvider, IRadialModeItem<DisassemblerMode> {

    //All basic dig actions except shears
    public static final Set<ToolAction> ALWAYS_SUPPORTED_ACTIONS = Set.of(ToolActions.AXE_DIG, ToolActions.HOE_DIG, ToolActions.SHOVEL_DIG, ToolActions.PICKAXE_DIG,
          ToolActions.SWORD_DIG);

    private final Multimap<Attribute, AttributeModifier> attributes;

    public ItemAtomicDisassembler(Properties properties) {
        super(MekanismConfig.gear.disassemblerChargeRate, MekanismConfig.gear.disassemblerMaxEnergy, properties.rarity(Rarity.RARE).setNoRepair());
        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4D, Operation.ADDITION));
        this.attributes = builder.build();
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(RenderPropertiesProvider.disassembler());
    }

    @Override
    public boolean isCorrectToolForDrops(@Nonnull BlockState state) {
        //Allow harvesting everything, things that are unbreakable are caught elsewhere
        return true;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        DisassemblerMode mode = getMode(stack);
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.INDIGO, mode));
        tooltip.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.INDIGO, mode.getEfficiency()));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return ALWAYS_SUPPORTED_ACTIONS.contains(action);
    }

    @Override
    public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull Entity target) {
        //If it is a vehicle that we want to damage
        if (target.getType().is(MekanismTags.Entities.HURTABLE_VEHICLES)) {
            if (target.isAttackable() && !target.skipAttackInteraction(player)) {
                //Always apply max damage to vehicles that can be hurt regardless of energy and don't actually
                target.hurt(DamageSource.playerAttack(player), MekanismConfig.gear.disassemblerMaxDamage.get());
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
        FloatingLong energyCost = MekanismConfig.gear.disassemblerEnergyUsageWeapon.get();
        int minDamage = MekanismConfig.gear.disassemblerMinDamage.get();
        int damageDifference = MekanismConfig.gear.disassemblerMaxDamage.get() - minDamage;
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
    public boolean mineBlock(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull LivingEntity entityliving) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null) {
            energyContainer.extract(getDestroyEnergy(stack, state.getDestroySpeed(world, pos)), Action.EXECUTE, AutomationType.MANUAL);
        }
        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.level.isClientSide || player.isCreative()) {
            return super.onBlockStartBreak(stack, pos, player);
        }
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null && getMode(stack) == DisassemblerMode.VEIN) {
            Level world = player.level;
            BlockState state = world.getBlockState(pos);
            FloatingLong baseDestroyEnergy = getDestroyEnergy(stack);
            FloatingLong energyRequired = getDestroyEnergy(baseDestroyEnergy, state.getDestroySpeed(world, pos));
            if (energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL).greaterOrEqual(energyRequired)) {
                // Only allow mining things that are considered an ore
                if (ModuleVeinMiningUnit.canVeinBlock(state) && state.is(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE)) {
                    Object2IntMap<BlockPos> found = ModuleVeinMiningUnit.findPositions(world, Map.of(pos, state), 0, Object2BooleanMaps.singleton(state.getBlock(), true));
                    MekanismUtils.veinMineArea(energyContainer, energyRequired, world, pos, (ServerPlayer) player, stack, this, found, hardness -> FloatingLong.ZERO,
                          (hardness, distance, bs) -> getDestroyEnergy(baseDestroyEnergy, hardness).multiply(0.5 * Math.pow(distance, 1.5)));
                }
            }
        }
        return super.onBlockStartBreak(stack, pos, player);
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
    public void setMode(ItemStack stack, Player player, DisassemblerMode mode) {
        ItemDataUtils.setInt(stack, NBTConstants.MODE, mode.ordinal());
    }

    @Override
    public Class<DisassemblerMode> getModeClass() {
        return DisassemblerMode.class;
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, @Nonnull ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? attributes : super.getAttributeModifiers(slot, stack);
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        DisassemblerMode mode = getMode(stack);
        list.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, EnumColor.INDIGO, mode));
        list.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, mode.getEfficiency()));
    }

    @Override
    public void changeMode(@Nonnull Player player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
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
    public Component getScrollTextComponent(@Nonnull ItemStack stack) {
        DisassemblerMode mode = getMode(stack);
        return MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.INDIGO, mode, EnumColor.AQUA, mode.getEfficiency());
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
        public Component getTextComponent() {
            return langEntry.translate(color);
        }

        @Override
        public Component getShortText() {
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