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
import mekanism.common.MekanismGases;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
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
            tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.noGas"), "."));
        } else {
            tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.stored"), " ", gasStack, ": " + gasStack.getAmount()));
        }
        tooltip.add(TextComponentUtil.build(EnumColor.GRAY, Translation.of("tooltip.mekanism.flowing"), ": ",
              (getFlowing(stack) ? EnumColor.DARK_GREEN : EnumColor.DARK_RED), getFlowingComponent(stack)));
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
        return "mekanism:render/NullArmor.png";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        ModelCustomArmor model = ModelCustomArmor.INSTANCE;
        model.modelType = ArmorModel.SCUBATANK;
        return model;
    }

    public void useGas(ItemStack itemstack) {
        GasStack gas = getGas(itemstack);
        if (!gas.isEmpty()) {
            setGas(itemstack, new GasStack(gas, gas.getAmount() - 1));
        }
    }

    @Nonnull
    public GasStack useGas(ItemStack itemstack, int amount) {
        GasStack gas = getGas(itemstack);
        if (gas.isEmpty()) {
            return GasStack.EMPTY;
        }
        Gas type = gas.getType();
        int gasToUse = Math.min(gas.getAmount(), Math.min(getRate(itemstack), amount));
        setGas(itemstack, new GasStack(type, gas.getAmount() - gasToUse));
        return new GasStack(type, gasToUse);
    }

    @Override
    public int getMaxGas(@Nonnull ItemStack itemstack) {
        return MekanismConfig.general.maxScubaGas.get();
    }

    @Override
    public int getRate(@Nonnull ItemStack itemstack) {
        return TRANSFER_RATE;
    }

    @Override
    public int addGas(@Nonnull ItemStack itemstack, @Nonnull GasStack stack) {
        GasStack gasInItem = getGas(itemstack);
        if (!gasInItem.isEmpty() && !gasInItem.isTypeEqual(stack)) {
            return 0;
        }
        if (!stack.getType().isIn(MekanismTags.OXYGEN)) {
            return 0;
        }
        int toUse = Math.min(getMaxGas(itemstack) - getStored(itemstack), Math.min(getRate(itemstack), stack.getAmount()));
        setGas(itemstack, new GasStack(stack, getStored(itemstack) + toUse));
        return toUse;
    }

    @Nonnull
    @Override
    public GasStack removeGas(@Nonnull ItemStack itemstack, int amount) {
        return GasStack.EMPTY;
    }

    public int getStored(ItemStack itemstack) {
        return getGas(itemstack).getAmount();
    }

    public void toggleFlowing(ItemStack stack) {
        setFlowing(stack, !getFlowing(stack));
    }

    public boolean getFlowing(ItemStack stack) {
        return ItemDataUtils.getBoolean(stack, "flowing");
    }

    public ITextComponent getFlowingComponent(ItemStack stack) {
        return YesNo.of(getFlowing(stack)).getTextComponent();
    }

    public void setFlowing(ItemStack stack, boolean flowing) {
        ItemDataUtils.setBoolean(stack, "flowing", flowing);
    }

    @Override
    public boolean canReceiveGas(@Nonnull ItemStack itemstack, @Nonnull Gas type) {
        return type.isIn(MekanismTags.OXYGEN);
    }

    @Override
    public boolean canProvideGas(@Nonnull ItemStack itemstack, @Nonnull Gas type) {
        return false;
    }

    @Nonnull
    @Override
    public GasStack getGas(@Nonnull ItemStack itemstack) {
        return GasStack.readFromNBT(ItemDataUtils.getCompound(itemstack, "stored"));
    }

    @Override
    public void setGas(@Nonnull ItemStack itemstack, @Nonnull GasStack stack) {
        if (stack.isEmpty()) {
            ItemDataUtils.removeData(itemstack, "stored");
        } else {
            int amount = Math.max(0, Math.min(stack.getAmount(), getMaxGas(itemstack)));
            GasStack gasStack = new GasStack(stack, amount);
            ItemDataUtils.setCompound(itemstack, "stored", gasStack.write(new CompoundNBT()));
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