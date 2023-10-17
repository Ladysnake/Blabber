package org.ladysnake.blabber.impl.common.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import org.ladysnake.blabber.Blabber;

public record SelectedDialogueStatePacket(String stateKey) implements FabricPacket {
    public static final PacketType<SelectedDialogueStatePacket> TYPE = PacketType.create(Blabber.id("selected_dialogue_state"), SelectedDialogueStatePacket::new);

    public SelectedDialogueStatePacket(PacketByteBuf buf) {
        this(buf.readString());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.stateKey);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
