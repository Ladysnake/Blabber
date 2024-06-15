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
package org.ladysnake.blabber.impl.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.api.client.BlabberDialogueScreen;
import org.ladysnake.blabber.api.client.BlabberScreenRegistry;
import org.ladysnake.blabber.api.client.illustration.DialogueIllustrationRenderer;
import org.ladysnake.blabber.api.illustration.DialogueIllustration;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;
import org.ladysnake.blabber.api.layout.DialogueLayout;
import org.ladysnake.blabber.api.layout.DialogueLayoutType;
import org.ladysnake.blabber.impl.client.illustrations.FakePlayerIllustrationRenderer;
import org.ladysnake.blabber.impl.client.illustrations.IllustrationCollectionRenderer;
import org.ladysnake.blabber.impl.client.illustrations.ItemIllustrationRenderer;
import org.ladysnake.blabber.impl.client.illustrations.NbtEntityIllustrationRenderer;
import org.ladysnake.blabber.impl.client.illustrations.SelectedEntityIllustrationRenderer;
import org.ladysnake.blabber.impl.client.illustrations.TextureIllustrationRenderer;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;
import org.ladysnake.blabber.impl.common.DialogueRegistry;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationCollection;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationItem;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationTexture;
import org.ladysnake.blabber.impl.common.illustrations.entity.DialogueIllustrationFakePlayer;
import org.ladysnake.blabber.impl.common.illustrations.entity.DialogueIllustrationNbtEntity;
import org.ladysnake.blabber.impl.common.illustrations.entity.DialogueIllustrationSelectorEntity;
import org.ladysnake.blabber.impl.common.packets.ChoiceAvailabilityPayload;
import org.ladysnake.blabber.impl.common.packets.DialogueListPayload;
import org.ladysnake.blabber.impl.common.packets.SelectedDialogueStatePayload;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BlabberClient implements ClientModInitializer {
    private static final Map<DialogueLayoutType<?>, HandledScreens.Provider<?, ?>> screenRegistry = new LinkedHashMap<>();
    private static final Map<DialogueIllustrationType<?>, DialogueIllustrationRenderer.Factory<?>> illustrationRenderers = new LinkedHashMap<>();

    @Override
    public void onInitializeClient() {
        DialogueIllustrationRenderer.register(DialogueIllustrationCollection.TYPE, IllustrationCollectionRenderer::new);
        DialogueIllustrationRenderer.register(DialogueIllustrationItem.TYPE, ItemIllustrationRenderer::new);
        DialogueIllustrationRenderer.register(DialogueIllustrationNbtEntity.TYPE, NbtEntityIllustrationRenderer::new);
        DialogueIllustrationRenderer.register(DialogueIllustrationFakePlayer.TYPE, FakePlayerIllustrationRenderer::new);
        DialogueIllustrationRenderer.register(DialogueIllustrationSelectorEntity.TYPE, SelectedEntityIllustrationRenderer::new);
        DialogueIllustrationRenderer.register(DialogueIllustrationTexture.TYPE, TextureIllustrationRenderer::new);
        BlabberScreenRegistry.register(BlabberRegistrar.CLASSIC_LAYOUT, BlabberDialogueScreen::new);
        BlabberScreenRegistry.register(BlabberRegistrar.RPG_LAYOUT, BlabberRpgDialogueScreen::new);
        HandledScreens.register(BlabberRegistrar.DIALOGUE_SCREEN_HANDLER, (HandledScreens.Provider<DialogueScreenHandler, BlabberDialogueScreen<?>>) BlabberClient::createDialogueScreen);
        ClientConfigurationNetworking.registerGlobalReceiver(DialogueListPayload.ID, (packet, ctx) -> DialogueRegistry.setClientIds(packet.dialogueIds()));
        ClientPlayNetworking.registerGlobalReceiver(DialogueListPayload.ID, (packet, ctx) -> DialogueRegistry.setClientIds(packet.dialogueIds()));
        ClientPlayNetworking.registerGlobalReceiver(ChoiceAvailabilityPayload.ID, (packet, ctx) -> {
            if (ctx.player().currentScreenHandler instanceof DialogueScreenHandler dialogueScreenHandler) {
                dialogueScreenHandler.handleAvailabilityUpdate(packet);
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(SelectedDialogueStatePayload.ID, (packet, ctx) -> {
            if (ctx.player().currentScreenHandler instanceof DialogueScreenHandler dialogueScreenHandler) {
                dialogueScreenHandler.setCurrentState(packet.stateKey());
            }
        });
    }

    public static <P extends DialogueLayout.Params> void registerLayoutScreen(
            DialogueLayoutType<P> layoutId,
            HandledScreens.Provider<DialogueScreenHandler, BlabberDialogueScreen<P>> screenProvider
    ) {
        screenRegistry.put(layoutId, screenProvider);
    }

    public static <I extends DialogueIllustration> void registerIllustrationRenderer(DialogueIllustrationType<I> type, DialogueIllustrationRenderer.Factory<I> rendererFactory) {
        illustrationRenderers.put(type, rendererFactory);
    }

    public static <I extends DialogueIllustration> DialogueIllustrationRenderer<I> createRenderer(I illustration) {
        @SuppressWarnings("unchecked") DialogueIllustrationRenderer.Factory<I> renderer = (DialogueIllustrationRenderer.Factory<I>) illustrationRenderers.get(illustration.getType());
        return renderer.create(illustration);
    }

    private static <P extends DialogueLayout.Params> BlabberDialogueScreen<P> createDialogueScreen(DialogueScreenHandler handler, PlayerInventory inventory, Text title) {
        @SuppressWarnings("unchecked") DialogueLayoutType<P> layoutType = (DialogueLayoutType<P>) handler.getLayout().type();
        @SuppressWarnings("unchecked") HandledScreens.Provider<DialogueScreenHandler, BlabberDialogueScreen<P>> provider =
                (HandledScreens.Provider<DialogueScreenHandler, BlabberDialogueScreen<P>>) screenRegistry.get(layoutType);

        if (provider != null) {
            return provider.create(handler, inventory, title);
        }

        Blabber.LOGGER.error("(Blabber) No screen provider found for {}", layoutType);
        return new BlabberDialogueScreen<>(handler, inventory, title);
    }
}
