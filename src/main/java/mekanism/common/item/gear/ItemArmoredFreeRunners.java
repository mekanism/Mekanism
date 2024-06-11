package mekanism.common.item.gear;

import java.util.function.Consumer;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.registries.MekanismArmorMaterials;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemArmoredFreeRunners extends ItemFreeRunners {

    public ItemArmoredFreeRunners(Properties properties) {
        super(MekanismArmorMaterials.ARMORED_FREE_RUNNERS, properties);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.armoredFreeRunners());
    }
}
