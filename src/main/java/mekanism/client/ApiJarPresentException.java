package mekanism.client;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Copied from EnderCore
 */
public class ApiJarPresentException extends CustomModLoadingErrorDisplayException
{
	private static final long serialVersionUID = 1L;

	private final @Nonnull String[] msgs;

	public ApiJarPresentException(@Nonnull String[] msgs) {
		super(msgs[0], new RuntimeException());
		this.msgs = msgs;
	}

	@Override
	public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
	}

	@Override
	public void drawScreen(@Nullable GuiErrorScreen errorScreen, @Nullable FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
		if (errorScreen == null || fontRenderer == null) {
			return;
		}
		int y = errorScreen.height / 2 - msgs.length * 5;
		for (String msg : msgs) {
			if (msg != null) {
				errorScreen.drawCenteredString(fontRenderer, msg, errorScreen.width / 2, y, 0xFFFFFF);
				y += 10;
			} else {
				y += 5;
			}
		}
	}
}
