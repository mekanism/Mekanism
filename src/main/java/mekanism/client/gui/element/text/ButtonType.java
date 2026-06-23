package mekanism.client.gui.element.text;

import java.util.function.BiFunction;
import mekanism.client.gui.element.GuiElement.ButtonBackground;
import mekanism.client.gui.element.GuiElement.IClickable;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.Mekanism;

public enum ButtonType {
    NORMAL((field, callback) -> new MekanismImageButton(field.gui(), field.getRelativeRight() - field.getHeight(), field.getRelativeY(),
          field.getHeight(), Mekanism.rl("button/checkmark"), callback)),
    DIGITAL((field, callback) -> {
        MekanismImageButton ret = new MekanismImageButton(field.gui(), field.getRelativeRight() - field.getHeight(), field.getRelativeY(),
              field.getHeight(), Mekanism.rl("button/checkmark_digital"), callback);
        ret.setButtonBackground(ButtonBackground.DIGITAL);
        return ret;
    });

    private final BiFunction<GuiTextField, IClickable, MekanismImageButton> buttonCreator;

    ButtonType(BiFunction<GuiTextField, IClickable, MekanismImageButton> buttonCreator) {
        this.buttonCreator = buttonCreator;
    }

    public MekanismImageButton getButton(GuiTextField field, IClickable callback) {
        return buttonCreator.apply(field, callback);
    }
}