package net.foulest.athena.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConditionMessagePair {

    public boolean condition;
    public String message;
}
