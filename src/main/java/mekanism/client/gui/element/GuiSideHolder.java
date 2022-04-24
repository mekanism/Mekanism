package mekanism.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;

public abstract class GuiSideHolder extends GuiTexturedElement {

    public static GuiSideHolder armorHolder(IGuiWrapper gui) {
        return create(gui, -26, 62, 98, true, true, SpecialColors.TAB_ARMOR_SLOTS);
    }

    public static GuiSideHolder create(IGuiWrapper gui, int x, int y, int height, boolean left, boolean slotHolder, ColorRegistryObject tabColor) {
        return new GuiSideHolder(gui, x, y, height, left, slotHolder) {
            @Override
            protected void colorTab() {
                MekanismRenderer.color(tabColor);
            }
        };
    }

    private static final ResourceLocation HOLDER_LEFT = MekanismUtils.getResource(ResourceType.GUI, "holder_left.png");
    private static final ResourceLocation HOLDER_RIGHT = MekanismUtils.getResource(ResourceType.GUI, "holder_right.png");
    private static final int TEXTURE_WIDTH = 26;
    private static final int TEXTURE_HEIGHT = 9;

    protected final boolean left;
    private final boolean slotHolder;

    protected GuiSideHolder(IGuiWrapper gui, int x, int y, int height, boolean left, boolean slotHolder) {
        super(left ? HOLDER_LEFT : HOLDER_RIGHT, gui, x, y, TEXTURE_WIDTH, height);
        this.left = left;
        this.slotHolder = slotHolder;
        active = false;
        if (!this.slotHolder) {
            setButtonBackground(ButtonBackground.DEFAULT);
        }
    }

    protected abstract void colorTab();

    @Override
    public void renderButton(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrix, mouseX, mouseY, partialTicks);
        if (this.slotHolder) {
            //Slot holders need to draw here to render behind the slots instead of in front of them
            draw(matrix, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void drawBackground(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        if (!this.slotHolder) {
            draw(matrix, mouseX, mouseY, partialTicks);
        }
    }

    private void draw(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, getResource());
        colorTab();
        //Top
        blit(matrix, x, y, 0, 0, width, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Middle
        int middleHeight = height - 8;
        if (middleHeight > 0) {
            blit(matrix, x, y + 4, width, middleHeight, 0, 4, width, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        //Bottom
        blit(matrix, x, y + 4 + middleHeight, 0, 5, width, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        MekanismRenderer.resetColor();
    }
}