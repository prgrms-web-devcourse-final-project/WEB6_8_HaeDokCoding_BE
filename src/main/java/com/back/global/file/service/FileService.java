package com.back.global.file.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.back.global.file.dto.UploadedFileDto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  private final AmazonS3 amazonS3;

  public List<UploadedFileDto> uploadFiles(List<MultipartFile> files) {
    if (files == null) return List.of();

    List<UploadedFileDto> results = new ArrayList<>();
    for (MultipartFile file : files) {
      // 난수를 이용한 고유한 파일 이름 생성
      String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

      try {
        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // S3에 파일 업로드 요청 생성 및 업로드
        amazonS3.putObject(new PutObjectRequest(
            bucket,
            fileName,
            file.getInputStream(),
            metadata
            )
            // 업로드하는 파일의 접근 권한(ACL, Access Control List) 을 미리 정의된(Canned) 설정으로 지정
            // PublicRead : 누구나 읽기 가능(브라우저에서 URL만 알면 열람 가능)
            // .withCannedAcl(CannedAccessControlList.PublicRead)
        );
        // S3에 업로드된 파일에 접근 가능한 URL을 문자열로 반환
        String url = amazonS3.getUrl(bucket, fileName).toString();

        results.add(new UploadedFileDto(fileName, url));
      } catch (IOException e) {
        throw new RuntimeException("S3 업로드 실패", e);
      }
    }
    return results;
  }

  // 개별 파일 업로드 로직
  public String uploadFile(MultipartFile file) {
    // 난수를 이용한 고유한 파일 이름 생성
    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

    try {
      // 메타데이터 설정
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType(file.getContentType());
      metadata.setContentLength(file.getSize());

      // S3에 파일 업로드 요청 생성 및 업로드
      amazonS3.putObject(new PutObjectRequest(
          bucket,
          fileName,
          file.getInputStream(),
          metadata
          )
          // 업로드하는 파일의 접근 권한(ACL, Access Control List) 을 미리 정의된(Canned) 설정으로 지정
          // PublicRead : 누구나 읽기 가능(브라우저에서 URL만 알면 열람 가능)
//          .withCannedAcl(CannedAccessControlList.PublicRead)
      );

    } catch (IOException e) {
      throw new RuntimeException("S3 파일 업로드 실패", e);
    }
    // S3에 업로드된 파일에 접근 가능한 URL을 문자열로 반환
    return amazonS3.getUrl(bucket, fileName).toString();
  }

  // 파일 삭제 로직
  public void deleteFile(String fileName){
    amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
  }
}
