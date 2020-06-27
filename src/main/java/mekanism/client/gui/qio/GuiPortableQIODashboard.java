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
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiQIOFrequencyTab(this, container.getHand()));
    }

    @Override
    public GuiQIOItemViewer<PortableQIODashboardContainer> recreate(PortableQIODashboardContainer container) {
        return new GuiPortableQIODashboard(container, playerInventory, field_230704_d_);
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, getName(), 5);
        super.func_230451_b_(matrix, mouseX, mouseY);
    }

    @Override
    public FrequencyIdentity getFrequency() {
        return ((IFrequencyItem) container.getStack().getItem()).getFrequency(container.getStack());
    }

    private ITextComponent getName() {
        return container.getStack().getDisplayName();
    }
}
