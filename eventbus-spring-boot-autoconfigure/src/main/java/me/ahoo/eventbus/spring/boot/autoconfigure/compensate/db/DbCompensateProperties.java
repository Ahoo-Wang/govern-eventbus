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

package me.ahoo.eventbus.spring.boot.autoconfigure.compensate.db;

import me.ahoo.eventbus.core.compensate.db.config.PublishConfig;
import me.ahoo.eventbus.core.compensate.db.config.SubscribeConfig;
import me.ahoo.eventbus.spring.boot.autoconfigure.compensate.CompensatePrefix;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author ahoo wang
 */
@ConfigurationProperties(DbCompensateProperties.PREFIX)
public class DbCompensateProperties {
    public final static String PREFIX = CompensatePrefix.PREFIX + ".db";
    private boolean enabled = true;
    @NestedConfigurationProperty
    private PublishConfig publish;
    @NestedConfigurationProperty
    private SubscribeConfig subscribe;

    public DbCompensateProperties() {
        publish = new PublishConfig();
        subscribe = new SubscribeConfig();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public PublishConfig getPublish() {
        return publish;
    }

    public void setPublish(PublishConfig publish) {
        this.publish = publish;
    }

    public SubscribeConfig getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(SubscribeConfig subscribe) {
        this.subscribe = subscribe;
    }
}
