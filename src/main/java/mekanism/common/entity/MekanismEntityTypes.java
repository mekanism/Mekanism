package mekanism.common.entity;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Check the different settings. For example should robit be immune to fire
public class MekanismEntityTypes {

    private static final List<EntityType<?>> types = new ArrayList<>();

    //TODO: Ensure baby skeleton has a spawn egg
    public static final EntityType<EntityBabySkeleton> BABY_SKELETON = create("baby_skeleton", EntityType.Builder.create(EntityBabySkeleton::new, EntityClassification.MONSTER));
    public static final EntityType<EntityRobit> ROBIT = create("robit", EntityType.Builder.<EntityRobit>create(EntityRobit::new, EntityClassification.MISC)
          .size(0.5F, 0.5F));
    public static final EntityType<EntityObsidianTNT> OBSIDIAN_TNT = create("obsidian_tnt", EntityType.Builder.<EntityObsidianTNT>create(EntityObsidianTNT::new,
          EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F));
    public static final EntityType<EntityBalloon> BALLOON = create("balloon", EntityType.Builder.<EntityBalloon>create(EntityBalloon::new, EntityClassification.MISC)
          .size(0.25F, 0.25F));
    public static final EntityType<EntityFlame> FLAME = create("flame", EntityType.Builder.<EntityFlame>create(EntityFlame::new, EntityClassification.MISC)
          .size(0.5F, 0.5F));

    private static <T extends Entity> EntityType<T> create(String name, EntityType.Builder<T> factory) {
        EntityType<T> type = factory.build(name);
        type.setRegistryName(new ResourceLocation(Mekanism.MODID, name));
        types.add(type);
        return type;
    }

    public static void registerEntities(IForgeRegistry<EntityType<?>> registry) {
        types.forEach(registry::register);
        //TODO: Should the list be cleared afterwards as it isn't really needed anymore after registration
    }
}