/**
 * 
 */
package mekanism.induction.client.gui;

import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.induction.common.MekanismInduction;
import mekanism.induction.common.inventory.container.ContainerMultimeter;
import mekanism.induction.common.tileentity.TileEntityMultimeter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Multimeter GUI
 * 
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiMultimeter extends GuiContainer
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(MekanismInduction.DOMAIN, MekanismInduction.GUI_DIRECTORY + "gui_multimeter.png");
	TileEntityMultimeter tileEntity;

	private int containerWidth;
	private int containerHeight;
	private GuiTextField textFieldLimit;

	public GuiMultimeter(InventoryPlayer inventoryPlayer, TileEntityMultimeter tile)
	{
		super(new ContainerMultimeter(inventoryPlayer, tile));
		tileEntity = tile;
		ySize = 217;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		buttonList.add(new GuiButton(0, width / 2 + 20, height / 2 - 30, 50, 20, "Toggle"));
		textFieldLimit = new GuiTextField(fontRenderer, 35, 82, 65, 12);
		textFieldLimit.setMaxStringLength(8);
		textFieldLimit.setText("" + tileEntity.getLimit());
	}

	@Override
	protected void keyTyped(char par1, int par2)
	{
		super.keyTyped(par1, par2);
		textFieldLimit.textboxKeyTyped(par1, par2);

		ArrayList data = new ArrayList();
		data.add((byte)3);
		data.add(Float.parseFloat(textFieldLimit.getText()));
		
		PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Object3D.get(tileEntity), data));
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		super.mouseClicked(par1, par2, par3);
		textFieldLimit.mouseClicked(par1 - containerWidth, par2 - containerHeight, par3);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		String s = tileEntity.getBlockType().getLocalizedName();
		fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 15, 4210752);
		fontRenderer.drawString("Average Energy: " + Math.round(tileEntity.getAverageDetectedEnergy()) + " J", 35, 25, 4210752);
		fontRenderer.drawString("Energy: " + Math.round(tileEntity.getDetectedEnergy()) + " J", 35, 35, 4210752);
		fontRenderer.drawString("Output Redstone If... ", 35, 54, 4210752);
		fontRenderer.drawString(tileEntity.getMode().display, 35, 65, 4210752);
		fontRenderer.drawString("KiloJoules", 35, 100, 4210752);

		textFieldLimit.drawTextBox();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		containerWidth = (width - xSize) / 2;
		containerHeight = (height - ySize) / 2;

		mc.renderEngine.bindTexture(TEXTURE);
		GL11.glColor4f(1, 1, 1, 1);
		drawTexturedModalRect(containerWidth, containerHeight, 0, 0, xSize, ySize);

		int length = Math.min((int) (tileEntity.getDetectedEnergy() / tileEntity.getPeak()) * 115, 115);
		drawTexturedModalRect(containerWidth + 14, containerHeight + 126 - length, 176, 115 - length, 6, length);
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		ArrayList data = new ArrayList();
		data.add((byte)2);
		
		PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Object3D.get(tileEntity), data));
	}
}
