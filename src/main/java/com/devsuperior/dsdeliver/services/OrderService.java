package com.devsuperior.dsdeliver.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.devsuperior.dsdeliver.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dsdeliver.dto.OrderDTO;
import com.devsuperior.dsdeliver.dto.ProductDTO;
import com.devsuperior.dsdeliver.entities.Order;
import com.devsuperior.dsdeliver.enums.OrderStatus;
import com.devsuperior.dsdeliver.entities.Product;
import com.devsuperior.dsdeliver.repositories.OrderRepository;
import com.devsuperior.dsdeliver.repositories.ProductRepository;

import javax.persistence.EntityNotFoundException;

@Service
public class OrderService {

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private ProductRepository productRepository;

	@Transactional(readOnly = true)
	public List<OrderDTO> findAll(){
		List<Order> list = orderRepository.findOrdersWithProducts();
		return list.stream().map(OrderDTO::new).collect(Collectors.toList());
	}
	
	@Transactional
	public OrderDTO insert(OrderDTO dto){
		Order order = new Order(null, dto.getAddress(), dto.getLatitude(),
				dto.getLongitude(), Instant.now(), OrderStatus.PENDING);

		for (ProductDTO p : dto.getProducts()) {
			try {
				Product product = productRepository.getOne(p.getId());
				order.getProducts().add(product);
			}
			catch (EntityNotFoundException e){
				throw new ResourceNotFoundException("Product id: " + p.getId() + " not found");
			}
		}

		order = orderRepository.save(order);
		return new OrderDTO(order);
	}

	@Transactional
	public OrderDTO setDelivered(Long id) {
		Optional<Order> opt = orderRepository.findById(id);
		opt.orElseThrow(() -> new ResourceNotFoundException("Order id: " + id + " not found"));
		opt.get().setStatus(OrderStatus.DELIVERED);
		Order order = orderRepository.save(opt.get());
		return new OrderDTO(order);
	}
}
