package com.example.demo.forecast.service;

import com.example.demo.forecast.dto.FcstItem;
import com.example.demo.forecast.dto.FcstItems;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FcstItemDeserializer extends JsonDeserializer<FcstItems> {

    private final ObjectMapper objectMapper;

    public FcstItemDeserializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public FcstItems deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        JsonNode itemNode = node.findValue("item");

        List<FcstItem> items = Arrays.stream(objectMapper.treeToValue(itemNode, FcstItem[].class)).toList();

        return new FcstItems(items);
    }
}