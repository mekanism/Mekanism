package mekanism.api;

import mekanism.additions.common.entity.baby.BabyType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test that the tags we expose in the API match what we generate internally")
class TagMatchesTest {

    @Test
    @DisplayName("Test that the tags we expose in the API for blacklisting baby bogged mob spawns match the tags that are generated from the mob's name")
    void testBabyBoggedBlacklist() {
        assertBlacklistMatches(BabyType.BOGGED, MekanismAPITags.Biomes.BLACKLIST_BABY_BOGGED, MekanismAPITags.Structures.BLACKLIST_BABY_BOGGED);
    }

    @Test
    @DisplayName("Test that the tags we expose in the API for blacklisting baby creeper mob spawns match the tags that are generated from the mob's name")
    void testBabyCreeperBlacklist() {
        assertBlacklistMatches(BabyType.CREEPER, MekanismAPITags.Biomes.BLACKLIST_BABY_CREEPERS, MekanismAPITags.Structures.BLACKLIST_BABY_CREEPERS);
    }

    @Test
    @DisplayName("Test that the tags we expose in the API for blacklisting baby endermen mob spawns match the tags that are generated from the mob's name")
    void testBabyEndermanBlacklist() {
        assertBlacklistMatches(BabyType.ENDERMAN, MekanismAPITags.Biomes.BLACKLIST_BABY_ENDERMEN, MekanismAPITags.Structures.BLACKLIST_BABY_ENDERMEN);
    }

    @Test
    @DisplayName("Test that the tags we expose in the API for blacklisting baby parched mob spawns match the tags that are generated from the mob's name")
    void testBabyParchedBlacklist() {
        assertBlacklistMatches(BabyType.PARCHED, MekanismAPITags.Biomes.BLACKLIST_BABY_PARCHED, MekanismAPITags.Structures.BLACKLIST_BABY_PARCHED);
    }

    @Test
    @DisplayName("Test that the tags we expose in the API for blacklisting baby skeleton mob spawns match the tags that are generated from the mob's name")
    void testBabySkeletonBlacklist() {
        assertBlacklistMatches(BabyType.SKELETON, MekanismAPITags.Biomes.BLACKLIST_BABY_SKELETONS, MekanismAPITags.Structures.BLACKLIST_BABY_SKELETONS);
    }

    @Test
    @DisplayName("Test that the tags we expose in the API for blacklisting baby stray mob spawns match the tags that are generated from the mob's name")
    void testBabyStrayBlacklist() {
        assertBlacklistMatches(BabyType.STRAY, MekanismAPITags.Biomes.BLACKLIST_BABY_STRAYS, MekanismAPITags.Structures.BLACKLIST_BABY_STRAYS);
    }

    @Test
    @DisplayName("Test that the tags we expose in the API for blacklisting baby wither skeleton mob spawns match the tags that are generated from the mob's name")
    void testBabyWitherSkeletonBlacklist() {
        assertBlacklistMatches(BabyType.WITHER_SKELETON, MekanismAPITags.Biomes.BLACKLIST_BABY_WITHER_SKELETONS, MekanismAPITags.Structures.BLACKLIST_BABY_WITHER_SKELETONS);
    }

    private void assertBlacklistMatches(BabyType babyType, TagKey<Biome> biomeBlacklist, TagKey<Structure> structureBlacklist) {
        Assertions.assertEquals(babyType.biomeBlacklist(), biomeBlacklist, "Biome blacklist tag does not match");
        Assertions.assertEquals(babyType.structureBlacklist(), structureBlacklist, "Structure blacklist tag does not match");
    }
}