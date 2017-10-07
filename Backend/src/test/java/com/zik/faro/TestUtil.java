package com.zik.faro;

import java.util.List;

import com.zik.faro.data.Item;

public class TestUtil {
	public static boolean isEqualList(List<Item> list1, List<Item> list2){
		if (list1 == list2) {
            return true;
        }
        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }

        for(Item item : list1){
        	if(!list2.contains(item)){
        		return false;
        	}
        }
        return true;
	}
}
