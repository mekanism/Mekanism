package mekanism.common.world;

import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeatureCountTracker;
import net.minecraft.world.level.levelgen.placement.FeaturePlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class RetrogenFeaturePlacer extends FeaturePlacer {

    public RetrogenFeaturePlacer(WorldGenLevel level, ChunkGenerator generator) {
        super(level, generator);
    }

    public boolean placeWithBiomeCheck(PlacedFeature placedFeature, PlacedFeature baseFeature, RandomSource random, BlockPos origin) {
        return place(placedFeature, baseFeature, random, origin, true);
    }

    private boolean place(PlacedFeature placedFeature, PlacedFeature baseFeature, RandomSource random, BlockPos origin, boolean biomeCheck) {
        //Note: We pass the context pretend that it is  directly to allow for doing a placeWithBiomeCheck, except by having the context pretend
        // it is the non retrogen feature which actually is added to the various biomes
        Optional<PlacedFeature> topFeature = biomeCheck ? Optional.of(baseFeature) : Optional.empty();
        //From super
        PlacementContext context = new PlacementContext(this.level, this.generator, topFeature);
        Feature feature = placedFeature.feature().value();
        List<PlacementModifier> placement = placedFeature.placement();
        if (placement.isEmpty()) {
            if (SharedConstants.DEBUG_FEATURE_COUNT) {
                FeatureCountTracker.featurePlaced(this.level.getLevel(), feature, topFeature);
            }

            return feature.place(this.level, this.generator, random, origin);
        }
        boolean placedAny = false;
        this.positions.add(origin);
        this.modifierIndices.add(0);

        for (; !this.positions.isEmpty(); this.modifiedPositions.clear()) {
            BlockPos pos = this.positions.removeLast();
            int modifierIndex = this.modifierIndices.removeInt(this.modifierIndices.size() - 1);
            PlacementModifier modifier = placement.get(modifierIndex);
            modifier.modify(context, random, pos, this.modifiedPositions::add);
            int nextModifierIndex = modifierIndex + 1;
            if (nextModifierIndex < placement.size()) {
                for (int i = this.modifiedPositions.size() - 1; i >= 0; i--) {
                    this.positions.add(this.modifiedPositions.get(i));
                    this.modifierIndices.add(nextModifierIndex);
                }
            } else {
                for (BlockPos nextPos : this.modifiedPositions) {
                    placedAny |= feature.place(this.level, this.generator, random, nextPos);
                    if (SharedConstants.DEBUG_FEATURE_COUNT) {
                        FeatureCountTracker.featurePlaced(this.level.getLevel(), feature, topFeature);
                    }
                }
            }
        }

        return placedAny;
    }
}