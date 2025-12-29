package mekanism.client.gui.element.gauge;

import com.mojang.blaze3d.platform.InputConstants;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerIngredientHelper;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.network.to_server.PacketDropperUse.DropperAction;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiTankGauge<T, TANK> extends GuiGauge<T> implements IRecipeViewerIngredientHelper {

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
        if (gui() instanceof GuiMekanismTile<?, ?> gui) {
            TANK tank = getTank();
            if (tank != null) {
                TileEntityMekanism tile = gui.getMenu().getTileEntity();
                if (tile instanceof ISideConfiguration config) {
                    DataType dataType = config.getActiveDataType(tank);
                    if (dataType != null) {
                        return GaugeInfo.get(dataType);
                    }
                }
            }
        }
        return super.getGaugeColor();
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        ItemStack stack = gui().getCarriedItem();
        if (gui() instanceof GuiMekanismTile<?, ?> gui && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
            int index = infoHandler.getTankIndex();
            if (index != -1) {
                DropperAction action;
                if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                    action = event.hasShiftDown() ? DropperAction.DUMP_TANK : DropperAction.FILL_DROPPER;
                } else { //InputConstants.MOUSE_BUTTON_RIGHT
                    action = DropperAction.DRAIN_DROPPER;
                }
                PacketUtils.sendToServer(new PacketDropperUse(gui.getTileEntity().getBlockPos(), action, tankType, index));
            }
        }
    }

    @Override
    public boolean isValidClickButton(@NotNull MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == InputConstants.MOUSE_BUTTON_LEFT || buttonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    public interface ITankInfoHandler<TANK> {

        @Nullable
        TANK getTank();

        int getTankIndex();
    }
}