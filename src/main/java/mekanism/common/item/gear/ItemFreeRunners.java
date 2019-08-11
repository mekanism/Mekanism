package mekanism.common.item.gear;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.item.IItemEnergized;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.TextComponentUtil;
import mekanism.common.util.TextComponentUtil.EnergyDisplay;
import mekanism.common.util.TextComponentUtil.Translation;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemFreeRunners extends ItemCustomArmorMekanism implements IItemEnergized {

    public static final FreeRunnerMaterial FREE_RUNNER_MATERIAL = new FreeRunnerMaterial();

    /**
     * The maximum amount of energy this item can hold.
     */
    public double MAX_ELECTRICITY = 64000;

    public ItemFreeRunners() {
        super(FREE_RUNNER_MATERIAL, EquipmentSlotType.FEET, "free_runners");
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "mekanism:render/NullArmor.png";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        ModelCustomArmor model = ModelCustomArmor.INSTANCE;
        model.modelType = ArmorModel.FREERUNNERS;
        return model;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(EnumColor.AQUA, Translation.of("mekanism.tooltip.storedEnergy"), ": ", EnumColor.GREY,
              EnergyDisplay.of(getEnergy(stack), getMaxEnergy(stack))));
        tooltip.add(TextComponentUtil.build(EnumColor.GREY, Translation.of("mekanism.tooltip.mode"), ": ", EnumColor.GREY, getMode(stack).getTextComponent()));
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (!isInGroup(group)) {
            return;
        }
        ItemStack charged = new ItemStack(this);
        setEnergy(charged, ((IEnergizedItem) charged.getItem()).getMaxEnergy(charged));
        items.add(charged);
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        return MAX_ELECTRICITY;
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return getMaxEnergy(itemStack) * 0.005;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return getMaxEnergy(itemStack) - getEnergy(itemStack) > 0;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - (getEnergy(stack) / getMaxEnergy(stack));
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1 - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event) {
        LivingEntity base = event.getEntityLiving();
        ItemStack stack = base.getItemStackFromSlot(EquipmentSlotType.FEET);
        if (!stack.isEmpty() && stack.getItem() instanceof ItemFreeRunners) {
            ItemFreeRunners boots = (ItemFreeRunners) stack.getItem();
            if (boots.getMode(stack) == FreeRunnerMode.NORMAL && boots.getEnergy(stack) > 0
                && event.getSource() == DamageSource.FALL) {
                boots.setEnergy(stack, boots.getEnergy(stack) - event.getAmount() * 50);
                event.setCanceled(true);
            }
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, new ForgeEnergyItemWrapper());
    }

    public FreeRunnerMode getMode(ItemStack itemStack) {
        return FreeRunnerMode.values()[ItemDataUtils.getInt(itemStack, "mode")];
    }

    public void setMode(ItemStack itemStack, FreeRunnerMode mode) {
        ItemDataUtils.setInt(itemStack, "mode", mode.ordinal());
    }

    public void incrementMode(ItemStack itemStack) {
        setMode(itemStack, getMode(itemStack).increment());
    }

    public enum FreeRunnerMode {
        NORMAL("tooltip.freerunner.regular", EnumColor.DARK_GREEN),
        DISABLED("tooltip.freerunner.disabled", EnumColor.DARK_RED);

        private String unlocalized;
        private EnumColor color;

        FreeRunnerMode(String unlocalized, EnumColor color) {
            this.unlocalized = unlocalized;
            this.color = color;
        }

        public FreeRunnerMode increment() {
            return ordinal() < values().length - 1 ? values()[ordinal() + 1] : values()[0];
        }

        public String getName() {
            return color + LangUtils.localize(unlocalized);
        }

        public ITextComponent getTextComponent() {
            return TextComponentUtil.build(color, unlocalized);
        }
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class FreeRunnerMaterial implements IArmorMaterial {

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
            return "free_runners";
        }

        @Override
        public float getToughness() {
            return 0;
        }
    }
}