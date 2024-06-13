package mekanism.common.item.gear;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismArmorMaterials;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemFreeRunners extends ItemSpecialArmor implements IItemHUDProvider, ICustomCreativeTabContents, IAttachmentBasedModeItem<FreeRunnerMode> {

    public ItemFreeRunners(Properties properties) {
        this(MekanismArmorMaterials.FREE_RUNNERS, properties);
    }

    public ItemFreeRunners(Holder<ArmorMaterial> material, Properties properties) {
        super(material, ArmorItem.Type.BOOTS, properties.rarity(Rarity.RARE).setNoRepair().component(MekanismDataComponents.FREE_RUNNER_MODE, FreeRunnerMode.NORMAL));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.freeRunners());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
    }

    @Override
    public void addItems(Consumer<ItemStack> tabOutput) {
        tabOutput.accept(StorageUtils.getFilledEnergyVariant(this));
    }

    @Override
    public boolean canWalkOnPowderedSnow(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return true;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    private int getFakeEnchantmentLevel(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.SOUL_SPEED) && getMode(stack) == FreeRunnerMode.NORMAL) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getEnchantmentLevel(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return Math.max(getFakeEnchantmentLevel(stack, enchantment), super.getEnchantmentLevel(stack, enchantment));
    }

    @NotNull
    @Override
    public ItemEnchantments getAllEnchantments(@NotNull ItemStack stack) {
        ItemEnchantments enchantments = super.getAllEnchantments(stack);
        //TODO - 1.21: Re-enable after https://github.com/neoforged/NeoForge/pull/1089
        /*int fakeLevel = getFakeEnchantmentLevel(stack, Enchantments.SOUL_SPEED);
        if (enchantments.getLevel(Enchantments.SOUL_SPEED) < fakeLevel) {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
            mutable.set(Enchantments.SOUL_SPEED, fakeLevel);
            return mutable.toImmutable();
        }*/
        return enchantments;
    }

    @Override
    public DataComponentType<FreeRunnerMode> getModeDataType() {
        return MekanismDataComponents.FREE_RUNNER_MODE.get();
    }

    @Override
    public FreeRunnerMode getDefaultMode() {
        return FreeRunnerMode.NORMAL;
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        if (slotType == getEquipmentSlot()) {
            list.add(MekanismLang.FREE_RUNNERS_MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
            StorageUtils.addStoredEnergy(stack, list, true, MekanismLang.FREE_RUNNERS_STORED);
        }
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        FreeRunnerMode mode = getMode(stack);
        FreeRunnerMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, player, newMode);
            displayChange.sendMessage(player, newMode, MekanismLang.FREE_RUNNER_MODE_CHANGE::translate);
        }
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        return slotType == getEquipmentSlot();
    }

    @NothingNullByDefault
    public enum FreeRunnerMode implements IIncrementalEnum<FreeRunnerMode>, IHasTextComponent, StringRepresentable {
        NORMAL(MekanismLang.FREE_RUNNER_NORMAL, EnumColor.DARK_GREEN, true, true),
        SAFETY(MekanismLang.FREE_RUNNER_SAFETY, EnumColor.ORANGE, true, false),
        DISABLED(MekanismLang.FREE_RUNNER_DISABLED, EnumColor.DARK_RED, false, false);

        public static final Codec<FreeRunnerMode> CODEC = StringRepresentable.fromEnum(FreeRunnerMode::values);
        public static final IntFunction<FreeRunnerMode> BY_ID = ByIdMap.continuous(FreeRunnerMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, FreeRunnerMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FreeRunnerMode::ordinal);

        private final String serializedName;
        private final boolean preventsFallDamage;
        private final boolean providesStepBoost;
        private final ILangEntry langEntry;
        private final EnumColor color;

        FreeRunnerMode(ILangEntry langEntry, EnumColor color, boolean preventsFallDamage, boolean providesStepBoost) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.preventsFallDamage = preventsFallDamage;
            this.providesStepBoost = providesStepBoost;
            this.langEntry = langEntry;
            this.color = color;
        }

        public boolean preventsFallDamage() {
            return preventsFallDamage;
        }

        public boolean providesStepBoost() {
            return providesStepBoost;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Override
        public FreeRunnerMode byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}