package mekanism.common.integration.framedblocks;

import io.github.xfacthd.framedblocks.api.camo.CamoContainer;
import io.github.xfacthd.framedblocks.api.camo.CamoContainerFactory;
import mekanism.api.chemical.ChemicalResource;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.Nullable;

final class ChemicalCamoContainer extends CamoContainer<ChemicalCamoContent, ChemicalCamoContainer> {

    ChemicalCamoContainer(ChemicalResource chemical) {
        super(new ChemicalCamoContent(chemical));
    }

    ChemicalResource getChemicalType() {
        return content.getChemicalType();
    }

    @Override
    public boolean canRotateCamo() {
        return false;
    }

    @Override
    @Nullable
    public ChemicalCamoContainer rotateCamo() {
        return null;
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != ChemicalCamoContainer.class) return false;
        return content.equals(((ChemicalCamoContainer) obj).content);
    }

    @Override
    public String toString() {
        return "ChemicalCamoContainer{" + content + "}";
    }

    @Override
    public CamoContainerFactory<ChemicalCamoContainer> getFactory() {
        return FramedBlocksIntegration.CHEMICAL_FACTORY.get();
    }

    @Override
    public ChemicalCamoContainer adjustForCarrierRotation(Rotation rotation) {
        return this;
    }
}
