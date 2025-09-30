package com.back.global.file.Controller;

import com.back.global.file.service.FileService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;


@Tag(name = "File", description = "file API")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

  private final S3Client s3Client;
  private final FileService fileService;

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

  /**
   * 개별 파일 업로드 API
   * @param file 업로드할 파일
   * @return 업로드 결과 및 파일 URL
   */
  @PostMapping("/upload")
  @Operation(summary = "개별 파일 업로드")
  public RsData<String> uploadFile(
      @RequestParam("file") MultipartFile file
  ) {
    return RsData.successOf(
        "개별 파일 업로드를 성공했습니다. 파일URL: " + fileService.uploadFile(file)
    );
  }

  /**
   * 파일 삭제 API
   * @param fileName 삭제할 파일 이름
   * @return 삭제 결과
   */
  @DeleteMapping
  @Operation(summary = "파일 삭제")
  public RsData<String> deleteFile(
      @RequestParam String fileName
  ){
    fileService.deleteFile(fileName);
    return RsData.successOf(null);
  }
}
