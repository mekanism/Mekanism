package calclavia.lib.render;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Rseifert & Calclavia
 */
@SideOnly(Side.CLIENT)
public abstract class RenderTaggedTile extends TileEntitySpecialRenderer
{
	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		if (t != null)
		{
			if (t instanceof ITagRender && this.getPlayer().getDistance(t.xCoord, t.yCoord, t.zCoord) <= RendererLivingEntity.NAME_TAG_RANGE)
			{
				HashMap<String, Integer> tags = new HashMap<String, Integer>();
				float height = ((ITagRender) t).addInformation(tags, this.getPlayer());

				EntityPlayer player = Minecraft.getMinecraft().thePlayer;

				if (player.ridingEntity == null)
				{
					MovingObjectPosition objectPosition = player.rayTrace(8, 1);

					if (objectPosition != null)
					{
						boolean isLooking = false;

						for (int h = 0; h < height; h++)
						{
							if (objectPosition.blockX == t.xCoord && objectPosition.blockY == t.yCoord + h && objectPosition.blockZ == t.zCoord)
							{
								isLooking = true;
							}
						}

						if (isLooking)
						{
							Iterator<Entry<String, Integer>> it = tags.entrySet().iterator();
							int i = 0;

							while (it.hasNext())
							{
								Entry<String, Integer> entry = it.next();

								if (entry.getKey() != null)
								{
									CalclaviaRenderHelper.renderFloatingText(entry.getKey(), (float) x + 0.5f, ((float) y + (i * 0.25f)) - 2f + height, (float) z + 0.5f, entry.getValue());
								}

								i++;
							}
						}
					}
				}

			}
		}
	}

	/**
	 * gets the player linked with the renderer
	 */
	public EntityPlayer getPlayer()
	{
		EntityLivingBase entity = this.tileEntityRenderer.entityLivingPlayer;

		if (entity instanceof EntityPlayer)
		{
			return (EntityPlayer) entity;
		}

		return null;
	}
}
