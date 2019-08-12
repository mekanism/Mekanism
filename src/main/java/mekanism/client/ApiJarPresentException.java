package mekanism.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ErrorScreen;
import net.minecraftforge.fml.ModLoadingException;

/**
 * Copied from EnderCore
 */
public class ApiJarPresentException extends ModLoadingException {

    private static final long serialVersionUID = 1L;

    @Nonnull
    private final String[] msgs;

    public ApiJarPresentException(@Nonnull String[] msgs) {
        super(msgs[0], new RuntimeException());
        this.msgs = msgs;
    }

    @Override
    public void initGui(ErrorScreen errorScreen, FontRenderer fontRenderer) {
    }

    @Override
    public void render(@Nullable ErrorScreen errorScreen, @Nullable FontRenderer font, int mouseRelX, int mouseRelY, float tickTime) {
        if (errorScreen == null || font == null) {
            return;
        }
        int y = errorScreen.height / 2 - msgs.length * 5;
        for (String msg : msgs) {
            if (msg != null) {
                errorScreen.drawCenteredString(font, msg, errorScreen.width / 2, y, 0xFFFFFF);
                y += 10;
            } else {
                y += 5;
            }
        }
    }
}