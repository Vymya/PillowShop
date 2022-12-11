package com.example.springsecurityapplication.repositories;

import com.example.springsecurityapplication.models.Order;
import com.example.springsecurityapplication.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    //метод по получению массива с заказом пользователя, в котором товары
    List<Order> findByPerson(Person person);

    //скорее всего нигде не используется, хз
    Order findByNumber(String number);

    //метод по поиску заказа по последним 4 символам
    @Query(value = "select * from orders where (lower(number) like %?1)", nativeQuery = true)
    List<Order> findByNumberContains(String number);
}