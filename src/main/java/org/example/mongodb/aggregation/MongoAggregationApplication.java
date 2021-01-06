package org.example.mongodb.aggregation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class MongoAggregationApplication {

    private final FooRepository fooRepository;

    public static void main(String[] args) {
        SpringApplication.run(MongoAggregationApplication.class, args);
    }

    public MongoAggregationApplication(FooRepository fooRepository) {
        this.fooRepository = fooRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initAfterStartup() {
        Bar bar = new Bar();
        bar.setIdentifier("bar");

        Foo foo = new Foo();
        foo.setIdentifier("foo");
        foo.setBar(bar);

        fooRepository.save(foo).block();

        fooRepository.findBarBy("foo")
                .subscribe(_bar -> System.out.println("Found bar with identifier: " + _bar.getIdentifier()));
    }
}

@Repository
interface FooRepository extends ReactiveMongoRepository<Foo, String> {

    @Aggregation(pipeline = {
            "{ $match : { _id : '?0' } }",
            "{ $replaceRoot : { newRoot : '$bar' } }",
            "{ $project : { identifier : 1 } }"
    })
    Mono<Bar> findBarBy(String identifier);
}

@Getter
@Setter
@Document
class Foo {

    @Id private String identifier;
    private Bar bar;
}

@Getter
@Setter
@Document
class Bar {

    private String identifier;
}