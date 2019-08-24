package mekanism.additions.common.entity;

import java.util.ArrayList;
import java.util.List;
import mekanism.additions.common.MekanismAdditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class AdditionsEntityTypes {

    private static final List<EntityType<?>> types = new ArrayList<>();

    //TODO: Ensure baby skeleton has a spawn egg
    public static final EntityType<EntityBabySkeleton> BABY_SKELETON = create("baby_skeleton", EntityType.Builder.create(EntityBabySkeleton::new, EntityClassification.MONSTER));
    public static final EntityType<EntityObsidianTNT> OBSIDIAN_TNT = create("obsidian_tnt", EntityType.Builder.<EntityObsidianTNT>create(EntityObsidianTNT::new,
          EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F));
    public static final EntityType<EntityBalloon> BALLOON = create("balloon", EntityType.Builder.<EntityBalloon>create(EntityBalloon::new, EntityClassification.MISC)
          .size(0.25F, 0.25F));

    private static <T extends Entity> EntityType<T> create(String name, EntityType.Builder<T> builder) {
        EntityType<T> type = builder.build(name);
        type.setRegistryName(new ResourceLocation(MekanismAdditions.MODID, name));
        types.add(type);
        return type;
    }

    public static void registerEntities(IForgeRegistry<EntityType<?>> registry) {
        types.forEach(registry::register);
        //TODO: Should the list be cleared afterwards as it isn't really needed anymore after registration
    }
}