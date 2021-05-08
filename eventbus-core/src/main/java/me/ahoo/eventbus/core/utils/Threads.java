package me.ahoo.eventbus.core.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.var;

import java.util.concurrent.ThreadFactory;

/**
 * @author ahoo wang
 */
public class Threads {
    public static ThreadFactory defaultFactory(String domain) {
        var nameFormat = domain + "-%d";
        return new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat(nameFormat)
                .build();
    }
}
