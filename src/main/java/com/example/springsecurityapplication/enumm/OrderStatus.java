package com.example.springsecurityapplication.enumm;

import java.util.HashMap;
import java.util.Map;

public enum OrderStatus {
    //лучше под эти статусы создать таблицу в БД, т.к. в enum элементы могут содержать только одно слово
    Принят,
    Оформлен,
    Оплачен,
    Едет,
    Ожидает,
    Получен;

    public int getValue() {
        return ordinal() + 1;
    }
}