package mekanism.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiBigLight extends GuiTexturedElement {

    private static final ResourceLocation LIGHTS = MekanismUtils.getResource(ResourceType.GUI, "big_lights.png");
    private final BooleanSupplier lightSupplier;

    public GuiBigLight(IGuiWrapper gui, int x, int y, BooleanSupplier lightSupplier) {
        super(LIGHTS, gui, x, y, 14, 14);
        this.lightSupplier = lightSupplier;
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        renderBackgroundTexture(matrix, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
        RenderSystem.setShaderTexture(0, getResource());
        blit(matrix, x + 1, y + 1, lightSupplier.getAsBoolean() ? 0 : 12, 0, width - 2, height - 2, 24, 12);
    }
}