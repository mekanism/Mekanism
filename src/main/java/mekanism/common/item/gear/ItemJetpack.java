package mekanism.common.item.gear;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.common.MekanismFluids;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemJetpack extends ItemArmorMekanism implements IGasItem, ISpecialArmor {

    public final int TRANSFER_RATE = 16;

    public ItemJetpack(String name) {
        super(EnumHelper.addArmorMaterial("JETPACK", "jetpack", 0, new int[]{0, 0, 0, 0}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
              0), EquipmentSlotType.CHEST, name);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - ((getGas(stack) != null ? (double) getGas(stack).amount : 0D) / (double) getMaxGas(stack));
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1 - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        GasStack gasStack = getGas(itemstack);

        if (gasStack == null) {
            list.add(LangUtils.localize("tooltip.noGas") + ".");
        } else {
            list.add(LangUtils.localize("tooltip.stored") + " " + gasStack.getGas().getLocalizedName() + ": " + gasStack.amount);
        }
        list.add(EnumColor.GREY + LangUtils.localize("tooltip.mode") + ": " + EnumColor.GREY + getMode(itemstack).getName());
    }

    @Override
    public boolean isValidArmor(ItemStack stack, EquipmentSlotType armorType, Entity entity) {
        return armorType == EquipmentSlotType.CHEST;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "mekanism:render/NullArmor.png";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ModelBiped getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, ModelBiped _default) {
        ModelCustomArmor model = ModelCustomArmor.INSTANCE;
        model.modelType = ArmorModel.JETPACK;
        return model;
    }

    public void incrementMode(ItemStack stack) {
        setMode(stack, getMode(stack).increment());
    }

    public void useGas(ItemStack stack) {
        GasStack gas = getGas(stack);
        if (gas != null) {
            setGas(stack, new GasStack(gas.getGas(), gas.amount - 1));
        }
    }

    @Override
    public int getMaxGas(ItemStack itemstack) {
        return MekanismConfig.current().general.maxJetpackGas.val();
    }

    @Override
    public int getRate(ItemStack itemstack) {
        return TRANSFER_RATE;
    }

    @Override
    public int addGas(ItemStack itemstack, GasStack stack) {
        if (getGas(itemstack) != null && getGas(itemstack).getGas() != stack.getGas()) {
            return 0;
        }
        if (stack.getGas() != MekanismFluids.Hydrogen) {
            return 0;
        }
        int toUse = Math.min(getMaxGas(itemstack) - getStored(itemstack), Math.min(getRate(itemstack), stack.amount));
        setGas(itemstack, new GasStack(stack.getGas(), getStored(itemstack) + toUse));
        return toUse;
    }

    @Override
    public GasStack removeGas(ItemStack itemstack, int amount) {
        return null;
    }

    public int getStored(ItemStack itemstack) {
        return getGas(itemstack) != null ? getGas(itemstack).amount : 0;
    }

    @Override
    public boolean canReceiveGas(ItemStack itemstack, Gas type) {
        return type == MekanismFluids.Hydrogen;
    }

    @Override
    public boolean canProvideGas(ItemStack itemstack, Gas type) {
        return false;
    }

    public JetpackMode getMode(ItemStack stack) {
        return JetpackMode.values()[ItemDataUtils.getInt(stack, "mode")];
    }

    public void setMode(ItemStack stack, JetpackMode mode) {
        ItemDataUtils.setInt(stack, "mode", mode.ordinal());
    }

    @Override
    public GasStack getGas(ItemStack itemstack) {
        return GasStack.readFromNBT(ItemDataUtils.getCompound(itemstack, "stored"));
    }

    @Override
    public void setGas(ItemStack itemstack, GasStack stack) {
        if (stack == null || stack.amount == 0) {
            ItemDataUtils.removeData(itemstack, "stored");
        } else {
            int amount = Math.max(0, Math.min(stack.amount, getMaxGas(itemstack)));
            GasStack gasStack = new GasStack(stack.getGas(), amount);
            ItemDataUtils.setCompound(itemstack, "stored", gasStack.write(new CompoundNBT()));
        }
    }

    public ItemStack getEmptyItem() {
        ItemStack empty = new ItemStack(this);
        setGas(empty, null);
        return empty;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tabs, @Nonnull NonNullList<ItemStack> list) {
        if (!isInCreativeTab(tabs)) {
            return;
        }
        ItemStack empty = new ItemStack(this);
        setGas(empty, null);
        list.add(empty);

        ItemStack filled = new ItemStack(this);
        setGas(filled, new GasStack(MekanismFluids.Hydrogen, ((IGasItem) filled.getItem()).getMaxGas(filled)));
        list.add(filled);
    }

    @Override
    public ArmorProperties getProperties(LivingEntity player, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
        return new ArmorProperties(0, 0, 0);
    }

    @Override
    public int getArmorDisplay(PlayerEntity player, @Nonnull ItemStack armor, int slot) {
        return 0;
    }

    @Override
    public void damageArmor(LivingEntity entity, @Nonnull ItemStack stack, DamageSource source, int damage, int slot) {
    }

    public enum JetpackMode {
        NORMAL("tooltip.jetpack.regular", EnumColor.DARK_GREEN),
        HOVER("tooltip.jetpack.hover", EnumColor.DARK_AQUA),
        DISABLED("tooltip.jetpack.disabled", EnumColor.DARK_RED);

        private String unlocalized;
        private EnumColor color;

        JetpackMode(String s, EnumColor c) {
            unlocalized = s;
            color = c;
        }

        public JetpackMode increment() {
            return ordinal() < values().length - 1 ? values()[ordinal() + 1] : values()[0];
        }

        public String getName() {
            return color + LangUtils.localize(unlocalized);
        }
    }
}