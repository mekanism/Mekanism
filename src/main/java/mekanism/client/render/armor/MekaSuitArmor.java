package mekanism.client.render.armor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.client.model.BaseModelCache.OBJModelData;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.lib.QuickHash;
import mekanism.client.render.lib.effect.BoltFeatureRenderer;
import mekanism.client.render.lib.effect.BoltFeatureRenderer.BoltRenderState;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.registries.MekanismModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.ElytraAnimationState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

//TODO - 26.2: Part of leg LED is not glowing
public class MekaSuitArmor implements ICustomArmor, ISpecialGear {

    private static final String INACTIVE_TAG = "inactive_";
    public static final String OVERRIDDEN_TAG = "override_";
    private static final String EXCLUSIVE_TAG = "excl_";
    private static final String SHARED_TAG = "shared_";
    private static final String GLASS_TAG = "glass";

    public static final MekaSuitArmor HELMET = new MekaSuitArmor(EquipmentSlot.HEAD, EquipmentSlot.CHEST);
    public static final MekaSuitArmor BODYARMOR = new MekaSuitArmor(EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    public static final MekaSuitArmor PANTS = new MekaSuitArmor(EquipmentSlot.LEGS, EquipmentSlot.FEET);
    public static final MekaSuitArmor BOOTS = new MekaSuitArmor(EquipmentSlot.FEET, EquipmentSlot.LEGS);

    private static final Table<EquipmentSlot, Holder<ModuleData<?>>, ModuleModelSpec<?>> moduleModelSpec = HashBasedTable.create();

    private static final Map<UUID, BoltRenderer> boltRenderMap = new Object2ObjectOpenHashMap<>();
    private static final BoltEffect LEFT_GRAV_BOLT = new BoltEffect(BoltRenderInfo.ELECTRICITY, new Vector3f(-0.01F, 0.35F, 0.37F),
          new Vector3f(-0.01F, 0.15F, 0.37F), 10).size(0.012F).lifespan(6).spawn(SpawnFunction.noise(3, 1));
    private static final BoltEffect RIGHT_GRAV_BOLT = new BoltEffect(BoltRenderInfo.ELECTRICITY, new Vector3f(0.025F, 0.35F, 0.37F),
          new Vector3f(0.025F, 0.15F, 0.37F), 10).size(0.012F).lifespan(6).spawn(SpawnFunction.noise(3, 1));
    public static final ContextKey<UUID> UUID_CONTEXT = new ContextKey<>(Mekanism.rl("uuid"));
    public static final ContextKey<Double> DELTA_Y_CONTEXT = new ContextKey<>(Mekanism.rl("delta_y"));

    private static final Vector3fc BASE_TRANSLATION = new Vector3f(-1, 0.5F, 0);

    private final LoadingCache<QuickHash, ArmorQuads> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @Override
        @SuppressWarnings("unchecked")
        public ArmorQuads load(QuickHash key) {
            return createQuads((Object2BooleanMap<ModuleModelSpec<?>>) key.objs()[0], (Set<EquipmentSlot>) key.objs()[1], (boolean) key.objs()[2], (boolean) key.objs()[3]);
        }
    });

    private final EquipmentSlot type;
    private final EquipmentSlot adjacentType;

    private MekaSuitArmor(EquipmentSlot type, EquipmentSlot adjacentType) {
        this.type = type;
        this.adjacentType = adjacentType;
        MekanismModelCache.INSTANCE.reloadCallback(cache::invalidateAll);
    }

    private static Color getColor(ItemStack stack) {
        IModule<ModuleColorModulationUnit> colorUnit = IModuleHelper.INSTANCE.getModule(stack, MekanismModules.COLOR_MODULATION_UNIT);
        return colorUnit == null ? Color.WHITE : colorUnit.getCustomInstance().color();
    }

    public <AVATAR extends Avatar & ClientAvatarEntity> void renderArm(AVATAR avatar, ModelPart armPart, PoseStack poseStack, SubmitNodeCollector nodeCollector,
          int lightCoords, int outlineColor, ItemStack stack, boolean rightHand) {
        ModelPos armPos = rightHand ? ModelPos.RIGHT_ARM : ModelPos.LEFT_ARM;
        ArmorQuads armorQuads = cache.getUnchecked(key(Either.right(avatar), avatar.getMainArm(), avatar, LivingEntity::getItemBySlot));
        boolean hasOpaqueArm = armorQuads.opaqueParts().containsKey(armPos);
        boolean hasTransparentArm = armorQuads.transparentParts().containsKey(armPos);
        if (hasOpaqueArm || hasTransparentArm) {
            poseStack.pushPose();
            armPart.translateAndRotate(poseStack);
            armPos.translateModel(poseStack);
            boolean hasFoil = stack.hasFoil();
            //Same as what HumanoidArmorLayer does for the starting order index
            int nextOrder = 1;
            if (hasOpaqueArm) {
                List<BlockStateModelPart> opaqueParts = armorQuads.opaqueParts().get(armPos);
                int[] tint = {getColor(stack).argb()};
                nodeCollector.order(nextOrder++).submitBlockModel(poseStack, MekanismRenderType.MEKASUIT, opaqueParts, tint, lightCoords, OverlayTexture.NO_OVERLAY, outlineColor);
                if (hasFoil) {
                    nodeCollector.order(nextOrder++).submitBlockModel(poseStack, MekanismRenderType.ARMOR_GLINT, opaqueParts, tint, lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
                }
            }
            if (hasTransparentArm) {
                List<BlockStateModelPart> transparentParts = armorQuads.transparentParts().get(armPos);
                nodeCollector.order(nextOrder++).submitBlockModel(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_ITEMS), transparentParts, BlockModelRenderState.EMPTY_TINTS,
                      lightCoords, OverlayTexture.NO_OVERLAY, outlineColor);
                if (hasFoil) {
                    nodeCollector.order(nextOrder).submitBlockModel(poseStack, MekanismRenderType.ARMOR_GLINT, transparentParts, BlockModelRenderState.EMPTY_TINTS,
                          lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
                }
            }
            poseStack.popPose();
        }
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, ItemStack stack) {
        baseModel.setupAnim(state);
        ArmorQuads armorQuads = cache.getUnchecked(key(Either.left(state), state.mainArm, state, MekaSuitArmor::getItemBySlot));
        boolean renderFoil = stack.hasFoil();
        //Same as what HumanoidArmorLayer does for the starting order index
        int nextOrder = render(baseModel, nodeCollector, poseStack, lightCoords, renderFoil, 1, getColor(stack), state, armorQuads.opaqueParts(), false);

        if (type == EquipmentSlot.CHEST) {
            UUID entityUUID = state.getRenderData(UUID_CONTEXT);
            if (entityUUID != null) {
                long gameTime = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
                BoltRenderer boltRenderer = boltRenderMap.computeIfAbsent(entityUUID, _ -> new BoltRenderer());
                if (IModuleHelper.INSTANCE.isEnabled(state.chestEquipment, MekanismModules.GRAVITATIONAL_MODULATING_UNIT)) {
                    boltRenderer.update(0, LEFT_GRAV_BOLT, gameTime, state.partialTick);
                    boltRenderer.update(1, RIGHT_GRAV_BOLT, gameTime, state.partialTick);
                }
                List<BoltRenderState> boltRenderStates = boltRenderer.collectBoltStates(gameTime, state.partialTick);
                if (!boltRenderStates.isEmpty()) {
                    //Adjust the poseStack so that we render the lightning in the correct spot if the player is crouching
                    poseStack.pushPose();
                    ModelPos.BODY.translate(baseModel, poseStack, state);
                    PoseStack.Pose pose = poseStack.last().copy();
                    for (BoltRenderState boltState : boltRenderStates) {
                        //TODO - 26.2: Figure out the render phase to target
                        nodeCollector.submitSpecial(RenderPhaseKeys.AFTER_TERRAIN, new BoltFeatureRenderer.Submit(pose, boltState));
                    }
                    poseStack.popPose();
                }
            }
        }

        //Pass white as the color because we don't want to tint transparent quads
        render(baseModel, nodeCollector, poseStack, lightCoords, renderFoil, nextOrder, Color.WHITE, state, armorQuads.transparentParts(), true);
    }

    private <STATE extends HumanoidRenderState> int render(HumanoidModel<STATE> baseModel, SubmitNodeCollector nodeCollector, PoseStack poseStack, int lightCoords,
          boolean renderFoil, int nextOrder, Color color, STATE state, Map<ModelPos, List<BlockStateModelPart>> quadMap, boolean transparent) {
        if (!quadMap.isEmpty()) {
            RenderType renderType = transparent ? RenderTypes.entityTranslucent(TextureAtlas.LOCATION_ITEMS) : MekanismRenderType.MEKASUIT;
            int[] tint = {color.argb()};
            for (Map.Entry<ModelPos, List<BlockStateModelPart>> entry : quadMap.entrySet()) {
                ModelPos modelPos = entry.getKey();
                poseStack.pushPose();
                modelPos.translate(baseModel, poseStack, state);
                modelPos.translateModel(poseStack);
                nodeCollector.order(nextOrder++)
                      .submitBlockModel(poseStack, renderType, entry.getValue(), tint, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                if (renderFoil) {
                    nodeCollector.order(nextOrder++)
                          .submitBlockModel(poseStack, MekanismRenderType.ARMOR_GLINT, entry.getValue(), tint, lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
                }
                poseStack.popPose();
            }
        }
        return nextOrder;
    }

    @Override
    public ICustomArmor gearModel() {
        return this;
    }

    public enum ModelPos {
        HEAD(BASE_TRANSLATION, s -> s.contains("head")),
        BODY(BASE_TRANSLATION, s -> s.contains("body")),
        LEFT_ARM(BASE_TRANSLATION.add(-0.3125F, -0.125F, 0, new Vector3f()), s -> s.contains("left_arm")),
        RIGHT_ARM(BASE_TRANSLATION.add(0.3125F, -0.125F, 0, new Vector3f()), s -> s.contains("right_arm")),
        LEFT_LEG(BASE_TRANSLATION.add(-0.125F, -0.75F, 0, new Vector3f()), s -> s.contains("left_leg")),
        RIGHT_LEG(BASE_TRANSLATION.add(0.125F, -0.75F, 0, new Vector3f()), s -> s.contains("right_leg")),
        LEFT_WING(BASE_TRANSLATION, s -> s.contains("left_wing")),
        RIGHT_WING(BASE_TRANSLATION, s -> s.contains("right_wing"));

        private static final float EXPANDED_WING_X = 1;
        private static final float EXPANDED_WING_Y = -2.5F;
        private static final float EXPANDED_WING_Z = 5;
        private static final float EXPANDED_WING_Y_ROT = 45;
        private static final float EXPANDED_WING_Z_ROT = 25;
        public static final ModelPos[] VALUES = values();

        private final Vector3fc transform;
        private final Predicate<String> modelSpec;

        ModelPos(Vector3fc transform, Predicate<String> modelSpec) {
            this.transform = transform;
            this.modelSpec = modelSpec;
        }

        public boolean contains(String s) {
            return modelSpec.test(s);
        }

        @Nullable
        public static ModelPos get(String name) {
            name = name.toLowerCase(Locale.ROOT);
            for (ModelPos pos : VALUES) {
                if (pos.contains(name)) {
                    return pos;
                }
            }
            return null;
        }

        public void translateModel(PoseStack poseStack) {
            poseStack.translate(transform.x(), transform.y(), transform.z());
        }

        public <STATE extends HumanoidRenderState> void translate(HumanoidModel<STATE> baseModel, PoseStack poseStack, STATE state) {
            switch (this) {
                case HEAD -> baseModel.head.translateAndRotate(poseStack);
                case BODY -> baseModel.body.translateAndRotate(poseStack);
                case LEFT_ARM -> baseModel.leftArm.translateAndRotate(poseStack);
                case RIGHT_ARM -> baseModel.rightArm.translateAndRotate(poseStack);
                case LEFT_LEG -> baseModel.leftLeg.translateAndRotate(poseStack);
                case RIGHT_LEG -> baseModel.rightLeg.translateAndRotate(poseStack);
                case LEFT_WING, RIGHT_WING -> translateWings(baseModel, poseStack, state);
            }
        }

        private <STATE extends HumanoidRenderState> void translateWings(HumanoidModel<STATE> baseModel, PoseStack poseStack, STATE state) {
            baseModel.body.translateAndRotate(poseStack);
            float x = 0;
            float y = 0;
            float z = 0;
            float yRot = 0;
            float zRot = 0;
            //Note: In theory the entity is always "fall flying" for wing rendering given our conditions
            // for it rendering, but we validate it just in case.
            //If the entity is not dive-bombing the ground (at which point the wings will be folded)
            if (state.isFallFlying && state.xRot < 45) {
                float scale = 0;
                // then we check if the entity is not pointing steeply into the sky
                double deltaY = state.getRenderDataOrDefault(DELTA_Y_CONTEXT, 0D);
                // if it isn't or if the entity has a lot of movement
                if (state.xRot > -45 || deltaY > 1) {
                    // then we fully expand the wings
                    scale = 1;
                } else if (deltaY > 0) {
                    // otherwise, if the entity is pointing steeply into the sky, and we have a small amount
                    // of movement (y movement between zero and one) then we partially expand the wings
                    scale = (float) deltaY;
                }
                // if we don't have any upwards momentum, and we are pointing steeply into the sky then we just fold the wings
                x = EXPANDED_WING_X * scale;
                y = EXPANDED_WING_Y * scale;
                z = EXPANDED_WING_Z * scale;
                yRot = EXPANDED_WING_Y_ROT * scale;
                zRot = EXPANDED_WING_Z_ROT * scale;
            }
            //TODO - 26.2: I think we should actually be updating the rotations in entity.elytraAnimationState rather than in the state? Maybe we can do it in the update render state method?
            // Also is there a reason to only be doing this for players?
            if (state instanceof AvatarRenderState playerState) {
                //If the entity is a player, then transition the wings gradually to their target position
                ElytraAnimationState elytraAnimationState;
                //TODO - 26.2: What is the difference between playerState.flyingYRot and state.elytraRotY?
                state.elytraRotX = 0;
                state.elytraRotY = state.elytraRotY + (yRot - state.elytraRotY) * 0.01F;
                //Base off of target values
                float scale = state.elytraRotY / EXPANDED_WING_Y_ROT;
                state.elytraRotZ = EXPANDED_WING_Z_ROT * scale;
                x = EXPANDED_WING_X * scale;
                y = EXPANDED_WING_Y * scale;
                z = EXPANDED_WING_Z * scale;
                yRot = state.elytraRotY;
                zRot = state.elytraRotZ;
            }
            if (this == RIGHT_WING) {
                //Invert things that need to be inverted for the right wing to mirror it properly
                x = -x;
                yRot = -yRot;
                zRot = -zRot;
            }
            poseStack.translate(x / 16, y / 16, z / 16);
            if (yRot != 0.0F) {
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            }
            if (zRot != 0.0F) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
            }

        }
    }

    private record OverrideData(OBJModelData modelData, String name) {
    }

    private ArmorQuads createQuads(Object2BooleanMap<ModuleModelSpec<?>> modules, Set<EquipmentSlot> wornParts, boolean hasMekaToolLeft, boolean hasMekaToolRight) {
        Map<OBJModelData, Map<ModelPos, Set<String>>> specialQuadsToRender = new Object2ObjectOpenHashMap<>();
        // map of normal model part name to overwritten model part name (i.e. helmet_head_center1 -> override_solar_helmet_helmet_head_center1)
        Map<String, OverrideData> overrides = new Object2ObjectOpenHashMap<>();
        Set<String> ignored = new HashSet<>();

        if (!modules.isEmpty()) {
            Map<OBJModelData, Set<String>> allMatchedParts = new Object2ObjectOpenHashMap<>();
            for (ModuleOBJModelData modelData : MekanismModelCache.INSTANCE.MEKASUIT_MODULES) {
                Set<String> matchedParts = allMatchedParts.computeIfAbsent(modelData, _ -> new HashSet<>());
                for (ObjectIterator<Object2BooleanMap.Entry<ModuleModelSpec<?>>> iterator = Object2BooleanMaps.fastIterator(modules); iterator.hasNext(); ) {
                    Object2BooleanMap.Entry<ModuleModelSpec<?>> entry = iterator.next();
                    ModuleModelSpec<?> spec = entry.getKey();
                    for (String name : modelData.getPartsForSpec(spec, entry.getBooleanValue())) {
                        if (name.contains(OVERRIDDEN_TAG)) {
                            overrides.put(spec.processOverrideName(name), new OverrideData(modelData, name));
                        }
                        // if this armor unit controls rendering of this module
                        if (type == spec.slotType) {
                            // then add the part as one we will need to add to render, this way we can ensure
                            // we respect any overrides that might be in a later model part
                            matchedParts.add(name);
                        }
                    }
                }
            }
            for (Map.Entry<OBJModelData, Set<String>> entry : allMatchedParts.entrySet()) {
                Set<String> matchedParts = entry.getValue();
                if (!matchedParts.isEmpty()) {
                    OBJModelData modelData = entry.getKey();
                    Map<ModelPos, Set<String>> quadsToRender = specialQuadsToRender.computeIfAbsent(modelData, _ -> new EnumMap<>(ModelPos.class));
                    //For all the parts we matched, go through and try adding them, while respecting any overrides we might have
                    for (String name : matchedParts) {
                        ModelPos pos = ModelPos.get(name);
                        if (pos == null) {
                            Mekanism.logger.warn("MekaSuit part '{}' is invalid from modules model. Ignoring.", name);
                        } else {
                            //Note: Currently the special quads here for overrides will likely point to our module quads to render
                            // but for consistency and future proofing it is better to make sure we look it up in case overrides gets other stuff
                            // added to it at some point
                            addQuadsToRender(pos, name, overrides, quadsToRender, specialQuadsToRender);
                        }
                    }
                }
            }
        }

        // handle mekatool overrides
        if (type == EquipmentSlot.CHEST) {
            if (hasMekaToolLeft) {
                ignored.addAll(MekanismModelCache.INSTANCE.getOverrideMekaToolParts(true));
            }
            if (hasMekaToolRight) {
                ignored.addAll(MekanismModelCache.INSTANCE.getOverrideMekaToolParts(false));
            }
        }

        Map<ModelPos, Set<String>> armorQuadsToRender = new EnumMap<>(ModelPos.class);
        for (String name : MekanismModelCache.INSTANCE.MEKASUIT.getPartNames()) {
            if (!checkEquipment(type, name)) {
                // skip if it's the wrong equipment type
                continue;
            } else if (name.startsWith(EXCLUSIVE_TAG)) {
                if (wornParts.contains(adjacentType)) {
                    // skip if the part is exclusive and the adjacent part is present
                    continue;
                }
            } else if (name.startsWith(SHARED_TAG) && wornParts.contains(adjacentType) && adjacentType.ordinal() > type.ordinal()) {
                // skip if the part is shared and the shared part already rendered
                continue;
            }
            ModelPos pos = ModelPos.get(name);
            if (pos == null) {
                Mekanism.logger.warn("MekaSuit part '{}' is invalid. Ignoring.", name);
            } else if (!ignored.contains(name)) {
                addQuadsToRender(pos, name, overrides, armorQuadsToRender, specialQuadsToRender);
            }
        }

        Map<ModelPos, List<BlockStateModelPart>> opaqueMap = new EnumMap<>(ModelPos.class);
        Map<ModelPos, List<BlockStateModelPart>> transparentMap = new EnumMap<>(ModelPos.class);
        for (ModelPos pos : ModelPos.VALUES) {
            for (OBJModelData modelData : MekanismModelCache.INSTANCE.MEKASUIT_MODULES) {
                parseTransparency(modelData, pos, opaqueMap, transparentMap, specialQuadsToRender.getOrDefault(modelData, Collections.emptyMap()));
            }
            parseTransparency(MekanismModelCache.INSTANCE.MEKASUIT, pos, opaqueMap, transparentMap, armorQuadsToRender);
        }
        return new ArmorQuads(opaqueMap, transparentMap);
    }

    private static void addQuadsToRender(ModelPos pos, String name, Map<String, OverrideData> overrides, Map<MekaSuitArmor.ModelPos, Set<String>> quadsToRender,
          Map<OBJModelData, Map<ModelPos, Set<String>>> specialQuadsToRender) {
        OverrideData override = overrides.get(name);
        if (override != null) {
            //Update the name and the target quads if there is an override
            name = override.name();
            // Note: In theory the special quads should have our model data corresponding
            // to a map already, but on the off chance they don't compute and add it
            OBJModelData overrideData = override.modelData();
            quadsToRender = specialQuadsToRender.computeIfAbsent(overrideData, _ -> new EnumMap<>(ModelPos.class));
        }
        quadsToRender.computeIfAbsent(pos, _ -> new HashSet<>()).add(name);
    }

    private static void parseTransparency(OBJModelData modelData, ModelPos pos, Map<ModelPos, List<BlockStateModelPart>> opaqueMap, Map<ModelPos, List<BlockStateModelPart>> transparentMap,
          Map<ModelPos, Set<String>> regularQuads) {
        Set<String> opaqueRegularQuads = new HashSet<>();
        Set<String> transparentRegularQuads = new HashSet<>();
        parseTransparency(pos, opaqueRegularQuads, transparentRegularQuads, regularQuads);
        addParsedQuads(modelData, pos, opaqueMap, opaqueRegularQuads);
        addParsedQuads(modelData, pos, transparentMap, transparentRegularQuads);
    }

    private static void addParsedQuads(OBJModelData modelData, ModelPos pos, Map<ModelPos, List<BlockStateModelPart>> map, Set<String> quads) {
        //Only add a new entry to our map if we will have any parts. Our getParts method will return empty if there are no quads
        List<BlockStateModelPart> allParts = modelData.getParts(quads);
        if (!allParts.isEmpty()) {
            map.computeIfAbsent(pos, _ -> new ArrayList<>()).addAll(allParts);
        }
    }

    private static void parseTransparency(ModelPos pos, Set<String> opaqueQuads, Set<String> transparentQuads, Map<ModelPos, Set<String>> quads) {
        for (String quad : quads.getOrDefault(pos, Collections.emptySet())) {
            if (quad.contains(GLASS_TAG)) {
                transparentQuads.add(quad);
            } else {
                opaqueQuads.add(quad);
            }
        }
    }

    private static boolean checkEquipment(EquipmentSlot type, String text) {
        return switch (type) {
            case HEAD -> text.contains("helmet");
            case CHEST -> text.contains("chest");
            case LEGS -> text.contains("leggings");
            case FEET -> text.contains("boots");
            default -> false;
        };
    }

    private record ArmorQuads(Map<ModelPos, List<BlockStateModelPart>> opaqueParts, Map<ModelPos, List<BlockStateModelPart>> transparentParts) {

        private ArmorQuads {
            if (opaqueParts.isEmpty()) {
                opaqueParts = Collections.emptyMap();
            }
            if (transparentParts.isEmpty()) {
                transparentParts = Collections.emptyMap();
            }
        }
    }

    private record ModuleModelSpec<AVATAR extends Avatar & ClientAvatarEntity>(ModuleData<?> module, EquipmentSlot slotType, String name, Predicate<Either<HumanoidRenderState, AVATAR>> isActive) {

        /// Score closest to zero is considered best, negative one for no match at all.
        public int score(String name) {
            return name.indexOf(this.name + "_");
        }

        public boolean isActive(Either<HumanoidRenderState, AVATAR> state) {
            return isActive.test(state);
        }

        public String processOverrideName(String part) {
            return MekaSuitArmor.processOverrideName(part, name);
        }
    }

    public static String processOverrideName(String part, String name) {
        return part.replaceFirst(OVERRIDDEN_TAG, "").replaceFirst(name + "_", "");
    }

    /// Call via [mekanism.api.gear.IClientModuleHelper#addMekaSuitModuleModelSpec(String, Holder, EquipmentSlot, Predicate)].
    @Internal
    public static <AVATAR extends Avatar & ClientAvatarEntity> void registerModule(String name, Holder<ModuleData<?>> moduleData, EquipmentSlot slotType, Predicate<Either<HumanoidRenderState, AVATAR>> isActive) {
        moduleModelSpec.put(slotType, moduleData, new ModuleModelSpec<>(moduleData.value(), slotType, name, isActive));
    }

    private static ItemStack getItemBySlot(HumanoidRenderState state, EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> state.mainArm == HumanoidArm.RIGHT ? state.rightHandItemStack : state.leftHandItemStack;
            case OFFHAND -> state.mainArm == HumanoidArm.RIGHT ? state.leftHandItemStack : state.rightHandItemStack;
            case FEET -> state.feetEquipment;
            case LEGS -> state.legsEquipment;
            case CHEST -> state.chestEquipment;
            case HEAD -> state.headEquipment;
            default -> ItemStack.EMPTY;
        };
    }

    public <STATE, AVATAR extends Avatar & ClientAvatarEntity> QuickHash key(Either<HumanoidRenderState, AVATAR> either, HumanoidArm mainArm, STATE state, BiFunction<STATE, EquipmentSlot, ItemStack> itemBySlot) {
        Object2BooleanMap<ModuleModelSpec<AVATAR>> modules = new Object2BooleanOpenHashMap<>();
        Set<EquipmentSlot> wornParts = EnumSet.noneOf(EquipmentSlot.class);
        for (EquipmentSlot slotType : EquipmentSlotGroup.ARMOR) {
            ItemStack stack = itemBySlot.apply(state, slotType);
            if (stack.getItem() instanceof ItemMekaSuitArmor) {
                IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
                if (container != null) {
                    wornParts.add(slotType);
                    for (Entry<Holder<ModuleData<?>>, ModuleModelSpec<?>> entry : moduleModelSpec.row(slotType).entrySet()) {
                        if (container.hasEnabled(entry.getKey())) {
                            ModuleModelSpec<AVATAR> spec = (ModuleModelSpec<AVATAR>) entry.getValue();
                            modules.put(spec, spec.isActive(either));
                        }
                    }
                }
            }
        }
        return new QuickHash(modules.isEmpty() ? Object2BooleanMaps.emptyMap() : modules, wornParts.isEmpty() ? Collections.emptySet() : wornParts,
              itemBySlot.apply(state, mainArm == HumanoidArm.LEFT ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND).getItem() instanceof ItemMekaTool,
              itemBySlot.apply(state, mainArm == HumanoidArm.RIGHT ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND).getItem() instanceof ItemMekaTool);
    }

    public static class ModuleOBJModelData extends OBJModelData {

        private record SpecData(Set<String> active, Set<String> inactive) {
        }

        private final Map<ModuleModelSpec<?>, SpecData> specParts = new Object2ObjectOpenHashMap<>();

        public ModuleOBJModelData(Identifier rl) {
            super(rl);
        }

        private Set<String> getPartsForSpec(ModuleModelSpec<?> spec, boolean active) {
            SpecData specData = specParts.get(spec);
            if (specData == null) {
                return Collections.emptySet();
            }
            return active ? specData.active() : specData.inactive();
        }

        @Override
        protected void reload(BakingCompleted evt) {
            super.reload(evt);
            Collection<ModuleModelSpec<?>> modules = moduleModelSpec.values();
            for (String name : getPartNames()) {
                //Find the "best" spec by checking all the specs and finding out which one is listed first
                // this way if we are overriding another module, then we just put the module that is overriding
                // the other one first in the name so that it gets the spec matched to it
                ModuleModelSpec<?> matchingSpec = null;
                int bestScore = -1;
                for (ModuleModelSpec<?> spec : modules) {
                    int score = spec.score(name);
                    if (score != -1 && (bestScore == -1 || score < bestScore)) {
                        bestScore = score;
                        matchingSpec = spec;
                    }
                }
                if (matchingSpec != null) {
                    SpecData specData = specParts.computeIfAbsent(matchingSpec, _ -> new SpecData(new HashSet<>(), new HashSet<>()));
                    if (name.contains(INACTIVE_TAG + matchingSpec.name + "_")) {
                        specData.inactive().add(name);
                    } else {
                        specData.active().add(name);
                    }
                }
            }
            //Update entries to reclaim some memory for empty sets
            for (Map.Entry<ModuleModelSpec<?>, SpecData> entry : specParts.entrySet()) {
                SpecData specData = entry.getValue();
                if (specData.active().isEmpty()) {
                    entry.setValue(new SpecData(Collections.emptySet(), specData.inactive()));
                } else if (specData.inactive().isEmpty()) {
                    entry.setValue(new SpecData(specData.active(), Collections.emptySet()));
                }
            }
        }
    }
}