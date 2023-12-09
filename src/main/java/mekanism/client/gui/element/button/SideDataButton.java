package mekanism.client.gui.element.button;

import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketConfigurationUpdate;
import mekanism.common.network.to_server.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SideDataButton extends BasicColorButton {

    private final Supplier<DataType> dataTypeSupplier;
    private final ItemStack otherBlockItem;

    public SideDataButton(IGuiWrapper gui, int x, int y, RelativeSide slotPos, Supplier<DataType> dataTypeSupplier, Supplier<EnumColor> colorSupplier,
          TileEntityMekanism tile, Supplier<TransmissionType> transmissionType, ConfigurationPacket packetType, @Nullable IHoverable onHover) {
        super(gui, x, y, 22, () -> {
                  DataType dataType = dataTypeSupplier.get();
                  return dataType == null ? null : colorSupplier.get();
              }, () -> Mekanism.packetHandler().sendToServer(new PacketConfigurationUpdate(packetType, tile.getBlockPos(), Screen.hasShiftDown() ? 2 : 0, slotPos, transmissionType.get())),
              () -> Mekanism.packetHandler().sendToServer(new PacketConfigurationUpdate(packetType, tile.getBlockPos(), 1, slotPos, transmissionType.get())), onHover);
        this.dataTypeSupplier = dataTypeSupplier;
        Level tileWorld = tile.getLevel();
        if (tileWorld != null) {
            Direction globalSide = slotPos.getDirection(tile.getDirection());
            BlockPos otherBlockPos = tile.getBlockPos().relative(globalSide);
            BlockState blockOnSide = tileWorld.getBlockState(otherBlockPos);
            if (!blockOnSide.isAir()) {
                otherBlockItem = blockOnSide.getCloneItemStack(new BlockHitResult(Vec3.atCenterOf(otherBlockPos).relative(globalSide.getOpposite(), 0.5), globalSide.getOpposite(), otherBlockPos, false), tileWorld, otherBlockPos, Minecraft.getInstance().player);
            } else {
                otherBlockItem = ItemStack.EMPTY;
            }
        } else {
            otherBlockItem = ItemStack.EMPTY;
        }
    }

    public DataType getDataType() {
        return this.dataTypeSupplier.get();
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);

        if (!otherBlockItem.isEmpty()) {
            GuiUtils.renderItem(guiGraphics, otherBlockItem, this.getRelativeX()+3, this.getRelativeY()+3, 1, getFont(), null, true);
        }
    }
}