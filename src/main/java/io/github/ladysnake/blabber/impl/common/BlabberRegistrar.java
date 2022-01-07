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
package io.github.ladysnake.blabber.impl.common;

import com.mojang.serialization.Lifecycle;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import io.github.ladysnake.blabber.Blabber;
import io.github.ladysnake.blabber.DialogueAction;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.World;

public final class BlabberRegistrar implements EntityComponentInitializer {
    public static final ScreenHandlerType<DialogueScreenHandler> DIALOGUE_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(Blabber.id("dialogue"), (syncId, inventory, buf) -> {
        DialogueStateMachine dialogue = DialogueStateMachine.fromPacket(inventory.player.world, buf);
        return new DialogueScreenHandler(syncId, dialogue);
    });
    public static final Identifier DIALOGUE_ACTION = Blabber.id("dialogue_action");
    public static final RegistryKey<Registry<DialogueTemplate>> DIALOGUE_REGISTRY_KEY = RegistryKey.ofRegistry(Blabber.id("blabber_dialogues"));
    public static final SimpleRegistry<DialogueTemplate> BUILTIN_DIALOGUES = new SimpleRegistry<>(DIALOGUE_REGISTRY_KEY, Lifecycle.stable());
    public static final Registry<DialogueAction> ACTION_REGISTRY = FabricRegistryBuilder.createSimple(DialogueAction.class, Blabber.id("dialogue_actions")).buildAndRegister();

    public static void init() {
        registerBuiltins();
        ServerPlayNetworking.registerGlobalReceiver(DIALOGUE_ACTION, (server, player, handler, buf, responseSender) -> {
            int choice = buf.readByte();
            server.execute(() -> {
                if (player.currentScreenHandler instanceof DialogueScreenHandler dialogueHandler) {
                    dialogueHandler.makeChoice(player, choice);
                }
            });
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerBuiltins() {
        ((MutableRegistry) BuiltinRegistries.REGISTRIES).add(DIALOGUE_REGISTRY_KEY, BUILTIN_DIALOGUES, Lifecycle.stable());
    }

    public static DialogueStateMachine startDialogue(World world, Identifier id) {
        return new DialogueStateMachine(
            world.getRegistryManager().get(DIALOGUE_REGISTRY_KEY).getOrEmpty(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown dialogue " + id)),
            id
        );
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PlayerDialogueTracker.KEY, PlayerDialogueTracker::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
