package com.trader.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yjt
 * @since 2020/9/22 上午9:29
 */
public final class ThreadLocalUtils {
    /**
     * 本地线程缓存
     */
    private final static ThreadLocal<Map<String, Object>> STORAGE = ThreadLocal.withInitial(() -> new HashMap<>(16));

    /**
     * 设置值
     *
     * @param key
     *         key
     * @param value
     *         value
     */
    public static void set(String key, Object value) {
        STORAGE.get().put(key, value);
    }

    /**
     * 获取一个值
     *
     * @param key
     *         key
     * @param <T>
     *         目标值类型
     *
     * @return 目标值
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) STORAGE.get().get(key);
    }
}
