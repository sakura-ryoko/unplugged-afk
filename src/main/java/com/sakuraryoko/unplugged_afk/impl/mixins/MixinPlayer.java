/*
 * This file is part of the Unplugged-AFK project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Sakura-Ryoko and contributors
 *
 * Unplugged-AFK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Unplugged-AFK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Unplugged-AFK.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sakuraryoko.unplugged_afk.impl.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Opcodes;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedServerPlayer;

@Mixin(Player.class)
@ApiStatus.Internal
public abstract class MixinPlayer
{
	@WrapOperation(
			//#if MC >= 1.21.11
			//$$ method = "causeExtraKnockback",
			//#else
			method = "attack",
			//#endif
			at = @At(value = "FIELD",
			         //#if MC >= 26.3
			         //$$ target = "Lnet/minecraft/world/entity/Entity;syncVelocity:Z",
			         //#else
			         target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z",
			         //#endif
			         ordinal = 0,
			         opcode = Opcodes.GETFIELD
			)
	)
	private boolean unplugged$onKnockback(Entity instance, Operation<Boolean> original)
	{
	//		boolean orig = original.call(instance);
		//#if MC >= 26.3
		//$$ return instance.syncVelocity && !(instance instanceof UnpluggedServerPlayer);
		//#else
		return instance.hurtMarked && !(instance instanceof UnpluggedServerPlayer);
		//#endif
	}
}
