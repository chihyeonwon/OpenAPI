package com.example.demo.forecast.service;

import com.example.demo.forecast.dto.CategoryCode;
import com.example.demo.forecast.dto.FcstItem;
import com.example.demo.forecast.dto.FcstItems;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ForecastService {

    public FcstItems parsingJsonObject(String json) {
        FcstItems result = new FcstItems(new ArrayList<>());
        try {
            ObjectMapper mapper = new ObjectMapper();
            FcstItems items = mapper.readValue(json, FcstItems.class);

            for(FcstItem item : items.getFcstItems()) {
                result.getFcstItems().add(decodeCategory(item));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private FcstItem decodeCategory(FcstItem item) {
        String name = CategoryCode.valueOf(item.getCategory()).getName();
        String value = CategoryCode.getCodeValue(item.getCategory(), item.getFcstValue());
        String unit = CategoryCode.valueOf(item.getCategory()).getUnit();

        item.setCategoryName(name);
        item.setFcstValue(value + unit);
        return item;
    }
}