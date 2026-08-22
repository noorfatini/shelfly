package com.shelfly.backend.repository;

import com.shelfly.backend.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;

@Repository
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public BookRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Book> search(String keyword, String category, String status, Pageable pageable) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        List<Criteria> filters = new java.util.ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            Pattern pattern = Pattern.compile(Pattern.quote(keyword.trim()), Pattern.CASE_INSENSITIVE);
            filters.add(new Criteria().orOperator(
                    Criteria.where("title").regex(pattern),
                    Criteria.where("author").regex(pattern)
            ));
        }
        if (category != null && !category.isBlank()) {
            filters.add(Criteria.where("category").is(category));
        }
        if (status != null && !status.isBlank()) {
            filters.add(Criteria.where("status").is(status));
        }

        if (!filters.isEmpty()) {
            criteria.andOperator(filters.toArray(new Criteria[0]));
            query.addCriteria(criteria);
        }

        long total = mongoTemplate.count(query, Book.class);
        query.with(pageable);
        List<Book> books = mongoTemplate.find(query, Book.class);

        return new PageImpl<>(books, pageable, total);
    }
}
