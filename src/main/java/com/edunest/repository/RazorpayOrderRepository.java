package com.edunest.repository;

import com.edunest.entity.RazorpayOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RazorpayOrderRepository extends JpaRepository<RazorpayOrder, Integer> {

}
