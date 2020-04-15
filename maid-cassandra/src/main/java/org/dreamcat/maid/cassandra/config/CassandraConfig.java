package org.dreamcat.maid.cassandra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver;

/**
 * Create by tuke on 2020/2/3
 */
public class CassandraConfig {
    //@Bean
    public CassandraClusterFactoryBean cluster() {
        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        cluster.setContactPoints("localhost");
        return cluster;
    }

    //@Bean
    public CassandraConverter converter() {
        CassandraMappingContext mappingContext = new CassandraMappingContext();
        mappingContext.setUserTypeResolver(new SimpleUserTypeResolver(cluster().getObject(), "mykeyspace"));
        return new MappingCassandraConverter(mappingContext);
    }

    //@Bean
    public CassandraSessionFactoryBean session() throws Exception {
        CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
        session.setCluster(cluster().getObject());
        session.setKeyspaceName("mykeyspace");
        session.setConverter(converter());
        session.setSchemaAction(SchemaAction.NONE);
        return session;
    }

    @Bean
    public CassandraOperations cassandraTemplate() throws Exception {
        return new CassandraTemplate(session().getObject());
    }


    //@Bean
    //public Session session() {
    //    Cluster cluster = Cluster.builder().addContactPoints("localhost").build();
    //    return cluster.connect("mykeyspace");
    //}

    //
    //public CassandraCqlClusterFactoryBean cluster() {
    //    CassandraCqlClusterFactoryBean cluster = new CassandraCqlClusterFactoryBean();
    //    cluster.setContactPoints("localhost");
    //    return cluster;
    //}
    //
    //@Bean
    //public CassandraCqlSessionFactoryBean session() {
    //    CassandraCqlSessionFactoryBean session = new CassandraCqlSessionFactoryBean();
    //    session.setCluster(Objects.requireNonNull(cluster().getObject()));
    //    session.setKeyspaceName("mykeyspace");
    //    return session;
    //}
}
