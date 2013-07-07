package universalelectricity.prefab;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiBase extends GuiScreen
{
	/**
	 * The X size of the inventory window in pixels.
	 */
	protected int xSize = 176;

	/**
	 * The Y size of the inventory window in pixels.
	 */
	protected int ySize = 166;

	/**
	 * Starting X position for the Gui. Inconsistent use for Gui backgrounds.
	 */
	protected int guiLeft;

	/**
	 * Starting Y position for the Gui. Inconsistent use for Gui backgrounds.
	 */
	protected int guiTop;

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui()
	{
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		this.drawDefaultBackground();
		int var4 = this.guiLeft;
		int var5 = this.guiTop;
		this.drawBackgroundLayer(par1, par2, par3);
		GL11.glPushMatrix();
		GL11.glTranslatef(var4, var5, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		short var7 = 240;
		short var8 = 240;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var7 / 1.0F, var8 / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		this.drawForegroundLayer(par1, par2, par3);

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		GL11.glPopMatrix();
		super.drawScreen(par1, par2, par3);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	/**
	 * Draws the foreground layer for the GUI
	 */
	protected abstract void drawForegroundLayer(int var2, int var3, float var1);

	/**
	 * Draws the background layer for the GUI
	 */

	protected abstract void drawBackgroundLayer(int var2, int var3, float var1);

	/**
	 * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char x, int y)
	{
		if (y == 1 || y == this.mc.gameSettings.keyBindInventory.keyCode)
		{
			this.mc.thePlayer.closeScreen();
		}
	}

	/**
	 * Returns true if this GUI should pause the game when it is displayed in single-player
	 */
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen()
	{
		super.updateScreen();

		if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead)
		{
			this.mc.thePlayer.closeScreen();
		}
	}

	public void drawTooltip(int x, int y, String... toolTips)
	{
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		if (toolTips != null)
		{
			int var5 = 0;
			int var6;
			int var7;

			for (var6 = 0; var6 < toolTips.length; ++var6)
			{
				var7 = this.fontRenderer.getStringWidth(toolTips[var6]);

				if (var7 > var5)
				{
					var5 = var7;
				}
			}

			var6 = x + 12;
			var7 = y - 12;
			int var9 = 8;

			if (toolTips.length > 1)
			{
				var9 += 2 + (toolTips.length - 1) * 10;
			}

			if (this.guiTop + var7 + var9 + 6 > this.height)
			{
				var7 = this.height - var9 - this.guiTop - 6;
			}

			this.zLevel = 300.0F;
			int var10 = -267386864;
			this.drawGradientRect(var6 - 3, var7 - 4, var6 + var5 + 3, var7 - 3, var10, var10);
			this.drawGradientRect(var6 - 3, var7 + var9 + 3, var6 + var5 + 3, var7 + var9 + 4, var10, var10);
			this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 + var9 + 3, var10, var10);
			this.drawGradientRect(var6 - 4, var7 - 3, var6 - 3, var7 + var9 + 3, var10, var10);
			this.drawGradientRect(var6 + var5 + 3, var7 - 3, var6 + var5 + 4, var7 + var9 + 3, var10, var10);
			int var11 = 1347420415;
			int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
			this.drawGradientRect(var6 - 3, var7 - 3 + 1, var6 - 3 + 1, var7 + var9 + 3 - 1, var11, var12);
			this.drawGradientRect(var6 + var5 + 2, var7 - 3 + 1, var6 + var5 + 3, var7 + var9 + 3 - 1, var11, var12);
			this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 - 3 + 1, var11, var11);
			this.drawGradientRect(var6 - 3, var7 + var9 + 2, var6 + var5 + 3, var7 + var9 + 3, var12, var12);

			for (int var13 = 0; var13 < toolTips.length; ++var13)
			{
				String var14 = toolTips[var13];

				this.fontRenderer.drawStringWithShadow(var14, var6, var7, -1);

				if (var13 == 0)
				{
					var7 += 2;
				}

				var7 += 10;
			}

			this.zLevel = 0.0F;
		}

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	}
}