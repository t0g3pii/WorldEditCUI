package eu.mikroskeem.worldeditcui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mumfrey.worldeditcui.WorldEditCUI;
import com.mumfrey.worldeditcui.config.CUIConfiguration;
import com.mumfrey.worldeditcui.event.listeners.CUIListenerChannel;
import com.mumfrey.worldeditcui.event.listeners.CUIListenerWorldRender;
import eu.mikroskeem.worldeditcui.mixins.MinecraftClientAccess;
import eu.mikroskeem.worldeditcui.render.OptifinePipelineProvider;
import eu.mikroskeem.worldeditcui.render.PipelineProvider;
import eu.mikroskeem.worldeditcui.render.VanillaPipelineProvider;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Fabric mod entrypoint
 *
 * @author Mark Vainomaa
 */
public final class FabricModWorldEditCUI implements ModInitializer {
    private static final int DELAYED_HELO_TICKS = 10;

    public static final String MOD_ID = "worldeditcui";
    private static FabricModWorldEditCUI instance;

    private static final String KEYBIND_CATEGORY_WECUI = "key.categories.worldeditcui";
    private final KeyMapping keyBindToggleUI = key("toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN);
    private final KeyMapping keyBindClearSel = key("clear", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN);
    private final KeyMapping keyBindChunkBorder = key("chunk", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN);

    private static final List<PipelineProvider> RENDER_PIPELINES = List.of(
            new OptifinePipelineProvider(),
            new VanillaPipelineProvider()
    );

    private WorldEditCUI controller;
    private CUIListenerWorldRender worldRenderListener;
    private CUIListenerChannel channelListener;

    private Level lastWorld;
    private LocalPlayer lastPlayer;

    private boolean visible = true;
    private int delayedHelo = 0;

    /**
     * Register a key binding
     *
     * @param name id, will be used as a localization key under {@code key.worldeditcui.<name>}
     * @param type type
     * @param code default value
     * @return new, registered keybinding in the mod category
     */
    private static KeyMapping key(final String name, final InputConstants.Type type, final int code) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping("key." + MOD_ID + '.' + name, type, code, KEYBIND_CATEGORY_WECUI));
    }

    @Override
    public void onInitialize() {
        if (Boolean.getBoolean("wecui.debug.mixinaudit")) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }

        instance = this;

        // Set up event listeners
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientLifecycleEvents.CLIENT_STARTED.register(this::onGameInitDone);
        CUINetworking.subscribeToCuiPacket(this::onPluginMessage);
        ClientPlayConnectionEvents.JOIN.register(this::onJoinGame);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            if (ctx.advancedTranslucency()) {
                try {
                    RenderSystem.getModelViewStack().pushPose();
                    RenderSystem.getModelViewStack().mulPoseMatrix(ctx.matrixStack().last().pose());
                    RenderSystem.applyModelViewMatrix();
                    ctx.worldRenderer().getTranslucentTarget().bindWrite(false);
                    this.onPostRenderEntities(ctx);
                } finally {
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                    RenderSystem.getModelViewStack().popPose();
                }
            }
        });
        WorldRenderEvents.LAST.register(ctx -> {
            if (!ctx.advancedTranslucency()) {
                this.onPostRenderEntities(ctx);
            }
        });
    }

    private void onTick(Minecraft mc) {
        CUIConfiguration config = controller.getConfiguration();
        boolean inGame = mc.player != null;
        boolean clock = ((MinecraftClientAccess) mc).getTimer().partialTick > 0;

        if (inGame && mc.screen == null) {
            while (this.keyBindToggleUI.consumeClick()) {
                this.visible = !this.visible;
            }

            while (this.keyBindClearSel.consumeClick()) {
                if (mc.player != null) {
                    mc.player.chat("//sel");
                }

                if (config.isClearAllOnKey()) {
                    controller.clearRegions();
                }
            }

            while (this.keyBindChunkBorder.consumeClick()) {
                controller.toggleChunkBorders();
            }
        }

        if (inGame && clock && controller != null) {
            if (mc.level != this.lastWorld || mc.player != this.lastPlayer) {
                this.lastWorld = mc.level;
                this.lastPlayer = mc.player;

                controller.getDebugger().debug("World change detected, sending new handshake");
                controller.clear();
                this.helo(mc.getConnection());
                this.delayedHelo = FabricModWorldEditCUI.DELAYED_HELO_TICKS;
                if (mc.player != null && config.isPromiscuous()) {
                    mc.player.chat("/we cui"); //Tricks WE to send the current selection
                }
            }

            if (this.delayedHelo > 0) {
                this.delayedHelo--;
                if (this.delayedHelo == 0) {
                    this.helo(mc.getConnection());
                }
            }
        }
    }

    private void onPluginMessage(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        try {
            int readableBytes = data.readableBytes();
            if (readableBytes > 0) {
                String stringPayload = data.toString(0, data.readableBytes(), StandardCharsets.UTF_8);
                client.execute(() -> channelListener.onMessage(stringPayload));
            } else {
                getController().getDebugger().debug("Warning, invalid (zero length) payload received from server");
            }
        } catch (Exception ex) {
            getController().getDebugger().info("Error decoding payload from server", ex);
        }
    }

    public void onGameInitDone(Minecraft client) {
        this.controller = new WorldEditCUI();
        this.controller.initialise(client);
        this.worldRenderListener = new CUIListenerWorldRender(this.controller, client, RENDER_PIPELINES);
        this.channelListener = new CUIListenerChannel(this.controller);
    }

    public void onJoinGame(final ClientPacketListener handler, final PacketSender sender, final Minecraft client) {
        this.visible = true;
        controller.getDebugger().debug("Joined game, sending initial handshake");
        this.helo(handler);
    }

    public void onPostRenderEntities(final WorldRenderContext ctx) {
        if (this.visible) {
            this.worldRenderListener.onRender(ctx.matrixStack(), ctx.tickDelta());
        }
    }

    private void helo(final ClientPacketListener handler) {
        String message = "v|" + WorldEditCUI.PROTOCOL_VERSION;
        ByteBuf buffer = Unpooled.copiedBuffer(message, StandardCharsets.UTF_8);
        CUINetworking.send(handler, new FriendlyByteBuf(buffer));
    }

    public WorldEditCUI getController()
    {
        return this.controller;
    }

    public static FabricModWorldEditCUI getInstance() {
        return instance;
    }
}
