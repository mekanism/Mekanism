package mekanism.client.gui.element.gauge;

import javax.annotation.Nullable;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.network.to_server.PacketDropperUse.DropperAction;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

public abstract class GuiTankGauge<T, TANK> extends GuiGauge<T> implements IJEIIngredientHelper {

    private final ITankInfoHandler<TANK> infoHandler;
    private final TankType tankType;

    public GuiTankGauge(GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY, ITankInfoHandler<TANK> infoHandler, TankType tankType) {
        super(type, gui, x, y, sizeX, sizeY);
        this.infoHandler = infoHandler;
        this.tankType = tankType;
    }

    public TANK getTank() {
        return infoHandler.getTank();
    }

    @Override
    protected GaugeInfo getGaugeColor() {
        if (gui() instanceof GuiMekanismTile) {
            TANK tank = getTank();
            if (tank != null) {
                TileEntityMekanism tile = ((GuiMekanismTile<?, ?>) gui()).getMenu().getTileEntity();
                if (tile instanceof ISideConfiguration) {
                    DataType dataType = ((ISideConfiguration) tile).getActiveDataType(tank);
                    if (dataType != null) {
                        return GaugeInfo.get(dataType);
                    }
                }
            }
        }
        return super.getGaugeColor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && tankType != null) {
            ItemStack stack = minecraft.player.inventory.getCarried();
            if (gui() instanceof GuiMekanismTile && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                int index = infoHandler.getTankIndex();
                if (index != -1) {
                    DropperAction action;
                    if (button == 0) {
                        action = Screen.hasShiftDown() ? DropperAction.DUMP_TANK : DropperAction.FILL_DROPPER;
                    } else {
                        action = DropperAction.DRAIN_DROPPER;
                    }
                    Mekanism.packetHandler.sendToServer(new PacketDropperUse(((GuiMekanismTile<?, ?>) gui()).getTileEntity().getBlockPos(), action, tankType, index));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public interface ITankInfoHandler<TANK> {

        @Nullable
        TANK getTank();

        int getTankIndex();
    }
}