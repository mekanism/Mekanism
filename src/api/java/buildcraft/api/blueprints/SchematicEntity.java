/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.common.util.Constants;

public class SchematicEntity extends Schematic {
    public Class<? extends Entity> entity;

    /** This tree contains additional data to be stored in the blueprint. By default, it will be initialized from
     * Schematic.readFromWord with the standard readNBT function of the corresponding tile (if any) and will be loaded
     * from BptBlock.writeToWorld using the standard writeNBT function. */
    public NBTTagCompound entityNBT = new NBTTagCompound();

    /** This field contains requirements for a given block when stored in the blueprint. Modders can either rely on this
     * list or compute their own int Schematic. */
    public ItemStack[] storedRequirements = new ItemStack[0];
    public BuildingPermission defaultPermission = BuildingPermission.ALL;

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, List<ItemStack> requirements) {
        Collections.addAll(requirements, storedRequirements);
    }

    public void writeToWorld(IBuilderContext context) {
        Entity e = EntityList.createEntityFromNBT(entityNBT, context.world());
        context.world().spawnEntityInWorld(e);
    }

    public void readFromWorld(IBuilderContext context, Entity entity) {
        entity.writeToNBTOptional(entityNBT);
    }

    @Override
    public void translateToBlueprint(Vec3d transform) {
        NBTTagList nbttaglist = entityNBT.getTagList("Pos", 6);
        Vec3d pos = new Vec3d(nbttaglist.getDoubleAt(0), nbttaglist.getDoubleAt(1), nbttaglist.getDoubleAt(2));
        pos = pos.add(transform);
        entityNBT.setTag("Pos", this.newDoubleNBTList(pos.xCoord, pos.yCoord, pos.zCoord));
    }

    @Override
    public void translateToWorld(Vec3d transform) {
        NBTTagList nbttaglist = entityNBT.getTagList("Pos", 6);
        Vec3d pos = new Vec3d(nbttaglist.getDoubleAt(0), nbttaglist.getDoubleAt(1), nbttaglist.getDoubleAt(2));
        pos = pos.add(transform);

        entityNBT.setTag("Pos", this.newDoubleNBTList(pos.xCoord, pos.yCoord, pos.zCoord));
    }

    @Override
    public void idsToBlueprint(MappingRegistry registry) {}

    @Override
    public void idsToWorld(MappingRegistry registry) {
        try {
            registry.scanAndTranslateStacksToWorld(entityNBT);
        } catch (MappingNotFoundException e) {
            entityNBT = new NBTTagCompound();
        }
    }

    @Override
    public void rotateLeft(IBuilderContext context) {
        NBTTagList nbttaglist = entityNBT.getTagList("Pos", 6);
        Vec3d pos = new Vec3d(nbttaglist.getDoubleAt(0), nbttaglist.getDoubleAt(1), nbttaglist.getDoubleAt(2));
        pos = context.rotatePositionLeft(pos);
        entityNBT.setTag("Pos", this.newDoubleNBTList(pos.xCoord, pos.yCoord, pos.zCoord));

        nbttaglist = entityNBT.getTagList("Rotation", 5);
        float yaw = nbttaglist.getFloatAt(0);
        yaw += 90;
        entityNBT.setTag("Rotation", this.newFloatNBTList(yaw, nbttaglist.getFloatAt(1)));
    }

    @Override
    public void writeSchematicToNBT(NBTTagCompound nbt, MappingRegistry registry) {
        super.writeSchematicToNBT(nbt, registry);

        nbt.setInteger("entityId", registry.getIdForEntity(entity));
        nbt.setTag("entity", entityNBT);

        NBTTagList rq = new NBTTagList();

        for (ItemStack stack : storedRequirements) {
            NBTTagCompound sub = new NBTTagCompound();
            stack.writeToNBT(stack.writeToNBT(sub));
            sub.setInteger("id", registry.getIdForItem(stack.getItem()));
            rq.appendTag(sub);
        }

        nbt.setTag("rq", rq);
    }

    @Override
    public void readSchematicFromNBT(NBTTagCompound nbt, MappingRegistry registry) {
        super.readSchematicFromNBT(nbt, registry);

        entityNBT = nbt.getCompoundTag("entity");

        NBTTagList rq = nbt.getTagList("rq", Constants.NBT.TAG_COMPOUND);

        ArrayList<ItemStack> rqs = new ArrayList<ItemStack>();

        for (int i = 0; i < rq.tagCount(); ++i) {
            try {
                NBTTagCompound sub = rq.getCompoundTagAt(i);

                if (sub.getInteger("id") >= 0) {
                    // Maps the id in the blueprint to the id in the world
                    sub.setInteger("id", Item.REGISTRY.getIDForObject(registry.getItemForId(sub.getInteger("id"))));

                    rqs.add(ItemStack.loadItemStackFromNBT(sub));
                } else {
                    defaultPermission = BuildingPermission.CREATIVE_ONLY;
                }
            } catch (Throwable t) {
                t.printStackTrace();
                defaultPermission = BuildingPermission.CREATIVE_ONLY;
            }
        }

        storedRequirements = rqs.toArray(new ItemStack[rqs.size()]);
    }

    protected NBTTagList newDoubleNBTList(double... par1ArrayOfDouble) {
        NBTTagList nbttaglist = new NBTTagList();
        double[] adouble = par1ArrayOfDouble;
        int i = par1ArrayOfDouble.length;

        for (int j = 0; j < i; ++j) {
            double d1 = adouble[j];
            nbttaglist.appendTag(new NBTTagDouble(d1));
        }

        return nbttaglist;
    }

    protected NBTTagList newFloatNBTList(float... par1ArrayOfFloat) {
        NBTTagList nbttaglist = new NBTTagList();
        float[] afloat = par1ArrayOfFloat;
        int i = par1ArrayOfFloat.length;

        for (int j = 0; j < i; ++j) {
            float f1 = afloat[j];
            nbttaglist.appendTag(new NBTTagFloat(f1));
        }

        return nbttaglist;
    }

    public boolean isAlreadyBuilt(IBuilderContext context) {
        NBTTagList nbttaglist = entityNBT.getTagList("Pos", 6);
        Vec3d newPosition = new Vec3d(nbttaglist.getDoubleAt(0), nbttaglist.getDoubleAt(1), nbttaglist.getDoubleAt(2));

        for (Object o : context.world().loadedEntityList) {
            Entity e = (Entity) o;

            Vec3d existingPositon = new Vec3d(e.posX, e.posY, e.posZ);

            if (existingPositon.distanceTo(newPosition) <= 0.1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int buildTime() {
        return 5;
    }

    @Override
    public BuildingPermission getBuildingPermission() {
        return defaultPermission;
    }
}
