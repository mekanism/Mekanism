package mekanism.generators.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelGasGenerator extends ModelBase
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
	ModelRenderer P;
	ModelRenderer Q;
	ModelRenderer R;
	ModelRenderer S;

	public ModelGasGenerator()
	{
		textureWidth = 128;
		textureHeight = 128;

		A = new ModelRenderer(this, 0, 0);
		A.addBox(0F, 0F, 0F, 16, 1, 16);
		A.setRotationPoint(-8F, 23F, -8F);
		A.setTextureSize(128, 128);
		A.mirror = true;
		setRotation(A, 0F, 0F, 0F);
		B = new ModelRenderer(this, 0, 45);
		B.addBox(0F, 0F, 0F, 16, 12, 6);
		B.setRotationPoint(-8F, 11F, -3F);
		B.setTextureSize(128, 128);
		B.mirror = true;
		setRotation(B, 0F, 0F, 0F);
		C = new ModelRenderer(this, 0, 64);
		C.addBox(-3F, 0F, -8F, 6, 12, 12);
		C.setRotationPoint(0F, 11F, 0F);
		C.setTextureSize(128, 128);
		C.mirror = true;
		setRotation(C, 0F, 0F, 0F);
		D = new ModelRenderer(this, 66, 0);
		D.addBox(-3F, 0F, 0F, 6, 10, 4);
		D.setRotationPoint(0F, 13F, 4F);
		D.setTextureSize(128, 128);
		D.mirror = true;
		setRotation(D, 0F, 0F, 0F);
		E = new ModelRenderer(this, 0, 20);
		E.addBox(-5F, 0F, -3F, 10, 13, 6);
		E.setRotationPoint(0F, 10F, 0F);
		E.setTextureSize(128, 128);
		E.mirror = true;
		setRotation(E, 0F, 0.5235988F, 0F);
		F = new ModelRenderer(this, 0, 20);
		F.addBox(-5F, 0F, -3F, 10, 13, 6);
		F.setRotationPoint(0F, 10F, 0F);
		F.setTextureSize(128, 128);
		F.mirror = true;
		setRotation(F, 0F, -0.5235988F, 0F);
		G = new ModelRenderer(this, 34, 20);
		G.addBox(-3F, 0F, -5F, 6, 13, 10);
		G.setRotationPoint(0F, 10F, 0F);
		G.setTextureSize(128, 128);
		G.mirror = true;
		setRotation(G, 0F, 0F, 0F);
		H = new ModelRenderer(this, 67, 20);
		H.addBox(-2F, 0F, -1F, 4, 2, 2);
		H.setRotationPoint(0F, 8F, 0F);
		H.setTextureSize(128, 128);
		H.mirror = true;
		setRotation(H, 0F, -0.5235988F, 0F);
		I = new ModelRenderer(this, 67, 20);
		I.addBox(-2F, 0F, -1F, 4, 2, 2);
		I.setRotationPoint(0F, 8F, 0F);
		I.setTextureSize(128, 128);
		I.mirror = true;
		setRotation(I, 0F, 0.5235988F, 0F);
		J = new ModelRenderer(this, 67, 26);
		J.addBox(-1F, 0F, -2F, 2, 2, 4);
		J.setRotationPoint(0F, 8F, 0F);
		J.setTextureSize(128, 128);
		J.mirror = true;
		setRotation(J, 0F, 0F, 0F);
		K = new ModelRenderer(this, 88, 0);
		K.addBox(-3F, 0F, 0F, 6, 2, 5);
		K.setRotationPoint(0F, 10F, 4F);
		K.setTextureSize(128, 128);
		K.mirror = true;
		setRotation(K, -0.6457718F, 0F, 0F);
		L = new ModelRenderer(this, 0, 97);
		L.addBox(0F, 0F, 0F, 4, 10, 4);
		L.setRotationPoint(2F, 13F, -5F);
		L.setTextureSize(128, 128);
		L.mirror = true;
		setRotation(L, 0F, 0.7853982F, 0F);
		M = new ModelRenderer(this, 0, 90);
		M.addBox(0F, 0F, 0F, 3, 1, 2);
		M.setRotationPoint(2F, 12F, -4F);
		M.setTextureSize(128, 128);
		M.mirror = true;
		setRotation(M, 0F, 0.7853982F, 0F);
		N = new ModelRenderer(this, 0, 97);
		N.addBox(0F, 0F, 0F, 4, 10, 4);
		N.setRotationPoint(-8F, 13F, -5F);
		N.setTextureSize(128, 128);
		N.mirror = true;
		setRotation(N, 0F, 0.7853982F, 0F);
		O = new ModelRenderer(this, 0, 90);
		O.addBox(0F, 0F, 0F, 2, 1, 3);
		O.setRotationPoint(-6F, 12F, -5F);
		O.setTextureSize(128, 128);
		O.mirror = true;
		setRotation(O, 0F, 0.7853982F, 0F);
		P = new ModelRenderer(this, 0, 118);
		P.addBox(-5F, 0F, 0F, 10, 1, 1);
		P.setRotationPoint(0F, 15F, -7F);
		P.setTextureSize(128, 128);
		P.mirror = true;
		setRotation(P, 0F, 0F, 0F);
		Q = new ModelRenderer(this, 0, 118);
		Q.addBox(-5F, 0F, 0F, 10, 1, 1);
		Q.setRotationPoint(0F, 19F, -7F);
		Q.setTextureSize(128, 128);
		Q.mirror = true;
		setRotation(Q, 0F, 0F, 0F);
		R = new ModelRenderer(this, 0, 112);
		R.addBox(-7F, 0F, -3F, 14, 1, 3);
		R.setRotationPoint(0F, 15F, -2F);
		R.setTextureSize(128, 128);
		R.mirror = true;
		setRotation(R, 0F, 0F, 0F);
		S = new ModelRenderer(this, 0, 112);
		S.addBox(-7F, 0F, -3F, 14, 1, 3);
		S.setRotationPoint(0F, 19F, -2F);
		S.setTextureSize(128, 128);
		S.mirror = true;
		setRotation(S, 0F, 0F, 0F);
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
		P.render(size);
		Q.render(size);
		R.render(size);
		S.render(size);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
