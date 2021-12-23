package com.mumfrey.worldeditcui.event.listeners;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mumfrey.worldeditcui.WorldEditCUI;
import com.mumfrey.worldeditcui.util.Vector3;
import eu.mikroskeem.worldeditcui.render.PipelineProvider;
import eu.mikroskeem.worldeditcui.render.RenderSink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import java.util.List;

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

	private final Minecraft minecraft;
	private final CUIRenderContext ctx = new CUIRenderContext();
	private final List<PipelineProvider> pipelines;
	private int currentPipelineIdx;
	private RenderSink sink;

	public CUIListenerWorldRender(WorldEditCUI controller, Minecraft minecraft, final List<PipelineProvider> pipelines)
	{
		this.controller = controller;
		this.minecraft = minecraft;
		this.pipelines = List.copyOf(pipelines);
	}

	private RenderSink providePipeline()
	{
		if (this.sink != null)
		{
			return this.sink;
		}

		for (int i = currentPipelineIdx; i < this.pipelines.size(); i++)
		{
			final PipelineProvider pipeline = this.pipelines.get(i);
			if (pipeline.available())
			{
				try
				{
					final RenderSink sink = pipeline.provide();
					this.currentPipelineIdx = i;
					return this.sink = sink;
				}
				catch (final Exception ex)
				{
					this.controller.getDebugger().info("Failed to render with pipeline " + pipeline.id() + ", which declared itself as available... trying next");
				}
			}
		}

		throw new IllegalStateException("No pipeline available to render with!");
	}

	private void invalidatePipeline() {
		if (this.currentPipelineIdx < this.pipelines.size() - 1) {
			this.currentPipelineIdx++;
			this.sink = null;
		}
	}

	public void onRender(float partialTicks) {
		try {
			final RenderSink sink = this.providePipeline();
			if (!this.pipelines.get(this.currentPipelineIdx).shouldRender())
			{
				// allow ignoring eg. shadow pass
				return;
			}
			Minecraft.getInstance().getProfiler().push("worldeditcui");
			this.ctx.init(new Vector3(this.minecraft.gameRenderer.getMainCamera().getPosition()), partialTicks, sink);
			float fogStart = RenderSystem.getShaderFogStart();
			FogRenderer.setupNoFog();
			PoseStack poseStack = RenderSystem.getModelViewStack();
			poseStack.pushPose();
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.disableTexture();
			RenderSystem.depthMask(false);
			RenderSystem.disableDepthTest();
			RenderSystem.lineWidth(6.0f);

			final ShaderInstance oldShader = RenderSystem.getShader();
			try {
				this.controller.renderSelections(this.ctx);
				this.sink.flush();
			} catch (Exception e) {
				this.controller.getDebugger().error("Error while attempting to render WorldEdit CUI", e);
				this.invalidatePipeline();
			}

			RenderSystem.depthFunc(GL32.GL_LEQUAL);

			RenderSystem.setShader(() -> oldShader);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
			poseStack.popPose();
			RenderSystem.setShaderFogStart(fogStart);
			Minecraft.getInstance().getProfiler().pop();
//		 	RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
		} catch (Exception ex)
		{
			this.controller.getDebugger().error("Failed while preparing state for WorldEdit CUI", ex);
			this.invalidatePipeline();
		}
	}
}
