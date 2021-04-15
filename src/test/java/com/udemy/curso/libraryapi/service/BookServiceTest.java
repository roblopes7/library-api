package com.udemy.curso.libraryapi.service;

import com.udemy.curso.libraryapi.api.exceptions.BusinessException;
import com.udemy.curso.libraryapi.model.entity.Book;
import com.udemy.curso.libraryapi.model.repositories.BookRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImp(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {
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
    public void shouldNotSaveABookWithDuplicatedISBN() {
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
        Throwable throwable = Assertions.catchThrowable(() -> service.save(book));
        assertThat(throwable).isInstanceOf(BusinessException.class).hasMessage("ISBN já cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void getByIdTest() {
        Long id = 1l;
        Book book = createValidBook();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> foundBook = service.getById(id);
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Não deve achar um livro por id")
    public void notFoundBookByIdTest() {
        Long id = 1l;
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> foundBook = service.getById(id);
        assertThat(foundBook.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() {
        Book book = createValidBook();
        book.setId(1L);
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));
        Mockito.verify(repository, Mockito.times(1)).delete(book);

    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente.")
    public void deleteInvalidBookTest() {
        Book book = new Book();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));
        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar editar um livro inexistente.")
    public void updateInvalidBookTest() {
        Book book = new Book();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest(){
        Book updatingBook = Book.builder().id(1L).build();

        Book updatedBook = createValidBook();
        updatedBook.setId(1L);
        Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);

        Book book = service.update(updatingBook);
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest(){
        Book book = createValidBook();
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> lista = Collections.singletonList(book);
        Page<Book> page = new PageImpl<Book>(Arrays.asList(book), pageRequest, 1);
        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);
        Page<Book> result = service.find(book, pageRequest);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }
}
