package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.IItemHUDProvider;
import mekanism.common.item.IModeItem;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.GasUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemFlamethrower extends Item implements IItemHUDProvider, IModeItem {

    public ItemFlamethrower(Properties properties) {
        super(properties.maxStackSize(1).setNoRepair().setISTER(ISTERProvider::flamethrower));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        StorageUtils.addStoredGas(stack, tooltip, true);
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack)));
    }

    @Nonnull
    public GasStack useGas(ItemStack stack, long amount) {
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem instanceof IMekanismGasHandler) {
                IGasTank gasTank = ((IMekanismGasHandler) gasHandlerItem).getGasTank(0, null);
                if (gasTank != null) {
                    //Should always reach here
                    return gasTank.extract(amount, Action.EXECUTE, AutomationType.MANUAL);
                }
            }
            return gasHandlerItem.extractGas(amount, Action.EXECUTE);
        }
        return GasStack.EMPTY;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        GasStack stored = StorageUtils.getStoredGasFromNBT(stack);
        return stored.isEmpty() ? 0 : stored.getChemicalTint();
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
            items.add(GasUtils.getFilledVariant(new ItemStack(this), MekanismConfig.gear.flamethrowerMaxGas.get(), MekanismGases.HYDROGEN));
        }
    }

    public FlamethrowerMode getMode(ItemStack stack) {
        return FlamethrowerMode.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.MODE));
    }

    public void setMode(ItemStack stack, FlamethrowerMode mode) {
        ItemDataUtils.setInt(stack, NBTConstants.MODE, mode.ordinal());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, RateLimitGasHandler.create(MekanismConfig.gear.flamethrowerFillRate, MekanismConfig.gear.flamethrowerMaxGas,
              (item, automationType) -> automationType != AutomationType.EXTERNAL, BasicGasTank.alwaysTrueBi, gas -> gas == MekanismGases.HYDROGEN.getGas()));
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, ItemStack stack, EquipmentSlotType slotType) {
        boolean hasGas = false;
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem.getGasTankCount() > 0) {
                //Validate something didn't go terribly wrong and we actually do have the tank we expect to have
                GasStack storedGas = gasHandlerItem.getGasInTank(0);
                if (!storedGas.isEmpty()) {
                    list.add(MekanismLang.FLAMETHROWER_STORED.translateColored(EnumColor.GRAY, EnumColor.ORANGE, storedGas.getAmount()));
                    hasGas = true;
                }
            }
        }
        if (!hasGas) {
            list.add(MekanismLang.FLAMETHROWER_STORED.translateColored(EnumColor.GRAY, EnumColor.ORANGE, MekanismLang.NO_GAS.translate()));
        }
        list.add(MekanismLang.MODE.translate(getMode(stack)));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        FlamethrowerMode mode = getMode(stack);
        FlamethrowerMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, newMode);
            if (displayChangeMessage) {
                player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                      MekanismLang.FLAMETHROWER_MODE_CHANGE.translateColored(EnumColor.GRAY, newMode)));
            }
        }
    }

    @Nonnull
    @Override
    public ITextComponent getScrollTextComponent(@Nonnull ItemStack stack) {
        return getMode(stack).getTextComponent();
    }

    public enum FlamethrowerMode implements IIncrementalEnum<FlamethrowerMode>, IHasTextComponent {
        COMBAT(MekanismLang.FLAMETHROWER_COMBAT, EnumColor.YELLOW),
        HEAT(MekanismLang.FLAMETHROWER_HEAT, EnumColor.ORANGE),
        INFERNO(MekanismLang.FLAMETHROWER_INFERNO, EnumColor.DARK_RED);

        private static final FlamethrowerMode[] MODES = values();
        private final ILangEntry langEntry;
        private final EnumColor color;

        FlamethrowerMode(ILangEntry langEntry, EnumColor color) {
            this.langEntry = langEntry;
            this.color = color;
        }

        @Override
        public ITextComponent getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Nonnull
        @Override
        public FlamethrowerMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static FlamethrowerMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}