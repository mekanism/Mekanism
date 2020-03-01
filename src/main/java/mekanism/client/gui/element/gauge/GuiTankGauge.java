package mekanism.client.gui.element.gauge;

import java.util.Arrays;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.Mekanism;
import mekanism.common.base.ITankManager;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketDropperUse;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.glfw.GLFW;

public abstract class GuiTankGauge<T, TANK> extends GuiGauge<T> {

    protected final ITankInfoHandler<TANK> infoHandler;

    public GuiTankGauge(GaugeType type, IGuiWrapper gui, int x, int y, ITankInfoHandler<TANK> infoHandler) {
        super(type, gui, x, y);
        this.infoHandler = infoHandler;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            ItemStack stack = GuiTexturedElement.minecraft.player.inventory.getItemStack();
            if (guiObj instanceof GuiMekanismTile && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                TileEntity tile = ((GuiMekanismTile<?, ?>) guiObj).getTileEntity();
                if (tile instanceof ITankManager && ((ITankManager) tile).getManagedTanks() != null) {
                    TANK tank = infoHandler.getTank();
                    if (tank != null) {
                        int index = Arrays.asList(((ITankManager) tile).getManagedTanks()).indexOf(tank);
                        if (index != -1) {
                            if (button == 0 && InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                                button = 2;
                            }
                            Mekanism.packetHandler.sendToServer(new PacketDropperUse(Coord4D.get(tile), button, index));
                        }
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public interface ITankInfoHandler<TANK> {

        @Nullable
        TANK getTank();
    }
}