package com.udemy.curso.libraryapi.service;

import com.udemy.curso.libraryapi.exceptions.BusinessException;
import com.udemy.curso.libraryapi.model.entity.Book;
import com.udemy.curso.libraryapi.model.repositories.BookRepository;
import org.springframework.stereotype.Service;


@Service
public class BookServiceImp implements BookService {


    final BookRepository repository;

    public BookServiceImp(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if(repository.existsByIsbn(book.getIsbn())){
            throw new BusinessException("ISBN já cadastrado.");
        }
        return repository.save(book);
    }
}
