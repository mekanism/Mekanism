package mekanism.common.item.gear;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemFreeRunners extends ItemSpecialArmor implements IItemHUDProvider, IModeItem {

    private static final FreeRunnerMaterial FREE_RUNNER_MATERIAL = new FreeRunnerMaterial();

    public ItemFreeRunners(Properties properties) {
        this(FREE_RUNNER_MATERIAL, properties);
    }

    public ItemFreeRunners(ArmorMaterial material, Properties properties) {
        super(material, EquipmentSlot.FEET, properties.rarity(Rarity.RARE).setNoRepair());
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(RenderPropertiesProvider.freeRunners());
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
    }

    @Override
    public void fillItemCategory(@Nonnull CreativeModeTab group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
        if (allowdedIn(group)) {
            items.add(StorageUtils.getFilledEnergyVariant(new ItemStack(this), MekanismConfig.gear.freeRunnerMaxEnergy.get()));
        }
    }

    @Override
    public boolean canWalkOnPowderedSnow(@Nonnull ItemStack stack, @Nonnull LivingEntity wearer) {
        return true;
    }

    @Override
    public boolean isBarVisible(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@Nonnull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@Nonnull ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        super.gatherCapabilities(capabilities, stack, nbt);
        capabilities.add(RateLimitEnergyHandler.create(MekanismConfig.gear.freeRunnerChargeRate, MekanismConfig.gear.freeRunnerMaxEnergy,
              BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue));
    }

    public FreeRunnerMode getMode(ItemStack itemStack) {
        return FreeRunnerMode.byIndexStatic(ItemDataUtils.getInt(itemStack, NBTConstants.MODE));
    }

    public void setMode(ItemStack itemStack, FreeRunnerMode mode) {
        ItemDataUtils.setInt(itemStack, NBTConstants.MODE, mode.ordinal());
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        if (slotType == getSlot()) {
            list.add(MekanismLang.FREE_RUNNERS_MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
            StorageUtils.addStoredEnergy(stack, list, true, MekanismLang.FREE_RUNNERS_STORED);
        }
    }

    @Override
    public void changeMode(@Nonnull Player player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        FreeRunnerMode mode = getMode(stack);
        FreeRunnerMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, newMode);
            if (displayChangeMessage) {
                player.sendMessage(MekanismUtils.logFormat(MekanismLang.FREE_RUNNER_MODE_CHANGE.translate(newMode)), Util.NIL_UUID);
            }
        }
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @Nonnull EquipmentSlot slotType) {
        return slotType == getSlot();
    }

    public enum FreeRunnerMode implements IIncrementalEnum<FreeRunnerMode>, IHasTextComponent {
        NORMAL(MekanismLang.FREE_RUNNER_NORMAL, EnumColor.DARK_GREEN),
        DISABLED(MekanismLang.FREE_RUNNER_DISABLED, EnumColor.DARK_RED);

        private static final FreeRunnerMode[] MODES = values();
        private final ILangEntry langEntry;
        private final EnumColor color;

        FreeRunnerMode(ILangEntry langEntry, EnumColor color) {
            this.langEntry = langEntry;
            this.color = color;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Nonnull
        @Override
        public FreeRunnerMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static FreeRunnerMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class FreeRunnerMaterial extends BaseSpecialArmorMaterial {

        @Override
        public String getName() {
            return Mekanism.MODID + ":free_runners";
        }
    }
}