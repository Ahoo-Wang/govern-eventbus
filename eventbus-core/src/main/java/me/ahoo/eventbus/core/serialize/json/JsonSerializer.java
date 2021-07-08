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

package me.ahoo.eventbus.core.serialize.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.ahoo.eventbus.core.serialize.Serializer;

/**
 * @author ahoo wang
 * Creation time 2021/2/1 22:05
 **/
public class JsonSerializer implements Serializer {
    private final ObjectMapper objectMapper;

    public JsonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public String serialize(Object payloadObj) {
        return objectMapper.writeValueAsString(payloadObj);
    }

}
