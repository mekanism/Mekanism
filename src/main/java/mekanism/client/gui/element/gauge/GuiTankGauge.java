package mekanism.client.gui.element.gauge;

import java.util.Arrays;
import mekanism.api.Coord4D;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.common.Mekanism;
import mekanism.common.base.ITankManager;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketDropperUse;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;

@OnlyIn(Dist.CLIENT)
public abstract class GuiTankGauge<T, TANK> extends GuiGauge<T> {

    protected final ITankInfoHandler<TANK> infoHandler;

    public GuiTankGauge(Type type, IGuiWrapper gui, ResourceLocation def, int x, int y, ITankInfoHandler<TANK> infoHandler) {
        super(type, gui, def, x, y);
        this.infoHandler = infoHandler;
    }

    @Override
    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= xLocation + 1 && xAxis <= xLocation + width - 1 && yAxis >= yLocation + 1 && yAxis <= yLocation + height - 1;
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
        if (inBounds(xAxis, yAxis)) {
            ItemStack stack = GuiElement.mc.player.inventory.getItemStack();
            if (guiObj instanceof GuiMekanismTile && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                TileEntity tile = ((GuiMekanismTile) guiObj).getTileEntity();
                if (tile instanceof ITankManager && ((ITankManager) tile).getTanks() != null) {
                    int index = Arrays.asList(((ITankManager) tile).getTanks()).indexOf(infoHandler.getTank());
                    if (index != -1) {
                        if (button == 0 && Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                            button = 2;
                        }
                        Mekanism.packetHandler.sendToServer(new PacketDropperUse(Coord4D.get(tile), button, index));
                    }
                }
            }
        }
    }

    public interface ITankInfoHandler<TANK> {

        TANK getTank();
    }
}