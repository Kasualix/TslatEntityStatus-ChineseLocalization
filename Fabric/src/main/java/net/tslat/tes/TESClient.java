package net.tslat.tes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.tslat.tes.networking.SyncEffectsPacket;

public class TESClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncEffectsPacket.ID, (client, handler, buf, responseSender) -> SyncEffectsPacket.decode(buf).handleMessage(client::submit));
	}

	public static void sendPacket(ResourceLocation packetId, FriendlyByteBuf buffer) {
		ClientPlayNetworking.send(packetId, buffer);
	}
}
