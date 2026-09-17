package mekanism.tools.common;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismBlockTransformers;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.registries.BaseRegistryProvider;
import mekanism.tools.common.recipe.ToolsRecipeProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

public class ToolsRegistryProvider extends BaseRegistryProvider {

    public static DatapackBuiltinEntriesProvider forWorldLayer(PackOutput output, CompletableFuture<HolderLookup.Provider> worldRegistries) {
        return forWorldLayer(output, worldRegistries, MekanismTools.MODID, new RegistrySetBuilder()
              .add(Registries.BLOCK_TRANSFORMER, context -> {
                  //TODO - 26.3: Add support for the stripping/waxing etc data maps?
                  context.register(MekanismBlockTransformers.PAXEL, new BlockTransformer(
                        ImmutableList.<BlockTransformer.BlockTransformData>builder()
                              //Axe
                              .addAll(List.of(BlockTransformers.axeStrippables()))
                              .addAll(BlockTransformers.axe(WeatheringCopper.PREVIOUS_BY_BLOCK.get().entrySet(), SoundEvents.AXE_SCRAPE, BlockTransformer.TransformParticle.SCRAPE))
                              .addAll(BlockTransformers.axe(HoneycombItem.WAX_OFF_BY_BLOCK.get().entrySet(), SoundEvents.AXE_WAX_OFF, BlockTransformer.TransformParticle.WAX_OFF))
                              //Shovel
                              .add(BlockTransformer.BlockTransformData.builder(
                                                BlockPredicate.allOf(BlockPredicate.matchesTag(BlockTags.TURNS_INTO_DIRT_PATH), BlockPredicate.matchesTag(Direction.UP, BlockTags.AIR)),
                                                Blocks.DIRT_PATH
                                          ).sound(SoundEvents.SHOVEL_FLATTEN)
                                          .disallowedFaces(List.of(Direction.DOWN))
                                          .build()
                              )
                              .build()
                        //TODO - 26.3: Axe and shovel
                  ));
              })
        );
    }

    public static DatapackBuiltinEntriesProvider forReloadableLayer(PackOutput output, CompletableFuture<HolderLookup.Provider> worldRegistries,
          CompletableFuture<HolderLookup.Provider> reloadableRegistries) {
        return forReloadableLayer(output, worldRegistries, reloadableRegistries, MekanismTools.MODID, new RegistrySetBuilder()
              .add(Registries.ADVANCEMENT, new AdvancementProvider(List.of(ToolsAdvancementProvider::new)))
              .add(BaseRecipeProvider.registerRecipes(ToolsRecipeProvider::new))
        );
    }
}