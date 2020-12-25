package mekanism.client.gui.qio;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.IFrequencyItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiPortableQIODashboard extends GuiQIOItemViewer<PortableQIODashboardContainer> {

    public GuiPortableQIODashboard(PortableQIODashboardContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiQIOFrequencyTab(this, container.getHand()));
    }

    @Override
    public GuiQIOItemViewer<PortableQIODashboardContainer> recreate(PortableQIODashboardContainer container) {
        return new GuiPortableQIODashboard(container, playerInventory, title);
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, getName(), titleY);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public FrequencyIdentity getFrequency() {
        return ((IFrequencyItem) container.getStack().getItem()).getFrequency(container.getStack());
    }

    private ITextComponent getName() {
        return container.getStack().getDisplayName();
    }
}
