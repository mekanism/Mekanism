package mekanism.common.item.gear;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.armor.CustomArmor;
import mekanism.client.render.armor.FreeRunnerArmor;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemFreeRunners extends ItemSpecialArmor implements IItemHUDProvider, IModeItem {

    private static final FreeRunnerMaterial FREE_RUNNER_MATERIAL = new FreeRunnerMaterial();

    public ItemFreeRunners(Properties properties) {
        super(FREE_RUNNER_MATERIAL, EquipmentSlotType.FEET, properties.rarity(Rarity.RARE).setNoRepair().setISTER(ISTERProvider::freeRunners));
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public CustomArmor getGearModel() {
        return FreeRunnerArmor.FREE_RUNNERS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
            items.add(StorageUtils.getFilledEnergyVariant(new ItemStack(this), MekanismConfig.gear.freeRunnerMaxEnergy.get()));
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getEnergyDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(MekanismConfig.gear.freeRunnerChargeRate, MekanismConfig.gear.freeRunnerMaxEnergy,
              BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue));
    }

    public FreeRunnerMode getMode(ItemStack itemStack) {
        return FreeRunnerMode.byIndexStatic(ItemDataUtils.getInt(itemStack, NBTConstants.MODE));
    }

    public void setMode(ItemStack itemStack, FreeRunnerMode mode) {
        ItemDataUtils.setInt(itemStack, NBTConstants.MODE, mode.ordinal());
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, ItemStack stack, EquipmentSlotType slotType) {
        if (slotType == getEquipmentSlot()) {
            list.add(MekanismLang.FREE_RUNNERS_MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
            StorageUtils.addStoredEnergy(stack, list, true, MekanismLang.FREE_RUNNERS_STORED);
        }
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        FreeRunnerMode mode = getMode(stack);
        FreeRunnerMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, newMode);
            if (displayChangeMessage) {
                player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY,
                      MekanismLang.FREE_RUNNER_MODE_CHANGE.translate(newMode)), Util.DUMMY_UUID);
            }
        }
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @Nonnull EquipmentSlotType slotType) {
        return slotType == getEquipmentSlot();
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
        public ITextComponent getTextComponent() {
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