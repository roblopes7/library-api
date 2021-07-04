package com.udemy.curso.libraryapi.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udemy.curso.libraryapi.api.dto.BookDTO;
import com.udemy.curso.libraryapi.exceptions.BusinessException;
import com.udemy.curso.libraryapi.api.resource.BookController;
import com.udemy.curso.libraryapi.model.entity.Book;
import com.udemy.curso.libraryapi.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";
    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Adicionar um livro com sucesso.")
    public void createBookTest() throws Exception {

        BookDTO dto = createNewBook();
        Book savedBook = Book.builder().author("JK Rowling").isbn("001").title("Harry Potter").id(1L).build();
        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);
        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(dto.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(dto.getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(dto.getIsbn()));

    }

    @Test
    @DisplayName("Erro ao adicionar um livro sem todos os campos obrigatórios.")
    public void createInvalidBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new BookDTO());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar um livro com ISBN já utilizado.")
    public void createBookWithDuplicatedIsbn() throws Exception {
        BookDTO dto = createNewBook();
        String json = new ObjectMapper().writeValueAsString(dto);
        String mensagemErro = "ISBN já cadastrado.";
        BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException(mensagemErro));
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value(mensagemErro));
    }

    @Test
    @DisplayName("Deve obter informações de um livro.")
    public void getBookDetailTest() throws Exception {
        Long id = 1l;
        Book book = Book.builder().id(id).title("Dragoes de Eter").author("Raphael Draccon").isbn("007").build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(book.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(book.getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(book.getIsbn()));


    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir")
    public void bookNotFoundTest() {
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API + "/" + 1)
                .accept(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception {

        BDDMockito.given(service.getById(ArgumentMatchers.anyLong())).willReturn(Optional.of(Book.builder().id(1l).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API + "/" + 1)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());

    }

    @Test
    @DisplayName("Deve retornar not found quando não encontrar o livro para deletar")
    public void deleteNotFoundBookTest() throws Exception {

        BDDMockito.given(service.getById(ArgumentMatchers.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API + "/" + 1)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception {
        Long id = 1L;
        String json = new ObjectMapper().writeValueAsString(createNewBook());
        Book updatingBook = Book.builder()
                .id(id)
                .title("Viajantes do Abismo")
                .author("Nikelen Witter")
                .isbn("55555")
                .build();
        Book updateBook = Book.builder().id(id).isbn("001").title("Harry Potter").author("JK Rowling").build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));

        BDDMockito.given(service.update(updatingBook)).willReturn(updateBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API + "/" + 1)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        BookDTO book = createNewBook();

        mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(book.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(book.getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(001));

    }


    @Test
    @DisplayName("Deve tentar atualizar um livro inexistente")
    public void updateNonexistentBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(createNewBook());
        BDDMockito.given(service.getById(ArgumentMatchers.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API + "/" + 1)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar por autor e titulo")
    public void findBookByTest() throws Exception {
        Long id = 1L;
        BookDTO b1 = createNewBook();
        Book book = Book.builder().id(id).title(b1.getTitle()).author(b1.getAuthor()).isbn(b1.getIsbn()).build();

        BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

        String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString)).accept(MediaType.APPLICATION_JSON);
        mvc
                .perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageSize").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageNumber").value(0));
    }

    private BookDTO createNewBook() {
        return BookDTO.builder().isbn("001").title("Harry Potter").author("JK Rowling").build();
    }

}
