package me.ahoo.eventbus.core.repository;

/**
 * 执行耗时，单位 ms
 *
 * @author ahoo wang
 */
public interface TimeTaken {

    Integer getTaken();

    void setTaken(Integer taken);
}
