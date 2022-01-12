/*
 * Blabber
 * Copyright (C) 2022 Ladysnake
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
package io.github.ladysnake.blabber;

import com.mojang.serialization.Codec;
import io.github.ladysnake.blabber.impl.common.BlabberCommand;
import io.github.ladysnake.blabber.impl.common.BlabberRegistrar;
import io.github.ladysnake.blabber.impl.common.CommandDialogueAction;
import io.github.ladysnake.blabber.impl.common.DialogueStateMachine;
import io.github.ladysnake.blabber.impl.common.PlayerDialogueTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

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
	 * @param player the player for whom to initiate a dialogue
	 * @param id the identifier for the dialogue
	 */
	public static void startDialogue(ServerPlayerEntity player, Identifier id) {
		PlayerDialogueTracker.get(player).startDialogue(id);
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
	 * Register a configurable {@link DialogueAction} to handle dialogue choices.
	 *
	 * @param actionId the identifier used to reference the action in dialogue definition files
	 * @param codec    a codec for deserializing dialogue actions using the given value
	 * @see #registerAction(Identifier, DialogueAction)
	 */
	public static void registerAction(Identifier actionId, Codec<? extends DialogueAction> codec) {
		Registry.register(BlabberRegistrar.ACTION_REGISTRY, actionId, codec);
	}

	@Override
	public void onInitialize() {
		BlabberRegistrar.init();
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> BlabberCommand.register(dispatcher));
		registerAction(id("command"), CommandDialogueAction.CODEC);
	}
}
