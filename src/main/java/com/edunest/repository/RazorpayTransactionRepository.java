package com.edunest.repository;

import com.edunest.entity.RazorpayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RazorpayTransactionRepository extends JpaRepository<RazorpayTransaction, Integer> {

    List<RazorpayTransaction> findByRazorpayOrderId(Integer razorpayOrderId);
}
