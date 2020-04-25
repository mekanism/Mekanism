package mekanism.client.gui.element.gauge;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.MathUtils;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.network.PacketDropperUse.TankType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public class GuiGasGauge extends GuiTankGauge<Gas, IGasTank> {

    private ITextComponent label;
    private Supplier<IGasTank> tankSupplier;

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
        this.tankSupplier = tankSupplier;
    }

    public static GuiGasGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiGasGauge gauge = new GuiGasGauge(null, type, gui, x, y);
        gauge.dummy = true;
        return gauge;
    }

    @Override
    protected GaugeInfo getGaugeColor() {
        IGasTank tank;
        if (guiObj instanceof GuiMekanismTile && tankSupplier != null && (tank = tankSupplier.get()) != null) {
            TileEntityMekanism tile = ((GuiMekanismTile<?, ?>) guiObj).getContainer().getTileEntity();
            if (tile instanceof ISideConfiguration) {
                DataType dataType = ((ISideConfiguration) tile).getActiveDataType(tank);
                if (dataType != null) {
                    return GaugeInfo.get(dataType);
                }
            }
        }
        return GaugeInfo.STANDARD;
    }

    public GuiGasGauge setLabel(ITextComponent label) {
        this.label = label;
        return this;
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
    public ITextComponent getLabel() {
        return label;
    }

    @Override
    public List<ITextComponent> getTooltipText() {
        if (dummy) {
            return Arrays.asList(TextComponentUtil.build(dummyType));
        }
        IGasTank tank = getTank();
        if (tank == null || tank.isEmpty()) {
            return Arrays.asList(MekanismLang.EMPTY.translate());
        }
        long amount = tank.getStored();
        if (amount == Long.MAX_VALUE) {
            return Arrays.asList(MekanismLang.GENERIC_STORED.translate(tank.getType(), MekanismLang.INFINITE));
        }
        return Arrays.asList(MekanismLang.GENERIC_STORED_MB.translate(tank.getType(), formatInt(amount)));
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