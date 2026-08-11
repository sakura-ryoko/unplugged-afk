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

package com.sakuraryoko.unplugged_afk.impl.config.data.gson;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.unplugged_afk.api.state.UnpluggedState;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedStatus;

/**
 * Gson TypeAdapter for {@link UnpluggedState}.
 *
 * <p>Java records have {@code final} fields, which Gson versions before 2.10
 * (e.g. 2.8.9 bundled with Minecraft 1.19.2) cannot deserialize reflectively.
 * This adapter lets the config load on every supported Gson version (2.8.9+).
 */
@ApiStatus.Internal
public class UnpluggedStateAdapter extends TypeAdapter<UnpluggedState>
{
	@Override
	public void write(JsonWriter out, UnpluggedState value) throws IOException
	{
		if (value == null)
		{
			out.nullValue();
			return;
		}

		out.beginObject();
		out.name("status").value(value.status().name());
		out.name("time").value(value.time());
		out.name("timeout").value(value.timeout());
		out.name("startTime").value(value.startTime());
		out.name("reason").value(value.reason());
		out.endObject();
	}

	@Override
	public UnpluggedState read(JsonReader in) throws IOException
	{
		if (in.peek() == JsonToken.NULL)
		{
			in.nextNull();
			return null;
		}

		UnpluggedStatus status = UnpluggedStatus.INACTIVE;
		int time = 129600;
		long timeout = -1L;
		long startTime = -1L;
		String reason = "";

		in.beginObject();
		while (in.hasNext())
		{
			String name = in.nextName();

			switch (name)
			{
				case "status" -> status = this.readStatus(in);
				case "time" -> time = in.nextInt();
				case "timeout" -> timeout = in.nextLong();
				case "startTime" -> startTime = in.nextLong();
				case "reason" -> reason = in.nextString();
				default -> in.skipValue();
			}
		}
		in.endObject();

		return new UnpluggedState(status, time, timeout, startTime, reason);
	}

	private UnpluggedStatus readStatus(JsonReader in) throws IOException
	{
		if (in.peek() == JsonToken.NULL)
		{
			in.nextNull();
			return UnpluggedStatus.INACTIVE;
		}

		String value = in.nextString();

		try
		{
			return UnpluggedStatus.valueOf(value);
		}
		catch (IllegalArgumentException err)
		{
			return UnpluggedStatus.INACTIVE;
		}
	}
}
