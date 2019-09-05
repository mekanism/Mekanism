package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO GuiTileElement
@OnlyIn(Dist.CLIENT)
public class GuiGasMode extends GuiTexturedElement {

    private static final ResourceLocation IDLE = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "gas_mode_idle.png");
    private static final ResourceLocation EXCESS = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "gas_mode_excess.png");
    private static final ResourceLocation DUMP = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "gas_mode_dump.png");

    private final boolean left;

    public GuiGasMode(IGuiWrapper gui, ResourceLocation def, int x, int y, boolean left) {
        super(IDLE, gui, def, x, y, 8, 8);
        this.left = left;
    }
}