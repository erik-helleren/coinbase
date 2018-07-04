package com.erik.bookManagement;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BookState {


    private final Map<Side,Map<String,String>> book;
    public static final Function<Side, Map<String, String>> SIDE_MAP_FUNCTION = (side) -> new HashMap<>();


    public BookState() {
        book=new EnumMap<Side, Map<String, String>>(Side.class);
    }

    public void processBookUpdate(BookUpdate u){
        if(u.getQuantity().equals("0")) {
            book.computeIfAbsent(u.getSide(), SIDE_MAP_FUNCTION)
                    .remove(u.getPrice());
        }else{
            book.computeIfAbsent(u.getSide(), SIDE_MAP_FUNCTION)
                    .put(u.getPrice(), u.getQuantity());
        }
    }
}
