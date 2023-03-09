package com.example.springs3practice.s3;

import com.example.springs3practice.s3.model.FileDetail;
import com.example.springs3practice.s3.model.GetPresignedUrlRes;
import com.example.springs3practice.util.MultipartUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3ResourceStorage s3Storage;

    public FileDetail save(MultipartFile multipartFile) {
        FileDetail fileDetail = FileDetail.multipartOf(multipartFile);
        s3Storage.store(fileDetail.getPath(), multipartFile);
        return fileDetail;
    }

    public ResponseEntity<byte[]> getObject(String fileName) throws IOException {
        return s3Storage.getObject(fileName);
    }

    public GetPresignedUrlRes getPresignedUrl(String fileName) {
        String fileId = MultipartUtil.createFileId();
        String format = MultipartUtil.getFormatByName(fileName);
        String filePath = MultipartUtil.createPath(fileId, format);

        GetPresignedUrlRes g = s3Storage.getPreSignedUrl(fileId, filePath);
        return g;
    }
}
