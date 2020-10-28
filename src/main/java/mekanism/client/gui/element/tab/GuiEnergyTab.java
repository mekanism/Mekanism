package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiEnergyTab extends GuiBiDirectionalTab {

    private final IInfoHandler infoHandler;
    private final Map<EnergyType, ResourceLocation> icons = new EnumMap<>(EnergyType.class);

    public GuiEnergyTab(IInfoHandler handler, IGuiWrapper gui) {
        super(MekanismUtils.getResource(ResourceType.GUI, "energy_info.png"), gui, -26, 137, 26, 26);
        infoHandler = handler;
    }

    public GuiEnergyTab(MachineEnergyContainer<?> energyContainer, IGuiWrapper gui) {
        this(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(energyContainer.getEnergyPerTick())),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(energyContainer.getNeeded()))), gui);
        //TODO: Re-evaluate uses of this constructor at some point, as well as the isActive constructor
    }

    public GuiEnergyTab(MachineEnergyContainer<?> energyContainer, FloatingLongSupplier lastEnergyUsed, IGuiWrapper gui) {
        this(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(lastEnergyUsed.get())),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(energyContainer.getNeeded()))), gui);
    }

    public GuiEnergyTab(MachineEnergyContainer<?> energyContainer, BooleanSupplier isActive, IGuiWrapper gui) {
        this(() -> {
            //Note: This isn't the most accurate using calculation as deactivation doesn't sync instantly
            // to the client but it is close enough given a lot more things would have to be kept track of otherwise
            // which would lead to higher memory usage
            FloatingLong using = isActive.getAsBoolean() ? energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
            return Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(using)),
                  MekanismLang.NEEDED.translate(EnergyDisplay.of(energyContainer.getNeeded())));
        }, gui);
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getResource());
        blit(matrix, x, y, 0, 0, width, height, width, height);
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        List<ITextComponent> info = new ArrayList<>(infoHandler.getInfo());
        info.add(MekanismLang.UNIT.translate(MekanismConfig.general.energyUnit.get()));
        displayTooltips(matrix, info, mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getResource() {
        return icons.computeIfAbsent(MekanismConfig.general.energyUnit.get(), type -> MekanismUtils.getResource(ResourceType.GUI,
              "tabs/energy_info_" + type.name().toLowerCase(Locale.ROOT) + ".png"));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        MekanismConfig.general.energyUnit.set(MekanismConfig.general.energyUnit.get().getNext());
    }

    @Override
    protected void onRightClick(double mouseX, double mouseY) {
        MekanismConfig.general.energyUnit.set(MekanismConfig.general.energyUnit.get().getPrevious());
    }
}