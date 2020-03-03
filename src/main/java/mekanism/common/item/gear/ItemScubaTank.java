package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.providers.IGasProvider;
import mekanism.api.text.EnumColor;
import mekanism.client.render.armor.CustomArmor;
import mekanism.client.render.armor.ScubaTankArmor;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.IItemHUDProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemScubaTank extends ItemGasArmor implements IItemHUDProvider {

    public static final ScubaTankMaterial SCUBA_TANK_MATERIAL = new ScubaTankMaterial();

    public ItemScubaTank(Properties properties) {
        super(SCUBA_TANK_MATERIAL, EquipmentSlotType.CHEST, properties.setISTER(ISTERProvider::scubaTank));
    }

    @Override
    protected IntSupplier getMaxGas() {
        return MekanismConfig.general.maxScubaGas::get;
    }

    @Override
    protected IGasProvider getGasType() {
        return MekanismGases.OXYGEN;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.FLOWING.translateColored(EnumColor.GRAY, YesNo.of(getFlowing(stack), true)));
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public CustomArmor getGearModel() {
        return ScubaTankArmor.SCUBA_TANK;
    }

    public void toggleFlowing(ItemStack stack) {
        setFlowing(stack, !getFlowing(stack));
    }

    public boolean getFlowing(ItemStack stack) {
        return ItemDataUtils.getBoolean(stack, "flowing");
    }

    public void setFlowing(ItemStack stack, boolean flowing) {
        ItemDataUtils.setBoolean(stack, "flowing", flowing);
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class ScubaTankMaterial implements IArmorMaterial {

        @Override
        public int getDurability(EquipmentSlotType slotType) {
            return 0;
        }

        @Override
        public int getDamageReductionAmount(EquipmentSlotType slotType) {
            return 0;
        }

        @Override
        public int getEnchantability() {
            return 0;
        }

        @Override
        public SoundEvent getSoundEvent() {
            return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
        }

        @Override
        public Ingredient getRepairMaterial() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "scuba_tank";
        }

        @Override
        public float getToughness() {
            return 0;
        }
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, ItemStack stack) {
        ItemScubaTank scubaTank = (ItemScubaTank) stack.getItem();
        list.add(MekanismLang.SCUBA_TANK_MODE.translateColored(EnumColor.DARK_GRAY, OnOff.of(scubaTank.getFlowing(stack), true)));
        GasStack stored = GasStack.EMPTY;
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem.getGasTankCount() > 0) {
                stored = gasHandlerItem.getGasInTank(0);
            }
        }
        list.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.DARK_GRAY, MekanismGases.OXYGEN, stored.getAmount()));
    }
}