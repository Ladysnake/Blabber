/*
 * Blabber
 * Copyright (C) 2022-2025 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package org.ladysnake.blabber.impl.common;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.impl.common.machine.DialogueStateMachine;
import org.ladysnake.blabber.impl.common.packets.ChoiceAvailabilityPayload;

import java.util.Optional;

public class DialogueScreenHandlerFactory implements ExtendedScreenHandlerFactory<DialogueScreenHandlerFactory.DialogueOpeningData> {
    private final DialogueStateMachine dialogue;
    private final Text displayName;
    private final @Nullable Entity interlocutor;

    public DialogueScreenHandlerFactory(DialogueStateMachine dialogue, Text displayName, @Nullable Entity interlocutor) {
        this.dialogue = dialogue;
        this.displayName = displayName;
        this.interlocutor = interlocutor;
    }

    @Override
    public Text getDisplayName() {
        return this.displayName;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new DialogueScreenHandler(BlabberRegistrar.DIALOGUE_SCREEN_HANDLER, syncId, this.dialogue, this.interlocutor);
    }

    @Override
    public DialogueOpeningData getScreenOpeningData(ServerPlayerEntity player) {
        return new DialogueOpeningData(
                this.dialogue,
                Optional.ofNullable(this.interlocutor).map(Entity::getId),
                this.dialogue.createFullAvailabilityUpdatePacket()
        );
    }

    public record DialogueOpeningData(DialogueStateMachine dialogue, Optional<Integer> interlocutorId,
                                      ChoiceAvailabilityPayload availableChoices) {
        public static final PacketCodec<RegistryByteBuf, DialogueOpeningData> PACKET_CODEC = PacketCodec.tuple(
                DialogueStateMachine.PACKET_CODEC, DialogueOpeningData::dialogue,
                PacketCodecs.VAR_INT.collect(PacketCodecs::optional), DialogueOpeningData::interlocutorId,
                ChoiceAvailabilityPayload.PACKET_CODEC, DialogueOpeningData::availableChoices,
                DialogueOpeningData::new
        );
    }
}
