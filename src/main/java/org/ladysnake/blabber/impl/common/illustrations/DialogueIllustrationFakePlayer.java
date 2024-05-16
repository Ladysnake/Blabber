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
package org.ladysnake.blabber.impl.common.illustrations;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Arm;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.DialogueIllustrationType;
import org.ladysnake.blabber.impl.mixin.PlayerEntityAccessor;
import org.ladysnake.blabber.impl.mixin.client.AbstractClientPlayerEntityAccessor;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DialogueIllustrationFakePlayer extends DialogueIllustrationEntity<DialogueIllustrationFakePlayer.Spec> {
    // Need to have a MapCodecCodec here, otherwise it will deserialize differently
    private static final Codec<DialogueIllustrationFakePlayer> CODEC = Spec.CODEC.xmap(DialogueIllustrationFakePlayer::new, DialogueIllustrationFakePlayer::spec).codec();
    public static final DialogueIllustrationType<DialogueIllustrationFakePlayer> TYPE = new DialogueIllustrationType<>(
            CODEC,
            buf -> new DialogueIllustrationFakePlayer(new Spec(
                    buf.readGameProfile(),
                    buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readFloat(),
                    buf.readOptional(PacketByteBuf::readInt),
                    buf.readOptional(PacketByteBuf::readInt),
                    buf.readOptional(PlayerModelOptions::new),
                    buf.readOptional(PacketByteBuf::readNbt)
            )),
            (buf, i) -> {
                buf.writeGameProfile(i.spec().profile());
                buf.writeInt(i.spec().x1());
                buf.writeInt(i.spec().y1());
                buf.writeInt(i.spec().x2());
                buf.writeInt(i.spec().y2());
                buf.writeInt(i.spec().size());
                buf.writeFloat(i.spec().yOff());
                buf.writeOptional(i.spec().stareAtX(), PacketByteBuf::writeInt);
                buf.writeOptional(i.spec().stareAtY(), PacketByteBuf::writeInt);
                buf.writeOptional(i.spec().modelOptions(), (b, opts) -> opts.writeToBuffer(b));
                buf.writeOptional(i.spec().data(), PacketByteBuf::writeNbt);
            }
    );

    public DialogueIllustrationFakePlayer(Spec spec) {
        super(spec);
    }

    @Override
    public DialogueIllustrationType<?> getType() {
        return TYPE;
    }

    @SuppressWarnings("UnreachableCode")
    @Environment(EnvType.CLIENT)
    @Override
    protected @Nullable LivingEntity getRenderedEntity(World world) {
        GameProfile profile = this.spec().profile();
        OtherClientPlayerEntity fakePlayer = new OtherClientPlayerEntity((ClientWorld) world, profile);
        this.spec().data().ifPresent(fakePlayer::readNbt);
        ((AbstractClientPlayerEntityAccessor) fakePlayer).setPlayerListEntry(new PlayerListEntry(profile, false));
        fakePlayer.prevBodyYaw = fakePlayer.bodyYaw = 0.0f;
        fakePlayer.prevHeadYaw = fakePlayer.headYaw = 0.0f;
        PlayerModelOptions playerModelOptions = this.spec().modelOptionsOrDefault();
        fakePlayer.getDataTracker().set(PlayerEntityAccessor.getPlayerModelParts(), playerModelOptions.packVisibleParts());
        fakePlayer.setMainArm(playerModelOptions.mainHand());
        return fakePlayer;
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

    public record Spec(GameProfile profile,
                       int x1,
                       int y1,
                       int x2,
                       int y2,
                       int size,
                       float yOff,
                       Optional<Integer> stareAtX,
                       Optional<Integer> stareAtY,
                       Optional<PlayerModelOptions> modelOptions,
                       Optional<NbtCompound> data) implements DialogueIllustrationEntity.Spec {
        private static final MapCodec<Spec> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codecs.GAME_PROFILE_WITH_PROPERTIES.fieldOf("profile").forGetter(Spec::profile),
                Codec.INT.fieldOf("x1").forGetter(Spec::x1),
                Codec.INT.fieldOf("y1").forGetter(Spec::y1),
                Codec.INT.fieldOf("x2").forGetter(Spec::x2),
                Codec.INT.fieldOf("y2").forGetter(Spec::y2),
                Codec.INT.fieldOf("size").forGetter(Spec::size),
                Codecs.createStrictOptionalFieldCodec(Codec.FLOAT, "y_offset", 0.0f).forGetter(Spec::yOff),
                Codecs.createStrictOptionalFieldCodec(Codec.INT, "stare_at_x").forGetter(Spec::stareAtX),
                Codecs.createStrictOptionalFieldCodec(Codec.INT, "stare_at_y").forGetter(Spec::stareAtY),
                Codecs.createStrictOptionalFieldCodec(PlayerModelOptions.CODEC, "model_customization").forGetter(Spec::modelOptions),
                Codecs.createStrictOptionalFieldCodec(NbtCompound.CODEC, "data").forGetter(Spec::data)
        ).apply(instance, Spec::new));

        public PlayerModelOptions modelOptionsOrDefault() {
            return this.modelOptions().orElse(PlayerModelOptions.DEFAULT);
        }
    }
}
