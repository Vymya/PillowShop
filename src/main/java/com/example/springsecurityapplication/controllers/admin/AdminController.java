package com.example.springsecurityapplication.controllers.admin;

import com.example.springsecurityapplication.enumm.OrderStatus;
import com.example.springsecurityapplication.models.Image;
import com.example.springsecurityapplication.models.Order;
import com.example.springsecurityapplication.models.Product;
import com.example.springsecurityapplication.repositories.CategoryRepository;
import com.example.springsecurityapplication.repositories.OrderRepository;
import com.example.springsecurityapplication.services.OrderService;
import com.example.springsecurityapplication.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
//@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;

    //обращение к репозиторию категорий, но вообще надо было делать сервис, как для товаров выше
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public AdminController(ProductService productService, OrderService orderService, CategoryRepository categoryRepository, OrderRepository orderRepository) {
        this.productService = productService;
        this.orderService = orderService;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
    }

    //    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    //метод открытия страницы администратора и вывод на ней всех товаров
    @GetMapping("")
    public String admin(Model model){
        model.addAttribute("products", productService.getAllProduct());
        List<Order> orderList = orderRepository.findAll();
        model.addAttribute("orders", orderList);
        return "admin/admin";
    }

    //метод по отображению страницы с формой добавления товаров, еще добавлен репозиторий с категорями, которые будут в выпадающем списке
    @GetMapping("/product/add")
    public String addProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("category", categoryRepository.findAll());
        return "product/addProduct";
    }

    //метод по добавлению объекта товара в БД
    @PostMapping("/product/add")
    public String addProduct(@ModelAttribute("product") @Valid Product product, BindingResult bindingResult, @RequestParam("fileOne")MultipartFile fileOne, @RequestParam("fileTwo")MultipartFile fileTwo, @RequestParam("fileThree")MultipartFile fileThree, @RequestParam("fileFour")MultipartFile fileFour, @RequestParam("fileFive")MultipartFile fileFive) throws IOException {
        if(bindingResult.hasErrors())
            return "product/addProduct";
        if(fileOne != null) {
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists())
                uploadDir.mkdir();
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + fileOne.getOriginalFilename();
            fileOne.transferTo(new File(uploadPath + "/" + resultFileName));
            Image image = new Image();
            image.setProduct(product);
            image.setFileName(resultFileName);
            product.addImageToProduct(image);
        }
        if(fileTwo != null) {
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists())
                uploadDir.mkdir();
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + fileTwo.getOriginalFilename();
            fileTwo.transferTo(new File(uploadPath + "/" + resultFileName));
            Image image = new Image();
            image.setProduct(product);
            image.setFileName(resultFileName);
            product.addImageToProduct(image);
        }
        if(fileThree != null) {
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists())
                uploadDir.mkdir();
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + fileThree.getOriginalFilename();
            fileThree.transferTo(new File(uploadPath + "/" + resultFileName));
            Image image = new Image();
            image.setProduct(product);
            image.setFileName(resultFileName);
            product.addImageToProduct(image);
        }
        if(fileFour != null) {
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists())
                uploadDir.mkdir();
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + fileFour.getOriginalFilename();
            fileFour.transferTo(new File(uploadPath + "/" + resultFileName));
            Image image = new Image();
            image.setProduct(product);
            image.setFileName(resultFileName);
            product.addImageToProduct(image);
        }
        if(fileFive != null) {
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists())
                uploadDir.mkdir();
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + fileFive.getOriginalFilename();
            fileFive.transferTo(new File(uploadPath + "/" + resultFileName));
            Image image = new Image();
            image.setProduct(product);
            image.setFileName(resultFileName);
            product.addImageToProduct(image);
        }
        productService.saveProduct(product);
        return "redirect:/admin";
    }

    //метод удаления товара из БД
    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") int id) {
        productService.deleteProduct(id);
        return "redirect:/admin";
    }

    //метод по отображению страницы с формой редактирования товаров
    @GetMapping("/product/edit/{id}")
    public String editProduct(Model model, @PathVariable("id") int id) {
        model.addAttribute("product", productService.getProductId(id));
        model.addAttribute("category", categoryRepository.findAll());
        return "product/editProduct";
    }

    //метод по отправке отредактированного товара в БД
    @PostMapping("/product/edit/{id}")
    public String editProduct(@ModelAttribute("product") @Valid Product product, @PathVariable("id") int id, BindingResult bindingResult) {
        if(bindingResult.hasErrors())
            return "product/editProduct";
        productService.updateProduct(id, product);
        return "redirect:/admin";
    }

    //метод по отображению страницы с формой редактирования заказа
    @GetMapping("/order/edit/{id}")
    public String editOrder(Model model, @PathVariable("id") int id) {
        model.addAttribute("order", orderService.getOrderId(id));
        model.addAttribute("orderStatus", OrderStatus.values());
        return "admin/editOrder";
    }

    //метод по отправке отредактированного заказа в БД
    @PostMapping("/order/edit/{id}")
    public String editOrder(@ModelAttribute("order") @Valid Order order, @PathVariable("id") int id, BindingResult bindingResult) {
        if(bindingResult.hasErrors())
            return "admin/admin";
        orderService.updateOrder(id, order);
        return "redirect:/admin";
    }

    //страница с заказами и поиском по ним
    @GetMapping("/order/search")
    public String orderSearch(Model model) {
        model.addAttribute("orders", orderService.getAllOrder());
        return "/admin/searchOrder";
    }

    @PostMapping("/order/search")
    public String orderSearch(@RequestParam("search") String search, Model model) {
        model.addAttribute("searchOrder", orderRepository.findByNumberContains(search.toLowerCase()));
        //передаем значения из поиска в отдельные модели, чтобы после обновления страницы вызвать их в полях формы на html
        model.addAttribute("orderNumberSearch", search);
        model.addAttribute("orders", orderService.getAllOrder());
        return "/admin/searchOrder";
    }

    @Value("${upload.path}")
    private String uploadPath;


}
