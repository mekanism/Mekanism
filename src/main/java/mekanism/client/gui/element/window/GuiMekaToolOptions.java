package mekanism.client.gui.element.window;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.item.gear.ItemMekaTool.MekaToolMode;
import mekanism.common.network.to_server.PacketSaveMode;
import mekanism.common.util.text.InputValidator;

public class GuiMekaToolOptions extends GuiWindow {

    private static final int HEIGHT = 12;
    private static final int WIDTH = 64;

    public GuiMekaToolOptions(IGuiWrapper gui, int x, int y, int slotId) {
        super(gui, x, y, 140, 115, WindowType.MEKA_TOOL_MODES);
        interactionStrategy = InteractionStrategy.NONE;

        for (MekaToolMode mode : MekaToolMode.values()) {
            GuiTextField profileName = new GuiTextField(gui, relativeX + 7, relativeY + 20 + HEIGHT * mode.ordinal(), WIDTH, HEIGHT);
            profileName.setText(MekanismConfig.client.mekaModeNames.get().get(mode.ordinal()));
            profileName.setMaxLength(10);
            profileName.setInputValidator(InputValidator.LETTER_OR_DIGIT);
            profileName.addCheckmarkButton(() -> {
                if (!profileName.getText().isBlank()) {
                    MekanismConfig.client.mekaModeNames.get().set(mode.ordinal(), profileName.getText());
                    // save the updated config info
                    MekanismConfig.client.save();
                }
            });
            addChild(profileName);
            addChild(new TranslationButton(gui, relativeX + 7 + WIDTH, relativeY + 20 + HEIGHT * mode.ordinal(), 32, HEIGHT,
                  MekanismLang.BUTTON_SAVE, () -> Mekanism.packetHandler().sendToServer(new PacketSaveMode(slotId, mode.ordinal()))));
        }
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);

        drawTitleText(matrix, MekanismLang.MEKATOOL_OPTIONS.translate(), 6);
    }
}
