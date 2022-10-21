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
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IDisableableEnum;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekatool.ModuleExcavationEscalationUnit.ExcavationMode;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.gear.ItemAtomicDisassembler.DisassemblerMode;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.common.lib.radial.IRadialEnumModeItem;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemAtomicDisassembler extends ItemEnergized implements IItemHUDProvider, IRadialEnumModeItem<DisassemblerMode>, IAttributeRefresher {

    //All basic dig actions except shears
    public static final Set<ToolAction> ALWAYS_SUPPORTED_ACTIONS = Set.of(ToolActions.AXE_DIG, ToolActions.HOE_DIG, ToolActions.SHOVEL_DIG, ToolActions.PICKAXE_DIG,
          ToolActions.SWORD_DIG);
    private static final Lazy<RadialData<DisassemblerMode>> LAZY_RADIAL_DATA = Lazy.of(() ->
          MekanismAPI.getRadialDataHelper().dataForEnum(Mekanism.rl("disassembler_mode"), DisassemblerMode.NORMAL));

    private final AttributeCache attributeCache;

    public ItemAtomicDisassembler(Properties properties) {
        super(MekanismConfig.gear.disassemblerChargeRate, MekanismConfig.gear.disassemblerMaxEnergy, properties.rarity(Rarity.RARE).setNoRepair());
        this.attributeCache = new AttributeCache(this, MekanismConfig.gear.disassemblerMaxDamage, MekanismConfig.gear.disassemblerAttackSpeed);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.disassembler());
    }

    @Override
    public boolean isCorrectToolForDrops(@NotNull BlockState state) {
        //Allow harvesting everything, things that are unbreakable are caught elsewhere
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
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
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null && !energyContainer.isEmpty()) {
            //Try to extract full energy, even if we have a lower damage amount this is fine as that just means
            // we don't have enough energy, but we will remove as much as we can, which is how much corresponds
            // to the amount of damage we will actually do
            energyContainer.extract(MekanismConfig.gear.disassemblerEnergyUsageWeapon.get(), Action.EXECUTE, AutomationType.MANUAL);
        }
        return true;
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
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
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level world, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entityliving) {
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
    public String getModeSaveKey() {
        return NBTConstants.MODE;
    }

    @Override
    public DisassemblerMode getModeByIndex(int ordinal) {
        return DisassemblerMode.byIndexStatic(ordinal);
    }

    @NotNull
    @Override
    public RadialData<DisassemblerMode> getRadialData(ItemStack stack) {
        return LAZY_RADIAL_DATA.get();
    }

    @NotNull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            FloatingLong energy = energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
            FloatingLong energyCost = MekanismConfig.gear.disassemblerEnergyUsageWeapon.get();
            if (energy.greaterOrEqual(energyCost)) {
                //If we have enough energy to act at full damage, use the cached multimap rather than creating a new one
                // This will be the case the vast majority of the time
                return attributeCache.get();
            }
            //If we don't have enough power use it at a reduced power level
            int minDamage = MekanismConfig.gear.disassemblerMinDamage.get();
            int damageDifference = MekanismConfig.gear.disassemblerMaxDamage.get() - minDamage;
            double damage = minDamage + damageDifference * energy.divideToLevel(energyCost);
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", damage, Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", MekanismConfig.gear.disassemblerAttackSpeed.get(), Operation.ADDITION));
            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public void addToBuilder(Builder<Attribute, AttributeModifier> builder) {
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", MekanismConfig.gear.disassemblerMaxDamage.get(), Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", MekanismConfig.gear.disassemblerAttackSpeed.get(), Operation.ADDITION));
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        DisassemblerMode mode = getMode(stack);
        list.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, EnumColor.INDIGO, mode));
        list.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, mode.getEfficiency()));
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, boolean displayChangeMessage) {
        DisassemblerMode mode = getMode(stack);
        DisassemblerMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, player, newMode);
            if (displayChangeMessage) {
                player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.DISASSEMBLER_MODE_CHANGE.translate(EnumColor.INDIGO, newMode, EnumColor.AQUA,
                      newMode.getEfficiency())));
            }
        }
    }

    @NotNull
    @Override
    public Component getScrollTextComponent(@NotNull ItemStack stack) {
        DisassemblerMode mode = getMode(stack);
        return MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.INDIGO, mode, EnumColor.AQUA, mode.getEfficiency());
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

    @NothingNullByDefault
    public enum DisassemblerMode implements IDisableableEnum<DisassemblerMode>, IHasTextComponent, IRadialMode {
        NORMAL(MekanismLang.RADIAL_EXCAVATION_SPEED_NORMAL, 20, () -> true, EnumColor.BRIGHT_GREEN, ExcavationMode.NORMAL.icon()),
        SLOW(MekanismLang.RADIAL_EXCAVATION_SPEED_SLOW, 8, MekanismConfig.gear.disassemblerSlowMode, EnumColor.PINK, ExcavationMode.SLOW.icon()),
        //Note: Uses extreme icon as both are efficiency 128
        FAST(MekanismLang.RADIAL_EXCAVATION_SPEED_FAST, 128, MekanismConfig.gear.disassemblerFastMode, EnumColor.RED, ExcavationMode.EXTREME.icon()),
        VEIN(MekanismLang.RADIAL_VEIN_NORMAL, 20, MekanismConfig.gear.disassemblerVeinMining, EnumColor.AQUA, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_RADIAL, "vein_normal.png")),
        OFF(MekanismLang.RADIAL_EXCAVATION_SPEED_OFF, 0, () -> true, EnumColor.WHITE, ExcavationMode.OFF.icon());

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

        @Override
        public DisassemblerMode byIndex(int index) {
            //Note: We can't just use byIndexStatic, as we want to be able to return disabled modes
            return MathUtils.getByIndexMod(MODES, index);
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translate(color);
        }

        @NotNull
        @Override
        public Component sliceName() {
            return getTextComponent();
        }

        public int getEfficiency() {
            return efficiency;
        }

        @Override
        public boolean isEnabled() {
            return checkEnabled.getAsBoolean();
        }

        @NotNull
        @Override
        public ResourceLocation icon() {
            return icon;
        }

        @Override
        public EnumColor color() {
            return color;
        }
    }
}