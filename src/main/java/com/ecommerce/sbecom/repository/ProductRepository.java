package com.ecommerce.sbecom.repository;

import com.ecommerce.sbecom.entity.Category;
import com.ecommerce.sbecom.entity.Product;
import com.ecommerce.sbecom.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByProductNameLikeIgnoreCase(String query, Pageable pageable);

    Page<Product> findByUser(User user, Pageable pageable);

    Page<Product> findByCategory(Category category, Pageable pageable);
}
