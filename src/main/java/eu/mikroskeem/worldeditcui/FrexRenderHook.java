package eu.mikroskeem.worldeditcui;

import com.mojang.blaze3d.systems.RenderSystem;
import grondag.frex.Frex;
import grondag.frex.FrexInitializer;
import grondag.frex.api.event.WorldRenderEvent;

public class FrexRenderHook implements FrexInitializer {
    @Override
    public void onInitalizeFrex() {
        if(Frex.isAvailable()) {
            FabricModWorldEditCUI.setRenderMode(RenderMode.FREX_POST_RENDER);
            WorldRenderEvent.AFTER_WORLD_RENDER.register((matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f) -> {
                RenderSystem.pushMatrix();
                RenderSystem.multMatrix(matrices.peek().getModel());
                FabricModWorldEditCUI.getInstance().onPostRenderEntities(tickDelta);
                RenderSystem.popMatrix();
            });
        }
    }
}
