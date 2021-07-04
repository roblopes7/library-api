package com.udemy.curso.libraryapi.model.entity;

import com.udemy.curso.libraryapi.model.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Loan {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
<<<<<<< HEAD:src/main/java/com/udemy/curso/libraryapi/model/entity/Loan.java
    @Column(name = "customer")
    private String customer;
    @Column(name = "customer_email")
    private String customerEmail;
    @Column(name = "loan_date")
    private LocalDate loanDate;
    @Column(name = "returned")
=======
    @Column
    private String customer;
    @JoinColumn(name = "id_book")
    @ManyToOne
    private Book book;
    @Column
    private LocalDate loanDate;
    @Column
>>>>>>> 68a74cf36bb12bc51ab2d3ccd0ee906320bab959:src/main/java/com/udemy/curso/libraryapi/model/Loan.java
    private boolean returned;
    @JoinColumn(name = "id_book")
    @ManyToOne
    private Book book;

}
