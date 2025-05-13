package br.com.gpi.faturamento.config;

import br.com.gpi.faturamento.batch.listener.CustomItemProcessListener;
import br.com.gpi.faturamento.batch.listener.CustomItemWriterListener;
import br.com.gpi.faturamento.domain.PlanoProcessamento;
import br.com.gpi.faturamento.enums.StatusProcessamento;
import br.com.gpi.faturamento.repository.PlanoProcessamentoRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;

@Configuration
public class JobConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public Job processJob(JobRepository jobRepository, Step processStep) {
        return new JobBuilder("processJob", jobRepository)
                .start(processStep)
                .build();
    }

    @Bean
    public Step processStep(JobRepository jobRepository,
                            ItemReader<PlanoProcessamento> reader,
                            ItemProcessor<PlanoProcessamento, PlanoProcessamento> processor,
                            ItemWriter<PlanoProcessamento> writer,
                            PlatformTransactionManager transactionManager,
                            CustomItemProcessListener itemProcessListener,
                            CustomItemWriterListener itemWriterListener) {
        return new StepBuilder("processStep", jobRepository)
                .<PlanoProcessamento, PlanoProcessamento>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(itemProcessListener)
                .listener(itemWriterListener)
                .build();
    }

    @Bean
    public RepositoryItemReader<PlanoProcessamento> itemReader(PlanoProcessamentoRepository repository) {
        RepositoryItemReader<PlanoProcessamento> reader = new RepositoryItemReader<>();
        reader.setRepository(repository);
        reader.setMethodName("findByIdStatus");

        reader.setArguments(Collections.singletonList(StatusProcessamento.PENDENTE.getId()));
        reader.setSort(Collections.singletonMap("id", Sort.Direction.ASC));
        return reader;
    }

}
