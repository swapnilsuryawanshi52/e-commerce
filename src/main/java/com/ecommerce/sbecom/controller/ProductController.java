package com.ecommerce.sbecom.controller;

import com.ecommerce.sbecom.config.AppConstants;
import com.ecommerce.sbecom.entity.User;
import com.ecommerce.sbecom.payload.ProductDTO;
import com.ecommerce.sbecom.payload.ProductResponse;
import com.ecommerce.sbecom.service.ProductService;
import com.ecommerce.sbecom.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;
    private final AuthUtil authUtil;

    public ProductController(ProductService productService,
                             AuthUtil authUtil) {
        this.productService = productService;
        this.authUtil = authUtil;
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/products")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO){
        ProductDTO savedProductDTO = productService.addProduct(productDTO);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    @GetMapping("/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "createdByMe", required = false, defaultValue = "false") Boolean createdByMe,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        User currentUser = authUtil.loggedInUser();
        ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder, query, categoryId, createdByMe, currentUser);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        ProductDTO productDTO = productService.getProductById(productId);
        return ResponseEntity.ok(productDTO);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO,
                                                    @PathVariable Long productId){
        ProductDTO updatedProductDTO = productService.updateProduct(productId, productDTO);
        return ResponseEntity.ok(updatedProductDTO);
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId){
        productService.deleteProduct(productId);
        return ResponseEntity.ok("Product deleted successfully.");
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId,
                                                         @RequestParam("image")MultipartFile image) throws IOException {
        ProductDTO updatedProductDTO = productService.updateProductImage(productId, image);
        return ResponseEntity.ok(updatedProductDTO);
    }
}
