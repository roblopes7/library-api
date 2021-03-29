package com.udemy.curso.libraryapi.service;

import com.udemy.curso.libraryapi.exceptions.BusinessException;
import com.udemy.curso.libraryapi.model.entity.Book;
import com.udemy.curso.libraryapi.model.repositories.BookRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImp(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        Book book = createValidBook();
        Mockito.when(repository.save(book)).thenReturn(Book.builder().id(1L).title("Sombra do vento").author("Zafon").isbn("002").build());
        Book savedBook = service.save(book);
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("002");
        assertThat(savedBook.getAuthor()).isEqualTo("Zafon");
        assertThat(savedBook.getTitle()).isEqualTo("Sombra do vento");
    }

    private Book createValidBook() {
        return Book.builder().author("Zafon").title("Sombra do vento").isbn("002").build();
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com isbn duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN(){
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
        Throwable throwable = Assertions.catchThrowable(() ->  service.save(book));
        assertThat(throwable).isInstanceOf(BusinessException.class).hasMessage("ISBN já cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }
}
