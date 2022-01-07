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

import io.github.ladysnake.blabber.impl.common.BlabberCommand;
import io.github.ladysnake.blabber.impl.common.BlabberRegistrar;
import io.github.ladysnake.blabber.impl.common.PlayerDialogueTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public final class Blabber implements ModInitializer {
	public static final String MOD_ID = "blabber";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	public static void startDialogue(ServerPlayerEntity player, Identifier id) {
		PlayerDialogueTracker.get(player).startDialogue(id);
	}

	public static void endDialogue(ServerPlayerEntity player) {
		PlayerDialogueTracker.get(player).endDialogue();
	}

	public static void registerAction(Identifier actionId, DialogueAction action) {
		Registry.register(BlabberRegistrar.ACTION_REGISTRY, actionId, action);
	}

	public static Optional<DialogueAction> getAction(Identifier actionId) {
		return BlabberRegistrar.ACTION_REGISTRY.getOrEmpty(actionId);
	}

	@Override
	public void onInitialize() {
		BlabberRegistrar.init();
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> BlabberCommand.register(dispatcher));
	}
}
