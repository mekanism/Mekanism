package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.inventory.container.tile.chance.PrecisionSawmillContainer;
import mekanism.common.recipe.machines.SawmillRecipe;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiPrecisionSawmill extends GuiChanceMachine<SawmillRecipe, TileEntityPrecisionSawmill, PrecisionSawmillContainer> {

    public GuiPrecisionSawmill(PrecisionSawmillContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.PURPLE;
    }
}