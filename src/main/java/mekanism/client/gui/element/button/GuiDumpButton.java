package mekanism.client.gui.element.button;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.interfaces.IHasDumpButton;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GuiDumpButton extends MekanismImageButton {

    public <TILE extends BlockEntity & IHasDumpButton> GuiDumpButton(IGuiWrapper gui, TILE tile, int x, int y) {
        super(gui, x, y, 21, 10, Mekanism.rl("button/dump"), (_, _, _) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.DUMP_BUTTON, tile)));
        setButtonBackground(ButtonBackground.NONE);
    }
}