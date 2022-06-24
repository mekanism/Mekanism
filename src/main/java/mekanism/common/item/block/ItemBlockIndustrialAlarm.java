package mekanism.common.item.block;

import java.util.function.Consumer;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.block.BlockIndustrialAlarm;
import mekanism.common.registration.impl.ItemDeferredRegister;
import net.minecraftforge.client.IItemRenderProperties;
import org.jetbrains.annotations.NotNull;

public class ItemBlockIndustrialAlarm extends ItemBlockTooltip<BlockIndustrialAlarm> {

    public ItemBlockIndustrialAlarm(BlockIndustrialAlarm block) {
        super(block, ItemDeferredRegister.getMekBaseProperties());
    }

    @Override
    public void initializeClient(@NotNull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(RenderPropertiesProvider.industrialAlarm());
    }
}