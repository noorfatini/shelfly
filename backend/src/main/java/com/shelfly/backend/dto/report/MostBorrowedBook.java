package com.shelfly.backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MostBorrowedBook {
    @Field("_id")
    private String bookId;
    private String title;
    private long borrowCount;
}
