package mekanism.client.gui.element.bar;

import java.util.List;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class GuiFluidBar extends GuiTankBar<FluidStack> {

    public GuiFluidBar(IGuiWrapper gui, TankInfoProvider<FluidStack> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(gui, infoProvider, x, y, width, height, horizontal);
    }

    @Override
    protected boolean isEmpty(FluidStack stack) {
        return stack.isEmpty();
    }

    @Override
    protected TankType getType(FluidStack stack) {
        return TankType.FLUID_TANK;
    }

    @Override
    protected void applyRenderColor(GuiGraphics guiGraphics, FluidStack stack) {
        MekanismRenderer.color(guiGraphics, stack);
    }

    @Override
    protected TextureAtlasSprite getIcon(FluidStack stack) {
        return MekanismRenderer.getFluidTexture(stack, FluidTextureType.STILL);
    }

    public static TankInfoProvider<FluidStack> getProvider(IExtendedFluidTank tank, List<IExtendedFluidTank> tanks) {
        return new TankInfoProvider<>() {
            @NotNull
            @Override
            public FluidStack getStack() {
                return tank.getFluid();
            }

            @Override
            public int getTankIndex() {
                return tanks.indexOf(tank);
            }

            @Override
            public Component getTooltip() {
                if (tank.isEmpty()) {
                    return MekanismLang.EMPTY.translate();
                } else if (tank.getFluidAmount() == Integer.MAX_VALUE) {
                    return MekanismLang.GENERIC_STORED.translate(tank.getFluid(), MekanismLang.INFINITE);
                }
                return MekanismLang.GENERIC_STORED_MB.translate(tank.getFluid(), TextUtils.format(tank.getFluidAmount()));
            }

            @Override
            public double getLevel() {
                return MathUtils.divideToLevel(tank.getFluidAmount(), tank.getCapacity());
            }
        };
    }
}