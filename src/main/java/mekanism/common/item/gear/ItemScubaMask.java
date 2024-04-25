package mekanism.common.item.gear;

import java.util.function.Consumer;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.registries.MekanismArmorMaterials;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemScubaMask extends ItemSpecialArmor {

    public ItemScubaMask(Properties properties) {
        super(MekanismArmorMaterials.SCUBA_GEAR, ArmorItem.Type.HELMET, properties.rarity(Rarity.RARE).setNoRepair());
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.scubaMask());
    }
}