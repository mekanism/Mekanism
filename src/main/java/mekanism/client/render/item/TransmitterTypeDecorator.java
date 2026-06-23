package mekanism.client.render.item;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;

public class TransmitterTypeDecorator implements IItemDecorator {

    public static void registerDecorators(RegisterItemDecorationsEvent event, BlockRegistryObject<?, ?>... blocks) {
        for (BlockRegistryObject<?, ?> block : blocks) {
            event.register(block, new TransmitterTypeDecorator(block.getId()));
        }
    }

    private final Identifier texture;

    private TransmitterTypeDecorator(Identifier blockId) {
        this.texture = Mekanism.rl("transmitter_icon/" + blockId.getPath());
    }

    @Override
    public boolean render(GuiGraphicsExtractor guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, xOffset, yOffset, 16, 16);
        return true;
    }
}