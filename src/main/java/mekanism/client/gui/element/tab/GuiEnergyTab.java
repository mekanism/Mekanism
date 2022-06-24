package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.UnaryOperator;
import mekanism.api.IIncrementalEnum;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiEnergyTab extends GuiBiDirectionalTab {

    private static final Map<EnergyUnit, ResourceLocation> ICONS = new EnumMap<>(EnergyUnit.class);
    private final IInfoHandler infoHandler;

    public GuiEnergyTab(IGuiWrapper gui, IInfoHandler handler) {
        super(MekanismUtils.getResource(ResourceType.GUI_TAB, "energy_info.png"), gui, -26, 137, 26, 26);
        infoHandler = handler;
    }

    public GuiEnergyTab(IGuiWrapper gui, MachineEnergyContainer<?> energyContainer) {
        this(gui, () -> List.of(MekanismLang.USING.translate(EnergyDisplay.of(energyContainer.getEnergyPerTick())),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(energyContainer.getNeeded()))));
        //TODO: Re-evaluate uses of this constructor at some point, as well as the isActive constructor
    }

    public GuiEnergyTab(IGuiWrapper gui, MachineEnergyContainer<?> energyContainer, FloatingLongSupplier lastEnergyUsed) {
        this(gui, () -> List.of(MekanismLang.USING.translate(EnergyDisplay.of(lastEnergyUsed.get())),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(energyContainer.getNeeded()))));
    }

    public GuiEnergyTab(IGuiWrapper gui, MachineEnergyContainer<?> energyContainer, BooleanSupplier isActive) {
        this(gui, () -> {
            //Note: This isn't the most accurate using calculation as deactivation doesn't sync instantly
            // to the client, but it is close enough given a lot more things would have to be kept track of otherwise
            // which would lead to higher memory usage
            FloatingLong using = isActive.getAsBoolean() ? energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
            return List.of(MekanismLang.USING.translate(EnergyDisplay.of(using)),
                  MekanismLang.NEEDED.translate(EnergyDisplay.of(energyContainer.getNeeded())));
        });
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderTexture(0, getResource());
        blit(matrix, x, y, 0, 0, width, height, width, height);
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        List<Component> info = new ArrayList<>(infoHandler.getInfo());
        info.add(MekanismLang.UNIT.translate(EnergyUnit.getConfigured()));
        displayTooltips(matrix, mouseX, mouseY, info);
    }

    @Override
    protected ResourceLocation getResource() {
        return ICONS.computeIfAbsent(EnergyUnit.getConfigured(), type -> MekanismUtils.getResource(ResourceType.GUI_TAB,
              "energy_info_" + type.getTabName() + ".png"));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        updateEnergyUnit(IIncrementalEnum::getNext);
    }

    @Override
    protected void onRightClick(double mouseX, double mouseY) {
        updateEnergyUnit(IIncrementalEnum::getPrevious);
    }

    private void updateEnergyUnit(UnaryOperator<EnergyUnit> converter) {
        EnergyUnit current = EnergyUnit.getConfigured();
        EnergyUnit updated = converter.apply(current);
        if (current != updated) {//May be equal if all other energy types are disabled
            MekanismConfig.common.energyUnit.set(updated);
            MekanismConfig.common.save();
        }
    }
}