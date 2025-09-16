package co.com.pragma.model.status;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusTest {

    @Test
    void merge_shouldUpdateOnlyNonNullValues() {
        UUID id = UUID.randomUUID ( );
        Status original = Status.builder ( )
                .idStatus ( id )
                .name ( "Status" )
                .description ( "desc" )

                .build ( );

        Status other = Status.builder ( )
                .name ( "Status" )
                .build ( );

        original.merge ( other );

        assertThat ( original.getName ( ) ).isEqualTo ( "Status" );
        assertThat ( original.getDescription ( ) ).isEqualTo ( "desc" );

    }

    @Test
    void merge_shouldUpdateOnlyNonNullValues2() {
        UUID id = UUID.randomUUID ( );
        Status original = Status.builder ( )
                .idStatus ( id )
                .name ( "Status" )
                .description ( "desc" )

                .build ( );

        Status other = Status.builder ( )
                .description ( "desc" )
                .build ( );

        original.merge ( other );

        assertThat ( original.getName ( ) ).isEqualTo ( "Status" );
        assertThat ( original.getDescription ( ) ).isEqualTo ( "desc" );

    }

    @Test
    void merge_shouldDoNothing_whenOtherHasAllNullValues() {
        Status original = Status.builder ( )
                .name ( "Status" )
                .description ( "desc" )
                .build ( );

        Status other = new Status ( );

        original.merge ( other );

        assertThat ( original.getName ( ) ).isEqualTo ( "Status" );
        assertThat ( original.getDescription ( ) ).isEqualTo ( "desc" );
    }
}
