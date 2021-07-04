package com.udemy.curso.libraryapi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

<<<<<<< HEAD
import javax.validation.constraints.NotEmpty;

=======
>>>>>>> 68a74cf36bb12bc51ab2d3ccd0ee906320bab959
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {

    private Long id;
<<<<<<< HEAD
    @NotEmpty
    private String isbn;
    @NotEmpty
    private String customer;
    @NotEmpty
    private String customerEmail;
=======
    private String isbn;
    private String customer;
>>>>>>> 68a74cf36bb12bc51ab2d3ccd0ee906320bab959
    private BookDTO book;
}
