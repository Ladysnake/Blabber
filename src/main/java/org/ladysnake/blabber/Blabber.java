/*
 * Blabber
 * Copyright (C) 2022-2024 Ladysnake
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
package org.ladysnake.blabber;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.DialogueActionV2;
import org.ladysnake.blabber.api.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;
import org.ladysnake.blabber.impl.common.CommandDialogueAction;
import org.ladysnake.blabber.impl.common.DialogueInitializationException;
import org.ladysnake.blabber.impl.common.PlayerDialogueTracker;
import org.ladysnake.blabber.impl.common.commands.BlabberCommand;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationCollection;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationFakePlayer;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationItem;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationNbtEntity;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationSelectorEntity;
import org.ladysnake.blabber.impl.common.machine.DialogueStateMachine;

public final class Blabber implements ModInitializer {
	public static final String MOD_ID = "blabber";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	/**
	 * Starts a dialogue
	 *
	 * <p>This operation closes the player's {@linkplain  PlayerEntity#currentScreenHandler current screen handler},
	 * if any, and opens a new dialogue screen instead.
	 *
	 * <p>A dialogue may fail to start if it contains malformed texts as per {@link net.minecraft.text.Texts#parse(ServerCommandSource, Text, Entity, int)}.
	 * In that case, this method will throw a {@link DialogueInitializationException}.
	 *
	 * @param player the player for whom to initiate a dialogue
	 * @param id the identifier for the dialogue
	 * @throws IllegalArgumentException if {@code id} is not a valid dialogue in this game instance
	 * @throws DialogueInitializationException if the dialogue failed to initialize
	 */
	public static void startDialogue(ServerPlayerEntity player, Identifier id) {
		startDialogue(player, id, null);
	}

	/**
	 * Starts a dialogue
	 *
	 * <p>This operation closes the player's {@linkplain  PlayerEntity#currentScreenHandler current screen handler},
	 * if any, and opens a new dialogue screen instead.
	 *
	 * <p>A dialogue may fail to start if it contains malformed texts as per {@link net.minecraft.text.Texts#parse(ServerCommandSource, Text, Entity, int)}.
	 * In that case, this method will throw a {@link DialogueInitializationException}.
	 *
	 * @param player the player for whom to initiate a dialogue
	 * @param id the identifier for the dialogue
	 * @param interlocutor the entity with which the player is conversing
	 * @throws IllegalArgumentException if {@code id} is not a valid dialogue in this game instance
	 * @throws DialogueInitializationException if the dialogue failed to initialize
	 */
	public static void startDialogue(ServerPlayerEntity player, Identifier id, @Nullable Entity interlocutor) {
		try {
			PlayerDialogueTracker.get(player).startDialogue(id, interlocutor);
		} catch (CommandSyntaxException e) {
			throw new DialogueInitializationException("Failed to parse texts in dialogue template " + id, e);
		}
	}

	/**
	 * Ends the current dialogue if its id equals {@code expectedDialogue}.
	 *
	 * <p>If the identifiers match, the current dialogue will be ended no matter
	 * its state.
	 *
	 * @param player the player for whom to end the current dialogue
	 * @param expectedDialogue the identifier being compared to the current dialogue, or {@code null} to end any ongoing dialogue
	 */
	public static void endDialogue(ServerPlayerEntity player, @Nullable Identifier expectedDialogue) {
		Identifier currentDialogueId = PlayerDialogueTracker.get(player).getCurrentDialogue().map(DialogueStateMachine::getId).orElse(null);
		if (currentDialogueId != null && (expectedDialogue == null || expectedDialogue.equals(currentDialogueId))) {
			PlayerDialogueTracker.get(player).endDialogue();
		}
	}

	/**
	 * Register a basic {@link DialogueAction} to handle dialogue choices.
	 *
	 * @param actionId the identifier used to reference the action in dialogue definition files
	 * @param action   the action to run when triggered by a player
	 * @see #registerAction(Identifier, Codec)
	 */
	public static void registerAction(Identifier actionId, DialogueAction action) {
		registerAction(actionId, Codec.unit(action));
	}

	/**
	 * Register a basic {@link DialogueAction} to handle dialogue choices.
	 *
	 * @param actionId the identifier used to reference the action in dialogue definition files
	 * @param action   the action to run when triggered by a player
	 * @see #registerAction(Identifier, Codec)
	 */
	public static void registerAction(Identifier actionId, DialogueActionV2 action) {
		registerAction(actionId, Codec.unit(action));
	}

	/**
	 * Register a configurable {@link DialogueAction} to handle dialogue choices.
	 *
	 * @param actionId the identifier used to reference the action in dialogue definition files
	 * @param codec    a codec for deserializing dialogue actions using the given value
	 * @see #registerAction(Identifier, DialogueAction)
	 */
	public static void registerAction(Identifier actionId, Codec<? extends DialogueActionV2> codec) {
		Registry.register(BlabberRegistrar.ACTION_REGISTRY, actionId, codec);
	}

	/**
	 * Register a configurable {@link DialogueIllustrationType} to draw extra features in dialogues.
	 *
	 * @param illustrationId the identifier used to reference the illustration type in dialogue definition files
	 * @param type           the dialogue illustration type
	 */
	public static void registerIllustration(Identifier illustrationId, DialogueIllustrationType<?> type) {
		Registry.register(BlabberRegistrar.ILLUSTRATION_REGISTRY, illustrationId, type);
	}

	@Override
	public void onInitialize() {
		BlabberRegistrar.init();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> BlabberCommand.register(dispatcher));
		registerAction(id("command"), CommandDialogueAction.CODEC);
		registerIllustration(id("group"), DialogueIllustrationCollection.TYPE);
		registerIllustration(id("item"), DialogueIllustrationItem.TYPE);
		registerIllustration(id("fake_entity"), DialogueIllustrationNbtEntity.TYPE);
		registerIllustration(id("fake_player"), DialogueIllustrationFakePlayer.TYPE);
		registerIllustration(id("entity"), DialogueIllustrationSelectorEntity.TYPE);
	}
}
