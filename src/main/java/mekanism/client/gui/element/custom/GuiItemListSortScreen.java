package mekanism.client.gui.element.custom;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.common.inventory.container.QIOItemViewerContainer.ListSortType;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;

public class GuiItemListSortScreen extends GuiInnerScreen {

    private ResourceLocation arrow_up = MekanismUtils.getResource(ResourceType.GUI, "arrow_up.png");
    private ResourceLocation arrow_down = MekanismUtils.getResource(ResourceType.GUI, "arrow_down.png");

    private Supplier<ListSortType> sortTypeSupplier;
    private Consumer<ListSortType> sortTypeSetter;

    public GuiItemListSortScreen(IGuiWrapper gui, int x, int y, Supplier<ListSortType> sortTypeSupplier, Consumer<ListSortType> sortTypeSetter) {
        super(gui, x, y, 50, 12);
        this.sortTypeSupplier = sortTypeSupplier;
        this.sortTypeSetter = sortTypeSetter;
        active = true;
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
        super.renderForeground(mouseX, mouseY, xAxis, yAxis);
        ListSortType type = sortTypeSupplier.get();
        drawScaledTextScaledBound(type.getShortName(), relativeX + 4, relativeY + 2, screenTextColor(), 30, 0.8F);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        super.renderButton(mouseX, mouseY, partialTicks);
        ListSortType type = sortTypeSupplier.get();
        minecraft.textureManager.bindTexture(type.isAscending() ? arrow_up : arrow_down);
        blit(x + width - 9, y + 3, 0, 0, 6, 6, 6, 6);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(MekanismSounds.BEEP.get(), 1.0F));
        ListSortType nextType = ListSortType.values()[(sortTypeSupplier.get().ordinal() + 1) % ListSortType.values().length];
        sortTypeSetter.accept(nextType);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(sortTypeSupplier.get().getTooltip(), mouseX, mouseY);
    }
}
