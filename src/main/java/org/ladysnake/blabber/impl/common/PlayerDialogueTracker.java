/*
 * Blabber
 * Copyright (C) 2022-2026 Ladysnake
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

import com.google.common.base.Preconditions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.impl.common.actions.CommandDialogueAction;
import org.ladysnake.blabber.impl.common.machine.DialogueStateMachine;
import org.ladysnake.blabber.impl.common.model.DialogueTemplate;
import org.ladysnake.blabber.impl.common.packets.ChoiceAvailabilityPayload;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Optional;
import java.util.UUID;

public final class PlayerDialogueTracker implements ServerTickingComponent {
    public static final ComponentKey<PlayerDialogueTracker> KEY = ComponentRegistry.getOrCreate(Blabber.id("dialogue_tracker"), PlayerDialogueTracker.class);

    private final Player player;
    private @Nullable DialogueStateMachine currentDialogue;
    private @Nullable Entity interlocutor;
    private @Nullable DeserializedState deserializedState;
    private int resumptionAttempts = 0;

    public PlayerDialogueTracker(Player player) {
        this.player = player;
    }

    public static PlayerDialogueTracker get(Player player) {
        return KEY.get(player);
    }

    public void startDialogue(Identifier id, @Nullable Entity interlocutor) throws CommandSyntaxException {
        DialogueTemplate template = DialogueRegistry.getOrEmpty(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown dialogue " + id));
        DialogueStateMachine currentDialogue = this.startDialogue0(
                id,
                template,
                template.start(),
                interlocutor
        );
        currentDialogue.getStartAction().ifPresent(a -> a.action().handle((ServerPlayer) this.player, interlocutor));
    }

    private DialogueStateMachine startDialogue0(Identifier id, DialogueTemplate template, @Nullable String start, @Nullable Entity interlocutor) throws CommandSyntaxException {
        try {
            this.interlocutor = interlocutor;
            this.currentDialogue = prepareDialogue(id, template, start);
        } catch (CommandSyntaxException e) {
            this.interlocutor = null;
            this.currentDialogue = null;
            throw e;
        }
        this.openDialogueScreen();
        return this.currentDialogue;
    }

    private DialogueStateMachine prepareDialogue(Identifier id, DialogueTemplate template, @Nullable String start) throws CommandSyntaxException {
        ServerPlayer serverPlayer = ((ServerPlayer) this.player);
        DialogueTemplate parsedTemplate = template.parseText(CommandDialogueAction.getSource(serverPlayer), serverPlayer);
        DialogueStateMachine currentDialogue = new DialogueStateMachine(id, parsedTemplate, start);
        this.updateConditions(serverPlayer, currentDialogue);
        return currentDialogue;
    }

    public void endDialogue() {
        this.currentDialogue = null;
        this.interlocutor = null;

        if (this.player instanceof ServerPlayer sp && this.player.containerMenu instanceof DialogueScreenHandler) {
            sp.closeContainer();
        }
    }

    public Optional<DialogueStateMachine> getCurrentDialogue() {
        return Optional.ofNullable(this.currentDialogue);
    }

    public Optional<Entity> getInterlocutor() {
        return Optional.ofNullable(this.interlocutor);
    }

    public void updateDialogue() {
        DialogueStateMachine oldDialogue = this.currentDialogue;
        Entity oldInterlocutor = this.interlocutor;
        if (oldDialogue != null) {
            this.endDialogue();

            DialogueRegistry.getOrEmpty(oldDialogue.getId())
                    .ifPresent(template -> this.tryResumeDialogue(
                            oldDialogue.getId(),
                            template,
                            oldDialogue.getCurrentStateKey(),
                            oldInterlocutor
                    ));
        }
    }

    @Override
    public void readData(ValueInput readView) {
        readView.getString("current_dialogue_id").map(Identifier::tryParse).ifPresent(dialogueId ->
            DialogueRegistry.getOrEmpty(dialogueId).ifPresent(dialogueTemplate -> {
                UUID interlocutorUuid = readView.read("interlocutor", UUIDUtil.CODEC).orElse(null);
                String selectedState = readView.getStringOr("current_dialogue_state", null);
                this.deserializedState = new DeserializedState(dialogueId, dialogueTemplate, selectedState, interlocutorUuid);
            })
        );
    }

    @Override
    public void writeData(ValueOutput writeView) {
        if (this.currentDialogue != null) {
            writeView.putString("current_dialogue_id", this.currentDialogue.getId().toString());
            writeView.putString("current_dialogue_state", this.currentDialogue.getCurrentStateKey());
            if (this.interlocutor != null) {
                writeView.store("interlocutor", UUIDUtil.CODEC, this.interlocutor.getUUID());
            }
        }
    }

    @Override
    public void serverTick() {
        DeserializedState saved = this.deserializedState;
        ServerPlayer serverPlayer = (ServerPlayer) this.player;
        if (saved != null) {
            if (resumptionAttempts++ < 200) {   // only try for like, 10 seconds after joining the world
                Entity interlocutor;
                if (saved.interlocutorUuid() != null) {
                    interlocutor = serverPlayer.level().getEntity(saved.interlocutorUuid());
                    if (interlocutor == null) return;    // no one to talk to
                } else {
                    interlocutor = null;
                }
                tryResumeDialogue(saved.dialogueId(), saved.template(), saved.selectedState(), interlocutor);
            }
            this.resumptionAttempts = 0;
            this.deserializedState = null;
        }

        if (this.currentDialogue != null) {
            if (this.player.containerMenu == this.player.inventoryMenu) {
                if (this.currentDialogue.isUnskippable()) {
                    this.openDialogueScreen();
                } else {
                    this.endDialogue();
                    return;
                }
            }

            try {
                ChoiceAvailabilityPayload update = this.updateConditions(serverPlayer, this.currentDialogue);

                if (update != null) {
                    ServerPlayNetworking.send(serverPlayer, update);
                }
            } catch (CommandSyntaxException e) {
                throw new IllegalStateException("Error while updating dialogue conditions", e);
            }
        }
    }

    private void tryResumeDialogue(Identifier id, DialogueTemplate template, String selectedState, Entity interlocutor) {
        try {
            this.startDialogue0(id, template, selectedState, interlocutor);
        } catch (CommandSyntaxException e) {
            Blabber.LOGGER.error("(Blabber) Failed to load dialogue template {}", id, e);
        }
    }

    private @Nullable ChoiceAvailabilityPayload updateConditions(ServerPlayer player, DialogueStateMachine currentDialogue) throws CommandSyntaxException {
        if (currentDialogue.hasConditions()) {
            return currentDialogue.updateConditions(new LootContext.Builder(
                    new net.minecraft.world.level.storage.loot.LootParams.Builder(player.level())
                            .withParameter(LootContextParams.ORIGIN, player.position())
                            .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                            .create(LootContextParamSets.COMMAND)
            ).create(Optional.empty()));
        }
        return null;
    }

    private void openDialogueScreen() {
        Preconditions.checkState(this.currentDialogue != null);
        this.player.openMenu(new DialogueScreenHandlerFactory(this.currentDialogue, this.interlocutor));
    }

    private record DeserializedState(Identifier dialogueId, DialogueTemplate template, String selectedState, @Nullable UUID interlocutorUuid) { }
}
