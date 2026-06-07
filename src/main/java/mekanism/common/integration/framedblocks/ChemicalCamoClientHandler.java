package mekanism.common.integration.framedblocks;

import io.github.xfacthd.framedblocks.api.camo.CamoContentClientHandler;
import io.github.xfacthd.framedblocks.api.camo.resource.ResourceCamoContentClientHandler;
import it.unimi.dsi.fastutil.ints.IntList;
import mekanism.api.annotations.MethodsAreNotNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
@MethodsAreNotNullByDefault
final class ChemicalCamoClientHandler extends ResourceCamoContentClientHandler<ChemicalResource, ChemicalCamoContent> {

    static final CamoContentClientHandler<ChemicalCamoContent> INSTANCE = new ChemicalCamoClientHandler();

    private ChemicalCamoClientHandler() { }

    @Override
    public ResourceModelSpec getModelSpec(ChemicalCamoContent chemicalCamoContent) {
        ChemicalResource resource = chemicalCamoContent.getResource();
        //TODO - 26.1: Should we force translucency?
        Material.Baked stillMaterial = new Material.Baked(MekanismRenderer.getChemicalTexture(resource), false);
        //TODO - 26.1: Check if we define alpha for chemicals is defined. We might want to enforce alpha to be specified when we used to not
        return new ResourceModelSpec(stillMaterial, null, resource.getChemicalTint() != 0XFFFFFFFF, null);
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
        tintList.add(camo.getResource().getChemicalTint());
    }

    @Override
    public void collectTintValues(ChemicalCamoContent camo, ItemStack stack, IntList tintList) {
        tintList.add(camo.getResource().getChemicalTint());
    }

    @Override
    public int getParticleTintValue(ChemicalCamoContent camo) {
        return camo.getResource().getChemicalTint();
    }
}
