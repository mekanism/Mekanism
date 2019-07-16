package mekanism.client.render.item.machine;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelChemicalDissolutionChamber;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderChemicalDissolutionChamberItem {

    private static ModelChemicalDissolutionChamber chemicalDissolutionChamber = new ModelChemicalDissolutionChamber();

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.translate(0.05F, -1.001F, 0.05F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalDissolutionChamber.png"));
        chemicalDissolutionChamber.render(0.0625F);
    }
}