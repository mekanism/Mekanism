package mekanism.common.item.gear;

import java.util.List;
import java.util.function.LongSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.AutomationType;
import mekanism.api.providers.IGasProvider;
import mekanism.client.render.armor.CustomArmor;
import mekanism.client.render.armor.ScubaTankArmor;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler;
import mekanism.common.item.interfaces.IGasItem;
import mekanism.common.item.interfaces.ISpecialGear;
import mekanism.common.util.GasUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public abstract class ItemGasArmor extends ArmorItem implements ISpecialGear, IGasItem {

    protected ItemGasArmor(IArmorMaterial material, EquipmentSlotType slot, Properties properties) {
        super(material, slot, properties.rarity(Rarity.RARE).setNoRepair().maxStackSize(1));
    }

    protected abstract LongSupplier getMaxGas();

    protected abstract LongSupplier getFillRate();

    protected abstract IGasProvider getGasType();

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        StorageUtils.addStoredGas(stack, tooltip, true, false);
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
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "mekanism:render/null_armor.png";
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public CustomArmor getGearModel() {
        return ScubaTankArmor.SCUBA_TANK;
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
            items.add(GasUtils.getFilledVariant(new ItemStack(this), getMaxGas().getAsLong(), getGasType()));
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, RateLimitGasHandler.create(getFillRate(), getMaxGas(),
              (item, automationType) -> automationType != AutomationType.EXTERNAL, BasicGasTank.alwaysTrueBi, gas -> gas == getGasType().getGas()));
    }
}