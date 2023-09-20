package com.example.csvspring.config;

import com.example.csvspring.model.Temperature;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.sql.DataSource;
import java.util.Map;
import java.util.stream.IntStream;

@Configuration
@AllArgsConstructor
@Slf4j
//@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfiguration {

    private final DataSource dataSource;
    @PersistenceContext(unitName = "default")
    private final EntityManager entityManager;

    @Bean
    public Job job() throws Exception {
        return new JobBuilder("upload", jobRepository())
                .incrementer(new RunIdIncrementer())
                .start(step())
                .build();
    }

    @Bean
    public Step step() throws Exception {
        return new StepBuilder("upload-steps", jobRepository())
                .<Temperature, Temperature>chunk(100, getTransactionManager())
                .reader(reader(null))
                .writer(itemWriterJdbc())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Temperature> reader(@Value("#{jobParameters['file']}") String file) {
        var reader = new FlatFileItemReader<Temperature>();
        reader.setResource(new FileSystemResource(file));
        reader.setLinesToSkip(0);
        reader.setName("csvReader");
        //line mapper
        var lineMapper = new DefaultLineMapper<Temperature>();
        lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
        lineMapper.setFieldSetMapper(fieldSetMapper()); // see implementation below
        lineMapper.afterPropertiesSet();
        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public FieldSetMapper<Temperature> fieldSetMapper() {
        return (fieldSet) -> {
            Temperature temperature = new Temperature();
            String[] values = fieldSet.getValues();
            for (int i = 5; i < values.length; i++) {
                temperature.setStationId(fieldSet.readLong(0));
                temperature.setWhetherId(fieldSet.readString(1));
                temperature.setStationName(fieldSet.readString(2));
                temperature.setYear(fieldSet.readLong(3));
                temperature.setMonth(fieldSet.readLong(4));
                if (i % 2 != 0) {
                    if (!fieldSet.readString(i).isEmpty()) {
                        temperature.getHighTemperatures().add(fieldSet.readDouble(i));
                    }
                } else {
                    if (!fieldSet.readString(i).isEmpty()) {
                        temperature.getLowTemperatures().add(fieldSet.readDouble(i));
                    }
                }
            }
            return temperature;
        };
    }


    @Bean
    public JpaItemWriter<Temperature> itemWriterJdbc() {
        return new JpaItemWriterBuilder<Temperature>()
                .entityManagerFactory(entityManager.getEntityManagerFactory())
                .build();
    }

    @SneakyThrows
    @Bean
    public JobRepository jobRepository() {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(getTransactionManager());
        factory.setDatabaseType("h2");
       factory.afterPropertiesSet();
    //    factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
     //   factory.setTablePrefix("BATCH_");
        return factory.getObject();
    }

    @Override
    @SneakyThrows
    @Bean
    public JobLauncher jobLauncher() throws BatchConfigurationException {
        var jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Override
    @SneakyThrows
    @Bean
    public JobExplorer jobExplorer() throws BatchConfigurationException {
        var jobExplorer = new JobExplorerFactoryBean();
        jobExplorer.setDataSource(dataSource);
        jobExplorer.setTransactionManager(getTransactionManager());
        jobExplorer.setTablePrefix("BATCH_");
        jobExplorer.afterPropertiesSet();
        return jobExplorer.getObject();
    }

    @Bean
    @Primary
    public JpaTransactionManager getTransactionManager() {
        final JpaTransactionManager tm = new JpaTransactionManager();
        tm.setDataSource(dataSource);
        return tm;
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<Temperature> readerJpa(@Value("#{jobParameters['from']}") Long from,
                                                      @Value("#{jobParameters['to']}") Long to) {
        var reader = new JpaCursorItemReader<Temperature>();
        reader.setEntityManagerFactory(entityManager.getEntityManagerFactory());
        reader.setQueryString("select t from Temperature t where t.id between :from and :to");
        reader.setParameterValues(Map.of("from", from, "to", to));
        return reader;
    }

    @Bean
    public ItemProcessor<Temperature, Temperature> processor() {
        return (item) -> {
            log.info("Processing Temperature: {}", item);
            IntStream.range(0, item.getHighTemperatures().size())
                    .mapToDouble(i -> (item.getHighTemperatures().get(i) + item.getLowTemperatures().get(i)) / 2)
                    .forEach(avg -> item.getAvgTemperatures().add(avg));
            return item;
        };
    }


}
