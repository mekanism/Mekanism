package mekanism.common.item.gear;

import java.util.List;
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
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.common.MekanismGases;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
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

public class ItemJetpack extends ItemCustomArmorMekanism implements IGasItem {

    public static final JetpackMaterial JETPACK_MATERIAL = new JetpackMaterial();

    public final int TRANSFER_RATE = 16;

    public ItemJetpack() {
        this(JETPACK_MATERIAL, "jetpack", new Item.Properties().setTEISR(() -> RenderJetpack::new));
    }

    public ItemJetpack(IArmorMaterial material, String name, Item.Properties properties) {
        super(material, EquipmentSlotType.CHEST, name, properties);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        GasStack gas = getGas(stack);
        return 1D - ((double) gas.getAmount() / (double) getMaxGas(stack));
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1 - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
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
        tooltip.add(TextComponentUtil.build(EnumColor.GRAY, Translation.of("tooltip.mekanism.mode"), ": ", EnumColor.GRAY, getMode(stack).getTextComponent()));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "mekanism:render/NullArmor.png";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        ModelCustomArmor model = ModelCustomArmor.INSTANCE;
        model.modelType = ArmorModel.JETPACK;
        return model;
    }

    public void incrementMode(ItemStack stack) {
        setMode(stack, getMode(stack).increment());
    }

    public void useGas(ItemStack stack) {
        GasStack gas = getGas(stack);
        if (!gas.isEmpty()) {
            //TODO: Can we just change the size of the gas stack instead?
            setGas(stack, new GasStack(gas, gas.getAmount() - 1));
        }
    }

    @Override
    public int getMaxGas(@Nonnull ItemStack itemstack) {
        return MekanismConfig.general.maxJetpackGas.get();
    }

    @Override
    public int getRate(@Nonnull ItemStack itemstack) {
        return TRANSFER_RATE;
    }

    @Override
    public int addGas(@Nonnull ItemStack itemstack, @Nonnull GasStack stack) {
        GasStack storedGas = getGas(itemstack);
        if (!storedGas.isTypeEqual(stack)) {
            return 0;
        }
        if (!stack.getGas().isIn(MekanismTags.HYDROGEN)) {
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

    @Override
    public boolean canReceiveGas(@Nonnull ItemStack itemstack, @Nonnull Gas type) {
        return type.isIn(MekanismTags.HYDROGEN);
    }

    @Override
    public boolean canProvideGas(@Nonnull ItemStack itemstack, @Nonnull Gas type) {
        return false;
    }

    public JetpackMode getMode(ItemStack stack) {
        return JetpackMode.values()[ItemDataUtils.getInt(stack, "mode")];
    }

    public void setMode(ItemStack stack, JetpackMode mode) {
        ItemDataUtils.setInt(stack, "mode", mode.ordinal());
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
        setGas(filled, MekanismGases.HYDROGEN.getGasStack(((IGasItem) filled.getItem()).getMaxGas(filled)));
        items.add(filled);
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

        public ITextComponent getTextComponent() {
            return TextComponentUtil.build(color, unlocalized);
        }
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class JetpackMaterial implements IArmorMaterial {

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
            return "jetpack";
        }

        @Override
        public float getToughness() {
            return 0;
        }
    }
}