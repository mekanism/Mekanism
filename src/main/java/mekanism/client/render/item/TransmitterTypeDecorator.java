package mekanism.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;

public class TransmitterTypeDecorator implements IItemDecorator {

    public static void registerDecorators(RegisterItemDecorationsEvent event, BlockRegistryObject<?, ?>... blocks) {
        for (BlockRegistryObject<?, ?> block : blocks) {
            event.register(block, new TransmitterTypeDecorator(block.getId()));
        }
    }

    private final ResourceLocation texture;

    private TransmitterTypeDecorator(ResourceLocation blockId) {
        this.texture = MekanismUtils.getResource(ResourceType.GUI_ICONS, blockId.getPath() + ".png");
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (stack.isEmpty()) {
            return false;
        }
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 200);
        guiGraphics.blit(texture, xOffset, yOffset, 0, 0, 16, 16, 16, 16);
        pose.popPose();
        return true;
    }
}