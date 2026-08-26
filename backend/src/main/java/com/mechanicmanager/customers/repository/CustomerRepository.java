package com.mechanicmanager.customers.repository;

import com.mechanicmanager.customers.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends
        JpaRepository<Customer, Long>,
        JpaSpecificationExecutor<Customer> {

    List<Customer> findAllByWorkshop_IdOrderByLastNameAscFirstNameAsc(Long workshopId);

    Optional<Customer> findByIdAndWorkshop_Id(Long id, Long workshopId);

}
