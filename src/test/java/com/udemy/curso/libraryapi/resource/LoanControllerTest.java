package com.udemy.curso.libraryapi.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udemy.curso.libraryapi.api.dto.LoanDTO;
import com.udemy.curso.libraryapi.api.dto.LoanFilterDTO;
import com.udemy.curso.libraryapi.api.dto.ReturnedLoanDTO;
import com.udemy.curso.libraryapi.exceptions.BusinessException;
import com.udemy.curso.libraryapi.api.resource.LoanController;
import com.udemy.curso.libraryapi.model.entity.Loan;
import com.udemy.curso.libraryapi.model.entity.Book;
import com.udemy.curso.libraryapi.service.BookService;
import com.udemy.curso.libraryapi.service.LoanService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService bookService;

    @MockBean
    LoanService loanService;


    @Test
    @DisplayName("Deve realizar um empréstimo")
    public void createLoanTest() throws Exception {
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("Robson Lopes").build() ;
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1l).isbn("123").build();
        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book) );
        Loan loan = Loan.builder().id(1l).customer("Robson Lopes").book(book).loanDate(LocalDate.now()).build();
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("1"));
    }

    @Test
    @DisplayName("Deve retornar erro por livro inexistente")
    public void invalidIsbnCreateLoanTest() throws Exception {
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("Robson Lopes").build() ;
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Book not found for  passed isbn"));
    }

    @Test
    @DisplayName("Deve retornar erro por livro já emprestado")
    public void loanedBookErrorOnCreateLoanTest() throws Exception {
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("Robson Lopes").build() ;
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1l).isbn("123").build();
        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book) );

        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willThrow(new BusinessException("Book already loaned"));


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Book already loaned"));
    }

    @Test
    @DisplayName("Deve retornar um livro")
    public void returnBookTest() throws Exception {
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();

        String json = new ObjectMapper().writeValueAsString(dto);
        Loan loan = Loan.builder().id(1l).build();
        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));

        mvc.perform(
                MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(MockMvcResultMatchers.status().isOk()
        );

        Mockito.verify(loanService, Mockito.times(1)).update(loan);
    }

    @Test
    @DisplayName("Deve retornar um 404 quando tentar devolver um livro inexistente.")
    public void returnInexistentBookTest() throws Exception {
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();

        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        mvc.perform(
                MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(MockMvcResultMatchers.status().isNotFound()
        );
    }

    @Test
    @DisplayName("Deve filtrar empréstimos")
    public void findLoansTest() throws Exception {
        Long id = 1L;
        Loan loan = createLoanExample();
        loan.setId(id);

        loan.setBook(Book.builder().id(1l).isbn("1234").build());

        BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 10), 1));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10", loan.getBook().getIsbn(), loan.getCustomer());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(LOAN_API.concat(queryString)).accept(MediaType.APPLICATION_JSON);
        mvc
                .perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageSize").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageNumber").value(0));
    }

    public Loan createLoanExample() {
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        return
                Loan.builder()
                        .book(book)
                        .customer(customer)
                        .loanDate(LocalDate.now())
                        .build();
    }

}
