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

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MenuType;
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
    public static final ResourceKey<Registry<MapCodec<? extends DialogueActionV2>>> ACTION_REGISTRY_KEY = ResourceKey.createRegistryKey(Blabber.id("dialogue_actions"));
    public static final Registry<MapCodec<? extends DialogueActionV2>> ACTION_REGISTRY = FabricRegistryBuilder.from(
            new MappedRegistry<>(ACTION_REGISTRY_KEY, Lifecycle.stable(), false)
    ).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    public static final ResourceKey<Registry<DialogueIllustrationType<?>>> ILLUSTRATION_REGISTRY_KEY = ResourceKey.createRegistryKey(Blabber.id("dialogue_illustrations"));
    public static final Registry<DialogueIllustrationType<?>> ILLUSTRATION_REGISTRY = FabricRegistryBuilder.from(
            new MappedRegistry<>(ILLUSTRATION_REGISTRY_KEY, Lifecycle.stable(), false)
    ).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    public static final ResourceKey<Registry<DialogueLayoutType<?>>> LAYOUT_REGISTRY_KEY = ResourceKey.createRegistryKey(Blabber.id("dialogue_layouts"));
    public static final Registry<DialogueLayoutType<?>> LAYOUT_REGISTRY = FabricRegistryBuilder.from(
            new MappedRegistry<>(LAYOUT_REGISTRY_KEY, Lifecycle.stable(), false)
    ).attribute(RegistryAttribute.SYNCED).buildAndRegister();
    public static final MenuType<DialogueScreenHandler> DIALOGUE_SCREEN_HANDLER = Registry.register(BuiltInRegistries.MENU, Blabber.id("dialogue"), new ExtendedScreenHandlerType<>((syncId, inventory, data) -> {
        DialogueStateMachine dialogue = data.dialogue();
        dialogue.applyAvailabilityUpdate(data.availableChoices());
        Optional<Entity> interlocutor = data.interlocutorId().map(inventory.player.level()::getEntity);
        return new DialogueScreenHandler(syncId, dialogue, interlocutor.orElse(null));
    }, DialogueScreenHandlerFactory.DialogueOpeningData.PACKET_CODEC));
    public static final DialogueLayoutType<DefaultLayoutParams> CLASSIC_LAYOUT = new DialogueLayoutType<>(DefaultLayoutParams.CODEC, DefaultLayoutParams.PACKET_CODEC, DefaultLayoutParams.DEFAULT);
    public static final DialogueLayoutType<DefaultLayoutParams> RPG_LAYOUT = new DialogueLayoutType<>(DefaultLayoutParams.CODEC, DefaultLayoutParams.PACKET_CODEC, DefaultLayoutParams.DEFAULT);

    public static final SuggestionProvider<CommandSourceStack> ALL_DIALOGUES = SuggestionProviders.register(
            Blabber.id("available_dialogues"),
            (context, builder) -> SharedSuggestionProvider.suggestResource(context.getSource() instanceof CommandSourceStack ? DialogueRegistry.getIds() : DialogueRegistry.getClientIds(), builder)
    );

    public static void init() {
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, Blabber.id("interlocutor_properties"), InterlocutorPropertiesLootCondition.TYPE);
        ArgumentTypeRegistry.registerArgumentType(Blabber.id("setting"), SettingArgumentType.class, SingletonArgumentInfo.contextFree(SettingArgumentType::setting));

        DialogueLoader.init();

        PayloadTypeRegistry.configurationS2C().register(DialogueListPayload.ID, DialogueListPayload.PACKET_CODEC);

        PayloadTypeRegistry.playS2C().register(DialogueListPayload.ID, DialogueListPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ChoiceAvailabilityPayload.ID, ChoiceAvailabilityPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SelectedDialogueStatePayload.ID, SelectedDialogueStatePayload.PACKET_CODEC);

        PayloadTypeRegistry.playC2S().register(ChoiceSelectionPayload.ID, ChoiceSelectionPayload.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ChoiceSelectionPayload.ID, (payload, ctx) -> {
            if (ctx.player().containerMenu instanceof DialogueScreenHandler dialogueHandler) {
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
                Blabber.LOGGER.warn("{} does not have Blabber installed, this will cause issues if they trigger a dialogue", handler.getOwner().name());
            }
        });
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadId(String name) {
        return new CustomPacketPayload.Type<>(Blabber.id(name));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PlayerDialogueTracker.KEY, PlayerDialogueTracker::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(BlabberSettingsComponent.KEY, BlabberSettingsComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
