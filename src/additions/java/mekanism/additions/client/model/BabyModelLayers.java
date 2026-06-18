package mekanism.additions.client.model;

import mekanism.additions.common.entity.baby.BabyType;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.ArmorModelSet;

public class BabyModelLayers {

    private BabyModelLayers() {
    }

    public static final ModelLayerLocation BABY_BOGGED = mainLayer(BabyType.BOGGED);
    public static final ModelLayerLocation BABY_BOGGED_OUTER_LAYER = outerLayer(BabyType.BOGGED);
    public static final ArmorModelSet<ModelLayerLocation> BABY_BOGGED_ARMOR = armorSet(BabyType.BOGGED);

    public static final ModelLayerLocation BABY_CREEPER = mainLayer(BabyType.CREEPER);
    public static final ModelLayerLocation BABY_CREEPER_ARMOR = layer(BabyType.CREEPER, "armor");

    public static final ModelLayerLocation BABY_ENDERMAN = mainLayer(BabyType.ENDERMAN);

    public static final ModelLayerLocation BABY_PARCHED = mainLayer(BabyType.PARCHED);
    public static final ArmorModelSet<ModelLayerLocation> BABY_PARCHED_ARMOR = armorSet(BabyType.PARCHED);

    public static final ModelLayerLocation BABY_SKELETON = mainLayer(BabyType.SKELETON);
    public static final ArmorModelSet<ModelLayerLocation> BABY_SKELETON_ARMOR = armorSet(BabyType.SKELETON);

    public static final ModelLayerLocation BABY_STRAY = mainLayer(BabyType.STRAY);
    public static final ModelLayerLocation BABY_STRAY_OUTER_LAYER = outerLayer(BabyType.STRAY);
    public static final ArmorModelSet<ModelLayerLocation> BABY_STRAY_ARMOR = armorSet(BabyType.STRAY);

    public static final ModelLayerLocation BABY_WITHER_SKELETON = mainLayer(BabyType.WITHER_SKELETON);
    public static final ArmorModelSet<ModelLayerLocation> BABY_WITHER_SKELETON_ARMOR = armorSet(BabyType.WITHER_SKELETON);

    private static ModelLayerLocation mainLayer(BabyType babyType) {
        return layer(babyType, "main");
    }

    private static ModelLayerLocation outerLayer(BabyType babyType) {
        return layer(babyType, "outer");
    }

    private static ModelLayerLocation layer(BabyType babyType, String layer) {
        return new ModelLayerLocation(babyType.id(), layer);
    }

    private static ArmorModelSet<ModelLayerLocation> armorSet(BabyType babyType) {
        return new ArmorModelSet<>(layer(babyType, "helmet"), layer(babyType, "chestplate"), layer(babyType, "leggings"), layer(babyType, "boots"));
    }
}