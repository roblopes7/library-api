package com.udemy.curso.libraryapi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LoanFilterDTO {

    private String isbn;
    private String customer;
}
