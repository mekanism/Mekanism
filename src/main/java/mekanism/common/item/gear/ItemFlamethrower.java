package mekanism.common.item.gear;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.AutomatedResourceHandler;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.item.interfaces.IChemicalItem;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.Stats;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ItemFlamethrower extends Item implements IItemHUDProvider, IChemicalItem, ICustomCreativeTabContents, IAttachmentBasedModeItem<FlamethrowerMode> {

    public ItemFlamethrower(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE).setNoCombineRepair()
              .component(MekanismDataComponents.FLAMETHROWER_MODE, FlamethrowerMode.COMBAT)
        );
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        StorageUtils.addStoredChemical(ItemAccessUtils.sideEffectFreeAccess(stack), tooltipAdder);
        tooltipAdder.accept(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack)));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return APPROXIMATELY_INFINITE_USE_DURATION;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && hasChemical(context.getItemInHand())) {
            player.startUsingItem(context.getHand());
            return InteractionResult.CONSUME;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hasChemical(ItemAccessUtils.playerHandAccess(player, hand))) {
            player.awardStat(Stats.ITEM_USED.get(this));
            player.startUsingItem(hand);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingDuration) {
        //TODO: Do we want to allow non players to use the flamethrower?
        if (remainingDuration >= 0 && entity instanceof Player player) {
            //If the flamethrower has gas, add the entity if we are on the server and use gas if we aren't creative
            ResourceHandler<ChemicalResource> chemicalHandler = AutomatedResourceHandler.manual(Capabilities.CHEMICAL.getCapability(ItemAccess.forStack(stack)));
            if (chemicalHandler != null) {
                //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
                try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                    if (chemicalHandler.extract(ChemicalResource.of(getChemicalType()), 1, transaction) == 1) {
                        if (!level.isClientSide()) {
                            EntityFlame flame = EntityFlame.create(level, entity, entity.getUsedItemHand(), getMode(stack));
                            if (flame != null) {
                                if (flame.isAlive()) {
                                    //If the flame is alive (and didn't just instantly hit a block while trying to spawn add it to the world)
                                    level.addFreshEntity(flame);
                                }
                                if (MekanismUtils.isPlayingMode(player)) {
                                    //Only consume fuel if the player is actually playing and isn't in creative
                                    transaction.commit();
                                }
                            }
                        }
                        return;
                    }
                }
            }
        }
        //If the flamethrower runs out of gas, make it act as if the entity stopped using the item
        // Have this happen on both the server and the client
        entity.releaseUsingItem();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return StorageUtils.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ContainerType.CHEMICAL.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(ContainerType.CHEMICAL.getFilledVariant(item, getChemicalType(), null));
    }

    private Holder<Chemical> getChemicalType() {
        return MekanismChemicals.HYDROGEN;
    }

    @Override
    public boolean hasChemical(ItemAccess itemAccess) {
        return ChemicalUtils.hasChemicalOfType(itemAccess, getChemicalType());
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
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDStrings(List<Component> list, Player player, ITEM instance, EquipmentSlot slotType) {
        long stored = 0;
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(ItemAccessUtils.sideEffectFreeAccess(instance));
        if (handler != null && handler.size() > 0) {
            //Validate something didn't go terribly wrong, and we actually do have the tank we expect to have
            stored = handler.getAmountAsLong(0);
            if (stored > 0) {
                list.add(MekanismLang.FLAMETHROWER_STORED.translateColored(EnumColor.GRAY, EnumColor.ORANGE, stored));
            }
        }
        if (stored == 0) {
            list.add(MekanismLang.FLAMETHROWER_STORED.translateColored(EnumColor.GRAY, EnumColor.ORANGE, MekanismLang.NO_CHEMICAL));
        }
        list.add(MekanismLang.MODE.translate(getMode(instance)));
    }

    @Override
    public void changeMode(Player player, ItemAccess itemAccess, int shift, DisplayChange displayChange, TransactionContext transaction) {
        FlamethrowerMode mode = getMode(itemAccess);
        FlamethrowerMode newMode = mode.adjust(shift);
        if (mode != newMode && setMode(itemAccess, player, newMode, transaction)) {
            displayChange.sendMessage(player, newMode, MekanismLang.FLAMETHROWER_MODE_CHANGE::translate);
        }
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> Component getScrollTextComponent(ITEM instance) {
        return getMode(instance).getTextComponent();
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.has(DataComponents.ENCHANTABLE) && super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.has(DataComponents.ENCHANTABLE) && super.supportsEnchantment(stack, enchantment);
    }

    public static boolean isIdleFlamethrower(Player player, InteractionHand hand) {
        ItemAccess itemAccess = ItemAccessUtils.playerHandAccess(player, hand);
        //If a flamethrower has no gas it can't be idle
        return itemAccess.getResource().getItem() instanceof ItemFlamethrower flamethrower && flamethrower.hasChemical(itemAccess);
    }

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