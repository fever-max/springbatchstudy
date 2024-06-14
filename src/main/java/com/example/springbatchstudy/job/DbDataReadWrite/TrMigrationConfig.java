package com.example.springbatchstudy.job.DbDataReadWrite;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import com.example.springbatchstudy.core.domain.accounts.Accounts;
import com.example.springbatchstudy.core.domain.accounts.AccountsRepository;
import com.example.springbatchstudy.core.domain.orders.Orders;
import com.example.springbatchstudy.core.domain.orders.OrdersRepository;

import lombok.RequiredArgsConstructor;

/**
 * desc: 주문 테이블 -> 정산 테이블 데이터 이관
 * run: --job.name=trMigrationJob
 */
@Configuration
@RequiredArgsConstructor
public class TrMigrationConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final OrdersRepository ordersRepository;
    private final AccountsRepository accountsRepository;

    @Bean
    public Job trMigrationJob(Step trMigrationStep) {
        return jobBuilderFactory.get("trMigrationJob")
                .incrementer(new RunIdIncrementer())
                .start(trMigrationStep)
                .build();
    }

    @JobScope
    @Bean
    public Step trMigrationStep(ItemReader trOrdersReader, ItemProcessor trOderProcessor,
            ItemWriter trOrderWriter) {
        // reader > processor > writer 순으로 작업 진행
        return stepBuilderFactory.get("trMigrationStep")
                .<Orders, Accounts>chunk(5) // 5개 단위로 처리
                .reader(trOrdersReader)
                .processor(trOderProcessor)
                .writer(trOrderWriter)
                .build();
    }

    // @StepScope
    // @Bean
    // public RepositoryItemWriter<Accounts> trOrderWriter() {
    // return new RepositoryItemWriterBuilder<Accounts>()
    // .repository(accountsRepository)
    // .methodName("save")
    // .build();
    // }

    @StepScope
    @Bean
    public ItemWriter<Accounts> trOrdersWriter() {
        return new ItemWriter<Accounts>() {
            @Override
            public void write(List<? extends Accounts> items) throws Exception {
                items.forEach(item -> accountsRepository.save(item));
            }
        };
    }

    @StepScope
    @Bean
    public ItemProcessor<Orders, Accounts> trOderProcessor() {
        return new ItemProcessor<Orders, Accounts>() {
            @Override
            public Accounts process(Orders item) throws Exception {
                // Orders > Accounts 변경
                return new Accounts(item);
            }
        };
    }

    @StepScope
    @Bean
    public RepositoryItemReader<Orders> trOrdersReader() {
        return new RepositoryItemReaderBuilder<Orders>()
                .name("trOrdersReader")
                .repository(ordersRepository)
                .methodName("findAll")
                .pageSize(5)
                .arguments(Arrays.asList()) // 비어있는 파라미터 전달
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC)) // 정렬 기준
                .build();
    }

}
