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

package me.ahoo.eventbus.spring.support;

import me.ahoo.eventbus.core.EventBusException;
import me.ahoo.eventbus.core.consistency.ConsistencyPublisher;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * PublishAnnotationAspect.
 *
 * @author : ahoo wang
 */
@Aspect
public class PublishAnnotationAspect {
    
    public static final String BEAN_NAME = PublishAnnotationAspect.class.getName();
    private final ConsistencyPublisher consistencyPublisher;
    
    public PublishAnnotationAspect(ConsistencyPublisher consistencyPublisher) {
        this.consistencyPublisher = consistencyPublisher;
    }
    
    @Pointcut(value = "@annotation(me.ahoo.eventbus.core.annotation.Publish)")
    public void publish() {
    
    }
    
    
    @Around("publish()")
    public Object publishConsistency(ProceedingJoinPoint proceedingJoinPoint) {
        return consistencyPublisher.publish(() -> {
            try {
                return proceedingJoinPoint.proceed();
            } catch (Throwable throwable) {
                throw new EventBusException(throwable.getMessage(), throwable);
            }
        });
    }
    
}
