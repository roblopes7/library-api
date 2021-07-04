package com.udemy.curso.libraryapi.model.repositories;

import com.udemy.curso.libraryapi.model.entity.Loan;
import com.udemy.curso.libraryapi.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan,Long > {

    @Query(value ="SELECT CASE WHEN (count(l.id) > 0) THEN true else false end " +
            "FROM Loan l where l.book = :book AND l.returned is not true")
    boolean existsByBookAndNotReturned(@Param("book") Book book);

    @Query(value = "SELECT l FROM Loan l JOIN l.book AS b where b.isbn = :isbn OR l.customer = :customer")
    Page<Loan> findByBookIsbnOrCustomer(@Param("isbn") String isbn, @Param("customer") String customer, Pageable pageRequest);
}