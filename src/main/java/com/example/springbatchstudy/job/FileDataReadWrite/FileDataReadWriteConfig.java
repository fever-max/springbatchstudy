package com.example.springbatchstudy.job.FileDataReadWrite;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.example.springbatchstudy.job.FileDataReadWrite.dto.Player;

/**
 * desc: csv 파일 읽고 쓰기
 * run: --job.name=fileReadWriteJob
 */
@Configuration
@RequiredArgsConstructor
public class FileDataReadWriteConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job fileReadWriterJob(@Qualifier("fileReadWriteStep") Step fileReadWriterStep) {
        return jobBuilderFactory.get("fileReadWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(fileReadWriterStep)
                .build();
    }

    @JobScope
    @Bean
    public Step fileReadWriteStep(@Qualifier("playItemReader") ItemReader playFileItemReader) {
        return stepBuilderFactory.get(("fileReadWriteStep"))
                .<Player, Player>chunk(5)
                .reader(playFileItemReader)
                .writer(new ItemWriter() {
                    @Override
                    public void write(List items) throws Exception {
                        items.forEach(System.out::println);
                    }
                })
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<Player> playItemReader() {
        return new FlatFileItemReaderBuilder<Player>()
                .name("playerItemReader")
                .resource(new FileSystemResource("Players.csv"))
                .lineTokenizer(new DelimitedLineTokenizer())
                .fieldSetMapper(new PlayerFieldSetMapper())
                .linesToSkip(1) // 1번째 줄 스킵
                .build();
    }

}
