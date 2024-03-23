package mekanism.client.gui.element.gauge;

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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

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
    public void onClick(double mouseX, double mouseY, int button) {
        ItemStack stack = gui().getCarriedItem();
        if (gui() instanceof GuiMekanismTile<?, ?> gui && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
            int index = infoHandler.getTankIndex();
            if (index != -1) {
                DropperAction action;
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    action = Screen.hasShiftDown() ? DropperAction.DUMP_TANK : DropperAction.FILL_DROPPER;
                } else { //GLFW.GLFW_MOUSE_BUTTON_RIGHT
                    action = DropperAction.DRAIN_DROPPER;
                }
                PacketUtils.sendToServer(new PacketDropperUse(gui.getTileEntity().getBlockPos(), action, tankType, index));
            }
        }
    }

    @Override
    public boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    public interface ITankInfoHandler<TANK> {

        @Nullable
        TANK getTank();

        int getTankIndex();
    }
}