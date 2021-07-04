package com.udemy.curso.libraryapi.service;

import com.udemy.curso.libraryapi.api.dto.LoanFilterDTO;
import com.udemy.curso.libraryapi.exceptions.BusinessException;
import com.udemy.curso.libraryapi.model.entity.Loan;
import com.udemy.curso.libraryapi.model.entity.Book;
import com.udemy.curso.libraryapi.model.repositories.LoanRepository;
import com.udemy.curso.libraryapi.service.imp.LoanServiceImp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {



    LoanService service;

    @MockBean
    LoanRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new LoanServiceImp(repository);
    }


    @Test
    @DisplayName("Deve salvar um empréstimo")
    public void saveLoanTest(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        Loan savingLoan =
                Loan.builder()
                        .book(book)
                        .customer(customer)
                        .loanDate(LocalDate.now())
                        .build();

        Loan savedLoan = Loan.builder()
                .id(1l)
                .loanDate(LocalDate.now())
                .customer(customer)
                .book(book).build();


        Mockito.when( repository.existsByBookAndNotReturned(book) ).thenReturn(false);
        Mockito.when( repository.save(savingLoan) ).thenReturn( savedLoan );

        Loan loan = service.save(savingLoan);

        Assertions.assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        Assertions.assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        Assertions.assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        Assertions.assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar um empréstimo com livro já emprestado")
    public void loanedBookSaveTest(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        Loan savingLoan =
                Loan.builder()
                        .book(book)
                        .customer(customer)
                        .loanDate(LocalDate.now())
                        .build();

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(savingLoan));

        Assertions.assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        Mockito.verify(repository, Mockito.never()).save(savingLoan);
    }

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo ID")
    public void getLoanDetailsTest(){
        Long id = 1l;
        Loan loan = createLoanExample();
        loan.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> result = service.getById(id);
        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get().getId()).isEqualTo(id);
        Assertions.assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        Assertions.assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        Assertions.assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        Mockito.verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoanTest(){
        Loan loan = createLoanExample();
        loan.setId(1l);
        loan.setReturned(true);

        Mockito.when(repository.save(loan)).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        Assertions.assertThat(updatedLoan.isReturned()).isTrue();
        Mockito.verify(repository).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades")
    public void findLoanTest(){

        //cenario
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();

        Loan loan = createLoanExample();
        loan.setId(1l);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> lista = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, lista.size());
        Mockito.when( repository.findByBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(PageRequest.class))
        )
                .thenReturn(page);

        //execucao
        Page<Loan> result = service.find( loanFilterDTO, pageRequest );


        //verificacoes
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
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
