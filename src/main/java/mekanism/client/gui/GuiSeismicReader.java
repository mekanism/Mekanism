package mekanism.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.client.gui.button.MekanismButton;
import mekanism.client.gui.button.SeismicReaderButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.item.SeismicReaderContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

//TODO: Test to see if this properly was converted to GuiMekanism
public class GuiSeismicReader extends GuiMekanism<SeismicReaderContainer> {

    private Coord4D pos;
    private World worldObj;
    private List<BlockState> blockList = new ArrayList<>();
    //private Rectangle tooltip;
    private MekanismButton upButton;
    private MekanismButton downButton;

    private int currentLayer;

    public GuiSeismicReader(SeismicReaderContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        //TODO: Is setting this like this correct
        xSize = 137;
        ySize = 182;
        PlayerEntity player = inv.player;
        BlockPos position = player.getPosition();
        worldObj = player.world;
        //TODO: Does this Math.min make it so that things like CubicChunks don't work
        pos = new Coord4D(position.getX(), Math.min(255, position.getY()), position.getZ(), worldObj.getDimension().getType());
        calculate();
        currentLayer = Math.max(0, blockList.size() - 1);
        //TODO: Add scroll bar element
    }

    @Override
    public void init() {
        super.init();
        addButton(upButton = new SeismicReaderButton(this, guiLeft + 70, guiTop + 75, 13, 13, 137, 0, getGuiLocation(),
              () -> currentLayer++));
        addButton(downButton = new SeismicReaderButton(this, guiLeft + 70, guiTop + 92, 13, 13, 150, 0, getGuiLocation(),
              () -> currentLayer--));
        //tooltip = new Rectangle(guiLeft + 30, guiTop + 82, 16, 16);
        updateEnabledButtons();
    }

    @Override
    public void tick() {
        super.tick();
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        upButton.active = currentLayer + 1 <= blockList.size() - 1;
        downButton.active = currentLayer - 1 >= 1;
    }

    //TODO: Rewrite this to properly use foreground and background, as well as, not have so many GL calls
    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        int guiLeft = (width - xSize) / 2;
        int guiTop = (height - ySize) / 2;
        minecraft.textureManager.bindTexture(getGuiLocation());
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Fix the overlapping if > 100
        GlStateManager.pushMatrix();
        GlStateManager.translatef(guiLeft + 48, guiTop + 87, 0);

        if (currentLayer >= 100) {
            GlStateManager.translatef(0, 1, 0);
            GlStateManager.scalef(0.7F, 0.7F, 0.7F);
        }

        drawString(String.format("%s", currentLayer), 0, 0, 0xAFAFAF);
        GlStateManager.popMatrix();

        // Render the item stacks
        for (int i = 0; i < 9; i++) {
            int centralX = guiLeft + 32, centralY = guiTop + 103;
            int layer = currentLayer + (i - 5);
            if (0 <= layer && layer < blockList.size()) {
                BlockState state = blockList.get(layer);
                ItemStack stack = new ItemStack(state.getBlock());
                GlStateManager.pushMatrix();
                GlStateManager.translatef(centralX - 2, centralY - i * 16 + (22 * 2), 0);
                if (i < 4) {
                    GlStateManager.translatef(0.2F, 2.5F, 0);
                }
                if (i != 4) {
                    GlStateManager.translatef(1.5F, 0, 0);
                    GlStateManager.scalef(0.8F, 0.8F, 0.8F);
                }
                renderItem(stack, 0, 0);
                GlStateManager.popMatrix();
            }
        }

        int frequency = 0;
        // Get the name from the stack and render it
        if (currentLayer - 1 >= 0) {
            BlockState state = blockList.get(currentLayer - 1);
            ItemStack nameStack = new ItemStack(state.getBlock());
            ITextComponent displayName = nameStack.getDisplayName();
            int lengthX = getStringWidth(displayName);
            float renderScale = lengthX > 53 ? 53f / lengthX : 1.0f;

            GlStateManager.pushMatrix();
            GlStateManager.translatef(guiLeft + 72, guiTop + 16, 0);
            GlStateManager.scalef(renderScale, renderScale, renderScale);
            drawString(displayName, 0, 0, 0x919191);
            GlStateManager.popMatrix();

            //TODO
            /*if (tooltip.intersects(new Rectangle(mouseX, mouseY, 1, 1))) {
                minecraft.textureManager.bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiTooltips.png"));
                int fontLengthX = lengthX + 5;
                int renderX = mouseX + 10, renderY = mouseY - 5;
                GlStateManager.pushMatrix();
                blit(renderX, renderY, 0, 0, fontLengthX, 16);
                blit(renderX + fontLengthX, renderY, 0, 16, 2, 16);
                drawString(displayName, renderX + 4, renderY + 4, 0x919191);
                GlStateManager.popMatrix();
            }*/

            frequency = (int) blockList.stream().filter(blockState -> state.getBlock() == blockState.getBlock()).count();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translatef(guiLeft + 72, guiTop + 26, 0);
        GlStateManager.scalef(0.7F, 0.7F, 0.7F);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.abundancy"), ": " + frequency), 0, 0, 0x919191);
        GlStateManager.popMatrix();
        MekanismRenderer.resetColor();
        super.render(mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        super.onClose();
        blockList.clear();
    }

    public void calculate() {
        for (BlockPos p = new BlockPos(pos.x, 0, pos.z); p.getY() < pos.y; p = p.up()) {
            blockList.add(worldObj.getBlockState(p));
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "seismic_reader.png");
    }
}