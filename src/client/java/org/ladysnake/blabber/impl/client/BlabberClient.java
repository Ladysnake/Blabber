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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
import org.ladysnake.blabber.impl.common.packets.ChoiceAvailabilityPacket;
import org.ladysnake.blabber.impl.common.packets.DialogueListPacket;
import org.ladysnake.blabber.impl.common.packets.SelectedDialogueStatePacket;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.netty.buffer.Unpooled.buffer;

@Mod.EventBusSubscriber(modid = Blabber.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BlabberClient {
    private static final Map<DialogueLayoutType<?>, MenuScreens.ScreenConstructor<?, ?>> screenRegistry = new LinkedHashMap<>();
    private static final Map<DialogueIllustrationType<?>, DialogueIllustrationRenderer.Factory<?>> illustrationRenderers = new LinkedHashMap<>();
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Blabber.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        DialogueIllustrationRenderer.register(DialogueIllustrationCollection.TYPE, IllustrationCollectionRenderer::new);
        DialogueIllustrationRenderer.register(DialogueIllustrationItem.TYPE, ItemIllustrationRenderer::new);
        DialogueIllustrationRenderer.register(DialogueIllustrationNbtEntity.TYPE, NbtEntityIllustrationRenderer::new);
        DialogueIllustrationRenderer.register(DialogueIllustrationFakePlayer.TYPE, FakePlayerIllustrationRenderer::new);
        DialogueIllustrationRenderer.register(DialogueIllustrationSelectorEntity.TYPE, SelectedEntityIllustrationRenderer::new);
        DialogueIllustrationRenderer.register(DialogueIllustrationTexture.TYPE, TextureIllustrationRenderer::new);
        BlabberScreenRegistry.register(BlabberRegistrar.CLASSIC_LAYOUT, BlabberDialogueScreen::new);
        BlabberScreenRegistry.register(BlabberRegistrar.RPG_LAYOUT, BlabberRpgDialogueScreen::new);
        MenuScreens.register(BlabberRegistrar.DIALOGUE_SCREEN_HANDLER, (MenuScreens.ScreenConstructor<DialogueScreenHandler, BlabberDialogueScreen<?>>) BlabberClient::createDialogueScreen);
        CHANNEL.registerMessage(0, DialogueListPacket.class, DialogueListPacket::encode, DialogueListPacket::decode, BlabberClient::handleDialogueListPacket);
        CHANNEL.registerMessage(1, ChoiceAvailabilityPacket.class, ChoiceAvailabilityPacket::encode, ChoiceAvailabilityPacket::decode, BlabberClient::handleChoiceAvailabilityPacket);
        CHANNEL.registerMessage(2, SelectedDialogueStatePacket.class, SelectedDialogueStatePacket::encode, SelectedDialogueStatePacket::decode, BlabberClient::handleSelectedDialogueStatePacket);
    }

    public static <P extends DialogueLayout.Params> void registerLayoutScreen(
            DialogueLayoutType<P> layoutId,
            MenuScreens.ScreenConstructor<DialogueScreenHandler, BlabberDialogueScreen<P>> screenProvider
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

    private static <P extends DialogueLayout.Params> BlabberDialogueScreen<P> createDialogueScreen(DialogueScreenHandler handler, Inventory inventory, Component title) {
        @SuppressWarnings("unchecked") DialogueLayoutType<P> layoutType = (DialogueLayoutType<P>) handler.getLayout().type();
        @SuppressWarnings("unchecked") MenuScreens.ScreenConstructor<DialogueScreenHandler, BlabberDialogueScreen<P>> provider =
                (MenuScreens.ScreenConstructor<DialogueScreenHandler, BlabberDialogueScreen<P>>) screenRegistry.get(layoutType);

        if (provider != null) {
            return provider.create(handler, inventory, title);
        }

        Blabber.LOGGER.error("(Blabber) No screen provider found for {}", layoutType);
        return new BlabberDialogueScreen<>(handler, inventory, title);
    }

    public static void sendDialogueActionMessage(int choice) {
        FriendlyByteBuf buf = new FriendlyByteBuf(buffer());
        buf.writeByte(choice);
        CHANNEL.sendToServer(new DialogueActionMessage(buf));
    }

    private static void handleDialogueListPacket(DialogueListPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> DialogueRegistry.setClientIds(packet.dialogueIds()));
        contextSupplier.get().setPacketHandled(true);
    }

    private static void handleChoiceAvailabilityPacket(ChoiceAvailabilityPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof DialogueScreenHandler dialogueScreenHandler) {
                dialogueScreenHandler.handleAvailabilityUpdate(packet);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }

    private static void handleSelectedDialogueStatePacket(SelectedDialogueStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof DialogueScreenHandler dialogueScreenHandler) {
                dialogueScreenHandler.setCurrentState(packet.stateKey());
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
