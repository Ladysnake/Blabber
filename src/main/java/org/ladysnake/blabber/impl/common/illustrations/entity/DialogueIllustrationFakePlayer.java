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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Arm;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;

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
    private static final Codec<DialogueIllustrationFakePlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.GAME_PROFILE_WITH_PROPERTIES.fieldOf("profile").forGetter(DialogueIllustrationFakePlayer::profile),
            Codecs.createStrictOptionalFieldCodec(IllustrationAnchor.CODEC, "anchor", IllustrationAnchor.TOP_LEFT).forGetter(DialogueIllustrationFakePlayer::anchor),
            Codec.INT.fieldOf("x").forGetter(DialogueIllustrationFakePlayer::x),
            Codec.INT.fieldOf("y").forGetter(DialogueIllustrationFakePlayer::y),
            Codec.INT.fieldOf("width").forGetter(DialogueIllustrationFakePlayer::width),
            Codec.INT.fieldOf("height").forGetter(DialogueIllustrationFakePlayer::height),
            Codec.INT.fieldOf("entity_size").forGetter(DialogueIllustrationFakePlayer::entitySize),
            Codecs.createStrictOptionalFieldCodec(Codec.FLOAT, "y_offset", 0.0f).forGetter(DialogueIllustrationFakePlayer::yOffset),
            Codecs.createStrictOptionalFieldCodec(StareTarget.CODEC, "stare_at", StareTarget.FOLLOW_MOUSE).forGetter(DialogueIllustrationFakePlayer::stareAt),
            Codecs.createStrictOptionalFieldCodec(PlayerModelOptions.CODEC, "model_customization").forGetter(DialogueIllustrationFakePlayer::modelOptions),
            Codecs.createStrictOptionalFieldCodec(NbtCompound.CODEC, "data").forGetter(DialogueIllustrationFakePlayer::data)
    ).apply(instance, DialogueIllustrationFakePlayer::new));
    // Need to have a MapCodecCodec here, otherwise it will deserialize differently
    public static final DialogueIllustrationType<DialogueIllustrationFakePlayer> TYPE = new DialogueIllustrationType<>(
            CODEC,
            buf -> new DialogueIllustrationFakePlayer(
                    buf.readGameProfile(),
                    buf.readEnumConstant(IllustrationAnchor.class),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readFloat(),
                    new StareTarget(buf),
                    buf.readOptional(PlayerModelOptions::new),
                    buf.readOptional(PacketByteBuf::readNbt)
            ),
            (buf, i) -> {
                buf.writeGameProfile(i.profile());
                buf.writeEnumConstant(i.anchor());
                buf.writeVarInt(i.x());
                buf.writeVarInt(i.y());
                buf.writeVarInt(i.width());
                buf.writeVarInt(i.height());
                buf.writeVarInt(i.entitySize());
                buf.writeFloat(i.yOffset());
                StareTarget.writeToPacket(buf, i.stareAt());
                buf.writeOptional(i.modelOptions(), (b, opts) -> opts.writeToBuffer(b));
                buf.writeOptional(i.data(), PacketByteBuf::writeNbt);
            }
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
        public static final EnumSet<PlayerModelPart> NO_VISIBLE_PARTS = EnumSet.allOf(PlayerModelPart.class);

        public static final Codec<PlayerModelOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.createStrictOptionalFieldCodec(StringIdentifiable.createBasicCodec(Arm::values), "main_hand", Arm.RIGHT).forGetter(PlayerModelOptions::mainHand),
                Codecs.createStrictOptionalFieldCodec(PLAYER_MODEL_PARTS_CODEC, "visible_parts", NO_VISIBLE_PARTS).forGetter(PlayerModelOptions::visibleParts)
        ).apply(instance, PlayerModelOptions::new));

        public PlayerModelOptions(PacketByteBuf buf) {
            this(buf.readEnumConstant(Arm.class), buf.readEnumSet(PlayerModelPart.class));
        }

        public void writeToBuffer(PacketByteBuf buf) {
            buf.writeEnumConstant(this.mainHand);
            buf.writeEnumSet(this.visibleParts, PlayerModelPart.class);
        }

        public byte packVisibleParts() {
            byte packed = 0;
            for (PlayerModelPart playerModelPart : this.visibleParts()) {
                packed |= (byte) playerModelPart.getBitFlag();
            }
            return packed;
        }
    }
}
