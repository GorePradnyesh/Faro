package com.zik.faro.notifications;

import java.net.MalformedURLException;

public interface NotificationClient<T,V> {
	
	// Send notification
	public V send(T t) throws Exception;
	
	public V subscribeToTopic(T t) throws Exception;
	
	public V unsubscribeToTopic(T t) throws Exception;
}
