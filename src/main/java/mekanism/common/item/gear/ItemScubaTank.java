package mekanism.common.item.gear;

import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.api.text.EnumColor;
import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.client.render.item.gear.RenderScubaTank;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemScubaTank extends ArmorItem implements IGasItem {

    public static final ScubaTankMaterial SCUBA_TANK_MATERIAL = new ScubaTankMaterial();

    public int TRANSFER_RATE = 16;

    public ItemScubaTank(Properties properties) {
        super(SCUBA_TANK_MATERIAL, EquipmentSlotType.CHEST, properties.setTEISR(() -> getTEISR()).setNoRepair());
    }

    @OnlyIn(Dist.CLIENT)
    private static Callable<ItemStackTileEntityRenderer> getTEISR() {
        //NOTE: This extra method is needed to avoid classloading issues on servers
        return RenderScubaTank::new;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        GasStack gasStack = getGas(stack);
        if (gasStack.isEmpty()) {
            tooltip.add(MekanismLang.NO_GAS.translate());
        } else {
            tooltip.add(MekanismLang.STORED.translate(gasStack, gasStack.getAmount()));
        }
        tooltip.add(MekanismLang.FLOWING.translateColored(EnumColor.GRAY, YesNo.of(getFlowing(stack), true)));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - ((double) getGas(stack).getAmount() / (double) getMaxGas(stack));
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1 - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "mekanism:render/null_armor.png";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        ModelCustomArmor model = ModelCustomArmor.INSTANCE;
        model.modelType = ArmorModel.SCUBATANK;
        return model;
    }

    public void useGas(ItemStack stack) {
        GasStack gas = getGas(stack);
        if (!gas.isEmpty()) {
            setGas(stack, new GasStack(gas, gas.getAmount() - 1));
        }
    }

    @Nonnull
    public GasStack useGas(ItemStack stack, int amount) {
        GasStack gas = getGas(stack);
        if (gas.isEmpty()) {
            return GasStack.EMPTY;
        }
        Gas type = gas.getType();
        int gasToUse = Math.min(gas.getAmount(), Math.min(getRate(stack), amount));
        setGas(stack, new GasStack(type, gas.getAmount() - gasToUse));
        return new GasStack(type, gasToUse);
    }

    @Override
    public int getMaxGas(@Nonnull ItemStack stack) {
        return MekanismConfig.general.maxScubaGas.get();
    }

    @Override
    public int getRate(@Nonnull ItemStack stack) {
        return TRANSFER_RATE;
    }

    @Override
    public int addGas(@Nonnull ItemStack itemStack, @Nonnull GasStack stack) {
        GasStack gasInItem = getGas(itemStack);
        if (!gasInItem.isEmpty() && !gasInItem.isTypeEqual(stack)) {
            return 0;
        }
        if (stack.getType() != MekanismGases.OXYGEN.getGas()) {
            return 0;
        }
        int toUse = Math.min(getMaxGas(itemStack) - getStored(itemStack), Math.min(getRate(itemStack), stack.getAmount()));
        setGas(itemStack, new GasStack(stack, getStored(itemStack) + toUse));
        return toUse;
    }

    @Nonnull
    @Override
    public GasStack removeGas(@Nonnull ItemStack stack, int amount) {
        return GasStack.EMPTY;
    }

    public int getStored(ItemStack stack) {
        return getGas(stack).getAmount();
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

    @Override
    public boolean canReceiveGas(@Nonnull ItemStack stack, @Nonnull Gas type) {
        return type == MekanismGases.OXYGEN.getGas();
    }

    @Override
    public boolean canProvideGas(@Nonnull ItemStack stack, @Nonnull Gas type) {
        return false;
    }

    @Nonnull
    @Override
    public GasStack getGas(@Nonnull ItemStack stack) {
        return GasStack.readFromNBT(ItemDataUtils.getCompound(stack, "stored"));
    }

    @Override
    public void setGas(@Nonnull ItemStack itemStack, @Nonnull GasStack stack) {
        if (stack.isEmpty()) {
            ItemDataUtils.removeData(itemStack, "stored");
        } else {
            int amount = Math.max(0, Math.min(stack.getAmount(), getMaxGas(itemStack)));
            GasStack gasStack = new GasStack(stack, amount);
            ItemDataUtils.setCompound(itemStack, "stored", gasStack.write(new CompoundNBT()));
        }
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (!isInGroup(group)) {
            return;
        }
        ItemStack filled = new ItemStack(this);
        setGas(filled, MekanismGases.OXYGEN.getGasStack(((IGasItem) filled.getItem()).getMaxGas(filled)));
        items.add(filled);
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
}