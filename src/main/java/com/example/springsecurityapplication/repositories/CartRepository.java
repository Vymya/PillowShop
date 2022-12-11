package com.example.springsecurityapplication.repositories;

import com.example.springsecurityapplication.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface CartRepository extends JpaRepository<Cart, Integer> {
    //метод для получения корзины (массив с товарами) по id пользователя
    List<Cart> findByPersonId(int id);
    //метод для удаления товара из корзины по id пользователя
    void deleteCartByProductId(int id);
}
