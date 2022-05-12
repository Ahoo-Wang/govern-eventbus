/*
 * Copyright [2021-2021] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.eventbus.spring.boot.autoconfigure.compensate;

import me.ahoo.eventbus.core.compensate.CompensateConfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

/**
 * CompensateProperties.
 *
 * @author ahoo wang
 */
@ConfigurationProperties(CompensatePrefix.PREFIX)
public class CompensateProperties {
    private boolean enabled = true;
    @NestedConfigurationProperty
    private Compensate publish;
    @NestedConfigurationProperty
    private Compensate subscribe;
    
    public CompensateProperties() {
        publish = new Compensate();
        subscribe = new Compensate();
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Compensate getPublish() {
        return publish;
    }
    
    public void setPublish(Compensate publish) {
        this.publish = publish;
    }
    
    public Compensate getSubscribe() {
        return subscribe;
    }
    
    public void setSubscribe(Compensate subscribe) {
        this.subscribe = subscribe;
    }
    
    public static class Compensate extends CompensateConfig {
        private DbMutex dbMutex = DbMutex.DEFAULT;
        private ScheduleConfig schedule = new ScheduleConfig();
        
        public DbMutex getDbMutex() {
            return dbMutex;
        }
        
        public void setDbMutex(DbMutex dbMutex) {
            this.dbMutex = dbMutex;
        }
        
        public ScheduleConfig getSchedule() {
            return schedule;
        }
        
        public void setSchedule(ScheduleConfig schedule) {
            this.schedule = schedule;
        }
    }
    
    public static class DbMutex {
        public static final DbMutex DEFAULT = new DbMutex();
        private Duration initialDelay = Duration.ofSeconds(2);
        private Duration ttl = Duration.ofSeconds(10);
        private Duration transition = Duration.ofSeconds(2);
        
        public Duration getInitialDelay() {
            return initialDelay;
        }
        
        public void setInitialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
        }
        
        public Duration getTtl() {
            return ttl;
        }
        
        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
        
        public Duration getTransition() {
            return transition;
        }
        
        public void setTransition(Duration transition) {
            this.transition = transition;
        }
    }
    
    public static class ScheduleConfig {
        
        private Duration initialDelay = Duration.ofSeconds(5);
        
        private Duration period = Duration.ofMinutes(5);
        
        public Duration getInitialDelay() {
            return initialDelay;
        }
        
        public void setInitialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
        }
        
        public Duration getPeriod() {
            return period;
        }
        
        public void setPeriod(Duration period) {
            this.period = period;
        }
    }
}

