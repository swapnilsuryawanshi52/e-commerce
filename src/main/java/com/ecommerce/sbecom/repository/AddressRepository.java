package com.ecommerce.sbecom.repository;

import com.ecommerce.sbecom.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
