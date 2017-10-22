package com.zik.faro;

import java.util.List;

import com.zik.faro.data.Item;

public class TestUtil {
	public static <T> boolean isEqualList(List<T> list1, List<T> list2){
		if (list1 == list2) {
            return true;
        }
        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }

        for(T item : list1){
        	if(!list2.contains(item)){
        		return false;
        	}
        }
        for(T item : list2){
        	if(!list1.contains(item)){
        		return false;
        	}
        }
        return true;
	}
}
