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

package me.ahoo.eventbus.core.repository;

/**
 * 订阅状态.
 *
 * @author ahoo wang
 */
public enum SubscribeStatus {

    INITIALIZED(0),
    SUCCEEDED(1),
    FAILED(2);
    private final int value;

    SubscribeStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SubscribeStatus valueOf(int value) {

        switch (value) {
            case 0:
                return INITIALIZED;
            case 1:
                return SUCCEEDED;
            case 2:
                return FAILED;
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
    }
}
