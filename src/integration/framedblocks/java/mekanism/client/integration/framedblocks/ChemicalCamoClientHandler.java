package mekanism.client.integration.framedblocks;

import io.github.xfacthd.framedblocks.api.camo.CamoContentClientHandler;
import io.github.xfacthd.framedblocks.api.camo.resource.ResourceCamoContentClientHandler;
import it.unimi.dsi.fastutil.ints.IntList;
import mekanism.api.chemical.ChemicalResource;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.integration.framedblocks.ChemicalCamoContent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

public final class ChemicalCamoClientHandler extends ResourceCamoContentClientHandler<ChemicalResource, ChemicalCamoContent> {

    public static final CamoContentClientHandler<ChemicalCamoContent> INSTANCE = new ChemicalCamoClientHandler();

    private ChemicalCamoClientHandler() { }

    @Override
    public ResourceModelSpec getModelSpec(ChemicalCamoContent chemicalCamoContent) {
        ChemicalResource resource = chemicalCamoContent.getResource();
        //TODO - 26.2: Should we force translucency?
        Material.Baked stillMaterial = new Material.Baked(MekanismRenderer.getChemicalTexture(resource), false);
        return new ResourceModelSpec(stillMaterial, null, resource.value().tint() != CommonColors.WHITE, null);
    }

    @Override
    public Particle makeHitDestroyParticle(ClientLevel level, double x, double y, double z, double sx, double sy, double sz, ChemicalCamoContent camo, BlockPos pos) {
        return new ChemicalSpriteParticle(level, x, y, z, sx, sy, sz, camo.getResource());
    }

    @Override
    public int getTintCount(ChemicalCamoContent camo) {
        return 1;
    }

    @Override
    public void collectTintValues(ChemicalCamoContent camo, BlockAndTintGetter level, BlockPos pos, IntList tintList) {
        tintList.add(getParticleTintValue(camo));
    }

    @Override
    public void collectTintValues(ChemicalCamoContent camo, ItemStack stack, IntList tintList) {
        tintList.add(getParticleTintValue(camo));
    }

    @Override
    public int getParticleTintValue(ChemicalCamoContent camo) {
        return camo.getResource().value().tint();
    }
}
