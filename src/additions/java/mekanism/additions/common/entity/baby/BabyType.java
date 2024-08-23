package mekanism.additions.common.entity.baby;

import com.mojang.serialization.Codec;
import java.util.Locale;
import mekanism.api.MekanismAPITags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;

public enum BabyType implements StringRepresentable {
    BOGGED(MekanismAPITags.Biomes.BLACKLIST_BABY_BOGGED, MekanismAPITags.Structures.BLACKLIST_BABY_BOGGED),
    CREEPER(MekanismAPITags.Biomes.BLACKLIST_BABY_CREEPERS, MekanismAPITags.Structures.BLACKLIST_BABY_CREEPERS),
    ENDERMAN(MekanismAPITags.Biomes.BLACKLIST_BABY_ENDERMEN, MekanismAPITags.Structures.BLACKLIST_BABY_ENDERMEN),
    SKELETON(MekanismAPITags.Biomes.BLACKLIST_BABY_SKELETONS, MekanismAPITags.Structures.BLACKLIST_BABY_SKELETONS),
    STRAY(MekanismAPITags.Biomes.BLACKLIST_BABY_STRAYS, MekanismAPITags.Structures.BLACKLIST_BABY_STRAYS),
    WITHER_SKELETON(MekanismAPITags.Biomes.BLACKLIST_BABY_WITHER_SKELETONS, MekanismAPITags.Structures.BLACKLIST_BABY_WITHER_SKELETONS);

    public static final Codec<BabyType> CODEC = StringRepresentable.fromEnum(BabyType::values);

    public final TagKey<Structure> structureBlacklist;
    public final TagKey<Biome> biomeBlacklist;
    private final String serializedName;

    BabyType(TagKey<Biome> biomeBlacklist, TagKey<Structure> structureBlacklist) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.biomeBlacklist = biomeBlacklist;
        this.structureBlacklist = structureBlacklist;

    }

    @NotNull
    @Override
    public String getSerializedName() {
        return serializedName;
    }
}