package com.erik.bookManagement;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BookState {

    private transient final boolean incremental;
    private final Map<Side,Map<String,String>> book;
    public transient static final Function<Side, Map<String, String>> SIDE_MAP_FUNCTION = (side) -> new HashMap<>();


    public BookState() {
        this(false);
    }

    /**
     * @param incremental if this is an incremental book state, all 0 quantity entries are preserved.
     */
    public BookState(boolean incremental){
        this.incremental=incremental;
        book=new EnumMap<Side, Map<String, String>>(Side.class);
    }

    public void processBookUpdate(BookUpdate u){
        if(u.getQuantity().equals("0")&&!incremental) {
            book.computeIfAbsent(u.getSide(), SIDE_MAP_FUNCTION)
                    .remove(u.getPrice());
        }else{
            book.computeIfAbsent(u.getSide(), SIDE_MAP_FUNCTION)
                    .put(u.getPrice(), u.getQuantity());
        }
    }

    public void clear(){
        for(Side s:book.keySet()){
            book.get(s).clear();
        }
    }
}
