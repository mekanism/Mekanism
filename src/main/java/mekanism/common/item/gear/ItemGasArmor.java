package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.providers.IGasProvider;
import mekanism.client.render.armor.CustomArmor;
import mekanism.client.render.armor.ScubaTankArmor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.RateLimitGasHandler;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public abstract class ItemGasArmor extends ArmorItem implements ISpecialGear {

    private final int TRANSFER_RATE = 16;

    protected ItemGasArmor(IArmorMaterial material, EquipmentSlotType slot, Properties properties) {
        super(material, slot, properties.setNoRepair().maxStackSize(1));
    }

    protected abstract IntSupplier getMaxGas();

    protected abstract IGasProvider getGasType();

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        boolean hasGas = false;
        if (Capabilities.GAS_HANDLER_CAPABILITY != null) {
            //Ensure the capability is not null, as the first call to addInformation happens before capability injection
            Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IGasHandler gasHandlerItem = capability.get();
                if (gasHandlerItem.getGasTankCount() > 0) {
                    //Validate something didn't go terribly wrong and we actually do have the tank we expect to have
                    GasStack storedGas = gasHandlerItem.getGasInTank(0);
                    if (!storedGas.isEmpty()) {
                        tooltip.add(MekanismLang.STORED.translate(storedGas, storedGas.getAmount()));
                        hasGas = true;
                    }
                }
            }
        }
        if (!hasGas) {
            tooltip.add(MekanismLang.NO_GAS.translate());
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return GasUtils.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1 - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
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

    @Nonnull
    public GasStack useGas(ItemStack stack, int amount) {
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            return gasHandlerItem.extractGas(0, amount, Action.EXECUTE);
        }
        return GasStack.EMPTY;
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
            items.add(GasUtils.getFilledVariant(new ItemStack(this), getMaxGas().getAsInt(), getGasType()));
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, RateLimitGasHandler.create(() -> TRANSFER_RATE, getMaxGas(), BasicGasTank.manualOnly, BasicGasTank.alwaysTrueBi,
              gas -> gas == getGasType().getGas()));
    }
}