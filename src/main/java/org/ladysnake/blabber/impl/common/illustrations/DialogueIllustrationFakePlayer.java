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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.DialogueIllustrationType;

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

    @Environment(EnvType.CLIENT)
    @Override
    protected @Nullable LivingEntity getRenderedEntity(World world) {
        OtherClientPlayerEntity fakePlayer = new OtherClientPlayerEntity(
                (ClientWorld) world,
                this.spec().profile()
        );
        spec().data().ifPresent(fakePlayer::readNbt);
        return fakePlayer;
    }

    public record Spec(GameProfile profile, int x1, int y1, int x2, int y2, int size, float yOff, Optional<Integer> stareAtX,
                       Optional<Integer> stareAtY, Optional<NbtCompound> data) implements DialogueIllustrationEntity.Spec {
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
                Codecs.createStrictOptionalFieldCodec(NbtCompound.CODEC, "data").forGetter(Spec::data)
        ).apply(instance, Spec::new));
    }
}
