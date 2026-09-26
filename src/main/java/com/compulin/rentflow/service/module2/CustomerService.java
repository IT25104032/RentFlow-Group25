package com.compulin.rentflow.service.module2;

import com.compulin.rentflow.dto.module2.CustomerRequest;
import com.compulin.rentflow.dto.module2.CustomerResponse;
import com.compulin.rentflow.entity.module2.Customer;
import com.compulin.rentflow.repository.module2.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CustomerRequest request) {

        Customer customer = new Customer();

        customer.setCompanyId(request.getCompanyId());
        customer.setCustomerName(request.getCustomerName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCustomerType(request.getCustomerType());
        customer.setCustomerStatus(request.getCustomerStatus());
        customer.setCreatedBy(request.getCreatedBy());

        Customer savedCustomer = customerRepository.save(customer);

        CustomerResponse response = new CustomerResponse();

        response.setCustomerId(savedCustomer.getCustomerId());
        response.setCompanyId(savedCustomer.getCompanyId());
        response.setCustomerName(savedCustomer.getCustomerName());
        response.setEmail(savedCustomer.getEmail());
        response.setPhone(savedCustomer.getPhone());
        response.setAddress(savedCustomer.getAddress());
        response.setCustomerType(savedCustomer.getCustomerType());
        response.setCustomerStatus(savedCustomer.getCustomerStatus());
        response.setCreatedBy(savedCustomer.getCreatedBy());

        if (savedCustomer.getCreatedAt() != null) {
            response.setCreatedAt(savedCustomer.getCreatedAt().toString());
        }

        return response;
    }

    public CustomerResponse getCustomerById(
            Integer customerId,
            Integer companyId) {

        Optional<Customer> optionalCustomer =
                customerRepository.findByCustomerIdAndCompanyId(
                        customerId,
                        companyId
                );

        if (optionalCustomer.isEmpty()) {
            return null;
        }

        Customer customer = optionalCustomer.get();

        CustomerResponse response = new CustomerResponse();

        response.setCustomerId(customer.getCustomerId());
        response.setCompanyId(customer.getCompanyId());
        response.setCustomerName(customer.getCustomerName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setAddress(customer.getAddress());
        response.setCustomerType(customer.getCustomerType());
        response.setCustomerStatus(customer.getCustomerStatus());
        response.setCreatedBy(customer.getCreatedBy());

        if (customer.getCreatedAt() != null) {
            response.setCreatedAt(customer.getCreatedAt().toString());
        }

        return response;
    }
}