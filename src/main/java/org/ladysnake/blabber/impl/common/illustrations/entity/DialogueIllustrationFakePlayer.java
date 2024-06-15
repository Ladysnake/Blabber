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
package org.ladysnake.blabber.impl.common.illustrations.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Arm;
import net.minecraft.util.dynamic.Codecs;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;
import org.ladysnake.blabber.impl.common.serialization.MorePacketCodecs;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record DialogueIllustrationFakePlayer(GameProfile profile,
                                             IllustrationAnchor anchor,
                                             int x,
                                             int y,
                                             int width,
                                             int height,
                                             int entitySize,
                                             float yOffset,
                                             StareTarget stareAt,
                                             Optional<PlayerModelOptions> modelOptions,
                                             Optional<NbtCompound> data) implements DialogueIllustrationEntity {
    private static final MapCodec<DialogueIllustrationFakePlayer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codecs.GAME_PROFILE_WITH_PROPERTIES.fieldOf("profile").forGetter(DialogueIllustrationFakePlayer::profile),
            IllustrationAnchor.CODEC.optionalFieldOf("anchor", IllustrationAnchor.TOP_LEFT).forGetter(DialogueIllustrationFakePlayer::anchor),
            Codec.INT.fieldOf("x").forGetter(DialogueIllustrationFakePlayer::x),
            Codec.INT.fieldOf("y").forGetter(DialogueIllustrationFakePlayer::y),
            Codec.INT.fieldOf("width").forGetter(DialogueIllustrationFakePlayer::width),
            Codec.INT.fieldOf("height").forGetter(DialogueIllustrationFakePlayer::height),
            Codec.INT.fieldOf("entity_size").forGetter(DialogueIllustrationFakePlayer::entitySize),
            Codec.FLOAT.optionalFieldOf("y_offset", 0.0f).forGetter(DialogueIllustrationFakePlayer::yOffset),
            StareTarget.CODEC.optionalFieldOf("stare_at", StareTarget.FOLLOW_MOUSE).forGetter(DialogueIllustrationFakePlayer::stareAt),
            PlayerModelOptions.CODEC.optionalFieldOf("model_customization").forGetter(DialogueIllustrationFakePlayer::modelOptions),
            NbtCompound.CODEC.optionalFieldOf("data").forGetter(DialogueIllustrationFakePlayer::data)
    ).apply(instance, DialogueIllustrationFakePlayer::new));
    public static final PacketCodec<PacketByteBuf, DialogueIllustrationFakePlayer> PACKET_CODEC = MorePacketCodecs.tuple(
            PacketCodecs.GAME_PROFILE, DialogueIllustrationFakePlayer::profile,
            IllustrationAnchor.PACKET_CODEC, DialogueIllustrationFakePlayer::anchor,
            PacketCodecs.VAR_INT, DialogueIllustrationFakePlayer::x,
            PacketCodecs.VAR_INT, DialogueIllustrationFakePlayer::y,
            PacketCodecs.VAR_INT, DialogueIllustrationFakePlayer::width,
            PacketCodecs.VAR_INT, DialogueIllustrationFakePlayer::height,
            PacketCodecs.VAR_INT, DialogueIllustrationFakePlayer::entitySize,
            PacketCodecs.FLOAT, DialogueIllustrationFakePlayer::yOffset,
            StareTarget.PACKET_CODEC, DialogueIllustrationFakePlayer::stareAt,
            PlayerModelOptions.PACKET_CODEC.collect(PacketCodecs::optional), DialogueIllustrationFakePlayer::modelOptions,
            PacketCodecs.NBT_COMPOUND.collect(PacketCodecs::optional), DialogueIllustrationFakePlayer::data,
            DialogueIllustrationFakePlayer::new
    );
    public static final DialogueIllustrationType<DialogueIllustrationFakePlayer> TYPE = new DialogueIllustrationType<>(
            CODEC, PACKET_CODEC
    );

    @Override
    public DialogueIllustrationType<?> getType() {
        return TYPE;
    }

    public PlayerModelOptions modelOptionsOrDefault() {
        return this.modelOptions().orElse(PlayerModelOptions.DEFAULT);
    }

    public record PlayerModelOptions(Arm mainHand, EnumSet<PlayerModelPart> visibleParts) {
        public static final PlayerModelOptions DEFAULT = new PlayerModelOptions(Arm.RIGHT, EnumSet.allOf(PlayerModelPart.class));
        private static final Map<String, PlayerModelPart> partsByName;

        static {
            partsByName = new HashMap<>();
            for (PlayerModelPart part : PlayerModelPart.values()) {
                partsByName.put(part.getName(), part);
            }
        }

        private static DataResult<PlayerModelPart> partFromString(String key) {
            PlayerModelPart part = partsByName.get(key);
            if (part != null) return DataResult.success(part);
            return DataResult.error(() -> "Not a valid player model part " + key + " (should be one of " + partsByName.keySet() + ")");
        }

        public static final Codec<EnumSet<PlayerModelPart>> PLAYER_MODEL_PARTS_CODEC = Codec.list(Codec.STRING.comapFlatMap(
                PlayerModelOptions::partFromString,
                PlayerModelPart::getName
        )).xmap(l -> {
            EnumSet<PlayerModelPart> ret = EnumSet.noneOf(PlayerModelPart.class);
            ret.addAll(l);
            return ret;
        }, List::copyOf);
        public static final EnumSet<PlayerModelPart> DEFAULT_VISIBLE_PARTS = EnumSet.allOf(PlayerModelPart.class);

        public static final Codec<PlayerModelOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Arm.CODEC.optionalFieldOf("main_hand", Arm.RIGHT).forGetter(PlayerModelOptions::mainHand),
                PLAYER_MODEL_PARTS_CODEC.optionalFieldOf("visible_parts", DEFAULT_VISIBLE_PARTS).forGetter(PlayerModelOptions::visibleParts)
        ).apply(instance, PlayerModelOptions::new));
        public static final PacketCodec<PacketByteBuf, PlayerModelOptions> PACKET_CODEC = PacketCodec.tuple(
                MorePacketCodecs.ARM, PlayerModelOptions::mainHand,
                PacketCodec.of((value, buf) -> buf.writeEnumSet(value, PlayerModelPart.class), buf -> buf.readEnumSet(PlayerModelPart.class)), PlayerModelOptions::visibleParts,
                PlayerModelOptions::new
        );

        public byte packVisibleParts() {
            byte packed = 0;
            for (PlayerModelPart playerModelPart : this.visibleParts()) {
                packed |= (byte) playerModelPart.getBitFlag();
            }
            return packed;
        }
    }
}
