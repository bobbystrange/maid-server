package org.dreamcat.maid.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;

import java.util.UUID;

/**
 * Create by tuke on 2020/2/3
 */
public class CassandraTest {

    public static Person newPerson(String name, int age) {
        return new Person(UUID.randomUUID().toString(), name, age);
    }

    @Test
    public void test() {
        Cluster cluster = Cluster.builder().addContactPoints("127.0.0.1").build();
        Session session = cluster.connect("my_key_space");

        CassandraOperations template = new CassandraTemplate(session);

        Person jonDoe = template.insert(newPerson("Jon Doe", 40));

        Query query = Query.query(Criteria.where("id").is(jonDoe.getId()));
        Person person = template.selectOne(query, Person.class);
        assert person != null;
        System.out.println(person.getId());
        template.truncate(Person.class);
        session.close();
        cluster.close();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Table
    public static class Person {
        @PrimaryKey
        private String id;
        private String name;
        private int age;
    }
}
