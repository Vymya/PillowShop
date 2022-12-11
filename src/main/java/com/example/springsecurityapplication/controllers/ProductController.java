package com.example.springsecurityapplication.controllers;

import com.example.springsecurityapplication.repositories.ProductRepository;
import com.example.springsecurityapplication.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    @Autowired
    public ProductController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @GetMapping("")
    public String getAllProduct(Model model) {
        model.addAttribute("products", productService.getAllProduct());
        return "product/product";
    }

    @GetMapping("/info/{id}")
    public String infoProduct(@PathVariable("id") int id, Model model) {
        model.addAttribute("product", productService.getProductId(id));
        return "product/infoProduct";
    }

    //метод для формы поиска, в котором прописаны все варианты возможных сортировок и фильтров
    @PostMapping("/search")
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
        return "/product/product";
    }
}

