package com.anazcom.labingddd.lending.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anazcom.labingddd.lending.domain.LoanStatus;

interface SpringDataLoanRepository extends JpaRepository<LoanJpaEntity, String> {

    boolean existsByBookIdAndStatus(String bookId, LoanStatus status);
}
