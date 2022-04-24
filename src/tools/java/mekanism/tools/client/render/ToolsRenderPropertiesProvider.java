package mekanism.tools.client.render;

import javax.annotation.Nonnull;
import mekanism.client.render.RenderPropertiesProvider.MekRenderProperties;
import mekanism.tools.client.render.item.RenderMekanismShieldItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class ToolsRenderPropertiesProvider {

    private ToolsRenderPropertiesProvider() {
    }

    public static IItemRenderProperties shield() {
        return new MekRenderProperties(RenderMekanismShieldItem.RENDERER);
    }

    public static IItemRenderProperties glowArmor() {
        return GLOW_ARMOR;
    }

    private static final IItemRenderProperties GLOW_ARMOR = new IItemRenderProperties() {
        @Nonnull
        @Override
        public Model getBaseArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> _default) {
            return GlowArmor.wrap(_default);
        }
    };
}