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
package org.ladysnake.blabber.impl.common;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.api.DialogueActionV2;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;
import org.ladysnake.blabber.api.layout.DefaultLayoutParams;
import org.ladysnake.blabber.api.layout.DialogueLayoutType;
import org.ladysnake.blabber.impl.common.commands.SettingArgumentType;
import org.ladysnake.blabber.impl.common.machine.DialogueStateMachine;
import org.ladysnake.blabber.impl.common.packets.ChoiceAvailabilityPayload;
import org.ladysnake.blabber.impl.common.packets.ChoiceSelectionPayload;
import org.ladysnake.blabber.impl.common.packets.DialogueListPayload;
import org.ladysnake.blabber.impl.common.packets.SelectedDialogueStatePayload;
import org.ladysnake.blabber.impl.common.settings.BlabberSettingsComponent;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

import java.util.Optional;
import java.util.Set;

public final class BlabberRegistrar implements EntityComponentInitializer {
    public static final RegistryKey<Registry<MapCodec<? extends DialogueActionV2>>> ACTION_REGISTRY_KEY = RegistryKey.ofRegistry(Blabber.id("dialogue_actions"));
    public static final Registry<MapCodec<? extends DialogueActionV2>> ACTION_REGISTRY = FabricRegistryBuilder.from(
            new SimpleRegistry<>(ACTION_REGISTRY_KEY, Lifecycle.stable(), false)
    ).buildAndRegister();

    public static final RegistryKey<Registry<DialogueIllustrationType<?>>> ILLUSTRATION_REGISTRY_KEY = RegistryKey.ofRegistry(Blabber.id("dialogue_illustrations"));
    public static final Registry<DialogueIllustrationType<?>> ILLUSTRATION_REGISTRY = FabricRegistryBuilder.from(
            new SimpleRegistry<>(ILLUSTRATION_REGISTRY_KEY, Lifecycle.stable(), false)
    ).buildAndRegister();

    public static final RegistryKey<Registry<DialogueLayoutType<?>>> LAYOUT_REGISTRY_KEY = RegistryKey.ofRegistry(Blabber.id("dialogue_layouts"));
    public static final Registry<DialogueLayoutType<?>> LAYOUT_REGISTRY = FabricRegistryBuilder.from(
            new SimpleRegistry<>(LAYOUT_REGISTRY_KEY, Lifecycle.stable(), false)
    ).buildAndRegister();
    public static final ScreenHandlerType<DialogueScreenHandler> DIALOGUE_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, Blabber.id("dialogue"), new ExtendedScreenHandlerType<>((syncId, inventory, data) -> {
        DialogueStateMachine dialogue = data.dialogue();
        dialogue.applyAvailabilityUpdate(data.availableChoices());
        Optional<Entity> interlocutor = data.interlocutorId().map(inventory.player.getWorld()::getEntityById);
        return new DialogueScreenHandler(syncId, dialogue, interlocutor.orElse(null));
    }, DialogueScreenHandlerFactory.DialogueOpeningData.PACKET_CODEC));
    public static final DialogueLayoutType<DefaultLayoutParams> CLASSIC_LAYOUT = new DialogueLayoutType<>(DefaultLayoutParams.CODEC, DefaultLayoutParams.PACKET_CODEC, DefaultLayoutParams.DEFAULT);
    public static final DialogueLayoutType<DefaultLayoutParams> RPG_LAYOUT = new DialogueLayoutType<>(DefaultLayoutParams.CODEC, DefaultLayoutParams.PACKET_CODEC, DefaultLayoutParams.DEFAULT);

    public static final SuggestionProvider<ServerCommandSource> ALL_DIALOGUES = SuggestionProviders.register(
            Blabber.id("available_dialogues"),
            (context, builder) -> CommandSource.suggestIdentifiers(context.getSource() instanceof ServerCommandSource ? DialogueRegistry.getIds() : DialogueRegistry.getClientIds(), builder)
    );

    public static void init() {
        Registry.register(Registries.LOOT_CONDITION_TYPE, Blabber.id("interlocutor_properties"), InterlocutorPropertiesLootCondition.TYPE);
        ArgumentTypeRegistry.registerArgumentType(Blabber.id("setting"), SettingArgumentType.class, ConstantArgumentSerializer.of(SettingArgumentType::setting));

        DialogueLoader.init();

        PayloadTypeRegistry.configurationS2C().register(DialogueListPayload.ID, DialogueListPayload.PACKET_CODEC);

        PayloadTypeRegistry.playS2C().register(DialogueListPayload.ID, DialogueListPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ChoiceAvailabilityPayload.ID, ChoiceAvailabilityPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SelectedDialogueStatePayload.ID, SelectedDialogueStatePayload.PACKET_CODEC);

        PayloadTypeRegistry.playC2S().register(ChoiceSelectionPayload.ID, ChoiceSelectionPayload.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ChoiceSelectionPayload.ID, (payload, ctx) -> {
            if (ctx.player().currentScreenHandler instanceof DialogueScreenHandler dialogueHandler) {
                if (!dialogueHandler.makeChoice(ctx.player(), payload.selectedChoice())) {
                    ctx.responseSender().sendPacket(new SelectedDialogueStatePayload(dialogueHandler.getCurrentStateKey()));
                }
            }
        });
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, DialogueListPayload.ID)) {
                Set<Identifier> dialogueIds = DialogueRegistry.getIds();
                ServerConfigurationNetworking.send(handler, new DialogueListPayload(dialogueIds));
            } else {
                Blabber.LOGGER.warn("{} does not have Blabber installed, this will cause issues if they trigger a dialogue", handler.getDebugProfile().getName());
            }
        });
    }

    public static <T extends CustomPayload> CustomPayload.Id<T> payloadId(String name) {
        return CustomPayload.id(Blabber.MOD_ID + ":" + name);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PlayerDialogueTracker.KEY, PlayerDialogueTracker::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(BlabberSettingsComponent.KEY, BlabberSettingsComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
