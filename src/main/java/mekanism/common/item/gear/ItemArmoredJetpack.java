package mekanism.common.item.gear;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.gas.IGasItem;
import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.client.render.item.gear.RenderArmoredJetpack;
import mekanism.common.MekanismGases;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemArmoredJetpack extends ItemJetpack {

    public static final ArmoredJetpackMaterial ARMORED_JETPACK_MATERIAL = new ArmoredJetpackMaterial();

    public ItemArmoredJetpack() {
        super(ARMORED_JETPACK_MATERIAL, "jetpack_armored", new Item.Properties().setTEISR(() -> RenderArmoredJetpack::new));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        ModelCustomArmor model = ModelCustomArmor.INSTANCE;
        model.modelType = ArmorModel.ARMOREDJETPACK;
        return model;
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

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    private static class ArmoredJetpackMaterial extends JetpackMaterial {

        @Override
        public int getDamageReductionAmount(EquipmentSlotType slotType) {
            return slotType == EquipmentSlotType.CHEST ? MekanismConfig.general.armoredJetpackArmor.get() : 0;
        }

        @Override
        public String getName() {
            return "jetpack_armored";
        }

        @Override
        public float getToughness() {
            return MekanismConfig.general.armoredJetpackToughness.get();
        }
    }
}