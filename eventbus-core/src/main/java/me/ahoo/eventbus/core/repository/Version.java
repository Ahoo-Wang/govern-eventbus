package me.ahoo.eventbus.core.repository;

/**
 * 版本号
 *
 * @author ahoo wang
 */
public interface Version {
    Integer INITIAL_VALUE = 1;

    Integer getVersion();

    void setVersion(Integer version);
}
