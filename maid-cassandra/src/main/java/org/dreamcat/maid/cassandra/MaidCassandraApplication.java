package org.dreamcat.maid.cassandra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Create by tuke on 2020/3/14
 */
@SpringBootApplication(scanBasePackages = {"org.dreamcat.maid"})
public class MaidCassandraApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaidCassandraApplication.class, args);
    }
}
