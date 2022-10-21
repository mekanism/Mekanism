package mekanism.tools.common.item;

import java.util.function.Consumer;
import mekanism.tools.client.render.ToolsRenderPropertiesProvider;
import mekanism.tools.common.material.MaterialCreator;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemRefinedGlowstoneArmor extends ItemMekanismArmor {

    public ItemRefinedGlowstoneArmor(MaterialCreator material, EquipmentSlot slot, Properties properties) {
        super(material, slot, properties);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ToolsRenderPropertiesProvider.glowArmor());
    }

    @Override
    public boolean makesPiglinsNeutral(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return true;
    }
}