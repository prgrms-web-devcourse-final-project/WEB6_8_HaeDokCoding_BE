package com.back.global.file;

import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

import java.util.List;
import java.util.stream.Collectors;


@Tag(name = "File", description = "file API")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
     private final S3Client s3Client;

     @Operation(summary = "S3 버킷 목록 조회", description = "모든 버킷 목록을 조회")
     @GetMapping("/buckets")
     public RsData<List<String>> listBuckets() {

          return RsData.of(
                  200,
                  "버킷 목록 조회",
                  s3Client
                          .listBuckets()
                          .buckets()
                          .stream()
                          .map(Bucket::name)
                          .collect(Collectors.toList())
          );

     }
}
