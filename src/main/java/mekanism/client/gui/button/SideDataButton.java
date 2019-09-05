package mekanism.client.gui.button;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class SideDataButton extends MekanismButton {

    private final Supplier<SideData> sideDataSupplier;
    private final Supplier<EnumColor> colorSupplier;

    public SideDataButton(IGuiWrapper gui, int x, int y, int slotPosMapIndex, Supplier<SideData> sideDataSupplier, Supplier<EnumColor> colorSupplier,
          TileEntity tile, TransmissionType transmissionType, ConfigurationPacket packetType, IHoverable onHover) {
        super(gui, x, y, 14, 14, "",
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(packetType, Coord4D.get(tile),
                    InputMappings.isKeyDown(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 0, slotPosMapIndex, transmissionType)),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(packetType, Coord4D.get(tile), 1, slotPosMapIndex, transmissionType)),
              onHover);
        this.sideDataSupplier = sideDataSupplier;
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        SideData data = getSideData();
        EnumColor color = data == TileComponentConfig.EMPTY ? null : getColor();
        boolean doColor = color != null && color != EnumColor.GRAY;
        if (doColor) {
            MekanismRenderer.color(color);
        } else {
            MekanismRenderer.resetColor();
        }
        super.renderButton(mouseX, mouseY, partialTicks);
        if (doColor) {
            MekanismRenderer.resetColor();
        }
    }

    @Override
    protected boolean resetColorBeforeRender() {
        return false;
    }

    public SideData getSideData() {
        return this.sideDataSupplier.get();
    }

    public EnumColor getColor() {
        return this.colorSupplier.get();
    }
}