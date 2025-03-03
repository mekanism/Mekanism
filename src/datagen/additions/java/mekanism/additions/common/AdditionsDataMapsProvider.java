package mekanism.additions.common;

import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.ParrotImitation;
import org.jetbrains.annotations.NotNull;

public class AdditionsDataMapsProvider extends DataMapProvider {

    public AdditionsDataMapsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(@NotNull HolderLookup.Provider provider) {
        //Add parrot sound imitations for baby mobs
        //Note: There is no imitation sound for endermen
        builder(NeoForgeDataMaps.PARROT_IMITATIONS)
              .add(AdditionsEntityTypes.BABY_BOGGED, new ParrotImitation(SoundEvents.PARROT_IMITATE_BOGGED), false)
              .add(AdditionsEntityTypes.BABY_CREEPER, new ParrotImitation(SoundEvents.PARROT_IMITATE_CREEPER), false)
              .add(AdditionsEntityTypes.BABY_SKELETON, new ParrotImitation(SoundEvents.PARROT_IMITATE_SKELETON), false)
              .add(AdditionsEntityTypes.BABY_STRAY, new ParrotImitation(SoundEvents.PARROT_IMITATE_STRAY), false)
              .add(AdditionsEntityTypes.BABY_WITHER_SKELETON, new ParrotImitation(SoundEvents.PARROT_IMITATE_WITHER_SKELETON), false)
        ;
    }
}