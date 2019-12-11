package mekanism.client.gui.button;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.glfw.GLFW;

public class SideDataButton extends MekanismButton {

    private final Supplier<DataType> dataTypeSupplier;
    private final Supplier<EnumColor> colorSupplier;

    public SideDataButton(IGuiWrapper gui, int x, int y, int slotPosMapIndex, Supplier<DataType> dataTypeSupplier, Supplier<EnumColor> colorSupplier,
          TileEntity tile, Supplier<TransmissionType> transmissionType, ConfigurationPacket packetType, IHoverable onHover) {
        super(gui, x, y, 14, 14, "",
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(packetType, Coord4D.get(tile),
                    InputMappings.isKeyDown(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 0, slotPosMapIndex, transmissionType.get())),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(packetType, Coord4D.get(tile), 1, slotPosMapIndex, transmissionType.get())),
              onHover);
        this.dataTypeSupplier = dataTypeSupplier;
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        DataType dataType = getDataType();
        EnumColor color = dataType == null ? null : getColor();
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

    public DataType getDataType() {
        return this.dataTypeSupplier.get();
    }

    public EnumColor getColor() {
        return this.colorSupplier.get();
    }
}