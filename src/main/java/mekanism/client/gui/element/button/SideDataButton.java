package mekanism.client.gui.element.button;

import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketConfigurationUpdate;
import mekanism.common.network.to_server.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SideDataButton extends BasicColorButton {

    private final Supplier<DataType> dataTypeSupplier;

    public SideDataButton(IGuiWrapper gui, int x, int y, RelativeSide slotPos, Supplier<DataType> dataTypeSupplier, Supplier<EnumColor> colorSupplier,
          BlockEntity tile, Supplier<TransmissionType> transmissionType, ConfigurationPacket packetType, IHoverable onHover) {
        super(gui, x, y, 14, () -> {
                  DataType dataType = dataTypeSupplier.get();
                  return dataType == null ? null : colorSupplier.get();
              }, () -> Mekanism.packetHandler().sendToServer(new PacketConfigurationUpdate(packetType, tile.getBlockPos(), Screen.hasShiftDown() ? 2 : 0, slotPos, transmissionType.get())),
              () -> Mekanism.packetHandler().sendToServer(new PacketConfigurationUpdate(packetType, tile.getBlockPos(), 1, slotPos, transmissionType.get())), onHover);
        this.dataTypeSupplier = dataTypeSupplier;
    }

    public DataType getDataType() {
        return this.dataTypeSupplier.get();
    }
}