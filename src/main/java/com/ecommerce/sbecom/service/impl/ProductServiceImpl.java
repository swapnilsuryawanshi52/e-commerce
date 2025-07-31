package com.ecommerce.sbecom.service.impl;

import com.ecommerce.sbecom.entity.*;
import com.ecommerce.sbecom.exception.APIException;
import com.ecommerce.sbecom.exception.ResourceNotFoundException;
import com.ecommerce.sbecom.payload.ProductDTO;
import com.ecommerce.sbecom.payload.ProductResponse;
import com.ecommerce.sbecom.repository.CartRepository;
import com.ecommerce.sbecom.repository.CategoryRepository;
import com.ecommerce.sbecom.repository.ProductRepository;
import com.ecommerce.sbecom.service.CartService;
import com.ecommerce.sbecom.service.FileService;
import com.ecommerce.sbecom.service.ProductService;
import com.ecommerce.sbecom.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final CartRepository cartRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;
    private final AuthUtil authUtil;

    public ProductServiceImpl(CartRepository cartRepository,
                              CartService cartService,
                              ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ModelMapper modelMapper,
                              FileService fileService,
                              AuthUtil authUtil) {
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
        this.authUtil = authUtil;
    }

    @Value("${project.image}")
    private String path;

    private Product getProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
    }

    private double computeSpecialPrice(double price, double discount) {
        return price * (1 - discount / 100.0);
    }

    private ProductDTO mapToDTO(Product product) {
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO addProduct(ProductDTO productDTO) {
        Category category = getCategoryOrThrow(productDTO.getCategoryId());
        User currentUser = authUtil.loggedInUser();

        if(!currentUser.hasRole(AppRole.ROLE_SELLER))
            throw new APIException("Only sellers can add products.");

        boolean alreadyExists = category.getProducts().stream()
                .anyMatch(p -> p.getProductName().equalsIgnoreCase(productDTO.getProductName()));

        if (alreadyExists) {
            throw new APIException("Product already exists!");
        }

        Product product = modelMapper.map(productDTO, Product.class);
        product.setCategory(category);
        product.setUser(currentUser);
        product.setImage("default.png");

        product.setSpecialPrice(computeSpecialPrice(product.getPrice(), product.getDiscount()));

        Product updatedProduct = productRepository.save(product);

        return mapToDTO(updatedProduct);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy,
                                          String sortOrder, String query, Long categoryId,
                                          Boolean createdByMe, User currentUser) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                "asc".equalsIgnoreCase(sortOrder) ? Sort.by(sortBy).ascending()
                                                               : Sort.by(sortBy).descending());

        Page<Product> page;

        if (Boolean.TRUE.equals(createdByMe) && currentUser.hasRole(AppRole.ROLE_SELLER)) {
            page = productRepository.findByUser(currentUser, pageable);
        } else if (query != null && !query.isBlank()) {
            page = productRepository.findByProductNameLikeIgnoreCase("%" + query + "%", pageable);
        } else if (categoryId != null) {
            page = productRepository.findByCategory(getCategoryOrThrow(categoryId), pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        List<ProductDTO> dtoList = page.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        return new ProductResponse(dtoList, pageNumber, pageSize,
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Override
    public ProductDTO getProductById(Long productId) {
        return mapToDTO(getProductOrThrow(productId));
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product productFromDb = getProductOrThrow(productId);

        productFromDb.setProductName(productDTO.getProductName());
        productFromDb.setDescription(productDTO.getDescription());
        productFromDb.setQuantity(productDTO.getQuantity());
        productFromDb.setDiscount(productDTO.getDiscount());
        productFromDb.setPrice(productDTO.getPrice());
        productFromDb.setSpecialPrice(computeSpecialPrice(productDTO.getPrice(), productDTO.getDiscount()));

        Product savedProduct = productRepository.save(productFromDb);

        cartRepository.findCartsByProductId(productId).forEach(cart ->
                cartService.updateProductInCarts(cart.getCartId(), productId));

        return mapToDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO deleteProduct(Long productId) {
        Product productFromDb = getProductOrThrow(productId);

        cartRepository.findCartsByProductId(productId).forEach(cart ->
            cartService.deleteProductFromCart(cart.getCartId(), productId)
        );

        productRepository.delete(productFromDb);
        return modelMapper.map(productFromDb, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product productFromDb = getProductOrThrow(productId);

        String fileName = fileService.uploadImage(path, image);
        productFromDb.setImage(fileName);

        Product updatedProduct = productRepository.save(productFromDb);
        return mapToDTO(updatedProduct);
    }
}