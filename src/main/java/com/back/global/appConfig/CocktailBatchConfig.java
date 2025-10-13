package com.back.global.appConfig;

import com.back.global.standard.util.DecimalToFractionConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

// DecimalToFractionConverter가 있다고 가정합니다.
// import com.example.DecimalToFractionConverter;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class CocktailBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private static final String INPUT_PATH = "src/main/resources/cocktails.csv";
    private static final String OUTPUT_PATH = "src/main/resources/cocktails_clean.csv";

    // 1.Reader: CSV 파일 한 줄씩 읽기
    @Bean
    public FlatFileItemReader<String> reader() {
        return new FlatFileItemReaderBuilder<String>()
                .name("cocktailReader")
                .resource(new FileSystemResource(INPUT_PATH))
                .lineMapper(new PassThroughLineMapper())
                .build();
    }

    // 2️⃣ Processor: ingredient 컬럼(6번째)만 DecimalToFractionConverter로 변환
    @Bean
    public ItemProcessor<String, String> processor() {
        return line -> {
            // CSV 한 줄을 안전하게 split
            String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            // Header인지 확인: "ingredient"라는 컬럼 이름으로 간단 체크
            if (columns.length > 6 && !columns[6].equalsIgnoreCase("ingredient")) {
                // DecimalToFractionConverter는 별도로 구현되어 있어야 합니다.
                columns[6] = DecimalToFractionConverter.convert(columns[6]);
            }

            // 다시 CSV 문자열로 합치기
            return String.join(",", columns);
        };
    }

    // 3️⃣ Writer: 변환된 CSV 출력 (변화 없음)
    @Bean
    public FlatFileItemWriter<String> writer() {
        return new FlatFileItemWriterBuilder<String>()
                .name("cocktailWriter")
                .resource(new FileSystemResource(OUTPUT_PATH))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }

    // 4️⃣ Step: Chunk 단위 처리 (StepBuilderFactory 대체)
    @Bean
    public Step convertStep() {
        // StepBuilder를 직접 생성하고, jobRepository와 transactionManager를 주입합니다.
        return new StepBuilder("convertStep", jobRepository)
                // chunk 메서드에 transactionManager를 인수로 전달합니다.
                .<String, String>chunk(5, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    // 5️⃣ Job 정의 (JobBuilderFactory 대체)
    @Bean
    public Job convertJob() {
        // JobBuilder를 직접 생성하고, jobRepository를 주입합니다.
        return new JobBuilder("convertJob", jobRepository)
                .start(convertStep())
                .build();
    }
}
