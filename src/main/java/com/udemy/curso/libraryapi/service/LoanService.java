package com.udemy.curso.libraryapi.service;

import com.udemy.curso.libraryapi.api.dto.LoanFilterDTO;
import com.udemy.curso.libraryapi.model.entity.Book;
import com.udemy.curso.libraryapi.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
<<<<<<< HEAD
import org.springframework.stereotype.Component;

import java.util.List;
=======

>>>>>>> 68a74cf36bb12bc51ab2d3ccd0ee906320bab959
import java.util.Optional;

public interface LoanService {
    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable);
<<<<<<< HEAD

    Page<Loan> getLoansByBook(Book book, Pageable pageable);

    List<Loan> getAllLateLoans();
=======
>>>>>>> 68a74cf36bb12bc51ab2d3ccd0ee906320bab959
}
