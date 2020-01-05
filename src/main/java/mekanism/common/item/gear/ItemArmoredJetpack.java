package mekanism.common.item.gear;

import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.armor.JetpackArmor;
import mekanism.client.render.item.gear.RenderArmoredJetpack;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemArmoredJetpack extends ItemJetpack {

    public static final ArmoredJetpackMaterial ARMORED_JETPACK_MATERIAL = new ArmoredJetpackMaterial();

    public ItemArmoredJetpack(Properties properties) {
        super(ARMORED_JETPACK_MATERIAL, properties.setTEISR(() -> getTEISR()));
    }

    @OnlyIn(Dist.CLIENT)
    private static Callable<ItemStackTileEntityRenderer> getTEISR() {
        //NOTE: This extra method is needed to avoid classloading issues on servers
        return RenderArmoredJetpack::new;
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public ModelCustomArmor getGearModel() {
        return JetpackArmor.ARMORED_JETPACK;
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