package com.foodlab.common.constant;

public interface RedisConstants {

    String SAMPLE_CACHE_KEY = "lims:sample:";
    String TASK_CACHE_KEY = "lims:task:";
    String USER_CACHE_KEY = "lims:user:";
    String DETECT_ITEM_CACHE_KEY = "lims:detect:item:";

    long DEFAULT_EXPIRE_TIME = 3600L;
    long TOKEN_EXPIRE_TIME = 7200L;
    long SAMPLE_EXPIRE_TIME = 1800L;

    String TOKEN_PREFIX = "lims:token:";
    String OFFLINE_SYNC_LOCK = "lims:offline:sync:lock:";
}
