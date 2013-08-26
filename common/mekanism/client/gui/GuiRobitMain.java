package mekanism.client.gui;

import mekanism.common.EntityRobit;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerRobitMain;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketRobit.RobitPacketType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRobitMain extends GuiContainer
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
    	confirmName.drawButton = displayNameChange;
    	nameChangeField.setFocused(displayNameChange);
    }
    
    private void changeName()
    {
    	if(nameChangeField.getText() != null && !nameChangeField.getText().isEmpty())
    	{
    		PacketHandler.sendPacket(Transmission.SERVER, new PacketRobit().setParams(RobitPacketType.NAME, nameChangeField.getText(), robit.entityId));
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
		buttonList.add(confirmName = new GuiButton(0, guiWidth + 58, guiHeight + 47, 60, 20, "Confirm"));
		confirmName.drawButton = displayNameChange;
		
		nameChangeField = new GuiTextField(fontRenderer, guiWidth + 48, guiHeight + 21, 80, 12);
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
    	fontRenderer.drawString("Robit", 76, 6, 0x404040);
    	
    	if(!displayNameChange)
    	{
	    	fontRenderer.drawString("Hi, I'm " + robit.getTranslatedEntityName() + "!", 29, 18, 0x00CD00);
	    	fontRenderer.drawString("Energy: " + ElectricityDisplay.getDisplayShort((float)(robit.getEnergy()*Mekanism.TO_UE), ElectricUnit.JOULES), 29, 36-4, 0x00CD00);
	    	fontRenderer.drawString("Following: " + robit.getFollowing(), 29, 45-4, 0x00CD00);
	    	fontRenderer.drawString("Drop pickup: " + robit.getDropPickup(), 29, 54-4, 0x00CD00);
	    	fontRenderer.drawString("Owner: " + robit.getOwnerName(), 29, 63-4, 0x00CD00);
    	}
    	
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
    	
		if(xAxis >= 28 && xAxis <= 148 && yAxis >= 75 && yAxis <= 79)
		{
			drawCreativeTabHoveringText(ElectricityDisplay.getDisplayShort((float)(robit.getEnergy()*Mekanism.TO_UE), ElectricUnit.JOULES), xAxis, yAxis);
		}
		else if(xAxis >= 152 && xAxis <= 170 && yAxis >= 54 && yAxis <= 72)
		{
			drawCreativeTabHoveringText("Toggle 'follow' mode", xAxis, yAxis);
		}
		else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 54 && yAxis <= 72)
		{
			drawCreativeTabHoveringText("Rename this Robit", xAxis, yAxis);
		}
		else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 16 && yAxis <= 34)
		{
			drawCreativeTabHoveringText("Teleport back home", xAxis, yAxis);
		}
		else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 35 && yAxis <= 53)
		{
			drawCreativeTabHoveringText("Toggle 'drop pickup' mode", xAxis, yAxis);
		}
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY)
    {
    	mc.renderEngine.func_110577_a(MekanismUtils.getResource(ResourceType.GUI, "GuiRobitMain.png"));
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
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 30 && yAxis <= 48)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRobit().setParams(RobitPacketType.GUI, 1, robit.entityId));
				mc.thePlayer.openGui(Mekanism.instance, 22, mc.theWorld, robit.entityId, 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 50 && yAxis <= 68)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRobit().setParams(RobitPacketType.GUI, 2, robit.entityId));
				mc.thePlayer.openGui(Mekanism.instance, 23, mc.theWorld, robit.entityId, 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 70 && yAxis <= 88)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRobit().setParams(RobitPacketType.GUI, 3, robit.entityId));
				mc.thePlayer.openGui(Mekanism.instance, 24, mc.theWorld, robit.entityId, 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 90 && yAxis <= 108)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRobit().setParams(RobitPacketType.GUI, 4, robit.entityId));
				mc.thePlayer.openGui(Mekanism.instance, 25, mc.theWorld, robit.entityId, 0, 0);
			}
			else if(xAxis >= 152 && xAxis <= 170 && yAxis >= 54 && yAxis <= 72)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRobit().setParams(RobitPacketType.FOLLOW, robit.entityId));
			}
			else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 54 && yAxis <= 72)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				toggleNameChange();
			}
			else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 16 && yAxis <= 34)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRobit().setParams(RobitPacketType.GO_HOME, robit.entityId));
				mc.displayGuiScreen(null);
			}
			else if(xAxis >= 6 && xAxis <= 24 && yAxis >= 35 && yAxis <= 53)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRobit().setParams(RobitPacketType.DROP_PICKUP, robit.entityId));
			}
		}
	}
}
