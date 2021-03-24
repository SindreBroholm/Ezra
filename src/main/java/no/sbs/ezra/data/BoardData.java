package no.sbs.ezra.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Entity
@Data
@RequiredArgsConstructor
public class BoardData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String contactName;

    @PositiveOrZero
    @Size(max = 15, min = 6)
    private String contactNumber;

    @NotEmpty
    private String contactEmail;

    private String homepage;
}
