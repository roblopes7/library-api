package com.udemy.curso.libraryapi.repositories;

import com.udemy.curso.libraryapi.model.entity.Book;
import com.udemy.curso.libraryapi.model.repositories.BookRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com isbn informado")
    public void returnTrueWhenIsbnExists(){
        String isbn = "123";
        Book book = getBook(isbn);
        entityManager.persist(book);

        boolean exists = repository.existsByIsbn(isbn);

        Assertions.assertThat(exists).isTrue();

    }

    private Book getBook(String isbn) {
        return Book.builder().author("José Mauro de Vasconscelos").title("Meu pé de laranja Lima").isbn(isbn).build();
    }

    @Test
    @DisplayName("Deve retornar falso quando existir um livro na base com isbn informado")
    public void returnFalseWhenIsbnDoesntExists(){
        String isbn = "123";

        boolean exists = repository.existsByIsbn(isbn);

        Assertions.assertThat(exists).isFalse();

    }

    @Test
    @DisplayName("Deve obter um livro por ID")
    public void findByIdTEst(){
        Book book = getBook("123");
        entityManager.persist(book);
        Optional<Book> foundBook = repository.findById(book.getId());
        Assertions.assertThat(foundBook.isPresent()).isTrue();

    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        Book book = getBook("123");
        Book savedBook = repository.save(book);

        Assertions.assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest(){
        String isbn = "123";
        Book book = getBook(isbn);
        entityManager.persist(book);

        Book foundedBook = entityManager.find(Book.class, book.getId());
        repository.delete(foundedBook);
        Book deletedBook = entityManager.find(Book.class, book.getId());
        Assertions.assertThat(deletedBook).isNull();
    }
}
