package mekanism.common.item.gear;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.item.interfaces.IChemicalItem;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.Stats;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemFlamethrower extends Item implements IItemHUDProvider, IChemicalItem, ICustomCreativeTabContents, IAttachmentBasedModeItem<FlamethrowerMode> {

    public ItemFlamethrower(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE).setNoRepair()
              .component(MekanismDataComponents.FLAMETHROWER_MODE, FlamethrowerMode.COMBAT)
        );
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredChemical(stack, tooltip);
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack)));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        //Note: This is the same value as tridents and shields use. Technically we would be fine with them using it forever, but this is fine
        // while we could base this on how much gas is stored in the flamethrower, then if something is filling it while using, then it will be wrong
        //Secondary note: When this does run out the use animation briefly resets so the item renders slightly different for a second,
        // but I don't think it is worth trying to fix given how long a use duration we have
        return 60 * SharedConstants.TICKS_PER_MINUTE;
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && hasChemical(context.getItemInHand())) {
            player.startUsingItem(context.getHand());
            return InteractionResult.CONSUME;
        }
        return super.useOn(context);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hasChemical(stack)) {
            player.awardStat(Stats.ITEM_USED.get(this));
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int remainingDuration) {
        //TODO: Do we want to allow non players to use the flamethrower?
        if (remainingDuration >= 0 && entity instanceof Player player) {
            //If the flamethrower has gas, add the entity if we are on the server and use gas if we aren't creative
            if (hasChemical(stack)) {
                if (!level.isClientSide) {
                    EntityFlame flame = EntityFlame.create(level, entity, entity.getUsedItemHand(), getMode(stack));
                    if (flame != null) {
                        if (flame.isAlive()) {
                            //If the flame is alive (and didn't just instantly hit a block while trying to spawn add it to the world)
                            level.addFreshEntity(flame);
                        }
                        if (MekanismUtils.isPlayingMode(player)) {
                            useChemical(stack, 1);
                        }
                    }
                }
            } else {
                //If the flamethrower runs out of gas, make it act as if the entity stopped using the item
                // Have this happen on both the server and the client
                entity.releaseUsingItem();
            }
        } else {
            entity.releaseUsingItem();
        }
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return ChemicalUtil.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(ChemicalUtil.getFilledVariant(item, MekanismChemicals.HYDROGEN));
    }

    @Override
    public DataComponentType<FlamethrowerMode> getModeDataType() {
        return MekanismDataComponents.FLAMETHROWER_MODE.get();
    }

    @Override
    public FlamethrowerMode getDefaultMode() {
        return FlamethrowerMode.COMBAT;
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        boolean hasGas = false;
        IChemicalHandler gasHandlerItem = Capabilities.CHEMICAL.getCapability(stack);
        if (gasHandlerItem != null && gasHandlerItem.getChemicalTanks() > 0) {
            //Validate something didn't go terribly wrong, and we actually do have the tank we expect to have
            ChemicalStack storedGas = gasHandlerItem.getChemicalInTank(0);
            if (!storedGas.isEmpty()) {
                list.add(MekanismLang.FLAMETHROWER_STORED.translateColored(EnumColor.GRAY, EnumColor.ORANGE, storedGas.getAmount()));
                hasGas = true;
            }
        }
        if (!hasGas) {
            list.add(MekanismLang.FLAMETHROWER_STORED.translateColored(EnumColor.GRAY, EnumColor.ORANGE, MekanismLang.NO_CHEMICAL));
        }
        list.add(MekanismLang.MODE.translate(getMode(stack)));
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        FlamethrowerMode mode = getMode(stack);
        FlamethrowerMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, player, newMode);
            displayChange.sendMessage(player, newMode, MekanismLang.FLAMETHROWER_MODE_CHANGE::translate);
        }
    }

    @NotNull
    @Override
    public Component getScrollTextComponent(@NotNull ItemStack stack) {
        return getMode(stack).getTextComponent();
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

    public static boolean isIdleFlamethrower(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        //If a flamethrower has no gas it can't be idle
        return !stack.isEmpty() && stack.getItem() instanceof ItemFlamethrower && ChemicalUtil.hasAnyChemical(stack);
    }

    @NothingNullByDefault
    public enum FlamethrowerMode implements IIncrementalEnum<FlamethrowerMode>, IHasEnumNameTextComponent, StringRepresentable {
        COMBAT(MekanismLang.FLAMETHROWER_COMBAT, EnumColor.YELLOW),
        HEAT(MekanismLang.FLAMETHROWER_HEAT, EnumColor.ORANGE),
        INFERNO(MekanismLang.FLAMETHROWER_INFERNO, EnumColor.DARK_RED);

        public static final Codec<FlamethrowerMode> CODEC = StringRepresentable.fromEnum(FlamethrowerMode::values);
        public static final IntFunction<FlamethrowerMode> BY_ID = ByIdMap.continuous(FlamethrowerMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, FlamethrowerMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FlamethrowerMode::ordinal);

        private final ILangEntry langEntry;
        private final String serializedName;
        private final EnumColor color;

        FlamethrowerMode(ILangEntry langEntry, EnumColor color) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
            this.color = color;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Override
        public FlamethrowerMode byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}