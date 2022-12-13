package com.example.springsecurityapplication.controllers.user;

import com.example.springsecurityapplication.enumm.OrderStatus;
import com.example.springsecurityapplication.models.Cart;
import com.example.springsecurityapplication.models.Order;
import com.example.springsecurityapplication.models.Product;
import com.example.springsecurityapplication.repositories.CartRepository;
import com.example.springsecurityapplication.repositories.OrderRepository;
import com.example.springsecurityapplication.repositories.ProductRepository;
import com.example.springsecurityapplication.security.PersonDetails;
import com.example.springsecurityapplication.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class UserController {
    private final ProductService productService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    @Autowired
    public UserController(ProductService productService, CartRepository cartRepository, OrderRepository orderRepository, ProductRepository productRepository) {
        this.productService = productService;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
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

    //метод для формы поиска, в котором прописаны все варианты возможных сортировок и фильтров
    @PostMapping("/index/search")
    public String productSearch(@RequestParam("search") String search, @RequestParam("from") String from, @RequestParam("to") String to, @RequestParam(value = "sortPrice", required = false, defaultValue = "") String sortPrice, @RequestParam(value = "sortCat", required = false, defaultValue = "") String sortCat, Model model) {
        if(!from.isEmpty() & !to.isEmpty()) {
            if(!sortPrice.isEmpty()) {
                if(sortPrice.equals("sortedByAscendingPrice")) {
                    if(!sortCat.isEmpty()) {
                        if(sortCat.equals("pillow")) {
                            model.addAttribute("searchProduct", productRepository.findByTitleAndCategoryOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(from), Float.parseFloat(to), 1));
                        } else if(sortCat.equals("pillowCase")) {
                            model.addAttribute("searchProduct", productRepository.findByTitleAndCategoryOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(from), Float.parseFloat(to), 2));
                        }
                    }
                } else if(sortPrice.equals("sortedByDescendingPrice")) {
                    if(!sortCat.isEmpty()) {
                        if(sortCat.equals("pillow")) {
                            model.addAttribute("searchProduct", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(from), Float.parseFloat(to), 1));
                        } else if(sortCat.equals("pillowCase")) {
                            model.addAttribute("searchProduct", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(from), Float.parseFloat(to), 2));
                        }
                    }
                }
            } else {
                model.addAttribute("searchProduct", productRepository.findByTitleAndPriceGreaterThanEqualAAndPriceLessThanEqual(search.toLowerCase(), Float.parseFloat(from), Float.parseFloat(to)));
            }
        } else {
            model.addAttribute("searchProduct", productRepository.findByTitleContainingIgnoreCase(search.toLowerCase()));
        }
        //передаем значения из поиска в отдельные модели, чтобы после обновления страницы вызвать их в полях формы на html, также передаем модель со всеми товарами, они всегда отображаются на странице
        model.addAttribute("valueSearch", search);
        model.addAttribute("valuePriceFrom", from);
        model.addAttribute("valuePriceTo", to);
        model.addAttribute("products", productService.getAllProduct());
        return "/product/searchProduct";
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
        //вызываем метод для удаления товара по id товара
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
