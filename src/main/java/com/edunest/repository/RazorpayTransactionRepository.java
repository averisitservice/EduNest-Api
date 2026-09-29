package com.edunest.repository;

import com.edunest.entity.RazorpayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RazorpayTransactionRepository extends JpaRepository<RazorpayTransaction, Integer> {

}
