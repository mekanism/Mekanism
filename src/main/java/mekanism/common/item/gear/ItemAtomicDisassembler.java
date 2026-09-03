package mekanism.common.item.gear;

import com.mojang.math.Constants;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMaps;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.api.IDisableableEnum;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.AutomatedEnergyHandler;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekatool.ModuleExcavationEscalationUnit.ExcavationMode;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.gear.ItemAtomicDisassembler.DisassemblerMode;
import mekanism.common.item.interfaces.IHasConditionalAttributes;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.lib.radial.IRadialModeItem;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ItemAtomicDisassembler extends ItemEnergized implements IItemHUDProvider, IRadialModeItem<DisassemblerMode>, IHasConditionalAttributes {

    //All basic dig actions except shears
    //TODO - 26.2: review why these not longer exist: https://github.com/neoforged/NeoForge/issues/3112
    public static final Set<ItemAbility> ALWAYS_SUPPORTED_ACTIONS = Set.of(/*ItemAbilities.AXE_DIG, ItemAbilities.HOE_DIG, ItemAbilities.SHOVEL_DIG, ItemAbilities.PICKAXE_DIG,
          ItemAbilities.SWORD_DIG*/);
    private static final Lazy<RadialData<DisassemblerMode>> LAZY_RADIAL_DATA = Lazy.of(() ->
          IRadialDataHelper.INSTANCE.dataForEnum(Mekanism.rl("disassembler_mode"), DisassemblerMode.NORMAL));

    /// @apiNote For use in calculating drops of given blocks. Given mods may do checks relating to tool actions we need to make sure that this stack is full energy.
    public static ItemStack fullyChargedStack(@Nullable TransactionContext transaction) {
        return ContainerType.ENERGY.getFilledVariant(MekanismItems.ATOMIC_DISASSEMBLER, transaction);
    }

    public ItemAtomicDisassembler(Properties properties) {
        //TODO - 26.2: Re-evaluate uses of setNoCombineRepair and see if any of them are not actually needed
        super(properties.rarity(Rarity.RARE).setNoCombineRepair().stacksTo(1)
              .component(MekanismDataComponents.DISASSEMBLER_MODE, DisassemblerMode.NORMAL)
              .delayedComponent(DataComponents.TOOL, context -> new Tool(List.of(
                    Tool.Rule.deniesDrops(context.getOrThrow(MekanismTags.Blocks.INCORRECT_FOR_DISASSEMBLER)),
                    new Tool.Rule(new AnyHolderSet<>(BuiltInRegistries.BLOCK), Optional.empty(), Optional.of(true))
              ), 1, 0, true))
        );
    }

    @Override
    public boolean canPerformAction(ItemInstance instance, ItemAbility action) {
        if (ALWAYS_SUPPORTED_ACTIONS.contains(action)) {
            EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccessUtils.sideEffectFreeAccess(instance));
            if (energyHandler != null) {
                //Note: We use a hardness of zero here as that will get the minimum potential destroy energy required
                // as that is the best guess we can currently give whether the corresponding dig action is supported
                int energyRequired = getDestroyEnergy(instance, 0);
                int energyAvailable = energyHandler.getAmountAsInt();
                //If we don't have enough energy to break at full speed check if the reduced speed could actually mine
                return energyRequired <= energyAvailable || energyAvailable / (double) energyRequired > Constants.EPSILON;
            }
        }
        return false;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        EnergyHandler energyHandler = AutomatedEnergyHandler.manual(Capabilities.ENERGY.getCapability(ItemAccess.forStack(stack)));
        if (energyHandler != null) {
            //Try to extract full energy, even if we have a lower damage amount this is fine as that just means
            // we don't have enough energy, but we will remove as much as we can, which is how much corresponds
            // to the amount of damage we will actually do
            //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
            try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                energyHandler.extract(MekanismConfig.gear.disassemblerEnergyUsageWeapon.get(), transaction);
                transaction.commit();
            }
        }
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        EnergyHandler energyHandler = AutomatedEnergyHandler.manual(Capabilities.ENERGY.getCapability(ItemAccessUtils.sideEffectFreeAccess(stack)));
        if (energyHandler == null) {
            return 0;
        }
        //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
        try (Transaction simulation = TransactionHelper.openTransactionSafe()) {
            //Use raw hardness to get the best guess of if it is zero or not
            int energyRequired = getDestroyEnergy(stack, state.destroySpeed);
            int energyAvailable = energyHandler.extract(energyRequired, simulation);
            if (energyAvailable < energyRequired) {
                //If we can't extract all the energy we need to break it go at base speed reduced by how much we actually have available
                return DisassemblerMode.NORMAL.getEfficiency() * ((float) energyAvailable / energyRequired);
            }
        }
        return getMode(stack).getEfficiency();
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        EnergyHandler energyHandler = AutomatedEnergyHandler.manual(Capabilities.ENERGY.getCapability(ItemAccess.forStack(stack)));
        if (energyHandler != null) {
            int baseDestroyEnergy = getDestroyEnergy(stack);
            int energyRequired = getDestroyEnergy(baseDestroyEnergy, state.getDestroySpeed(world, pos));
            //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
            try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                energyHandler.extract(energyRequired, transaction);
                //Vein mining handling
                if (!world.isClientSide() && entity instanceof ServerPlayer player && !player.isCreative() && getMode(stack) == DisassemblerMode.VEIN) {
                    boolean hasEnergyToVeinMine;
                    try (Transaction simulation = Transaction.open(transaction)) {
                        hasEnergyToVeinMine = energyHandler.extract(energyRequired, simulation) == energyRequired;
                    }
                    // Only allow mining things that are considered an ore
                    if (hasEnergyToVeinMine && ModuleVeinMiningUnit.canVeinBlock(state) && state.is(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE)) {
                        Object2IntMap<BlockPos> found = ModuleVeinMiningUnit.findPositions(world, Map.of(pos, state), 0,
                              Reference2BooleanMaps.singleton(state.getBlock(), true));
                        MekanismUtils.veinMineArea(energyHandler, 0, baseDestroyEnergy, world, pos, player, stack, this, found, transaction,
                              (_, _) -> 0,
                              (base, hardness, distance, _) -> Mth.ceil(getDestroyEnergy(base, hardness) * (0.5 * Math.pow(distance, 1.5))));
                    }
                }
                transaction.commit();
            }
        }
        return true;
    }

    private int getDestroyEnergy(ItemInstance itemStack, float hardness) {
        return getDestroyEnergy(getDestroyEnergy(itemStack), hardness);
    }

    private static int getDestroyEnergy(int baseDestroyEnergy, float hardness) {
        return hardness == 0 ? Math.max(baseDestroyEnergy / 2, 1) : baseDestroyEnergy;
    }

    private int getDestroyEnergy(ItemInstance itemStack) {
        return MathUtils.multiplyClamped(MekanismConfig.gear.disassemblerEnergyUsage.get(), getMode(itemStack).getEfficiency());
    }

    @Override
    public DataComponentType<DisassemblerMode> getModeDataType() {
        return MekanismDataComponents.DISASSEMBLER_MODE.get();
    }

    @Override
    public DisassemblerMode getDefaultMode() {
        return DisassemblerMode.NORMAL;
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> RadialData<DisassemblerMode> getRadialData(ITEM instance) {
        return LAZY_RADIAL_DATA.get();
    }

    @Override
    public void adjustAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccessUtils.sideEffectFreeAccess(stack));
        int energy = energyHandler == null ? 0 : energyHandler.getAmountAsInt();
        int energyCost = MekanismConfig.gear.disassemblerEnergyUsageWeapon.get();
        double damage = MekanismConfig.gear.disassemblerMaxDamage.get();
        double attackSpeed = MekanismConfig.gear.disassemblerAttackSpeed.get();
        if (energy < energyCost) {
            //If we don't have enough power use it at a reduced power level
            int minDamage = MekanismConfig.gear.disassemblerMinDamage.get();
            int damageDifference = MekanismConfig.gear.disassemblerMaxDamage.get() - minDamage;
            damage = minDamage + damageDifference * MathUtils.divideToLevel(energy, energyCost);
        }
        //Replace any existing value that might have been set via NBT, as we want to tbe the ones handling the scaling based on the config
        event.replaceModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        event.replaceModifier(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDStrings(List<Component> list, Player player, ITEM instance, EquipmentSlot slotType) {
        DisassemblerMode mode = getMode(instance);
        list.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, EnumColor.INDIGO, mode));
        list.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, mode.getEfficiency()));
    }

    @Override
    public void changeMode(Player player, ItemAccess itemAccess, int shift, DisplayChange displayChange, TransactionContext transaction) {
        DisassemblerMode mode = getMode(itemAccess);
        DisassemblerMode newMode = mode.adjust(shift);
        if (mode != newMode && setMode(itemAccess, player, newMode, transaction)) {
            displayChange.sendMessage(player, newMode, m -> MekanismLang.DISASSEMBLER_MODE_CHANGE.translate(EnumColor.INDIGO, m, EnumColor.AQUA, m.getEfficiency()));
        }
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> Component getScrollTextComponent(ITEM instance) {
        DisassemblerMode mode = getMode(instance);
        return MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.INDIGO, mode, EnumColor.AQUA, mode.getEfficiency());
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.has(DataComponents.ENCHANTABLE) && super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.has(DataComponents.ENCHANTABLE) && super.supportsEnchantment(stack, enchantment);
    }

    public enum DisassemblerMode implements IDisableableEnum<DisassemblerMode>, IHasEnumNameTextComponent, IRadialMode, StringRepresentable, TooltipProvider {
        NORMAL(MekanismLang.RADIAL_EXCAVATION_SPEED_NORMAL, 20, ConstantPredicates.ALWAYS_TRUE, EnumColor.BRIGHT_GREEN, ExcavationMode.NORMAL.icon()),
        SLOW(MekanismLang.RADIAL_EXCAVATION_SPEED_SLOW, 8, MekanismConfig.gear.disassemblerSlowMode, EnumColor.PINK, ExcavationMode.SLOW.icon()),
        //Note: Uses extreme icon as both are efficiency 128
        FAST(MekanismLang.RADIAL_EXCAVATION_SPEED_FAST, 128, MekanismConfig.gear.disassemblerFastMode, EnumColor.RED, ExcavationMode.EXTREME.icon()),
        VEIN(MekanismLang.RADIAL_VEIN_NORMAL, 20, MekanismConfig.gear.disassemblerVeinMining, EnumColor.AQUA, Mekanism.rl("radial/vein_normal")),
        OFF(MekanismLang.RADIAL_EXCAVATION_SPEED_OFF, 0, ConstantPredicates.ALWAYS_TRUE, EnumColor.WHITE, ExcavationMode.OFF.icon());

        //We only allow deserializing to enabled modes
        public static final Codec<DisassemblerMode> CODEC = StringRepresentable.fromEnum(DisassemblerMode::values)
              .xmap(mode -> mode.isEnabled() ? mode : NORMAL, Function.identity());
        public static final IntFunction<DisassemblerMode> BY_ID = ByIdMap.continuous(DisassemblerMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        //Though we allow network handling to sync it whether it is enabled or not
        public static final StreamCodec<ByteBuf, DisassemblerMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DisassemblerMode::ordinal);

        private final String serializedName;
        private final BooleanSupplier checkEnabled;
        private final ILangEntry langEntry;
        private final int efficiency;
        private final EnumColor color;
        private final Identifier icon;

        DisassemblerMode(ILangEntry langEntry, int efficiency, BooleanSupplier checkEnabled, EnumColor color, Identifier icon) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
            this.efficiency = efficiency;
            this.checkEnabled = checkEnabled;
            this.color = color;
            this.icon = icon;
        }

        @Override
        public DisassemblerMode byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translate(color);
        }

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

        @Override
        public Identifier icon() {
            return icon;
        }

        @Override
        public EnumColor color() {
            return color;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        @Override
        public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
            builder.accept(MekanismLang.MODE.translateColored(EnumColor.INDIGO, this));
            builder.accept(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.INDIGO, getEfficiency()));
        }
    }
}