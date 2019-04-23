package mekanism.client.gui.element;

import java.util.Arrays;
import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTank;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.base.ITankManager;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketDropperUse.DropperUseMessage;
import mekanism.common.util.LangUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiGasGauge extends GuiGauge<Gas> {

    private final IGasInfoHandler infoHandler;

    public GuiGasGauge(IGasInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        super(type, gui, def, x, y);
        infoHandler = handler;
    }

    public static GuiGasGauge getDummy(Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        GuiGasGauge gauge = new GuiGasGauge(null, type, gui, def, x, y);
        gauge.dummy = true;

        return gauge;
    }

    @Override
    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= xLocation + 1 && xAxis <= xLocation + width - 1 && yAxis >= yLocation + 1
              && yAxis <= yLocation + height - 1;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.GAS;
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
        if (inBounds(xAxis, yAxis)) {
            ItemStack stack = mc.player.inventory.getItemStack();

            if (guiObj instanceof GuiMekanismTile && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                TileEntity tile = ((GuiMekanismTile) guiObj).getTileEntity();

                if (tile instanceof ITankManager && ((ITankManager) tile).getTanks() != null) {
                    int index = Arrays.asList(((ITankManager) tile).getTanks()).indexOf(infoHandler.getTank());

                    if (index != -1) {
                        if (button == 0 && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                            button = 2;
                        }

                        Mekanism.packetHandler.sendToServer(new DropperUseMessage(Coord4D.get(tile), button, index));
                    }
                }
            }
        }
    }

    @Override
    public int getScaledLevel() {
        if (dummy) {
            return height - 2;
        }

        if (infoHandler.getTank().getGas() == null || infoHandler.getTank().getMaxGas() == 0) {
            return 0;
        }

        return infoHandler.getTank().getStored() * (height - 2) / infoHandler.getTank().getMaxGas();
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return dummyType.getSprite();
        }

        return (infoHandler.getTank() != null && infoHandler.getTank().getGas() != null
              && infoHandler.getTank().getGas().getGas() != null) ? infoHandler.getTank().getGas().getGas().getSprite()
              : null;
    }

    @Override
    public String getTooltipText() {
        if (dummy) {
            return dummyType.getLocalizedName();
        }

        return (infoHandler.getTank().getGas() != null) ? infoHandler.getTank().getGas().getGas().getLocalizedName()
              + ": " + infoHandler.getTank().getStored() : LangUtils.localize("gui.empty");
    }

    @Override
    public int getRenderColor() {
        if (dummy) {
            return dummyType.getTint();
        }

        return (infoHandler.getTank().getGas() != null) ? infoHandler.getTank().getGas().getGas().getTint()
              : super.getRenderColor();
    }

    public interface IGasInfoHandler {

        GasTank getTank();
    }
}