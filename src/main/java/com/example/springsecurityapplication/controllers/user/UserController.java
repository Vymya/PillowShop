package com.example.springsecurityapplication.controllers.user;

import com.example.springsecurityapplication.enumm.OrderStatus;
import com.example.springsecurityapplication.models.Cart;
import com.example.springsecurityapplication.models.Order;
import com.example.springsecurityapplication.models.Product;
import com.example.springsecurityapplication.repositories.CartRepository;
import com.example.springsecurityapplication.repositories.OrderRepository;
import com.example.springsecurityapplication.security.PersonDetails;
import com.example.springsecurityapplication.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class UserController {
    private final ProductService productService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public UserController(ProductService productService, CartRepository cartRepository, OrderRepository orderRepository) {
        this.productService = productService;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/index")
    public String index(Model model){
        // Получаем объект аутентификации - > с помощью Spring SecurityContextHolder обращаемся к контексту и на нем вызываем метод аутентификации.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Из потока для текущего пользователя мы получаем объект, который был положен в сессию после аутентификации
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        //и из объекта получаем роль пользователя данной сессии
        String role = personDetails.getPerson().getRole();
        if(role.equals("ROLE_ADMIN"))
            return "redirect:/admin";
        model.addAttribute("products", productService.getAllProduct());
        return "user/index";
    }

    @GetMapping("/cart/add/{id}")
    public String addProductInCart(@PathVariable("id") int id, Model model) {
        Product product = productService.getProductId(id);
        //аналогично примеру выше вытаскиваем из сессии не роль, а id пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        int idPerson = personDetails.getPerson().getId();
        //создаем объект корзины
        Cart cart = new Cart(idPerson, product.getId());
        //сохраняем объект корзины
        cartRepository.save(cart);
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        //аналогично примеру выше вытаскиваем из сессии id пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        int idPerson = personDetails.getPerson().getId();
        //получаем объект корзины этого пользователя
        List<Cart> cartList = cartRepository.findByPersonId(idPerson);
        //создаем массив для товаров, в него будут помещаться товары из объекта корзины
        List<Product> productsList = new ArrayList<>();
        //перебираем все товары в объекте корзины, помещаем эти товары в массив
        for (Cart cart: cartList) {
            productsList.add(productService.getProductId(cart.getProductId()));
        }
        model.addAttribute("cartProduct", productsList);
        //реализуем подсчет итоговой суммы товаров в корзине
        double priceCart = 0;
        for (Product product: productsList) {
            priceCart += product.getPrice();
        }
        model.addAttribute("priceCart", priceCart);
        return "user/cart";
    }

    @GetMapping("/cart/delete/{id}")
    public String deleteProductFromCart(Model model, @PathVariable("id") int id) {
        //вызываем метод для удаления товара по id пользователя
        cartRepository.deleteCartByProductId(id);
        return "redirect:/cart";
    }

    @GetMapping("/order/create")
    public String order() {
        //аналогично примерам выше вытаскиваем из сессии id пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        int idPerson = personDetails.getPerson().getId();
        //получаем объект корзины этого пользователя
        List<Cart> cartList = cartRepository.findByPersonId(idPerson);
        //создаем массив для товаров, в него будут помещаться товары из объекта корзины
        List<Product> productsList = new ArrayList<>();
        //перебираем все товары в объекте корзины, помещаем эти товары в массив
        for (Cart cart: cartList) {
            productsList.add(productService.getProductId(cart.getProductId()));
        }
        //реализуем подсчет итоговой суммы товаров в корзине
        double priceCart = 0;
        for (Product product: productsList) {
            priceCart += product.getPrice();
        }
        //генерируем номер заказа
        String uuid = UUID.randomUUID().toString();
        //создаем объекты заказов через цикл (каждый продукт - объект заказа), count ставим 1, это заглушка, его лучше реализовать динамически на js. Также после сохранения товара в объекте заказа мы удаляем этот товар из корзины
        for (Product product: productsList) {
            Order newOrder = new Order(uuid, product, personDetails.getPerson(), 1, product.getPrice(), OrderStatus.Оформлен);
            orderRepository.save(newOrder);
            cartRepository.deleteCartByProductId(product.getId());
        }
        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String ordersUser(Model model) {
        //получаем объект пользователя из сесссии
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        //создаем массив с заказами пользователя
        List<Order> orderList = orderRepository.findByPerson(personDetails.getPerson());
        model.addAttribute("orders", orderList);
        return "/user/orders";
    }
}
