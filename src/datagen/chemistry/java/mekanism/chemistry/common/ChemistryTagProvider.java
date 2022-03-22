package mekanism.chemistry.common;

import java.util.List;
import mekanism.api.providers.IBlockProvider;
import mekanism.chemistry.common.registries.ChemistryBlocks;
import mekanism.chemistry.common.registries.ChemistryFluids;
import mekanism.chemistry.common.registries.ChemistryGases;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tag.MekanismTagProvider;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ChemistryTagProvider extends BaseTagProvider {

    public ChemistryTagProvider(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, MekanismChemistry.MODID, existingFileHelper);
    }

    @Override
    protected List<IBlockProvider> getAllBlocks() {
        return ChemistryBlocks.BLOCKS.getAllBlocks();
    }

    @Override
    protected void registerTags() {
        addBoxBlacklist();
        addEndermanBlacklist();
        addFluids();
        addGases();
        addHarvestRequirements();
    }

    private void addBoxBlacklist() {
//        addToTag(MekanismTags.Blocks.RELOCATION_NOT_SUPPORTED,
//        );
        TileEntityTypeRegistryObject<?>[] tilesToBlacklist = {
        };
        addToTag(MekanismTags.TileEntityTypes.IMMOVABLE, tilesToBlacklist);
        addToTag(MekanismTags.TileEntityTypes.RELOCATION_NOT_SUPPORTED, tilesToBlacklist);
    }

    private void addEndermanBlacklist() {
//        addToTag(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST,
//        );
    }

    private void addFluids() {
        addToTag(ChemistryTags.Fluids.AMMONIA, ChemistryFluids.AMMONIA);
        addToTag(ChemistryTags.Fluids.NITROGEN, ChemistryFluids.NITROGEN);
        addToTag(ChemistryTags.Fluids.AIR, ChemistryFluids.AIR);
        //Prevent all our fluids from being duped by create
        for (FluidRegistryObject<?, ?, ?, ?> fluid : ChemistryFluids.FLUIDS.getAllFluids()) {
            addToTag(MekanismTagProvider.CREATE_NO_INFINITE_FLUID, fluid);
        }
    }

    private void addGases() {
        addToTag(ChemistryTags.Gases.AMMONIA, ChemistryGases.AMMONIA);
        addToTag(ChemistryTags.Gases.NITROGEN, ChemistryGases.NITROGEN);
        addToTag(ChemistryTags.Gases.AIR, ChemistryGases.AIR);
    }

    private void addHarvestRequirements() {
        addToHarvestTag(BlockTags.MINEABLE_WITH_PICKAXE,
              ChemistryBlocks.AIR_COMPRESSOR,
              ChemistryBlocks.FRACTIONATING_DISTILLER_CONTROLLER,
              ChemistryBlocks.FRACTIONATING_DISTILLER_VALVE,
              ChemistryBlocks.FRACTIONATING_DISTILLER_BLOCK
        );
    }
}
