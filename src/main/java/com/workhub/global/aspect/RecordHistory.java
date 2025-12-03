package com.workhub.global.aspect;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordHistory {
    HistoryType type();
    ActionType action(); // CREATE, UPDATE 등..
}
