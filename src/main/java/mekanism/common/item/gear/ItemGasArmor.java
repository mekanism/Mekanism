package mekanism.common.item.gear;

import java.util.List;
import java.util.function.LongSupplier;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.providers.IGasProvider;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler;
import mekanism.common.item.interfaces.IGasItem;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemGasArmor extends ItemSpecialArmor implements IGasItem {

    protected ItemGasArmor(ArmorMaterial material, EquipmentSlot slot, Properties properties) {
        super(material, slot, properties.rarity(Rarity.RARE).setNoRepair().stacksTo(1));
    }

    protected abstract LongSupplier getMaxGas();

    protected abstract LongSupplier getFillRate();

    protected abstract IGasProvider getGasType();

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredGas(stack, tooltip, true, false);
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
    public void fillItemCategory(@NotNull CreativeModeTab group, @NotNull NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
        if (allowedIn(group)) {
            items.add(ChemicalUtil.getFilledVariant(new ItemStack(this), getMaxGas().getAsLong(), getGasType()));
        }
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        super.gatherCapabilities(capabilities, stack, nbt);
        capabilities.add(RateLimitGasHandler.create(getFillRate(), getMaxGas(), ChemicalTankBuilder.GAS.notExternal, ChemicalTankBuilder.GAS.alwaysTrueBi,
              gas -> gas == getGasType().getChemical()));
    }
}