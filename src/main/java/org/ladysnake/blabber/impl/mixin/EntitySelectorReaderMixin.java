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
package org.ladysnake.blabber.impl.mixin;

import com.mojang.brigadier.StringReader;
import net.minecraft.command.EntitySelectorReader;
import org.ladysnake.blabber.impl.common.BlabberEntitySelectorExt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntitySelectorReader.class)
public abstract class EntitySelectorReaderMixin {
    @Shadow @Final private StringReader reader;
    @Unique
    private boolean blabber$interlocutorSelector;

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "readAtVariable", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/brigadier/StringReader;read()C"), allow = 1)
    private char parseInterlocutor(char selector) {
        if (selector == 'i') {
            int cursor = this.reader.getCursor();
            String fullSelector = this.reader.readUnquotedString();

            if (fullSelector.equals("nterlocutor")) {
                this.blabber$interlocutorSelector = true;
                return 's'; // hijack self-selector for the rest of the parsing
            } else {
                this.reader.setCursor(cursor);
            }
        }

        return selector;
    }

    @Inject(method = "build", at = @At("RETURN"))
    private void configureInterlocutor(CallbackInfoReturnable<BlabberEntitySelectorExt> cir) {
        cir.getReturnValue().blabber$setInterlocutorSelector(this.blabber$interlocutorSelector);
    }
}
