package com.mumfrey.worldeditcui.event.listeners;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mumfrey.worldeditcui.WorldEditCUI;
import com.mumfrey.worldeditcui.util.Vector3;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
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
	private final WorldEditCUI controller;

	private final MinecraftClient minecraft;
	private final CUIRenderContext ctx = new CUIRenderContext();

	public CUIListenerWorldRender(WorldEditCUI controller, MinecraftClient minecraft)
	{
		this.controller = controller;
		this.minecraft = minecraft;
	}

	public void onRender(final MatrixStack matrices, float partialTicks)
	{
		try
		{
			this.ctx.init(new Vector3(this.minecraft.gameRenderer.getCamera().getPos()), matrices, partialTicks);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			// RenderSystem.enableAlphaTest();
			// RenderSystem.alphaFunc(GL11.GL_GREATER, 0.0F);
			RenderSystem.disableTexture();
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(false);
			final float oldFog = RenderSystem.getShaderFogStart();
			BackgroundRenderer.method_23792(); // disableFog
			RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);

			try
			{
				RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);
				this.controller.renderSelections(this.ctx);
			}
			catch (Exception e) {
				this.controller.getDebugger().error("Error while attempting to render WorldEdit CUI", e);
			}

			Tessellator.getInstance().getBuffer().unfixColor();
			RenderSystem.depthFunc(GL11.GL_LEQUAL);

			RenderSystem.setShaderFogStart(oldFog);
			RenderSystem.depthMask(true);
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
			// RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
		} catch (Exception ex)
		{
			this.controller.getDebugger().error("Failed while preparing state for WorldEdit CUI", ex);
		}
	}
}
