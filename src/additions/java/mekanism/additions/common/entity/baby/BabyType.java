package mekanism.additions.common.entity.baby;

import com.mojang.serialization.Codec;
import mekanism.additions.common.MekanismAdditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypeIds;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public enum BabyType implements StringRepresentable {
    BOGGED(EntityTypeIds.BOGGED, "Baby Bogged"),
    CREEPER(EntityTypeIds.CREEPER, "Baby Creeper"),
    ENDERMAN(EntityTypeIds.ENDERMAN, "Baby Enderman"),
    PARCHED(EntityTypeIds.PARCHED, "Baby Parched"),
    SKELETON(EntityTypeIds.SKELETON, "Baby Skeleton"),
    STRAY(EntityTypeIds.STRAY, "Baby Stray"),
    WITHER_SKELETON(EntityTypeIds.WITHER_SKELETON, "Baby Wither Skeleton");

    /// @apiNote **Do not modify this array**
    public static final BabyType[] VALUES = values();

    public static final Codec<BabyType> CODEC = StringRepresentable.fromEnum(BabyType::values);

    private final ResourceKey<EntityType<?>> parentId;
    private final TagKey<Structure> structureBlacklist;
    private final TagKey<Biome> biomeBlacklist;
    private final String displayName;
    private final Identifier id;
    private final String name;

    BabyType(ResourceKey<EntityType<?>> parentId, String displayName) {
        this.parentId = parentId;
        this.displayName = displayName;
        this.name = "baby_" + this.parentId.identifier().getPath();
        this.id = MekanismAdditions.rl(this.name);
        Identifier blacklist = id.withPrefix("blacklist/");
        this.biomeBlacklist = TagKey.create(Registries.BIOME, blacklist);
        this.structureBlacklist = TagKey.create(Registries.STRUCTURE, blacklist);
    }

    public String displayName() {
        return this.displayName;
    }

    public ResourceKey<EntityType<?>> parentId() {
        return this.parentId;
    }

    public Identifier id() {
        return this.id;
    }

    public TagKey<Biome> biomeBlacklist() {
        return this.biomeBlacklist;
    }

    public TagKey<Structure> structureBlacklist() {
        return this.structureBlacklist;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}