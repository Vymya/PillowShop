package com.example.springsecurityapplication.repositories;

import com.example.springsecurityapplication.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    //метод для поиска без учета регистра по тайтлу (встроенный в модуль)
    List<Product> findByTitleContainingIgnoreCase(String name);

    //кастомный метод для поиска по названию и ценовому диапазону
    //(lower(title) - переводим введенное пользователем значение в нижний регистр
    //like %?1% - проверка, что введенное значение будет находиться всередине тайтла товара в массиве товаров, %?1% - проценты по бокам значат, что по бокам могут быть другие символы
    //like '?1%' - проверка на то, что введенное значение будет находиться вначале, а доп символы могут быть в конце существующего тайтла
    //like '%?1' - аналогично доп символы в начале
    //price >= ?2 and price <= ?3 - проверка для вывода диапазона цен (цифры - порядковые номера аргументов в методе: title from to)
    @Query(value = "select * from product where ((lower(title) like %?1%) or (lower(title) like '?1%') or (lower(title) like '%?1')) and (price >= ?2 and price <= ?3)", nativeQuery = true)
    List<Product> findByTitleAndPriceGreaterThanEqualAAndPriceLessThanEqual(String title, double from, double to);

    //кастомный метод для поиска по названию и ценовому диапазону с сортировкой по убыванию цены
    @Query(value = "select * from product where ((lower(title) like %?1%) or (lower(title) like '?1%') or (lower(title) like '%?1')) and (price >= ?2 and price <= ?3) order by price desc", nativeQuery = true)
    List<Product> findByTitleOrderByPriceAsc(String title, double from, double to);

    //кастомный метод для поиска по названию и ценовому диапазону и фильтрации по категории товаров, сортировка по возрастанию цены
    @Query(value = "select * from product where category_id=?4 and ((lower(title) like %?1%) or (lower(title) like '?1%') or (lower(title) like '%?1')) and (price >= ?2 and price <= ?3) order by price", nativeQuery = true)
    List<Product> findByTitleAndCategoryOrderByPriceAsc(String title, double from, double to, int category);

    //кастомный метод для поиска по названию и ценовому диапазону и фильтрации по категории товаров, сортировка по убыванию цены
    @Query(value = "select * from product where category_id=?4 and ((lower(title) like %?1%) or (lower(title) like '?1%') or (lower(title) like '%?1')) and (price >= ?2 and price <= ?3) order by price desc", nativeQuery = true)
    List<Product> findByTitleAndCategoryOrderByPriceDesc(String title, double from, double to, int category);
}