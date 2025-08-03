package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.entity.User;
import com.ecommerce.sbecom.payload.ProductDTO;
import com.ecommerce.sbecom.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductDTO addProduct(ProductDTO productDTO);

    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String keyword, Long categoryId, Boolean createdByMe, User currentUser);

//    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy,
//                                   String sortOrder, ProductFilterDTO filterDTO, User currentUser);

    ProductDTO updateProduct(Long productId, ProductDTO product);

    ProductDTO deleteProduct(Long productId);

    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;

    ProductDTO getProductById(Long productId);

}