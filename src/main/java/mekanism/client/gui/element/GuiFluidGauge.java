package mekanism.client.gui.element;

import java.util.Arrays;
import mekanism.api.Coord4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.Mekanism;
import mekanism.common.base.ITankManager;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketDropperUse.DropperUseMessage;
import mekanism.common.util.LangUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiFluidGauge extends GuiGauge<Fluid> {

    private final IFluidInfoHandler infoHandler;

    public GuiFluidGauge(IFluidInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        super(type, gui, def, x, y);
        infoHandler = handler;
    }

    public static GuiFluidGauge getDummy(Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        GuiFluidGauge gauge = new GuiFluidGauge(null, type, gui, def, x, y);
        gauge.dummy = true;

        return gauge;
    }

    @Override
    public int getRenderColor() {
        if (dummy) {
            return dummyType.getColor();
        }

        FluidStack fluid = infoHandler.getTank().getFluid();
        return fluid == null ? dummyType.getColor() : fluid.getFluid().getColor();
    }

    @Override
    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= xLocation + 1 && xAxis <= xLocation + width - 1 && yAxis >= yLocation + 1
              && yAxis <= yLocation + height - 1;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.FLUID;
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
        if (infoHandler.getTank().getFluid() == null || infoHandler.getTank().getCapacity() == 0) {
            return 0;
        }
        if (infoHandler.getTank().getFluidAmount() == Integer.MAX_VALUE) {
            return height - 2;
        }
        return infoHandler.getTank().getFluidAmount() * (height - 2) / infoHandler.getTank().getCapacity();
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return MekanismRenderer.getFluidTexture(dummyType, FluidType.STILL);
        }
        FluidStack fluid = infoHandler.getTank().getFluid();
        return MekanismRenderer.getFluidTexture(fluid == null ? dummyType : fluid.getFluid(), FluidType.STILL);
    }

    @Override
    public String getTooltipText() {
        if (dummy) {
            return dummyType.getLocalizedName(null);
        }

        String amountStr = (infoHandler.getTank().getFluidAmount() == Integer.MAX_VALUE ? LangUtils
              .localize("gui.infinite") : infoHandler.getTank().getFluidAmount() + " mB");

        return infoHandler.getTank().getFluid() != null ? LangUtils.localizeFluidStack(infoHandler.getTank().getFluid())
              + ": " + amountStr : LangUtils.localize("gui.empty");
    }

    public interface IFluidInfoHandler {

        FluidTank getTank();
    }
}