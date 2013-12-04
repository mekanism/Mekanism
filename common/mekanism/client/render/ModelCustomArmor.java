package mekanism.client.render;

import mekanism.client.model.ModelJetpack;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

public class ModelCustomArmor extends ModelBiped
{
	public static ModelCustomArmor INSTANCE = new ModelCustomArmor();

	public ArmorModel modelType;

	public void init(Entity entity, float f, float f1, float f2, float f3, float f4, float size)
	{
		resetPart(bipedHead, 0, 0, 0);
		resetPart(bipedBody, 0, 0, 0);
		resetPart(bipedRightArm, 5, 2, 0);
		resetPart(bipedLeftArm, -5, 2, 0);
		resetPart(bipedRightLeg, 2, 12, 0);
		resetPart(bipedLeftLeg, -2, 12, 0);

		bipedHeadwear.cubeList.clear();
		bipedEars.cubeList.clear();
		bipedCloak.cubeList.clear();

		bipedHead.isHidden = false;
		bipedBody.isHidden = false;
		bipedRightArm.isHidden = false;
		bipedLeftArm.isHidden = false;
		bipedRightLeg.isHidden = false;
		bipedLeftLeg.isHidden = false;

		bipedHead.showModel = true;
		bipedBody.showModel = true;
		bipedRightArm.showModel = true;
		bipedLeftArm.showModel = true;
		bipedRightLeg.showModel = true;
		bipedLeftLeg.showModel = true;

		setRotationAngles(f, f1, f2, f3, f4, size, entity);
	}

	public void resetPart(ModelRenderer renderer, float x, float y, float z)
	{
		renderer.cubeList.clear();
		ModelCustom model = new ModelCustom(this, renderer);
		renderer.addChild(model);
		setOffset(renderer, x, y, z);
	}

	public void setOffset(ModelRenderer renderer, float x, float y, float z)
	{
		renderer.offsetX = x;
		renderer.offsetY = y;
		renderer.offsetZ = z;
	}

	public class ModelCustom extends ModelRenderer
	{
		public ModelCustom(ModelCustomArmor base, ModelRenderer renderer)
		{
			super(base);
		}

		@Override
		public void render(float size)
		{
			GL11.glScalef(size, size, size);

			if (ModelCustomArmor.this.modelType == ArmorModel.JETPACK)
			{
				System.out.println("yes");
			}
		}
	}

	public static enum ArmorModel 
	{
		JETPACK(1);

		public int armorSlot;
		public static ModelJetpack jetpackModel = new ModelJetpack();

		private ArmorModel(int i)
		{
			armorSlot = i;
		}
	}
}
