package mekanism.generators.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelHeatGenerator extends ModelBase
{
	ModelRenderer A;
	ModelRenderer B;
	ModelRenderer C;
	ModelRenderer D;
	ModelRenderer E;
	ModelRenderer F;
	ModelRenderer G;
	ModelRenderer H;
	ModelRenderer I;
	ModelRenderer J;
	ModelRenderer K;
	ModelRenderer L;
	ModelRenderer M;
	ModelRenderer N;
	ModelRenderer O;

	public ModelHeatGenerator()
	{
		textureWidth = 128;
		textureHeight = 128;

		A = new ModelRenderer(this, 0, 0);
		A.addBox(-8F, 0F, -8F, 16, 1, 16);
		A.setRotationPoint(0F, 23F, 0F);
		A.setTextureSize(128, 128);
		A.mirror = true;
		setRotation(A, 0F, 0F, 0F);
		B = new ModelRenderer(this, 0, 85);
		B.addBox(0F, 0F, 0F, 8, 6, 10);
		B.setRotationPoint(-8F, 17F, -5F);
		B.setTextureSize(128, 128);
		B.mirror = true;
		setRotation(B, 0F, 0F, 0F);
		C = new ModelRenderer(this, 0, 67);
		C.addBox(-1F, -2F, 0F, 2, 4, 12);
		C.setRotationPoint(-5F, 20F, -6F);
		C.setTextureSize(128, 128);
		C.mirror = true;
		setRotation(C, 0F, 0F, 1.570796F);
		D = new ModelRenderer(this, 0, 67);
		D.addBox(-1F, -2F, 0F, 2, 4, 12);
		D.setRotationPoint(-5F, 20F, -6F);
		D.setTextureSize(128, 128);
		D.mirror = true;
		setRotation(D, 0F, 0F, 0.5235988F);
		E = new ModelRenderer(this, 0, 67);
		E.addBox(-1F, -2F, 0F, 2, 4, 12);
		E.setRotationPoint(-5F, 20F, -6F);
		E.setTextureSize(128, 128);
		E.mirror = true;
		setRotation(E, 0F, 0F, -0.5235988F);
		F = new ModelRenderer(this, 68, 0);
		F.addBox(-2F, -4F, 0F, 4, 8, 10);
		F.setRotationPoint(-4F, 13F, -5F);
		F.setTextureSize(128, 128);
		F.mirror = true;
		setRotation(F, 0F, 0F, 0.5235988F);
		G = new ModelRenderer(this, 68, 0);
		G.addBox(-2F, -4F, 0F, 4, 8, 10);
		G.setRotationPoint(-4F, 13F, -5F);
		G.setTextureSize(128, 128);
		G.mirror = true;
		setRotation(G, 0F, 0F, -0.5235988F);
		H = new ModelRenderer(this, 68, 0);
		H.addBox(-2F, -4F, 0F, 4, 8, 10);
		H.setRotationPoint(-4F, 13F, -5F);
		H.setTextureSize(128, 128);
		H.mirror = true;
		setRotation(H, 0F, 0F, 1.570796F);
		I = new ModelRenderer(this, 0, 18);
		I.addBox(0F, 0F, 0F, 8, 13, 14);
		I.setRotationPoint(0F, 10F, -6F);
		I.setTextureSize(128, 128);
		I.mirror = true;
		setRotation(I, 0F, 0F, 0F);
		J = new ModelRenderer(this, 0, 47);
		J.addBox(0F, 0F, 0F, 6, 11, 1);
		J.setRotationPoint(1F, 11F, -7F);
		J.setTextureSize(128, 128);
		J.mirror = true;
		setRotation(J, 0F, 0F, 0F);
		K = new ModelRenderer(this, 51, 21);
		K.addBox(0F, 0F, 0F, 10, 4, 1);
		K.setRotationPoint(-8F, 13F, -4F);
		K.setTextureSize(128, 128);
		K.mirror = true;
		setRotation(K, 0F, 0F, 0F);
		L = new ModelRenderer(this, 51, 21);
		L.addBox(0F, 0F, 0F, 10, 4, 1);
		L.setRotationPoint(-8F, 13F, 3F);
		L.setTextureSize(128, 128);
		L.mirror = true;
		setRotation(L, 0F, 0F, 0F);
		M = new ModelRenderer(this, 0, 103);
		M.addBox(0F, 0F, 0F, 2, 4, 2);
		M.setRotationPoint(-2F, 19F, -7F);
		M.setTextureSize(128, 128);
		M.mirror = true;
		setRotation(M, 0F, 0F, 0F);
		N = new ModelRenderer(this, 51, 0);
		N.addBox(0F, 0F, 0F, 6, 1, 1);
		N.setRotationPoint(-3F, 9F, 0F);
		N.setTextureSize(128, 128);
		N.mirror = true;
		setRotation(N, 0F, 0F, 0F);
		O = new ModelRenderer(this, 51, 0);
		O.addBox(0F, 0F, 0F, 6, 1, 1);
		O.setRotationPoint(-3F, 9F, 2F);
		O.setTextureSize(128, 128);
		O.mirror = true;
		setRotation(O, 0F, 0F, 0F);
	}

	public void render(float size)
	{
		A.render(size);
		B.render(size);
		C.render(size);
		D.render(size);
		E.render(size);
		F.render(size);
		G.render(size);
		H.render(size);
		I.render(size);
		J.render(size);
		K.render(size);
		L.render(size);
		M.render(size);
		N.render(size);
		O.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
