package mekanism.client.gui.element.tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.LongSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.tile.prefab.TileEntityExponentialMachine;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class GuiEnergyTab extends GuiTexturedElement {

    public static GuiEnergyTab create(IGuiWrapper gui, TileEntityExponentialMachine<?, ?> tile) {
        if (tile.hasVariableEnergyUsage()) {
            return new GuiEnergyTab(gui, tile.energyContainer(), tile::getEnergyUsed);
        }
        return new GuiEnergyTab(gui, tile.energyContainer(), tile::getActive);
    }

    private final Function<IGuiWrapper, List<Component>> infoHandler;

    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

    public GuiEnergyTab(IGuiWrapper gui, Function<IGuiWrapper, List<Component>> handler) {
        super(Mekanism.rl("tab/energy_info"), gui, -26, 137, 26, 26);
        infoHandler = handler;
    }

    public GuiEnergyTab(IGuiWrapper gui, MachineEnergyContainer<?> energyContainer, LongSupplier lastEnergyUsed) {
        this(gui, g -> List.of(MekanismLang.USING.translate(g.energyDisplayOf(lastEnergyUsed.getAsLong())),
              MekanismLang.NEEDED.translate(g.energyDisplayOf(energyContainer.getNeededAsLong()))));
    }

    //TODO: Re-evaluate uses of this constructor at some point
    public GuiEnergyTab(IGuiWrapper gui, MachineEnergyContainer<?> energyContainer, BooleanSupplier isActive) {
        this(gui, g -> {
            //Note: This isn't the most accurate using calculation as deactivation doesn't sync instantly
            // to the client, but it is close enough given a lot more things would have to be kept track of otherwise
            // which would lead to higher memory usage
            int using = isActive.getAsBoolean() ? energyContainer.getEnergyPerTick() : 0;
            return List.of(MekanismLang.USING.translate(g.energyDisplayOf(using)),
                  MekanismLang.NEEDED.translate(g.energyDisplayOf(energyContainer.getNeededAsLong())));
        });
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        List<Component> info = new ArrayList<>(infoHandler.apply(gui()));
        info.add(MekanismLang.UNIT.translate(MekanismLang.ENERGY_FORGE));
        if (!info.equals(lastInfo)) {
            lastInfo = info;
            lastTooltip = TooltipUtils.create(info);
        }
        setTooltip(lastTooltip);
    }
}