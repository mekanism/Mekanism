package mekanism.client.gui.element.gauge;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.MathUtils;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketDropperUse.TankType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public class GuiGasGauge extends GuiTankGauge<Gas, IGasTank> {

    public GuiGasGauge(IGasInfoHandler handler, GaugeType type, IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y, handler, TankType.GAS_TANK);
    }

    public GuiGasGauge(Supplier<IGasTank> tankSupplier, Supplier<List<IGasTank>> tanksSupplier, GaugeType type,
          IGuiWrapper gui, int x, int y) {
        this(new IGasInfoHandler() {
            @Nullable
            @Override
            public IGasTank getTank() {
                return tankSupplier.get();
            }

            @Override
            public int getTankIndex() {
                IGasTank tank = getTank();
                return tank == null ? -1 : tanksSupplier.get().indexOf(tank);
            }
        }, type, gui, x, y);
    }

    public static GuiGasGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiGasGauge gauge = new GuiGasGauge(null, type, gui, x, y);
        gauge.dummy = true;
        return gauge;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.GAS;
    }

    @Override
    public int getScaledLevel() {
        if (dummy) {
            return height - 2;
        }
        IGasTank tank = getTank();
        if (tank == null || tank.isEmpty() || tank.getCapacity() == 0) {
            return 0;
        }
        double scale = tank.getStored() / (double) tank.getCapacity();
        return MathUtils.clampToInt(Math.round(scale * (height - 2)));
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return MekanismRenderer.getChemicalTexture(dummyType);
        }
        return getTank() == null || getTank().isEmpty() ? null : MekanismRenderer.getChemicalTexture(getTank().getType());
    }

    @Override
    public ITextComponent getTooltipText() {
        if (dummy) {
            return TextComponentUtil.build(dummyType);
        }
        IGasTank tank = getTank();
        if (tank == null || tank.isEmpty()) {
            return MekanismLang.EMPTY.translate();
        }
        long amount = tank.getStored();
        if (amount == Long.MAX_VALUE) {
            return MekanismLang.GENERIC_STORED.translate(tank.getType(), MekanismLang.INFINITE);
        }
        return MekanismLang.GENERIC_STORED_MB.translate(tank.getType(), amount);
    }

    @Override
    protected void applyRenderColor() {
        if (dummy || getTank() == null) {
            MekanismRenderer.color(dummyType);
        } else {
            MekanismRenderer.color(getTank().getStack());
        }
    }

    @Nullable
    @Override
    public Object getIngredient() {
        return getTank().isEmpty() ? null : getTank().getStack();
    }

    public interface IGasInfoHandler extends ITankInfoHandler<IGasTank> {
    }
}