package eu.mikroskeem.worldeditcui;

import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.earthcomputer.multiconnect.api.Protocols;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Networking wrappers to integrate nicely with MultiConnect.
 *
 * <p>These methods generally first call </p>
 */
final class CUINetworking {

    private static final boolean MULTICONNECT_AVAILABLE = FabricLoader.getInstance().isModLoaded("multiconnect");

    private static final String CHANNEL_LEGACY = "WECUI"; // pre-1.13 channel name
    public static final Identifier CHANNEL_WECUI = new Identifier("worldedit", "cui");

    private CUINetworking() {
    }

    public static void send(final ClientPlayNetworkHandler handler, final PacketByteBuf codec) {
        if (!MULTICONNECT_AVAILABLE) {
            ClientPlayNetworking.send(CHANNEL_WECUI, codec);
            return;
        }

        sendUnchecked(handler, codec);
    }

    private static void sendUnchecked(final ClientPlayNetworkHandler handler, final PacketByteBuf data) {
        final MultiConnectAPI api = MultiConnectAPI.instance();
        if (api.getProtocolVersion() <= Protocols.V1_12_2) {
            // Legacy string-based
            api.forceSendStringCustomPayload(handler, CHANNEL_LEGACY, data);
        } else {
            api.forceSendCustomPayload(handler, CHANNEL_WECUI, data);
        }
    }


    public static void subscribeToCuiPacket(final ClientPlayNetworking.PlayChannelHandler handler) {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL_WECUI, handler);
        if (MULTICONNECT_AVAILABLE) {
            subscribeToCuiPacketUnchecked(handler);
        }
    }

    private static void subscribeToCuiPacketUnchecked(final ClientPlayNetworking.PlayChannelHandler handler) {
        MultiConnectAPI.instance().addClientboundStringCustomPayloadListener(event -> {
            if (event.getChannel().equals(CHANNEL_LEGACY)) {
                handler.receive(MinecraftClient.getInstance(), event.getNetworkHandler(), event.getData(), ClientPlayNetworking.getSender());
            }
        });
        MultiConnectAPI.instance().addClientboundIdentifierCustomPayloadListener(event -> {
            if (event.getChannel().equals(CHANNEL_WECUI)) {
                handler.receive(MinecraftClient.getInstance(), event.getNetworkHandler(), event.getData(), ClientPlayNetworking.getSender());
            }
        });
    }

}
