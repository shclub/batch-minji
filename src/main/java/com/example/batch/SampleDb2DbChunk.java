package com.example.batch;
import javax.sql.DataSource;
 
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

@Configuration
public class SampleDb2DbChunk {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
 
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    
    
    @Bean
    public Job sampleDb2DbChunkJob(Step sampleDb2DbChunkStep) {
        return jobBuilderFactory
            .get("sampleDb2DbChunkJob")
            .incrementer(new RunIdIncrementer())
            .flow(sampleDb2DbChunkStep)
            .end()
            .build();
    }
 
    @Bean
    public Step sampleDb2DbChunkStep(JdbcCursorItemReader<Person> jdbcCursorItemReader, JdbcBatchItemWriter<Person> writer) {
        return stepBuilderFactory.get("sampleDb2DbChunkStep")
            .<Person, Person> chunk(10)
            .reader(jdbcCursorItemReader)
            .processor(sampleDb2DbChunkProcessor())
            .writer(writer)
            .build();
    }
    
    @Bean
    public JdbcCursorItemReader<Person> jdbcCursorItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Person>() 
                .name("jdbcCursorItemReader")   
                .fetchSize(100) 
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Person.class))  
                .sql("SELECT firstname, lastname, birthdate FROM source_person")
                .build();
    }

    @Bean
    public PersonItemProcessor sampleDb2DbChunkProcessor() {
        return new PersonItemProcessor();
    }
    

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql("INSERT INTO target_person (firstname, lastname, birthdate " + 
                    ") VALUES (:firstname, :lastname, :birthdate"
                    + ")")
            .dataSource(dataSource)
            .build();
    }
}
