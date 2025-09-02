package co.com.pragma.r2dbc.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table("status")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StatusEntity {

    @Id
    @Column("id_status")
    private UUID idStatus;

    @Column("name")
    private String name;

    @Column("description")
    private String description;


}
