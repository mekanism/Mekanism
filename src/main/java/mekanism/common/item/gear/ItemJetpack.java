package mekanism.common.item.gear;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IIncrementalEnum;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.common.MekanismGases;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ItemDataUtils;
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

public class ItemJetpack extends ArmorItem implements IGasItem {

    public static final JetpackMaterial JETPACK_MATERIAL = new JetpackMaterial();

    public final int TRANSFER_RATE = 16;

    public ItemJetpack(Properties properties) {
        this(JETPACK_MATERIAL, properties.setTEISR(() -> getTEISR()));
    }

    @OnlyIn(Dist.CLIENT)
    private static Callable<ItemStackTileEntityRenderer> getTEISR() {
        //NOTE: This extra method is needed to avoid classloading issues on servers
        return RenderJetpack::new;
    }

    public ItemJetpack(IArmorMaterial material, Properties properties) {
        super(material, EquipmentSlotType.CHEST, properties.setNoRepair());
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
            tooltip.add(MekanismLang.NO_GAS.translate());
        } else {
            tooltip.add(MekanismLang.STORED.translate(gasStack, gasStack.getAmount()));
        }
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "mekanism:render/null_armor.png";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        ModelCustomArmor model = ModelCustomArmor.INSTANCE;
        model.modelType = ArmorModel.JETPACK;
        return model;
    }

    public void incrementMode(ItemStack stack) {
        setMode(stack, getMode(stack).getNext());
    }

    public void useGas(ItemStack stack) {
        GasStack gas = getGas(stack);
        if (!gas.isEmpty()) {
            //TODO: Can we just change the size of the gas stack instead?
            setGas(stack, new GasStack(gas, gas.getAmount() - 1));
        }
    }

    @Override
    public int getMaxGas(@Nonnull ItemStack stack) {
        return MekanismConfig.general.maxJetpackGas.get();
    }

    @Override
    public int getRate(@Nonnull ItemStack stack) {
        return TRANSFER_RATE;
    }

    @Override
    public int addGas(@Nonnull ItemStack itemStack, @Nonnull GasStack stack) {
        GasStack storedGas = getGas(itemStack);
        if (!storedGas.isTypeEqual(stack)) {
            return 0;
        }
        if (stack.getType() != MekanismGases.HYDROGEN.getGas()) {
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

    @Override
    public boolean canReceiveGas(@Nonnull ItemStack stack, @Nonnull Gas type) {
        return type == MekanismGases.HYDROGEN.getGas();
    }

    @Override
    public boolean canProvideGas(@Nonnull ItemStack stack, @Nonnull Gas type) {
        return false;
    }

    public JetpackMode getMode(ItemStack stack) {
        return JetpackMode.byIndexStatic(ItemDataUtils.getInt(stack, "mode"));
    }

    public void setMode(ItemStack stack, JetpackMode mode) {
        ItemDataUtils.setInt(stack, "mode", mode.ordinal());
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
        setGas(filled, MekanismGases.HYDROGEN.getGasStack(((IGasItem) filled.getItem()).getMaxGas(filled)));
        items.add(filled);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        //TODO: Use this in various places??
        return 0;
    }

    public enum JetpackMode implements IIncrementalEnum<JetpackMode>, IHasTextComponent {
        NORMAL(MekanismLang.JETPACK_NORMAL, EnumColor.DARK_GREEN),
        HOVER(MekanismLang.JETPACK_HOVER, EnumColor.DARK_AQUA),
        DISABLED(MekanismLang.JETPACK_DISABLED, EnumColor.DARK_RED);

        private static final JetpackMode[] MODES = values();
        private final ILangEntry langEntry;
        private final EnumColor color;

        JetpackMode(ILangEntry langEntry, EnumColor color) {
            this.langEntry = langEntry;
            this.color = color;
        }

        @Override
        public ITextComponent getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Nonnull
        @Override
        public JetpackMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static JetpackMode byIndexStatic(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return MODES[Math.floorMod(index, MODES.length)];
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