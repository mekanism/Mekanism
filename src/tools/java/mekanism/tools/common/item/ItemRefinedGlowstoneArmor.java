package mekanism.tools.common.item;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.tools.client.render.ToolsRenderPropertiesProvider;
import mekanism.tools.common.material.MaterialCreator;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemRefinedGlowstoneArmor extends ItemMekanismArmor {

    public ItemRefinedGlowstoneArmor(MaterialCreator material, EquipmentSlot slot, Properties properties) {
        super(material, slot, properties);
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(ToolsRenderPropertiesProvider.glowArmor());
    }

    @Override
    public boolean makesPiglinsNeutral(@Nonnull ItemStack stack, @Nonnull LivingEntity wearer) {
        return true;
    }
}