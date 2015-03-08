package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSeismicReader extends GuiScreen {
    private World worldObj;

    public ItemStack itemStack;

    private ArrayList<Pair<Integer, Block>> blockList = new ArrayList<Pair<Integer, Block>>();

    public Coord4D pos;

    protected int xSize = 137;
    protected int ySize = 179;

    private Rectangle upButton, downButton;

    private int currentLayer = 0;

    public GuiSeismicReader(World world, Coord4D coord, ItemStack stack) {
	pos = coord;
	pos.yCoord = Math.min(255, pos.yCoord);
	worldObj = world;

	itemStack = stack;
	calculate();
	currentLayer = Math.max(0, blockList.size() - 1);
    }

    @Override
    public void initGui() {
	upButton = new Rectangle((width - xSize) / 2 + 70, (height - ySize) / 2 + 75, 13, 13);
	downButton = new Rectangle((width - xSize) / 2 + 70, (height - ySize) / 2 + 92, 13, 13);
	super.initGui();
    }

    int rotation = 0;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	int guiWidth = (width - xSize) / 2;
	int guiHeight = (height - ySize) / 2;

	mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiSeismicReader.png"));

	drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
	if (upButton.intersects(new Rectangle(mouseX, mouseY, 1, 1))) {
	    GL11.glColor3f(0.5f, 0.5f, 1f);
	}
	drawTexturedModalRect(upButton.getX(), upButton.getY(), 137, 0, upButton.getWidth(), upButton.getHeight());
	GL11.glColor3f(1, 1, 1);
	if (downButton.intersects(new Rectangle(mouseX, mouseY, 1, 1))) {
	    GL11.glColor3f(0.5f, 0.5f, 1f);
	}
	drawTexturedModalRect(downButton.getX(), downButton.getY(), 150, 0, downButton.getWidth(), downButton.getHeight());
	GL11.glColor3f(1, 1, 1);

	GL11.glPushMatrix();
	GL11.glTranslatef(guiWidth + 48, guiHeight + 87, 0);
	if (currentLayer >= 100) {
	    GL11.glTranslatef(0, 1, 0);
	    GL11.glScalef(0.7f, 0.7f, 0.7f);
	}
	fontRendererObj.drawString(String.format("%s", currentLayer), 0, 0, 0xAFAFAF);
	GL11.glPopMatrix();

	for (int i = 0; i < 7; i++) {
	    int centralX = guiWidth + 32, centralY = guiHeight + 103;
	    int layer = currentLayer + (i - 4);
	    if (0 <= layer && layer < blockList.size()) {
		ItemStack stack = new ItemStack(blockList.get(layer).getRight(), 1, blockList.get(layer).getLeft());
		if (stack.getItem() == null) {
		    continue;
		}
		GL11.glPushMatrix();
		GL11.glTranslatef(centralX - 2, centralY - i * 20 + (20 * 2), 0);
		itemRender.renderItemAndEffectIntoGUI(fontRendererObj, this.mc.getTextureManager(), stack, 0, 0);
		GL11.glPopMatrix();
	    }
	}
	mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiTooltips.png"));
	if (currentLayer - 1 >= 0) {
	    ItemStack nameStack = new ItemStack(blockList.get(currentLayer - 1).getRight(), 0, blockList.get(currentLayer - 1).getLeft());
	    String renderString = "unknown";
	    if (nameStack.getItem() != null) {
		renderString = nameStack.getDisplayName();
	    } else if (blockList.get(currentLayer - 1).getRight() == Blocks.air) {
		renderString = "Air";
	    }
	    String capitalised = renderString.substring(0, 1).toUpperCase() + renderString.substring(1);
	    float renderScale = 1.0f;
	    int lengthX = fontRendererObj.getStringWidth(capitalised);

	    renderScale = lengthX > 53 ? 53f / lengthX : 1.0f;

	    GL11.glPushMatrix();
	    GL11.glTranslatef(guiWidth + 72, guiHeight + 16, 0);
	    GL11.glScalef(renderScale, renderScale, renderScale);
	    fontRendererObj.drawString(capitalised, 0, 0, 0x919191);
	    GL11.glPopMatrix();
	}
	int frequency = 0;
	for (Pair<Integer, Block> pair : blockList) {
	    if (blockList.get(currentLayer - 1) != null) {
		Block block = blockList.get(currentLayer - 1).getRight();
		if (pair.getRight() == block && pair.getLeft() == blockList.get(currentLayer - 1).getLeft()) {
		    frequency++;
		}
	    }
	}
	GL11.glPushMatrix();
	GL11.glTranslatef(guiWidth + 72, guiHeight + 26, 0);
	GL11.glScalef(0.70f, 0.70f, 0.70f);
	fontRendererObj.drawString(String.format("Abundancy: %s", frequency), 0, 0, 0x919191);
	GL11.glPopMatrix();
	super.drawScreen(mouseX, mouseY, partialTick);
    }

    public String wrapString(String str, int index) {
	String string = str;
	for (int i = 0; i < string.length(); i++) {
	    if (i == index) {
		string = string.substring(0, i) + "\n" + string.substring(i);
	    }
	}
	return string;
    }

    @Override
    public void drawBackground(int p_146278_1_) {
	super.drawBackground(p_146278_1_);
    }

    @Override
    public void onGuiClosed() {
	blockList.clear();
	super.onGuiClosed();
    }

    public void calculate() {
	for (int y = 0; y < pos.yCoord; y++) {
	    Block block = worldObj.getBlock(pos.xCoord - 1, y, pos.zCoord - 1);
	    int metadata = worldObj.getBlockMetadata(pos.xCoord - 1, y, pos.zCoord - 1);
	    blockList.add(Pair.of(metadata, block));
	}
    }

    @Override
    protected void mouseClicked(int xPos, int yPos, int buttonClicked) {
	if (upButton.intersects(new Rectangle(xPos, yPos, 1, 1))) {
	    if (currentLayer + 1 <= blockList.size() - 1) {
		currentLayer++;
	    }
	}
	if (downButton.intersects(new Rectangle(xPos, yPos, 1, 1))) {
	    if (currentLayer - 1 >= 1) {
		currentLayer--;
	    }
	}
	super.mouseClicked(xPos, yPos, buttonClicked);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
	super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char c, int p_73869_2_) {
	super.keyTyped(c, p_73869_2_);
    }

    public double getEnergy() {
	return ((ItemSeismicReader) itemStack.getItem()).getEnergy(itemStack);
    }

    @Override
    public boolean doesGuiPauseGame() {
	return false;
    }
}
