package mekanism.client.gui;

import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ContainerRobitMain;
import mekanism.common.network.PacketRobit.RobitMessage;
import mekanism.common.network.PacketRobit.RobitPacketType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiRobitMain extends GuiMekanism
{
	public EntityRobit robit;

	public boolean displayNameChange;

	private GuiTextField nameChangeField;
	private GuiButton confirmName;

	public GuiRobitMain(InventoryPlayer inventory, EntityRobit entity)
	{
		super(new ContainerRobitMain(inventory, entity));
		xSize += 25;
		robit = entity;
	}

	private void toggleNameChange()
	{
		displayNameChange = !displayNameChange;
		confirmName.visible = displayNameChange;
		nameChangeField.setFocused(displayNameChange);
	}

	private void changeName()
	{
		if(nameChangeField.getText() != null && !nameChangeField.getText().isEmpty())
		{
			Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.NAME, robit.getEntityId(), 0, nameChangeField.getText()));
			toggleNameChange();
			nameChangeField.setText("");
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if(guibutton.id == 0)
		{
			changeName();
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		buttonList.clear();
		buttonList.add(confirmName = new GuiButton(0, guiWidth + 58, guiHeight + 47, 60, 20, MekanismUtils.localize("gui.confirm")));
		confirmName.visible = displayNameChange;

		nameChangeField = new GuiTextField(fontRendererObj, guiWidth + 48, guiHeight + 21, 80, 12);
		nameChangeField.setMaxStringLength(12);
		nameChangeField.setFocused(true);
	}

	@Override
	public void keyTyped(char c, int i)
	{
		if(!displayNameChange)
		{
			super.keyTyped(c, i);
		}
		else {
			if(i == Keyboard.KEY_RETURN)
			{
				changeName();
			}
			else if(i == Keyboard.KEY_ESCAPE)
			{
				mc.thePlayer.closeScreen();
			}

			nameChangeField.textboxKeyTyped(c, i);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRendererObj.drawString(MekanismUtils.localize("gui.robit"), 76, 6, 0x404040);

		if(!displayNameChange)
		{
			CharSequence owner = robit.getOwnerName().length() > 14 ? robit.getOwnerName().subSequence(0, 14) : robit.getOwnerName();
			fontRendererObj.drawString(MekanismUtils.localize("gui.robit.greeting") + " " + robit.getCommandSenderName() + "!", 29, 18, 0x00CD00);
			fontRendererObj.drawString(MekanismUtils.localize("gui.energy") + ": " + MekanismUtils.getEnergyDisplay(robit.getEnergy()), 29, 36-4, 0x00CD00);
			fontRendererObj.drawString(MekanismUtils.localize("gui.robit.following") + ": " + robit.getFollowing(), 29, 45-4, 0x00CD00);
			fontRendererObj.drawString(MekanismUtils.localize("gui.robit.dropPickup") + ": " + robit.getDropPickup(), 29, 54-4, 0x00CD00);
			fontRendererObj.drawString(MekanismUtils.localize("gui.robit.owner") + ": " + owner, 29, 63-4, 0x00CD00);
		}

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(xAxis >= 28 && xAxis <= 148 && yAxis >= 75 && yAxis <= 79)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(robit.getEnergy()), xAxis, yAxis);
		}
		else if(xAxis >= 152 && xAxis <= 170 && yAxis >= 54 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.robit.toggleFollow"), xAxis, yAxis);
		}
		else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 54 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.robit.rename"), xAxis, yAxis);
		}
		else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 16 && yAxis <= 34)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.robit.teleport"), xAxis, yAxis);
		}
		else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 35 && yAxis <= 53)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.robit.togglePickup"), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiRobitMain.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 10 && yAxis <= 28)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 10, 176 + 25, 0, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 10, 176 + 25, 18, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 30 && yAxis <= 48)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 30, 176 + 25, 36, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 30, 176 + 25, 54, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 50 && yAxis <= 68)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 50, 176 + 25, 72, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 50, 176 + 25, 90, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 70 && yAxis <= 88)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 70, 176 + 25, 108, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 70, 176 + 25, 126, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 90 && yAxis <= 108)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 90, 176 + 25, 144, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 90, 176 + 25, 162, 18, 18);
		}

		if(xAxis >= 152 && xAxis <= 170 && yAxis >= 54 && yAxis <= 72)
		{
			drawTexturedModalRect(guiWidth + 152, guiHeight + 54, 176 + 25, 180, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 152, guiHeight + 54, 176 + 25, 198, 18, 18);
		}

		if(xAxis >= 6 && xAxis <= 24 && yAxis >= 54 && yAxis <= 72)
		{
			drawTexturedModalRect(guiWidth + 6, guiHeight + 54, 176 + 25, 216, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 6, guiHeight + 54, 176 + 25, 234, 18, 18);
		}

		if(xAxis >= 6 && xAxis <= 24 && yAxis >= 16 && yAxis <= 34)
		{
			drawTexturedModalRect(guiWidth + 6, guiHeight + 16, 176 + 25 + 18, 36, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 6, guiHeight + 16, 176 + 25 + 18, 54, 18, 18);
		}

		if(xAxis >= 6 && xAxis <= 24 && yAxis >= 35 && yAxis <= 53)
		{
			drawTexturedModalRect(guiWidth + 6, guiHeight + 35, 176 + 25 + 18, 72, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 6, guiHeight + 35, 176 + 25 + 18, 90, 18, 18);
		}

		int displayInt;

		displayInt = getScaledEnergyLevel(120);
		drawTexturedModalRect(guiWidth + 28, guiHeight + 75, 0, 166, displayInt, 4);

		if(displayNameChange)
		{
			drawTexturedModalRect(guiWidth + 28, guiHeight + 17, 0, 166 + 4, 120, 54);
			nameChangeField.drawTextBox();
		}
	}

	private int getScaledEnergyLevel(int i)
	{
		return (int)(robit.getEnergy()*i / robit.MAX_ELECTRICITY);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		nameChangeField.updateCursorCounter();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		nameChangeField.mouseClicked(mouseX, mouseY, button);

		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);

			if(xAxis >= 179 && xAxis <= 197 && yAxis >= 10 && yAxis <= 28)
			{
                SoundHandler.playSound("gui.button.press");
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 30 && yAxis <= 48)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 1, robit.getEntityId(), null));
				mc.thePlayer.openGui(Mekanism.instance, 22, mc.theWorld, robit.getEntityId(), 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 50 && yAxis <= 68)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 2, robit.getEntityId(), null));
				mc.thePlayer.openGui(Mekanism.instance, 23, mc.theWorld, robit.getEntityId(), 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 70 && yAxis <= 88)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 3, robit.getEntityId(), null));
				mc.thePlayer.openGui(Mekanism.instance, 24, mc.theWorld, robit.getEntityId(), 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 90 && yAxis <= 108)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 4, robit.getEntityId(), null));
				mc.thePlayer.openGui(Mekanism.instance, 25, mc.theWorld, robit.getEntityId(), 0, 0);
			}
			else if(xAxis >= 152 && xAxis <= 170 && yAxis >= 54 && yAxis <= 72)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.FOLLOW, robit.getEntityId(), 0, null));
			}
			else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 54 && yAxis <= 72)
			{
                SoundHandler.playSound("gui.button.press");
				toggleNameChange();
			}
			else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 16 && yAxis <= 34)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GO_HOME, robit.getEntityId(), 0, null));
				mc.displayGuiScreen(null);
			}
			else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 35 && yAxis <= 53)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.DROP_PICKUP, robit.getEntityId(), 0, null));
			}
		}
	}
}
