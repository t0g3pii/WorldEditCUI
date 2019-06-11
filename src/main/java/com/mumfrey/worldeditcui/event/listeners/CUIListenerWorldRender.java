package com.mumfrey.worldeditcui.event.listeners;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mumfrey.worldeditcui.WorldEditCUI;
import com.mumfrey.worldeditcui.util.Vector3;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

/**
 * Listener for WorldRenderEvent
 * 
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class CUIListenerWorldRender
{
	private WorldEditCUI controller;
	
	private MinecraftClient minecraft;
	
	public CUIListenerWorldRender(WorldEditCUI controller, MinecraftClient minecraft)
	{
		this.controller = controller;
		this.minecraft = minecraft;
	}
	
	public void onRender(float partialTicks)
	{
		try
		{
			GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240F, 240F);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableBlend();
			GlStateManager.enableAlphaTest();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
			GlStateManager.disableTexture();
			GlStateManager.enableDepthTest();
			GlStateManager.depthMask(false);
			GlStateManager.pushMatrix();
			GlStateManager.disableFog();
			
			try
			{
				Entity player = this.minecraft.player;
				Vector3 cameraPos = new Vector3(
						player.prevX + ((player.x - player.prevX) * partialTicks),
						player.prevY + ((player.y - player.prevY) * partialTicks) + player.getEyeHeight(player.getPose()),
						player.prevZ + ((player.z - player.prevZ) * partialTicks)
				);
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.5F);
				this.controller.renderSelections(cameraPos, partialTicks);
			}
			catch (Exception e)
			{
			}

			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.popMatrix();

			GlStateManager.depthMask(true);
			GlStateManager.enableTexture();
			GlStateManager.disableBlend();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		}
		catch (Exception ex) {}
	}
}
