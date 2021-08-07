package com.roze.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.roze.model.MyOrder;

public interface MyOrderRepository extends JpaRepository<MyOrder, Long>{

	
	public MyOrder findByOrderId(String orderId);
}
