package dev.kir.sync.client.render;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

@Environment(EnvType.CLIENT)
public final class CustomVertexFormats extends VertexFormats {
    // TODO: Padding went poof :despair:
    public static final VertexFormat POSITION_COLOR_OVERLAY_LIGHT_NORMAL = new VertexFormat((ImmutableMap.<String, VertexFormatElement>builder()).put("Position", VertexFormatElement.POSITION).put("Color", VertexFormatElement.COLOR).put("UV1", VertexFormatElement.UV_1).put("UV2", VertexFormatElement.UV_2).put("Normal", VertexFormatElement.NORMAL).put("Padding", PADDING_ELEMENT).build());
}