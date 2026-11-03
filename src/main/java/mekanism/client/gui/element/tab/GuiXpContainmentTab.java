package mekanism.client.gui.element.tab;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.lib.SupportsXpContainment;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketEntityGuiInteract;
import mekanism.common.network.to_server.PacketEntityGuiInteract.GuiInteractionEntity;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GuiXpContainmentTab extends GuiInsetElement<SupportsXpContainment> {

    private static final Identifier ICON = Mekanism.rl("tab/xp_containment");

    public GuiXpContainmentTab(IGuiWrapper gui, SupportsXpContainment supportsXpContainment, boolean left, int y) {
        this(gui, supportsXpContainment, left, y, true);
    }

    public GuiXpContainmentTab(IGuiWrapper gui, SupportsXpContainment supportsXpContainment, boolean left, int y, boolean supportsRecipeBook) {
        if (Mekanism.hooks.isRecipeBookDisabled() || !supportsRecipeBook) {
            //Move the xp containment tab to where the recipe book tab would normally be
            y -= 28;
        }
        super(ICON, gui, supportsXpContainment, left ? -26 : gui.getImageWidth(), y, 26, 18, left);
        setTooltip(MekanismLang.XP_CONTAINMENT_TAB);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        if (dataSource instanceof BlockEntity tile) {
            PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.COLLECT_XP, tile));
        } else if (dataSource instanceof Entity entity) {
            PacketUtils.sendToServer(new PacketEntityGuiInteract(GuiInteractionEntity.COLLECT_XP, entity));
        }
    }

    @Override
    protected int getTabColor() {
        return SpecialColors.TAB_XP_CONTAINMENT.argb();
    }

    @Override
    public void tick() {
        visible = dataSource.hasXpContainment();
        super.tick();
    }
}