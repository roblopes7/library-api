package com.udemy.curso.libraryapi.repositories;

import com.udemy.curso.libraryapi.model.entity.Book;
import com.udemy.curso.libraryapi.model.entity.Loan;
import com.udemy.curso.libraryapi.model.repositories.LoanRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository repository;

    @Test
    @DisplayName("deve verificar se existe empréstimo não devolvido para o livro")
    public void existsByBookAndNotReturnedBook(){

        Book book = getBook("5552");
        entityManager.persist(book);

        Loan loan = Loan.builder().loanDate(LocalDate.now()).book(book).customer("Fulano").build();
        entityManager.persist(loan);

        boolean exists = repository.existsByBookAndNotReturned(book);
        Assertions.assertThat(exists).isTrue();
    }

    private Book getBook(String isbn) {
        return Book.builder().author("José Mauro de Vasconscelos").title("Meu pé de laranja Lima").isbn(isbn).build();
    }

    @Test
    @DisplayName("Deve buscar um empréstimo pelo isbn do livro ou customer")
    public void findByBookIsbnOrCustomer() {
        Book book = getBook("321");
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).loanDate(LocalDate.now()).build();
        entityManager.persist(loan);

        Page<Loan> result = repository.findByBookIsbnOrCustomer("321", "Fulano", PageRequest.of(0,10));

        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent()).contains(loan);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);


    }

    @Test
    @DisplayName("Deve obter empréstimos cujo a data de empréstimo for menor ou igual a 3 dias atrás e não retornados")
    public void findByLoanDateLessThanAndNotReturnedTest(){
        Loan loan = createAndPersistLoan( LocalDate.now().minusDays(5) );

        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertThat(result).hasSize(1).contains(loan);

    }

    @Test
    @DisplayName("Não Deve obter nenhum empréstimos")
    public void notFindByLoanDateLessThanAndNotReturnedTest(){
        Loan loan = createAndPersistLoan( LocalDate.now() );

        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertThat(result).isEmpty();

    }

    public static Book createNewBook(String isbn) {
        return Book.builder().title("Aventuras").author("Fulano").isbn(isbn).build();
    }

    public Loan createAndPersistLoan(LocalDate loanDate){
        Book book = createNewBook("123");
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(loanDate).build();
        entityManager.persist(loan);

        return loan;
    }

}
