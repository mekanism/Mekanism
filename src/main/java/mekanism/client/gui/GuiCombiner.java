package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.inventory.container.tile.double_electric.CombinerContainer;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.tile.TileEntityCombiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiCombiner extends GuiDoubleElectricMachine<CombinerRecipe, TileEntityCombiner, CombinerContainer> {

    public GuiCombiner(CombinerContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.STONE;
    }
}