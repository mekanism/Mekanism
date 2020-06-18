package mekanism.client.gui.element.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiElement.IHoverable;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.sound.SoundHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

//TODO: Use interfaces to implement common code as defaulted, so that duplicate miner code and duplicate transporter code can be deduplicated
// even across different filter types
@Deprecated//TODO: Transition over to GuiFilterDialog
public abstract class GuiFilterBase<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiFilter<TILE, CONTAINER> {

    protected ITextComponent status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN);
    protected FILTER origFilter;
    protected FILTER filter;
    protected boolean isNew;
    protected int ticker;

    protected MekanismButton saveButton;
    protected MekanismButton deleteButton;

    protected GuiSequencedSlotDisplay slotDisplay;

    protected GuiFilterBase(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        if (isNew) {
            deleteButton.active = false;
        }
        addButton(slotDisplay = new GuiSequencedSlotDisplay(this, 12, 19, this::getRenderStacks));
        updateRenderStacks();
    }

    protected IHoverable getOnHoverReplace(MinerFilter<?> filter) {
        return getOnHover(() -> MekanismLang.MINER_REQUIRE_REPLACE.translate(YesNo.of(filter.requireStack)));
    }

    protected List<ItemStack> getRenderStacks() {
        return Collections.emptyList();
    }

    public void updateRenderStacks() {
        slotDisplay.updateStackList();
    }

    protected void drawMinerForegroundLayer() {
        if (filter instanceof MinerFilter) {
            MinerFilter<?> mFilter = (MinerFilter<?>) filter;
            renderItem(mFilter.replaceStack, 149, 19);
        }
    }

    protected void drawTransporterForegroundLayer() {
        if (filter instanceof TransporterFilter) {
            TransporterFilter<?> tFilter = (TransporterFilter<?>) filter;
            drawString(OnOff.of(tFilter.allowDefault).getTextComponent(), 24, 66, titleTextColor());
        }
    }

    protected void minerFilterClickCommon(double xAxis, double yAxis, MinerFilter<?> filter) {
        //Over replace output
        if (xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35) {
            boolean doNull = false;
            ItemStack stack = minecraft.player.inventory.getItemStack();
            ItemStack toUse = ItemStack.EMPTY;
            if (!stack.isEmpty() && !hasShiftDown()) {
                if (stack.getItem() instanceof BlockItem) {
                    //TODO: Either look at unbreakable blocks or make a tag for a blacklist
                    if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                        toUse = stack.copy();
                        toUse.setCount(1);
                    }
                }
            } else if (stack.isEmpty() && hasShiftDown()) {
                doNull = true;
            }
            if (!toUse.isEmpty() || doNull) {
                filter.replaceStack = toUse;
            }
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }
}