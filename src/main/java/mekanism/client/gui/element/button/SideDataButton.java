package mekanism.client.gui.element.button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.MekanismLang;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.MekClickType;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class SideDataButton extends BasicColorButton {

    private final SideDataPacketCreator packetCreator;
    private final Supplier<@Nullable DataType> dataTypeSupplier;
    private final TileEntityMekanism tile;
    private final RelativeSide slotPos;
    private final ItemStack otherBlockItem;
    private final boolean displayDataType;

    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

    public SideDataButton(IGuiWrapper gui, int x, int y, RelativeSide slotPos, Supplier<@Nullable DataType> dataTypeSupplier, Supplier<@Nullable EnumColor> colorSupplier,
          TileEntityMekanism tile, SideDataPacketCreator packetCreator, boolean displayDataType) {
        super(gui, x, y, 22, () -> {
            DataType dataType = dataTypeSupplier.get();
            return dataType == null ? null : colorSupplier.get();
        }, (element, event, _) -> {
            SideDataButton button = (SideDataButton) element;
            return PacketUtils.sendToServer(button.packetCreator.create(button.tile.getBlockPos(), MekClickType.left(event.hasShiftDown()), button.slotPos));
        }, (element, _, _) -> {
            SideDataButton button = (SideDataButton) element;
            return PacketUtils.sendToServer(button.packetCreator.create(button.tile.getBlockPos(), MekClickType.RIGHT, button.slotPos));
        });
        this.dataTypeSupplier = dataTypeSupplier;
        this.displayDataType = displayDataType;
        this.packetCreator = packetCreator;
        this.tile = tile;
        this.slotPos = slotPos;
        Level tileWorld = tile.getLevel();
        if (tileWorld != null) {
            Direction globalSide = slotPos.getDirection(tile.getDirection());
            BlockPos otherBlockPos = tile.getBlockPos().relative(globalSide);
            BlockState blockOnSide = tileWorld.getBlockState(otherBlockPos);
            if (!blockOnSide.isAir()) {
                //TODO - 26.2 check that previous false was the same param, and if there's supposed to be a HitResult version???
                otherBlockItem = blockOnSide.getCloneItemStack(otherBlockPos, tileWorld, false, Minecraft.getInstance().player);
            } else {
                otherBlockItem = ItemStack.EMPTY;
            }
        } else {
            otherBlockItem = ItemStack.EMPTY;
        }
    }

    @Nullable
    public DataType getDataType() {
        return this.dataTypeSupplier.get();
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);

        if (!otherBlockItem.isEmpty()) {
            int itemX = getRelativeX() + 3;
            int itemY = getRelativeY() + 3;
            guiGraphics.item(otherBlockItem, itemX, itemY);
            guiGraphics.itemDecorations(font(), otherBlockItem, itemX, itemY);
        }
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        DataType dataType = getDataType();
        if (dataType != null) {
            List<Component> tooltipLines = new ArrayList<>(3);
            tooltipLines.add(TextComponentUtil.build(slotPos));
            if (displayDataType) {
                tooltipLines.add(TextComponentUtil.build(dataType.getColor(), dataType));
            } else {
                EnumColor color = getColor();
                tooltipLines.add(color == null ? MekanismLang.NONE.translate() : color.getColoredName());
            }
            if (!otherBlockItem.isEmpty()) {
                tooltipLines.add(otherBlockItem.getHoverName());
            }
            if (!tooltipLines.equals(lastInfo)) {
                lastInfo = tooltipLines;
                lastTooltip = TooltipUtils.create(tooltipLines);
            }
        } else {
            lastTooltip = null;
            lastInfo = Collections.emptyList();
        }
        setTooltip(lastTooltip);
    }

    @FunctionalInterface
    public interface SideDataPacketCreator {

        IMekanismPacket create(BlockPos pos, MekClickType clickType, RelativeSide inputSide);
    }
}