package com.udemy.curso.libraryapi.service.imp;

import com.udemy.curso.libraryapi.api.dto.LoanFilterDTO;
import com.udemy.curso.libraryapi.exceptions.BusinessException;
import com.udemy.curso.libraryapi.model.entity.Loan;
import com.udemy.curso.libraryapi.model.repositories.LoanRepository;
import com.udemy.curso.libraryapi.service.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoanServiceImp implements LoanService {

    private LoanRepository repository;

    public LoanServiceImp(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        if(repository.existsByBookAndNotReturned(loan.getBook())){
            throw new BusinessException("Book already loaned");
        }
        return repository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return repository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable) {
        return repository.findByBookIsbnOrCustomer(filterDTO.getIsbn(), filterDTO.getCustomer(), pageable);
    }
}
