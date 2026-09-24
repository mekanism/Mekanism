
package mekanism.common.tests.tag;

import java.util.Set;
import java.util.stream.Collectors;
import mekanism.common.Mekanism;
import mekanism.common.tests.helpers.MissingElementTestHelper;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "tag.missing")
public class MissingTagsTest {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that we didn't accidentally forget to add any blocks to the block motion tag.")
    public static void testMissingBlocksMotionTags(final DynamicTest test, final RegistrationHelper reg) {
        final TagKey<Block> KNOWN_MISSING = BlockTags.create(Identifier.fromNamespaceAndPath(reg.modId(), "known_missing"));
        reg.addClientProvider(event -> new BlockTagsProvider(event.getGenerator().getPackOutput(), event.getWorldLookupProvider(), reg.modId()) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                tag(KNOWN_MISSING);
            }
        });

        test.onGameTest(MissingElementTestHelper.class, helper -> helper.succeedIf(() -> {
            Set<ResourceKey<Block>> missingTags = BuiltInRegistries.BLOCK.listElements()
                  //Only mekanism items
                  .filter(element -> element.key().identifier().getNamespace().startsWith(Mekanism.MODID))
                  // If it doesn't block motion, and isn't known to not block motion
                  .filter(element -> !element.is(BlockTags.BLOCKS_MOTION) && !element.is(KNOWN_MISSING))
                  .filter(element -> !(element.value() instanceof LiquidBlock))
                  .map(Reference::key)
                  .collect(Collectors.toSet());
            helper.checkForMissing("tags", missingTags);
        }));
    }
}