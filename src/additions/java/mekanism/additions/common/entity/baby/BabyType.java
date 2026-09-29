package mekanism.additions.common.entity.baby;

import com.mojang.serialization.Codec;
import java.util.List;
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
import org.jetbrains.annotations.Unmodifiable;

public enum BabyType implements StringRepresentable {
    BOGGED(EntityTypeIds.BOGGED, "Baby Bogged", true),
    CREEPER(EntityTypeIds.CREEPER, "Baby Creeper", false),
    ENDERMAN(EntityTypeIds.ENDERMAN, "Baby Enderman", false),
    PARCHED(EntityTypeIds.PARCHED, "Baby Parched", true),
    SKELETON(EntityTypeIds.SKELETON, "Baby Skeleton", true),
    STRAY(EntityTypeIds.STRAY, "Baby Stray", true),
    WITHER_SKELETON(EntityTypeIds.WITHER_SKELETON, "Baby Wither Skeleton", true);

    /// Cached value of [BabyType#values()].
    @Unmodifiable
    public static final List<BabyType> VALUES = List.of(values());

    public static final Codec<BabyType> CODEC = StringRepresentable.fromEnum(BabyType::values);

    private final ResourceKey<EntityType<?>> parentId;
    private final TagKey<Structure> structureBlacklist;
    private final TagKey<Biome> biomeBlacklist;
    private final boolean hasEquipment;
    private final String displayName;
    private final Identifier id;
    private final String name;

    BabyType(ResourceKey<EntityType<?>> parentId, String displayName, boolean hasEquipment) {
        this.parentId = parentId;
        this.displayName = displayName;
        this.name = "baby_" + this.parentId.identifier().getPath();
        this.hasEquipment = hasEquipment;
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

    public boolean hasEquipment() {
        return this.hasEquipment;
    }
}